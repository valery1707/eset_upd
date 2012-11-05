package name.valery1707.tools;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import name.valery1707.tools.configuration.Configuration;
import name.valery1707.tools.configuration.InvalidConfigurationException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.CharEncoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import static name.valery1707.tools.Utils.closeQuietly;

public class EsetUpdater {

    private static final int EXIT_STATUS_ERROR_IN_CONFIGURATION = 1;

    public static void main(String[] args) {
        EsetUpdater esetUpdater = new EsetUpdater();
        esetUpdater.run();
    }

    private final File rootDir;
    private Configuration configuration;

    public EsetUpdater() {
        rootDir = detectRootDir();
        //todo check for writable
    }

    public void run() {
        System.out.println("Home directory: " + rootDir.getAbsolutePath());
        extractSamples(rootDir);
        configureLogging(new File(rootDir, "logback4j.xml"));
        loadConfiguration();
        runUpdater();
    }

    private void extractSamples(File targetDir) {
        if (!targetDir.isDirectory() || !targetDir.canWrite()) {
            throw new IllegalStateException("Target directory is not directory or not writable: " + targetDir.getAbsolutePath());
        }
        try {
            LineIterator files = IOUtils.lineIterator(getClass().getResourceAsStream("/sample/files.txt"), CharEncoding.UTF_8);
            while (files.hasNext()) {
                String fileName = files.nextLine();
                File targetFile = new File(targetDir, fileName);
                InputStream sourceStream = getClass().getResourceAsStream("/sample/" + fileName);
                if (sourceStream != null && !targetFile.exists()) {
                    IOUtils.copy(sourceStream, FileUtils.openOutputStream(targetFile));
                }
            }
            files.close();
        } catch (UnsupportedEncodingException ignored) {
        } catch (IOException e) {
            throw new RuntimeException("Exception while reading files.txt", e);
        }
    }

    private void configureLogging(File configuration) {
        // assume SLF4J is bound to logback in the current environment
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        try {
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(context);
            // Call context.reset() to clear any previous configuration, e.g. default configuration
            context.reset();
            configurator.doConfigure(configuration);
        } catch (JoranException je) {
            // StatusPrinter will handle this
        }
        StatusPrinter.printInCaseOfErrorsOrWarnings(context);
    }

    private void loadConfiguration() {
        try {
            configuration = new Configuration(new File(rootDir, "config.ini"));
        } catch (InvalidConfigurationException e) {
            log.error("Error in configuration", e);
            System.out.println("Error in configuration: " + e.getMessage());
            System.exit(EXIT_STATUS_ERROR_IN_CONFIGURATION);
        }
    }

    private void runUpdater() {
        Updater updater = new Updater(configuration);
        try {
            updater.run();
        } catch (Throwable t) {
            log.error("Error: ", t);
        } finally {
            closeQuietly(updater);
        }
    }

    private static File detectRootDir() {
        //todo -h --home: Use dir ".eset_upd" in user home
        //todo -p --path: User user defined directory
        String classPath = System.getProperty("java.class.path");
        if (!classPath.contains(File.pathSeparator)) {
            return new File(classPath).getParentFile().getAbsoluteFile();
        } else {
            return new File(System.getProperty("user.dir")).getAbsoluteFile();
        }
    }

    private static final Logger log = LoggerFactory.getLogger(EsetUpdater.class);
}
