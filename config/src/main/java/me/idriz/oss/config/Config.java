package me.idriz.oss.config;

import me.idriz.oss.config.adapter.ConfigAdapter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public interface Config extends ConfigSection {

    Map<Class, ConfigAdapter> ADAPTERS = new HashMap<>();
    Map<Class, ConfigAdapter> CLASS_TO_ADAPTER_MAP = new HashMap<>();

    static <T> void registerTypeAdapter(Class<T> clazz, ConfigAdapter<T> adapter) {
        ADAPTERS.put(clazz, adapter);
        CLASS_TO_ADAPTER_MAP.put(adapter.getClass(), adapter);
    }

    static <T> void registerCustomAdapter(ConfigAdapter<T> adapter) {
        CLASS_TO_ADAPTER_MAP.put(adapter.getClass(), adapter);
    }

    static <T> ConfigAdapter<T> getAdapter(Class<T> clazz) {
        return ADAPTERS.get(clazz);
    }

    static <T> ConfigAdapter<T> getAdapterByAdapterClass(Class<?> clazz) {
        return CLASS_TO_ADAPTER_MAP.get(clazz);
    }


    Set<Object> getHooks();

    void reload();

    void save();

    void addHook(Object object);

}
