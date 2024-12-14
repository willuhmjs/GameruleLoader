package com.willuhmjs.gameruleLoader;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.Level;

public class GameruleLoader extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("GameruleLoader is being enabled.");
        saveDefaultConfig();
        applyGameRules();
        getLogger().info("GameruleLoader has been enabled.");
    }

    @Override
    public void saveDefaultConfig() {
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            getDataFolder().mkdirs();
            try {
                if (getResource("config.yml") != null) {
                    Files.copy(getResource("config.yml"), configFile.toPath());
                    getLogger().info("Default config.yml has been created.");
                } else {
                    getLogger().severe("Default config.yml resource not found.");
                }
            } catch (IOException e) {
                getLogger().log(Level.SEVERE, "Could not create default config.yml", e);
            }
        }
    }

    private void applyGameRules() {
        FileConfiguration config = getConfig();
        for (World world : Bukkit.getWorlds()) {
            String worldName = world.getName();
            if (config.isConfigurationSection("gamerules." + worldName)) {
                config.getConfigurationSection("gamerules." + worldName).getKeys(false).forEach(gameruleName -> {
                    String path = "gamerules." + worldName + "." + gameruleName;
                    try {
                        GameRule<?> gamerule = GameRule.getByName(gameruleName);
                        if (gamerule != null) {
                            String value = config.getString(path);
                            if (value != null) {
                                world.setGameRule((GameRule<Object>) gamerule, parseValue(gamerule, value));
                            }
                        } else {
                            getLogger().warning("Unknown gamerule: " + gameruleName);
                        }
                    } catch (Exception e) {
                        getLogger().log(Level.WARNING, "Error setting gamerule " + gameruleName + " for world " + worldName, e);
                    }
                });
            }
        }
    }

    private Object parseValue(GameRule<?> gamerule, String value) {
        if (gamerule.getType() == Boolean.class) {
            return Boolean.parseBoolean(value);
        } else if (gamerule.getType() == Integer.class) {
            return Integer.parseInt(value);
        } else {
            throw new IllegalArgumentException("Unsupported gamerule type: " + gamerule.getType());
        }
    }
}
