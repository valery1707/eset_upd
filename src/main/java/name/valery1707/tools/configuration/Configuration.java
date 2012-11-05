package name.valery1707.tools.configuration;

import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class Configuration {

    @ConfigurationPath(path = "auth.user")
    private String username;

    @ConfigurationPath(path = "auth.pass")
    private String password;

    @ConfigurationPath(path = "path.tmp", type = ConfigurationType.DIRECTORY)
    private File pathTmp;

    @ConfigurationPath(path = "path.web", type = ConfigurationType.DIRECTORY)
    private File pathWeb;

    @ConfigurationPath(path = "remote.host", def = "update.eset.com")
    private String remoteHost;

    @ConfigurationPath(path = "remote.protocol", def = "http")
    private String remoteProtocol;

    @ConfigurationPath(path = "remote.maxRetries", def = "2", type = ConfigurationType.INTEGER)
    private Integer maxRetries;

    @ConfigurationPath(path = "db.version", def = "5", type = ConfigurationType.INTEGER)
    private Integer dbVersion;

    @ConfigurationPath(path = "db.langs", def = "1049")
    private String dbLangs;

    public Configuration(File file) throws InvalidConfigurationException {
        try {
            Ini ini = new Ini(file);
            loadValues(ini);
        } catch (InvalidFileFormatException e) {
            throw new InvalidConfigurationException("Invalid file format: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new InvalidConfigurationException("IO exception:" + e.getMessage(), e);
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
                    setFieldValue(field, annotation, value);
                } catch (IllegalAccessException ignored) {
                } catch (InvalidConfigurationException e) {
                    errors.append(e.getMessage()).append("\r\n");
                }
            }
            if (errors.length() > 0) {
                throw new InvalidConfigurationException(errors.toString());
            }
        }
    }

    private void setFieldValue(Field field, ConfigurationPath ann, String value) throws IllegalAccessException, InvalidConfigurationException {
        switch (ann.type()) {
            case STRING:
                field.set(this, value);
                break;
            case DIRECTORY:
                if (value == null) {
                    throw new InvalidConfigurationException("Invalid value for file-property " + ann.path());
                }
                File file = new File(value);
                if (file.exists() && file.isDirectory() && file.canWrite()) {
                    field.set(this, file);
                } else {
                    throw new InvalidConfigurationException("Not writable directory: " + file.getAbsolutePath());
                }
                break;
            case INTEGER:
                field.set(this, Integer.parseInt(value));
                break;
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
