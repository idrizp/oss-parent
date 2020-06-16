package me.idriz.oss.config.adapter;

import me.idriz.oss.config.Config;

import java.util.List;
import java.util.function.Consumer;

public interface ConfigAdapter<T> {

    void read(Config config, String path, Consumer<T> consumer);

    void write(Config config, String key, T object);

    void writeList(Config config, String path, List<T> objects);

    void readList(Config config, String listPath, Consumer<List<T>> callback);

}
