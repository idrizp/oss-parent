package me.idriz.oss.config.annotation;

import me.idriz.oss.config.adapter.ConfigAdapter;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Adapter {

    Class<? extends ConfigAdapter> value();

}
