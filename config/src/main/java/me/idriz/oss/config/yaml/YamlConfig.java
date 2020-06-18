package me.idriz.oss.config.yaml;

import me.idriz.oss.config.Config;
import me.idriz.oss.config.ConfigSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class YamlConfig extends YamlConfiguration implements Config {

    private final Set<Object> hooks = new HashSet<>();

    private final File file;
    private final File directory;

    private final String name;

    public YamlConfig(File directory, String name) {
        this.directory = directory;
        this.file = new File(directory, name + ".yml");
        if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.name = name;
        try {
            load(file);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Set<Object> getHooks() {
        return hooks;
    }

    @Override
    public void reload() {
        for (Object hook : hooks) {
            try {
                YamlProcessor.initializeHook(this, hook);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void save() {
        try {
            hooks.forEach((hook) -> YamlProcessor.writeHook(this, hook));
            this.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveFile() {
        try {
            this.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addHook(Object object) {
        hooks.add(object);
        try {
            YamlProcessor.initializeHook(this, object);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Map<String, Object> getValues() {
        return getValues(false);
    }

    @Override
    public Set<String> getKeys() {
        return getKeys(false);
    }

    @Override
    public ConfigSection getSection(String section) {
        try {
            return new YamlConfigSection(this, section);
        } catch (NullPointerException ex) {
            return null;
        }
    }

}
