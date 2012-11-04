package name.valery1707.tools.configuration;

import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

public class Configuration {

    @ConfigurationPath(path = "auth.user")
    private String username;

    @ConfigurationPath(path = "auth.pass")
    private String password;

    @ConfigurationPath(path = "path.tmp", type = ConfigurationType.DIRECTORY)
    private String pathTmp;

    @ConfigurationPath(path = "path.web", type = ConfigurationType.DIRECTORY)
    private String pathWeb;

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
        for (Field field : this.getClass().getDeclaredFields()) {
            ConfigurationPath annotation = field.getAnnotation(ConfigurationPath.class);
            if (annotation != null) {
                String[] paths = annotation.path().split("\\.");
                String section = paths[0];
                String option = paths[1];
                String value = ini.get(section, option);
                try {
                    switch (annotation.type()) {
                        case STRING:
                            field.set(this, value);
                            break;
                        case DIRECTORY:
                            File file = new File(value);
                            if (file.exists() && file.isDirectory() && file.canWrite()) {
                                field.set(this, file);
                            } else {
                                //todo Cummulative error
                                throw new InvalidConfigurationException("Not writable directory: " + file.getAbsolutePath());
                            }
                            break;
                        default:
                            throw new InvalidConfigurationException("Unknown configuration type: " + annotation.type());
                    }
                } catch (IllegalAccessException ignored) {
                }
            }
        }
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getPathTmp() {
        return pathTmp;
    }

    public String getPathWeb() {
        return pathWeb;
    }
}
