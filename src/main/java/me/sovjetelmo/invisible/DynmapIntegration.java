package me.sovjetelmo.invisible;

import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import static org.dynmap.bukkit.DynmapPlugin.plugin;

public class DynmapIntegration {

    public static void setInvisibleOnDynmap(Player player, boolean invisible) {
        // This is where you interact with Dynmap API to set invisibility
        // This method should toggle the player's visibility on Dynmap
        // Example: dynmap API logic here to set player visibility

        if (invisible) {
            // Hide player from Dynmap
            player.setMetadata("dynmap-invisible", new FixedMetadataValue(plugin, true));
        } else {
            // Show player on Dynmap
            player.removeMetadata("dynmap-invisible", plugin);
        }
    }
}
