package name.valery1707.tools;

import name.valery1707.tools.configuration.Configuration;
import name.valery1707.tools.eset.EsetDbInfo;
import name.valery1707.tools.eset.FileInfo;
import name.valery1707.tools.eset.FileSizeInfo;
import name.valery1707.tools.eset.FileStat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static name.valery1707.tools.Utils.canReadFile;
import static name.valery1707.tools.Utils.propagate;
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
        EsetDbInfo remoteInfo = new EsetDbInfo(download(configuration.getDbUrl()));
        if (remoteInfo.isEmptyDb()) {
            log.warn("Downloaded empty update.ver");
            return;
        }
        Integer remoteVersion = remoteInfo.getEngineVersion();
        //todo Download from random host
        log.info("Versions: local ({}) vs remote ({})", localVersion, remoteVersion);
        if (localVersion >= remoteVersion) {
            log.info("Done");
            return;
        }

        List<FileInfo> files = remoteInfo.getFiles(configuration.getDbLangs());
        Map<FileInfo, File> downloaded = new HashMap<FileInfo, File>(files.size());

        log.info("Downloading different files");
        FileStat stat = new FileStat();
        int pos = 0;
        for (FileInfo file : files) {
            pos++;
            String posInfo = String.format("%3d/%3d", pos, files.size());
            try {
                FileSizeInfo size = new FileSizeInfo(configuration, downloader, file);
                if (size.isSizeDiffers()) {
                    log.info("{}: Going to download '{}' on size diff ({})", posInfo, file.getFilename(), size);
                    File fileContent = download(file.getUrl(), size.getRemote());
                    downloaded.put(file, fileContent);
                    stat.touch(FileStat.Type.DOWNLOADED, size);
                } else if (size.getRemote() < 0) {
                    log.info("{}: Skip inaccessible '{}'", posInfo, file.getFilename());
                    stat.touch(FileStat.Type.INACCESSIBLE);
                } else {
                    log.info("{}: Keep old '{}' on file size equal ({})", posInfo, file.getFilename(), size);
                    stat.touch(FileStat.Type.KEEPED, size);
                }
            } catch (IOException e) {
                log.warn("{}: Error: {}", posInfo, e.getMessage());
                stat.touch(FileStat.Type.ERROR);
            }
        }

        if (stat.getCount(FileStat.Type.ERROR) > 0) {
            log.warn("Get {} errors while downloading files: {}", stat.getCount(FileStat.Type.ERROR), stat);
            return;
        }

        log.info("Save new DB file");
        EsetDbInfo localInfo = new EsetDbInfo(files);
        store(localInfo, localFile);

        log.info("Move downloaded files");
        for (FileInfo file : downloaded.keySet()) {
            move(downloaded.get(file), new File(configuration.getPathWeb(), file.getFilename()));
        }

        log.info("Processing new settings done ({})!", stat);
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

    private File download(String url, long sizeRemote) throws IOException {
        long maxSize = 0;
        for (int attempt = 0; attempt < configuration.getMaxRetries(); attempt++) {
            File file = download(url);
            long sizeLocal = file.length();
            if (sizeLocal == sizeRemote) {
                return file;
            } else {
                maxSize = Math.max(sizeLocal, maxSize);
                if (!file.delete()) {
                    log.warn("Could not delete temporary file " + file.getAbsolutePath());
                }
            }
        }

        throw new IOException(String.format("Error while downloading file: downloaded only %d of %d for %s", maxSize, sizeRemote, url));
    }

    private File download(String urlPart) {
        return downloader.download(urlPart);
    }

    private static final Logger log = LoggerFactory.getLogger(Updater.class);
}
