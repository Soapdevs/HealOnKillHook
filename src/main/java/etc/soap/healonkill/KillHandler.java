package etc.soap.healonkill;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class KillHandler implements Listener {
    private final HealOnKill main;

    public KillHandler(HealOnKill main) {
        this.main = main;
    }

    @EventHandler
    public void playerDeathEvent(PlayerDeathEvent event) {
        Player player = event.getPlayer();

        if (main.wgHookActive) {
            Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
                boolean flagTestResult = new WGHookHandler(main).flagTest(player);

                if (flagTestResult) {
                    Player killer = player.getKiller();
                    if (killer == null) return;

                    // Run the killAction on the main thread since it involves Bukkit API
                    Bukkit.getScheduler().runTask(main, () -> killAction(killer));
                }
            });
        }
    }

    public void killAction(Player target) {
          Bukkit.getScheduler().runTask(main, () -> {
            if (main.getConfig().getString("mode").equalsIgnoreCase("Instant")) {
                // Directly set the player's health to maximum
                double maxHealth = target.getAttribute(Attribute.MAX_HEALTH).getBaseValue();
                target.setHealth(maxHealth);
            } else {
                PotionEffectType type;
                String configType = main.getConfig().getString("effect.type");
                if (configType.equalsIgnoreCase("Instant-Health")) {
                    // Simulate an instant heal effect by applying a health boost
                    type = PotionEffectType.REGENERATION; // Use regeneration as a substitute
                    int amplifier = main.getConfig().getInt("effect.amplifier");
                    int duration = 1; // Duration in ticks
                    target.addPotionEffect(new PotionEffect(type, duration, amplifier, false, main.getConfig().getBoolean("effect.particles")));

                    // Optionally set the player's health directly if regeneration is not instant enough
                    double maxHealth = target.getAttribute(Attribute.MAX_HEALTH).getBaseValue();
                    target.setHealth(maxHealth);
                } else {
                    type = PotionEffectType.REGENERATION;
                    target.addPotionEffect(new PotionEffect(type, main.getConfig().getInt("effect.duration") * 20,
                            main.getConfig().getInt("effect.amplifier"), false,
                            main.getConfig().getBoolean("effect.particles")));
                }
            }
        });
    }
}