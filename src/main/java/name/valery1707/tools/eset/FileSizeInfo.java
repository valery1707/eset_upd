package name.valery1707.tools.eset;

import name.valery1707.tools.Downloader;
import name.valery1707.tools.configuration.Configuration;

import java.io.IOException;

import static java.lang.String.format;
import static name.valery1707.tools.Utils.byteCountForUser;

public class FileSizeInfo {
    private final long local;
    private final long ini;
    private final long remote;
    private final long temp;

    public FileSizeInfo(long local, long ini, long remote, long temp) {
        this.local = local;
        this.ini = ini;
        this.remote = remote;
        this.temp = temp;
    }

    public FileSizeInfo(Configuration configuration, Downloader downloader, FileInfo fileInfo) throws IOException {
        this(fileInfo.sizeLocal(configuration.getPathWeb()),
                fileInfo.sizeIni(),
                downloader.size(fileInfo.getUrl()),
                fileInfo.sizeLocal(configuration.getPathTmp()));
    }

    public long getLocal() {
        return local;
    }

    public long getIni() {
        return ini;
    }

    public long getRemote() {
        return remote;
    }

    public long getTemp() {
        return temp;
    }

    public boolean isSizeDiffers() {
        return remote != local && remote > 0;
    }

    @Override
    public String toString() {
        return format("local: %s, remote: %s, ini: %s", toS(local), toS(remote), toS(ini));
    }

    private String toS(long size) {
        return format("%d[%s]", size, byteCountForUser(size));
    }
}
