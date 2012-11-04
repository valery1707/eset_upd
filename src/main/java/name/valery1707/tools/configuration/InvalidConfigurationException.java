package name.valery1707.tools.configuration;

public class InvalidConfigurationException extends Throwable {
    public InvalidConfigurationException(String message) {
        super(message);
    }

    public InvalidConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
