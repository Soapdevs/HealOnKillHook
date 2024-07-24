package etc.soap.healonkill;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public final class HealOnKill extends JavaPlugin {
    public boolean wgHookActive = getConfig().getBoolean("worldguard-hook");

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
        saveDefaultConfig();
        invalidConfigCheck();

        KillHandler killHandler = new KillHandler(this);
        //noinspection ConstantConditions
        getCommand("healonkill").setExecutor(new Commands(this, killHandler));
        Bukkit.getPluginManager().registerEvents(killHandler, this);

        getLogger().info("HealOnKill plugin has started");
    }

    @Override
    public void onDisable() {
        getLogger().info("HealOnKill plugin has stopped");
    }

    public void invalidConfigCheck() {
        try {
            new YamlConfiguration().load(new File(getDataFolder(), "config.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            getLogger().warning("----------");
            try {
                Files.copy(Paths.get(new File(getDataFolder(), "config.yml").getPath()), Paths.get(new File(getDataFolder(), "old_config.yml").getPath()), REPLACE_EXISTING);
                saveResource("config.yml", true);
                getLogger().warning("Invalid configuration detected - Current configuration was backed up to old_config.yml and a new config.yml generated");
            } catch (IOException e2) {
                e2.printStackTrace();
                getLogger().severe("Invalid configuration detected - Configuration backup failed");
            }
        }
    }
}
