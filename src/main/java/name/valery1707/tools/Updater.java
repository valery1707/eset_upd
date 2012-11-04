package name.valery1707.tools;

import name.valery1707.tools.configuration.Configuration;
import name.valery1707.tools.eset.EsetDbInfo;
import name.valery1707.tools.eset.FileInfo;
import name.valery1707.tools.eset.FileSizeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static name.valery1707.tools.Utils.*;
import static org.apache.commons.io.FileUtils.copyFile;

@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
public class Updater implements Closeable {
    private final Configuration configuration;
    private final Downloader downloader;

    public Updater(Configuration configuration) {
        this.configuration = configuration;
        downloader = new Downloader(configuration);
    }

    public void run() {
        File localFile = new File(configuration.getPathWeb(), "update.ver");
        Integer localVersion = canReadFile(localFile) ? new EsetDbInfo(localFile).getEngineVersion() : 0;
        EsetDbInfo remoteInfo = new EsetDbInfo(download("/eset_upd/v4/update.ver"));//todo To configuration: eset_upd, 4?
        Integer remoteVersion = remoteInfo.getEngineVersion();
        log.info("Versions: local ({}) vs remote ({})", localVersion, remoteVersion);
        if (localVersion >= remoteVersion) {
            log.info("Done");
            return;
        }

        List<FileInfo> files = remoteInfo.getFiles();
        Map<FileInfo, File> downloaded = new HashMap<FileInfo, File>(files.size());

        log.info("Downloading different files");
        long sizeDownloaded = 0;
        long sizeKeeped = 0;
        int pos = 0;
        for (FileInfo file : files) {
            pos++;
            String posInfo = String.format("%3d/%3d", pos, files.size());
            FileSizeInfo size = new FileSizeInfo(file.sizeLocal(configuration.getPathWeb()), file.sizeIni(), downloader.size(file.getUrl()));
            if (size.isSizeDiffers()) {
                log.info("{}: Going to download '{}' on size diff ({})", posInfo, file.getFilename(), size);
                File fileContent = download(file.getUrl());
                sizeDownloaded += fileContent.length();
                //todo check Size
                downloaded.put(file, fileContent);
            } else {
                log.info("{}: Keep old '{}' on file size equal ({})", posInfo, file.getFilename(), size);
                sizeKeeped += size.getLocal();
            }
        }

        log.info("Save new DB file");
        EsetDbInfo localInfo = new EsetDbInfo(files);
        store(localInfo, localFile);

        log.info("Move downloaded files");
        for (FileInfo file : downloaded.keySet()) {
            move(downloaded.get(file), new File(configuration.getPathWeb(), file.getFilename()));
        }

        log.info("Processing new settings done (total: {}; downloaded: {} [{}]; keeped: {} [{}])!",
                files.size(), downloaded.size(), byteCountForUser(sizeDownloaded), files.size() - downloaded.size(), byteCountForUser(sizeKeeped));
    }

    private void store(EsetDbInfo dbInfo, File targetFile) {
        File tmpFile = new File(targetFile.getParentFile(), targetFile.getName() + ".tmp");
        try {
            dbInfo.store(tmpFile);
            move(tmpFile, targetFile);
        } catch (IOException e) {
            throw propagate(e);
        }
    }

    private void move(File src, File dst) {
        File tmp = new File(dst.getParentFile(), dst.getName() + ".del");
        if (canReadFile(dst) && !dst.renameTo(tmp) && !dst.delete()) {
            log.warn("Could not delete file {}", dst.getAbsolutePath());
        }
        if (!src.renameTo(dst)) {
            log.info("Could not move file {} to {}. Use content copy", src.getAbsolutePath(), dst.getAbsolutePath());
            try {
                copyFile(src, dst, true);
            } catch (IOException e) {
                log.warn("Error while copy file: ", e);
            }
        }
        if (canReadFile(tmp) && !tmp.delete()) {
            log.warn("Could not delete file {}", tmp.getAbsolutePath());
        }
    }

    @Override
    public void close() throws IOException {
        downloader.close();
    }

    private File download(String urlPart) {
        return downloader.download(urlPart);
    }

    private static final Logger log = LoggerFactory.getLogger(Updater.class);
}
