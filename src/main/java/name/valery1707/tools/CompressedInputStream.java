package name.valery1707.tools;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.rarfile.FileHeader;
import name.valery1707.tools.rar.InputStreamRarVolumeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static name.valery1707.tools.Utils.checkArgument;
import static name.valery1707.tools.Utils.propagate;

public class CompressedInputStream extends BufferedInputStream {

    private enum FileType {
        RAW, ZIP, RAR
    }

    public CompressedInputStream(InputStream in, long length) {
        super(unPack(in, length));
    }

    private static InputStream unPack(InputStream in, long length) {
        try {
            in = new BufferedInputStream(in);
            switch (detectFileType(in)) {
                case ZIP:
                    return unZip(in);
                case RAR:
                    return unRar(in, length);
                case RAW:
                    return in;
                default:
                    return in;
            }
        } catch (IOException e) {
            log.error("Error while detecting archive type: ", e);
            throw propagate(e);
        }
    }

    private static FileType detectFileType(InputStream in) throws IOException {
        DataInputStream is = new DataInputStream(in);
        if (isContainMark(is, ZIP_POS, ZIP_MARK)) {
            return FileType.ZIP;
        }
        if (isContainMark(is, RAR_POS, RAR_MARK)) {
            return FileType.RAR;
        }
        return FileType.RAW;
    }

    private static final byte[] RAR_MARK = new byte[]{82, 97, 114, 33};//Rar!
    private static final int RAR_POS = 0;
    private static final byte[] ZIP_MARK = new byte[]{80, 75};//PK
    private static final int ZIP_POS = 0;

    private static boolean isContainMark(DataInputStream is, int pos, byte[] mark) throws IOException {
        is.mark(pos + mark.length);
        byte[] buffer = new byte[mark.length];
        int len = is.read(buffer);
        is.reset();
        return len == mark.length && Arrays.equals(buffer, mark);
    }

    private static InputStream unZip(InputStream is) {
        ZipInputStream zis = new ZipInputStream(is);
        try {
            ZipEntry zipEntry = zis.getNextEntry();
            checkArgument(zipEntry != null && !zipEntry.isDirectory(), "Incorrect zip entry '%s'", zipEntry);
        } catch (IOException ex) {
            throw propagate(ex);
        }
        return zis;
    }

    private static InputStream unRar(InputStream is, long length) {
        try {
            Archive archive = new Archive(new InputStreamRarVolumeManager(is, length));
            List<FileHeader> fileHeaders = archive.getFileHeaders();
            checkArgument(fileHeaders.size() == 1, "Incorrect rar entry count %d", fileHeaders.size());
            FileHeader fileHeader = fileHeaders.get(0);
            checkArgument(!fileHeader.isDirectory() && !fileHeader.isEncrypted(), "Incorrect rar entry '%s'", fileHeader);
            return archive.getInputStream(fileHeader);
            //todo Not all stream we be closed
        } catch (RarException e) {
            throw propagate(e);
        } catch (IOException e) {
            throw propagate(e);
        }
    }

    private static final Logger log = LoggerFactory.getLogger(CompressedInputStream.class);
}
