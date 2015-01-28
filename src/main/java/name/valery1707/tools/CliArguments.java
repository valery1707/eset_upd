package name.valery1707.tools;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import java.io.File;

@SuppressWarnings("FieldCanBeLocal")
@Parameters(separators = " =")
public class CliArguments {
	@Parameter(names = {"--log", "--verbose"}, description = "Level of verbosity. Not used", hidden = true)
	private Integer verbose = 1;

	@Parameter(names = {"-h", "-?", "--help"}, description = "Show this help", help = true)
	private boolean help = false;

	@Parameter(
			names = {"-H", "--home"},
			description = "Home directory used from store configuration files and as root for open config (see --file)\r\n" +
						  "       Default:\r\n" +
						  "       *) option not set: directory where .jar-file located\r\n" +
						  "       *) option set with arg: user defined directory"
	)
	private File home;

	@Parameter(
			names = {"-f", "--file", "--conf", "--configuration"},
			description = "Path for file with configuration, relative from home directory (see --home)"
	)
	private String configuration = "config.ini";

	@Parameter(
			names = "--skip-extract-samples",
			description = "Skip extracting samples of configuration files to home directory"
	)
	private boolean skipExtractSamples = false;

	public Integer getVerbose() {
		return verbose;
	}

	public boolean isHelp() {
		return help;
	}

	public File getHome() {
		return home;
	}

	public String getConfiguration() {
		return configuration;
	}

	public boolean isSkipExtractSamples() {
		return skipExtractSamples;
	}
}
