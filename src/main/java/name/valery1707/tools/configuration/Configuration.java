package name.valery1707.tools.configuration;

import name.valery1707.tools.eset.FileInfo;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class Configuration {

    public static final String REMOTE_MODE_DEF = "all";
    public static final String REMOTE_MODE_TYPE_URL = "Url";
    public static final String REMOTE_MODE_TYPE_NAME = "Name";
    public static final String REMOTE_MODE_TYPE_TYPE = "Type";
    public static final String REMOTE_MODE_TYPE_GROUP = "Group";
    public static final String[] REMOTE_MODE_TYPES = new String[]{REMOTE_MODE_TYPE_URL, REMOTE_MODE_TYPE_NAME, REMOTE_MODE_TYPE_TYPE, REMOTE_MODE_TYPE_GROUP};

    @ConfigurationPath(path = "auth.user", required = true)
    private String username;

    @ConfigurationPath(path = "auth.pass", required = true)
    private String password;

    @ConfigurationPath(path = "path.tmp", required = true, type = ConfigurationType.DIRECTORY)
    private File pathTmp;

    @ConfigurationPath(path = "path.web", required = true, type = ConfigurationType.DIRECTORY)
    private File pathWeb;

    @ConfigurationPath(path = "remote.host", def = "update.eset.com")
    private String remoteHost;

    @ConfigurationPath(path = "remote.protocol", def = "http")
    private String remoteProtocol;

    @ConfigurationPath(path = "remote.maxRetries", def = "2", type = ConfigurationType.INTEGER)
    private Integer maxRetries;

    @ConfigurationPath(path = "remote.mode", def = REMOTE_MODE_DEF)
    private String remoteMode;
    private Map<String, RemoteModeProcessor> remoteModeProcessors = new HashMap<String, RemoteModeProcessor>(REMOTE_MODE_TYPES.length);

    @ConfigurationPath(path = "db.version", def = "5", type = ConfigurationType.INTEGER)
    private Integer dbVersion;

    @ConfigurationPath(path = "db.langs", def = "1049")
    private String dbLangs;

    public Configuration(File file) throws InvalidConfigurationException {
        try {
            Ini ini = new Ini(file);
            loadValues(ini);
            initRemoteMode(ini);
        } catch (InvalidFileFormatException e) {
            throw new InvalidConfigurationException("Invalid file format: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new InvalidConfigurationException("IO exception:" + e.getMessage(), e);
        }
    }

    private void initRemoteMode(Ini ini) throws InvalidConfigurationException {
        String section = "mode_" + remoteMode;
        boolean allMode = false;
        if (!ini.containsKey(section)) {
            if (remoteMode.equals(REMOTE_MODE_DEF)) {
                allMode = true;
            } else {
                throw new InvalidConfigurationException("Configuration must contains section '" + section + "', with keys 'include' and 'exclude'.");
            }
        }
        for (String type : REMOTE_MODE_TYPES) {
            RemoteModeProcessor processor;
            if (allMode) {
                processor = new RemoteModeProcessor(".*", "");
            } else {
                processor = new RemoteModeProcessor(ini.get(section).get("include" + type, ".*"), ini.get(section).get("exclude" + type, ""));
            }
            remoteModeProcessors.put(type, processor);
        }
    }

    private void loadValues(Ini ini) throws InvalidConfigurationException {
        StringBuilder errors = new StringBuilder();
        for (Field field : this.getClass().getDeclaredFields()) {
            ConfigurationPath annotation = field.getAnnotation(ConfigurationPath.class);
            if (annotation != null) {
                String[] paths = annotation.path().split("\\.");
                String value = ini.get(paths[0]).get(paths[1], annotation.def());
                try {
                    if (isNotEmpty(value)) {
                        field.set(this, prepareValue(annotation, value));
                    } else {
                        throw new InvalidConfigurationException("Empty value in required parameter");
                    }
                } catch (IllegalAccessException ignored) {
                } catch (InvalidConfigurationException e) {
                    errors.append("\r\n").append(annotation.path()).append(": ").append(e.getMessage());
                }
            }
        }
        if (errors.length() > 0) {
            throw new InvalidConfigurationException(errors.toString());
        }
    }

    private Object prepareValue(ConfigurationPath ann, String value) throws InvalidConfigurationException {
        switch (ann.type()) {
            case STRING:
                return value;
            case DIRECTORY:
                File file = new File(value);
                if (file.exists() && file.isDirectory() && file.canWrite()) {
                    return file;
                } else {
                    throw new InvalidConfigurationException("Not writable directory: " + file.getAbsolutePath());
                }
            case INTEGER:
                try {
                    return Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    throw new InvalidConfigurationException("Invalid number: " + value, e);
                }
            default:
                throw new InvalidConfigurationException("Unknown configuration type: " + ann.type());
        }
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public File getPathTmp() {
        return pathTmp;
    }

    public File getPathWeb() {
        return pathWeb;
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public String getRemoteProtocol() {
        return remoteProtocol;
    }

    public boolean isRemoteProtocolHTTP() {
        return "HTTP".equalsIgnoreCase(remoteProtocol);
    }

    public int getRemotePort() {
        return isRemoteProtocolHTTP() ? 80 : 443;
    }

    public Integer getMaxRetries() {
        return maxRetries;
    }

    public boolean canProcessFile(FileInfo file) {
        return remoteModeProcessors.get(REMOTE_MODE_TYPE_URL).canProcess(file.getUrl()) &&
                remoteModeProcessors.get(REMOTE_MODE_TYPE_NAME).canProcess(file.getFilename()) &&
                remoteModeProcessors.get(REMOTE_MODE_TYPE_TYPE).canProcess(file.getType()) &&
                remoteModeProcessors.get(REMOTE_MODE_TYPE_GROUP).canProcess(file.getGroup());
    }

    public Integer getDbVersion() {
        return dbVersion;
    }

    public String getDbUrl() {
        return String.format("/eset_upd/v%d/update.ver", getDbVersion());
    }

    public List<String> getDbLangs() {
        return Arrays.asList(dbLangs.split(",\\s*"));
    }
}
