package name.valery1707.tools.configuration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ConfigurationPath {
    String path();
    ConfigurationType type() default ConfigurationType.STRING;
    String def() default "";
    boolean required() default false;
}
