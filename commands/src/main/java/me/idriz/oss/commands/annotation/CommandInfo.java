package me.idriz.oss.commands.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface CommandInfo {

    String value();

    String[] aliases() default {};

    String[] usage() default {"&cContact an administrator for help regarding the usage."};

    String permission() default "";

    boolean async() default false;

}
