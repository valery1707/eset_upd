package name.valery1707.tools.rar;

import com.github.junrar.Archive;
import com.github.junrar.Volume;
import com.github.junrar.VolumeManager;

import java.io.IOException;
import java.io.InputStream;

public class InputStreamRarVolumeManager implements VolumeManager {
    private final InputStream firstVolume;
    private final long length;

    public InputStreamRarVolumeManager(InputStream firstVolume, long length) {
        super();
        this.firstVolume = firstVolume;
        this.length = length;
    }

    @Override
    public Volume nextArchive(Archive archive, Volume lastVolume) throws IOException {
        if (lastVolume == null) {
            return new InputStreamVolume(archive, firstVolume, length);
        }
        throw new IllegalStateException("Usable only with 1 volume archives");
    }
}
