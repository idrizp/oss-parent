package me.idriz.oss.config.adapter;

import me.idriz.oss.config.Config;

import java.util.List;

public interface ConfigAdapter<T> {

    T fromString(Config config, String path);

    String toString(Config config, T object);

    List<T> fromStringList(Config config, String listPath);

}
