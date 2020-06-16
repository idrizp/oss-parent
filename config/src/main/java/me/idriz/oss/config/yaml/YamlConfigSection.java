package me.idriz.oss.config.yaml;

import me.idriz.oss.config.ConfigSection;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Map;
import java.util.Set;

public class YamlConfigSection implements ConfigSection {

    private final YamlConfig yamlConfig;
    private final String sectionPath;
    private final ConfigurationSection yamlSection;

    public YamlConfigSection(YamlConfig yamlConfig, String sectionPath) {
        this.yamlConfig = yamlConfig;
        this.yamlSection = yamlConfig.getConfigurationSection(sectionPath);
        if(yamlSection == null) throw new NullPointerException("Couldn't find that section.");
        this.sectionPath = sectionPath;
    }

    @Override
    public void set(String path, Object value) {
        yamlSection.set(path, value);
    }

    @Override
    public Map<String, Object> getValues() {
        return yamlSection.getValues(false);
    }

    @Override
    public Set<String> getKeys() {
        return yamlSection.getKeys(false);
    }


    @Override
    public Object get(String path) {
        return yamlSection.get(path);
    }

    @Override
    public ConfigSection getSection(String section) {
        try {
            return new YamlConfigSection(yamlConfig, sectionPath + "." + section);
        } catch(NullPointerException ex) {
            return null;
        }
    }
}
