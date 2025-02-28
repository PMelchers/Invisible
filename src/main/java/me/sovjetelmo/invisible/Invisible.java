package me.sovjetelmo.invisible;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public final class Invisible extends JavaPlugin {

    private Economy economy;
    private FileConfiguration config;
    private final Map<Player, Long> lastUsedTime = new HashMap<>();

    @Override
    public void onEnable() {
        getLogger().info("Invisible plugin is enabling...");

        // Delay economy setup to allow Vault to load first
        getServer().getScheduler().runTask(this, () -> {
            if (!setupEconomy()) {
                getLogger().warning("Failed to set up economy. Disabling plugin.");
                getServer().getPluginManager().disablePlugin(this);
            } else {
                getLogger().info("Economy setup complete.");
            }
        });

        saveDefaultConfig();
        config = getConfig();

        // Register command and listener
        getCommand("invisible").setExecutor(new InvisibleCommand(this));

        // Any other initialization logic you need here
    }

    @Override
    public void onDisable() {
        getLogger().info("Invisible plugin is disabling...");
    }

    private boolean setupEconomy() {
        getLogger().info("Attempting to set up economy...");

        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);

        if (economyProvider != null) {
            economy = economyProvider.getProvider();
            getLogger().info("Vault economy provider successfully found and initialized.");
        } else {
            getLogger().warning("Vault economy provider not found. Please ensure Vault and an economy plugin like EssentialsX are installed and enabled.");
        }

        return economy != null;
    }

    // Check if cooldown is active for a player
    public boolean isCooldownActive(Player player) {
        long currentTime = System.currentTimeMillis();
        long lastTimeUsed = lastUsedTime.getOrDefault(player, 0L);
        long cooldownTime = config.getLong("invisibility.cooldown"); // Cooldown time in ticks
        return currentTime - lastTimeUsed < cooldownTime * 50L; // Convert to milliseconds (1 tick = 50 ms)
    }

    // Set the cooldown for a player
    public void setCooldown(Player player) {
        lastUsedTime.put(player, System.currentTimeMillis());
    }

    // Activate invisibility for a player
    public void activateInvisibility(Player player) {
        double cost = config.getDouble("invisibility.cost");

        // Check if player has sufficient funds
        if (economy.getBalance(player) < cost) {
            player.sendMessage(ChatColor.RED + getConfig().getString("messages.insufficient_funds"));
            return; // Stop further execution if player doesn't have enough funds
        }

        economy.withdrawPlayer(player, cost);

        // Set cooldown for the player
        setCooldown(player);

        // Set the player invisible on Dynmap
        DynmapIntegration.setInvisibleOnDynmap(player, true);

        // Get the duration in seconds
        int duration = config.getInt("invisibility.duration");

        // Send activation message (now only happens if invisibility is successfully activated)
        player.sendMessage(ChatColor.GREEN + getConfig().getString("messages.invisibility_activate")
                .replace("%duration%", String.valueOf(duration / 20))); // Convert ticks to seconds

        // Schedule task to end invisibility after the specified duration
        new BukkitRunnable() {
            @Override
            public void run() {
                endInvisibility(player);
            }
        }.runTaskLater(this, duration); // Duration in ticks (1 second = 20 ticks)
    }


    // Cancel invisibility for a player
    public void cancelInvisibility(Player player) {
        DynmapIntegration.setInvisibleOnDynmap(player, false);
        player.sendMessage(ChatColor.YELLOW + getConfig().getString("messages.invisibility_cancelled"));
    }

    // End invisibility for a player
    public void endInvisibility(Player player) {
        DynmapIntegration.setInvisibleOnDynmap(player, false);
        player.sendMessage(ChatColor.RED + getConfig().getString("messages.invisibility_expired"));
    }

    // Send a message to the player about the remaining cooldown
    public void sendCooldownMessage(Player player) {
        long remainingCooldown = config.getLong("invisibility.cooldown") - (System.currentTimeMillis() - lastUsedTime.get(player)) / 50;
        player.sendMessage(ChatColor.RED + getConfig().getString("messages.cooldown_active").replace("%time%", String.valueOf(remainingCooldown / 60)));
    }
}
