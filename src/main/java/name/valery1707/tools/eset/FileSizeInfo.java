package name.valery1707.tools.eset;

import static java.lang.String.format;
import static name.valery1707.tools.Utils.byteCountForUser;

public class FileSizeInfo {
    private final long local;
    private final long ini;
    private final long remote;

    public FileSizeInfo(long local, long ini, long remote) {
        this.local = local;
        this.ini = ini;
        this.remote = remote;
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
