package me.idriz.oss.commands.validation;

import me.idriz.oss.commands.annotation.CommandInfo;
import me.idriz.oss.commands.annotation.Default;

import java.util.Arrays;

public class Validation {

    public static <T> boolean isCommandClass(Class<T> o) {

        boolean hasAnnotation = o.isAnnotationPresent(CommandInfo.class);
        if (!hasAnnotation) return false;
        boolean hasName = !o.getAnnotation(CommandInfo.class).value().equals("");
        if (!hasName) return false;
        return Arrays.stream(o.getDeclaredMethods()).anyMatch(method -> method.isAnnotationPresent(CommandInfo.class) || method.isAnnotationPresent(Default.class));
    }

}
