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
import org.bukkit.entity.Player;

public class WGHookHandler {
    private HealOnKill main;
    public WGHookHandler(HealOnKill main) {
        this.main = main;
    }

    public void registerFlag() {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();

        try {
            StateFlag flag = new StateFlag("heal-on-kill", false);
            registry.register(flag);
            main.getLogger().info("WorldGuard flag registered successfully");
        } catch (FlagConflictException e) {
            main.wgHookActive = false;
            main.getLogger().severe("WorldGuard flag conflict detected. WorldGuard hook disabled automatically");
        }
    }

    public boolean flagTest(Player player) {
        BlockVector3 location = BukkitAdapter.asBlockVector(player.getLocation());
        World world = BukkitAdapter.adapt(player.getWorld());
        LocalPlayer wgPlayer = WorldGuardPlugin.inst().wrapPlayer(player);

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(world);

        if (regions == null)
            return true;

        ApplicableRegionSet set = regions.getApplicableRegions(location);

        return set.testState(wgPlayer, (StateFlag) WorldGuard.getInstance().getFlagRegistry().get("heal-on-kill"));
    }
}
