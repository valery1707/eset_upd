package name.valery1707.tools;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

import static java.lang.String.format;

public class Utils {
    private Utils() {
    }

    //todo Check usage
    public static RuntimeException propagate(Throwable throwable) {
        throw new RuntimeException(throwable);
    }

    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ignored) {
            }
        }
    }

    public static boolean canReadFile(File file) {
        return file != null && file.exists() && file.isFile() && file.canRead();
    }

    public static String byteCountForUser(long value) {
        return byteCountForUser(value, 2);
    }

    private static final String[] SIZE_SUFFEXES = new String[]{"B", "KiB", "MiB", "GiB"};

    public static String byteCountForUser(long value, int precision) {
        int mod = 0;
        double v = value;
        while (v > 1000 && mod < SIZE_SUFFEXES.length) {
            mod++;
            v = v / 1024;
        }
        if (mod == 0) {
            return format("%.0f %s", v, SIZE_SUFFEXES[mod]);
        } else {
            return format("%." + precision + "f %s", v, SIZE_SUFFEXES[mod]);
        }
    }
}
