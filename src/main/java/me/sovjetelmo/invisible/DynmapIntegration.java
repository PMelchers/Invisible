package me.sovjetelmo.invisible;

import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import static org.dynmap.bukkit.DynmapPlugin.plugin;

public class DynmapIntegration {

    public static void setInvisibleOnDynmap(Player player, boolean invisible) {

        if (invisible) {
            player.setMetadata("dynmap-invisible", new FixedMetadataValue(plugin, true));
        } else {
            player.removeMetadata("dynmap-invisible", plugin);
        }
    }
}
