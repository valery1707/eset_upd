package name.valery1707.tools;

import java.io.File;

public class EsetUpdater {

    public static void main(String[] args) {
        EsetUpdater esetUpdater = new EsetUpdater();
        esetUpdater.run();
    }

    private final File rootDir;

    public EsetUpdater() {
        rootDir = detectRootDir();
    }

    public void run() {
        System.out.println("Running at: " + rootDir.getAbsolutePath());
    }

    private static File detectRootDir() {
        String classPath = System.getProperty("java.class.path");
        if (!classPath.contains(File.pathSeparator)) {
            return new File(classPath).getParentFile().getAbsoluteFile();
        } else {
            return new File(System.getProperty("user.dir")).getAbsoluteFile();
        }
    }
}
