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
    private File pathTmp;

    @ConfigurationPath(path = "path.web", type = ConfigurationType.DIRECTORY)
    private File pathWeb;

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
                String value = ini.get(paths[0], paths[1]);
                try {
                    setFieldValue(field, annotation, value);
                } catch (IllegalAccessException ignored) {
                } catch (InvalidConfigurationException e) {
                    //todo Cummulative error
                    throw e;
                }
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
}
