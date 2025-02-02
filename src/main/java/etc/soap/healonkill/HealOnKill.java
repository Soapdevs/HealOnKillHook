package etc.soap.healonkill;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public final class HealOnKill extends JavaPlugin {
    public boolean wgHookActive;

    @Override
    public void onLoad() {
        if (wgHookActive && Bukkit.getPluginManager().getPlugin("WorldGuard") != null)
            new WGHookHandler(this).registerFlag();
        else {
            wgHookActive = false;
            getLogger().severe("Failed to detect WorldGuard. WorldGuard hook disabled automatically");
        }
    }

    @Override
    public void onEnable() {
        if (wgHookActive && Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
            WGHookHandler wgHookHandler = new WGHookHandler(this);
            wgHookHandler.registerFlag();
            getLogger().info("WorldGuard hook and flag registered during onEnable.");
        } else {
            wgHookActive = false;
            getLogger().warning("WorldGuard not detected or disabled in config. Hook disabled.");
        }

        // Register commands and event listeners after loading config and WorldGuard hook status
        KillHandler killHandler = new KillHandler(this);
        getCommand("healonkill").setExecutor(new Commands(this, killHandler));
        Bukkit.getPluginManager().registerEvents(killHandler, this);

        getLogger().info("HealOnKill plugin has started");
    }

    @Override
    public void onDisable() {
        getLogger().info("HealOnKill plugin has stopped");
    }

    public void invalidConfigCheck() {
        File configFile = new File(getDataFolder(), "config.yml");
        File backupFile = new File(getDataFolder(), "old_config.yml");

        try {
            YamlConfiguration config = new YamlConfiguration();
            config.load(configFile);
        } catch (IOException e) {
            getLogger().severe("Error loading configuration file: " + e.getMessage());
        } catch (InvalidConfigurationException e) {
            getLogger().warning("Invalid configuration detected. Backing up and regenerating config.yml...");
            try {
                Files.copy(configFile.toPath(), backupFile.toPath(), REPLACE_EXISTING);
                saveResource("config.yml", true);
                getLogger().info("Configuration was backed up to old_config.yml, and a new config.yml was generated.");
            } catch (IOException e2) {
                getLogger().severe("Failed to back up invalid configuration: " + e2.getMessage());
            }
        }
    }
}