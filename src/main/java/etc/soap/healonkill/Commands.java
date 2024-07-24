package etc.soap.healonkill;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Commands implements CommandExecutor {
    private HealOnKill main;
    private KillHandler killHandler;

    public Commands(HealOnKill main, KillHandler killHandler) {
        this.main = main;
        this.killHandler = killHandler;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sender.sendMessage("""
                    §7----------
                    §f§lAvailable Commands §f(§1Command §7- §aDescription §7- §cPermission§f):
                    §b/healonkill help §7- §aDisplays this help page
                    §b/healonkill reload §7- §aReloads the configuration §7- §chealonkill.reload
                    §b/healonkill test <player> §7- §aTest the active settings on yourself or another player §7- §chealonkill.test
                    §cMaintained by @Soapdevs on Github.
                    """);
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("healonkill.reload")) {
                sender.sendMessage("§cYou do not have permission to execute this command");
                return true;
            }

            main.reloadConfig();
            main.invalidConfigCheck();
            sender.sendMessage("§aConfiguration reloaded successfully");
            main.getLogger().info("Configuration was reloaded by " + sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("test")) {
            if (!sender.hasPermission("healonkill.test")) {
                sender.sendMessage("§cYou do not have permission to execute this command");
                return true;
            }

            Player target;

            if (args.length < 2)
                target = Bukkit.getPlayer(sender.getName());
            else target = Bukkit.getPlayer(args[1]);

            if (target == null) {
                sender.sendMessage("Insufficient arguments: Missing player target");
                return true;
            }

            killHandler.killAction(target);
            sender.sendMessage("§aTest executed on " + target.getName());
            return true;
        }

        sender.sendMessage("§cUnknown arguments, please type §c§l/healonkill help §cfor a list of commands");
        return true;
    }
}
