package xyz.regulad.AdvancementHunt.util;

import org.bukkit.Bukkit;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

import java.util.Iterator;

public class PlayerUtil { // Stupid, but I do these a lot.
    public static void resetPlayer(Player target) { // Like dying, but not!
        // target.teleport(target.getBedSpawnLocation() != null ? target.getBedSpawnLocation() : target.getWorld().getSpawnLocation());
        target.getInventory().clear();
        target.setHealth(target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        target.setFoodLevel(20);
        target.setSaturation(5);
        target.setExhaustion(0);
        target.setTotalExperience(0);

        Iterator<Advancement> advancementIterator = Bukkit.getServer().advancementIterator();
        while (advancementIterator.hasNext()) {
            Advancement advancementToCheck = advancementIterator.next();
            AdvancementProgress advancementProgress = target.getAdvancementProgress(advancementToCheck);
            for (String criteria : advancementProgress.getAwardedCriteria()) {
                advancementProgress.revokeCriteria(criteria);
            }
        }
    }

    public static void resetAllPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            resetPlayer(player);
        }
    }
}
