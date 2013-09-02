package name.valery1707.tools;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import name.valery1707.tools.configuration.Configuration;
import name.valery1707.tools.configuration.InvalidConfigurationException;
import org.apache.commons.cli.*;
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
import java.util.Comparator;

import static java.lang.String.format;
import static name.valery1707.tools.Utils.closeQuietly;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.lang3.Validate.isTrue;

public class EsetUpdater {

    private static final int EXIT_STATUS_ERROR_IN_CONFIGURATION = 1;
    private static final int EXIT_STATUS_ERROR_IN_PARAMS = 2;
	private static final String DEFAULT_HOME_DIR_NAME = ".eset_upd";

	@SuppressWarnings("AccessStaticViaInstance")
	private static final Options cliOptions = new Options()
			.addOption(OptionBuilder
					.withLongOpt("file")
					.hasArg()
					.withArgName("file")
					.withDescription(format("Path for file with configuration, relative from home directory (see --home)%n" +
											"Default value: config.ini"))
					.create("f"))
			.addOption(OptionBuilder
					.withLongOpt("home")
					.hasOptionalArg()
					.withArgName("dir")
					.withDescription(format("Home directory used from store configuration files and as root for open config (see --file)%n" +
											"Default value:%n" +
											"*) option not set: directory where .jar-file located%n" +
											"*) option set without arg: directory '%s' in user home%n" +
											"*) option set with arg: user defined directory", DEFAULT_HOME_DIR_NAME))
					.create("H"))
			.addOption("h", "help", false, "Print this help");

    public static void main(String[] args) {
		try {
			CommandLine cli = new GnuParser().parse(cliOptions, args);
			if (cli.hasOption("help")) {
				HelpFormatter helpFormatter = new HelpFormatter();
				helpFormatter.setWidth(120);
				helpFormatter.setOptionComparator(new EmptyComparator());
				helpFormatter.printHelp(detectRootJar(), cliOptions, true);
				return;
			}
			EsetUpdater esetUpdater = new EsetUpdater(cli);
			esetUpdater.run();
		} catch (ParseException e) {
			System.err.println("Parsing failed.  Reason: " + e.getMessage());
			System.exit(EXIT_STATUS_ERROR_IN_PARAMS);
		}
    }

	private final CommandLine cli;
    private final File rootDir;
    private Configuration configuration;

    public EsetUpdater(CommandLine line) {
		cli = line;
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
        try {
            LineIterator files = IOUtils.lineIterator(getClass().getResourceAsStream("/sample/files.txt"), CharEncoding.UTF_8);
            while (files.hasNext()) {
                String fileName = files.nextLine();
                File targetFile = new File(targetDir, fileName);
                InputStream sourceStream = getClass().getResourceAsStream("/sample/" + fileName);
                if (sourceStream != null && !targetFile.exists()) {
                    if (!targetDir.isDirectory() || !targetDir.canWrite()) {
                        throw new IllegalStateException("Target directory is not directory or not writable: " + targetDir.getAbsolutePath());
                    }
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
            configuration = new Configuration(new File(rootDir, cli.getOptionValue("file", "config.ini")));
        } catch (InvalidConfigurationException e) {
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

    private File detectRootDir() {
		if (cli.hasOption("home")) {
			String path = cli.getOptionValue("home", System.getProperty("user.home") + File.separator + DEFAULT_HOME_DIR_NAME);
			File file = new File(path);
			if (!file.exists()) {
				isTrue(file.mkdirs(), "Invalid home path: %s", file.getAbsolutePath());
			}
			return file;
		}
        String command = System.getProperty("sun.java.command");
        if (isNotEmpty(command) && !command.contains(File.pathSeparator) && command.contains(".jar")) {
            File jar = command.contains(File.separator) ? new File(command) : new File("./" + command);
            isTrue(jar.getParentFile() != null, "Invalid user path: %s", jar.getAbsolutePath());
            return jar.getParentFile().getAbsoluteFile();
        } else {
            return new File(System.getProperty("user.dir")).getAbsoluteFile();
        }
    }

	private static String detectRootJar() {
		String property = System.getProperty("sun.java.command");
		if (property.contains(".jar")) {
			return property.substring(0, property.indexOf(".jar") + 4);
		} else {
			return property.substring(0, property.indexOf(' '));
		}
	}

	/**
	 * Save original element positions
	 */
	private static class EmptyComparator implements Comparator {
		@SuppressWarnings("ComparatorMethodParameterNotUsed")
		@Override
		public int compare(Object o1, Object o2) {
			return 0;
		}
	}

    private static final Logger log = LoggerFactory.getLogger(EsetUpdater.class);
}
