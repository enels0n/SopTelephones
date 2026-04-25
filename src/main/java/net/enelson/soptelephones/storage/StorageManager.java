package net.enelson.soptelephones.storage;

import java.io.File;
import java.io.IOException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public final class StorageManager {
    private final Plugin plugin;

    private YamlConfiguration providersConfig;
    private YamlConfiguration towersConfig;
    private YamlConfiguration phonesConfig;

    public StorageManager(Plugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        this.providersConfig = load("providers.yml");
        this.towersConfig = load("towers.yml");
        this.phonesConfig = load("phones.yml");
    }

    private YamlConfiguration load(String fileName) {
        File file = new File(this.plugin.getDataFolder(), fileName);
        if (!file.exists()) {
            this.plugin.saveResource(fileName, true);
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    public void saveProviders() {
        save(this.providersConfig, "providers.yml");
    }

    public void saveTowers() {
        save(this.towersConfig, "towers.yml");
    }

    public void savePhones() {
        save(this.phonesConfig, "phones.yml");
    }

    private void save(YamlConfiguration configuration, String fileName) {
        try {
            configuration.save(new File(this.plugin.getDataFolder(), fileName));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to save " + fileName, exception);
        }
    }

    public YamlConfiguration getProvidersConfig() {
        return providersConfig;
    }

    public YamlConfiguration getTowersConfig() {
        return towersConfig;
    }

    public YamlConfiguration getPhonesConfig() {
        return phonesConfig;
    }
}

