package me.idriz.oss.commands.annotation;

import me.idriz.oss.commands.CommandAdapter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Argument {

    String[] completions() default {};

    //This gets checked for so don't use CommandAdapter as your adapter class.
    Class<? extends CommandAdapter> adapter() default CommandAdapter.class;

}
