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

        getCommand("invisible").setExecutor(new InvisibleCommand(this));

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

    public boolean isCooldownActive(Player player) {
        long currentTime = System.currentTimeMillis();
        long lastTimeUsed = lastUsedTime.getOrDefault(player, 0L);
        long cooldownTime = config.getLong("invisibility.cooldown");
        return currentTime - lastTimeUsed < cooldownTime * 50L;
    }

    public void setCooldown(Player player) {
        lastUsedTime.put(player, System.currentTimeMillis());
    }

    public void activateInvisibility(Player player) {
        double cost = config.getDouble("invisibility.cost");

        if (economy.getBalance(player) < cost) {
            player.sendMessage(ChatColor.RED + getConfig().getString("messages.insufficient_funds"));
            return;
        }

        economy.withdrawPlayer(player, cost);

        setCooldown(player);

        DynmapIntegration.setInvisibleOnDynmap(player, true);

        int duration = config.getInt("invisibility.duration");

        player.sendMessage(ChatColor.GREEN + getConfig().getString("messages.invisibility_activate")
                .replace("%duration%", String.valueOf(duration / 20)));

        new BukkitRunnable() {
            @Override
            public void run() {
                endInvisibility(player);
            }
        }.runTaskLater(this, duration);
    }

    public void cancelInvisibility(Player player) {
        DynmapIntegration.setInvisibleOnDynmap(player, false);
        player.sendMessage(ChatColor.YELLOW + getConfig().getString("messages.invisibility_cancelled"));
    }

    public void endInvisibility(Player player) {
        DynmapIntegration.setInvisibleOnDynmap(player, false);
        player.sendMessage(ChatColor.RED + getConfig().getString("messages.invisibility_expired"));
    }

    public void sendCooldownMessage(Player player) {
        long remainingCooldown = config.getLong("invisibility.cooldown") - (System.currentTimeMillis() - lastUsedTime.get(player)) / 50;
        player.sendMessage(ChatColor.RED + getConfig().getString("messages.cooldown_active").replace("%time%", String.valueOf(remainingCooldown / 60)));
    }
}
