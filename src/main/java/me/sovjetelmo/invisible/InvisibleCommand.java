package me.sovjetelmo.invisible;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InvisibleCommand implements CommandExecutor {

    private final Invisible plugin;

    public InvisibleCommand(Invisible plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return false;
        }

        Player player = (Player) sender;

        if (plugin.isCooldownActive(player)) {
            plugin.sendCooldownMessage(player);
            return false;
        }

        if (args.length == 0) {
            plugin.activateInvisibility(player);
        } else if (args.length == 1 && args[0].equalsIgnoreCase("cancel")) {
            plugin.cancelInvisibility(player);
            player.sendMessage(ChatColor.YELLOW + "Your invisibility has been canceled.");
        } else {
            player.sendMessage(ChatColor.RED + "Usage: /invisible [cancel]");
        }

        return true;
    }
}
