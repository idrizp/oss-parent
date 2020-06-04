package me.idriz.oss.config;

import java.util.List;
import java.util.Map;

public interface ConfigSection {

    void set(String path, Object value);

    Map<String, Object> getValues();

    Object get(String path);

    default int getInt(String path) {
        return (int) get(path);
    }

    default boolean getBoolean(String path) {
        return (boolean) get(path);
    }

    default double getDouble(String path) {
        return (double) get(path);
    }

    default float getFloat(String path) {
        return (float) get(path);
    }

    default String getString(String path) {
        return (String) get(path);
    }

    default List<?> getList(String path) {
        return (List<?>) get(path);
    }

    default short getShort(String path) {
        return (short) get(path);
    }

    default byte getByte(String path) {
        return (byte) get(path);
    }

    default List<String> getStringList(String path) {
        return (List<String>) getList(path);
    }


}
