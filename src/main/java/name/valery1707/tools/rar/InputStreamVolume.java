package name.valery1707.tools.rar;

import com.github.junrar.Archive;
import com.github.junrar.Volume;
import com.github.junrar.io.IReadOnlyAccess;
import com.github.junrar.io.InputStreamReadOnlyAccessFile;

import java.io.IOException;
import java.io.InputStream;

public class InputStreamVolume implements Volume {
    private final Archive archive;
    private final InputStream volume;
    private final long length;

    public InputStreamVolume(Archive archive, InputStream volume, long length) {
        this.archive = archive;
        this.volume = volume;
        this.length = length;
    }

    @Override
    public IReadOnlyAccess getReadOnlyAccess() throws IOException {
        return new InputStreamReadOnlyAccessFile(volume);
    }

    @Override
    public long getLength() {
        return length;
    }

    @Override
    public Archive getArchive() {
        return archive;
    }
}
