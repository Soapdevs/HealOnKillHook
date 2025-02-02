package etc.soap.healonkill;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class WGHookHandler {
    private final HealOnKill main;
    private StateFlag healOnKillFlag;

    public WGHookHandler(HealOnKill main) {
        this.main = main;
        registerFlag();  // Register the flag upon initialization
    }

    // Changed access modifier to public
    public void registerFlag() {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();

        try {
            healOnKillFlag = new StateFlag("heal-on-kill", false);
            registry.register(healOnKillFlag);
            main.getLogger().info("Heal-on-kill WorldGuard flag has been registered successfully.");
        } catch (FlagConflictException e) {
            main.wgHookActive = false;
            main.getLogger().severe("WorldGuard flag conflict detected. Heal-on-kill hook disabled automatically.");
        }
    }

    public void flagTestAsync(Player player, FlagTestCallback callback) {
        new BukkitRunnable() {
            @Override
            public void run() {
                boolean result = flagTest(player);
                Bukkit.getScheduler().runTask(main, () -> callback.onComplete(result));
            }
        }.runTaskAsynchronously(main);
    }


    // Changed access modifier to public
    public boolean flagTest(Player player) {
        if (healOnKillFlag == null) {
            main.getLogger().warning("Heal-on-kill flag is not registered.");
            return false;
        }

        BlockVector3 location = BukkitAdapter.asBlockVector(player.getLocation());
        World world = BukkitAdapter.adapt(player.getWorld());
        LocalPlayer wgPlayer = WorldGuardPlugin.inst().wrapPlayer(player);

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(world);

        if (regions == null) {
            main.getLogger().warning("RegionManager is null for world: " + world.getName());
            return false;
        }

        ApplicableRegionSet set = regions.getApplicableRegions(location);
        return set.testState(wgPlayer, healOnKillFlag);
    }

    public interface FlagTestCallback {
        void onComplete(boolean result);
    }
}