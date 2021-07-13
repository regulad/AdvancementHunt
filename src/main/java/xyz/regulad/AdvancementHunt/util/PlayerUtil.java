package xyz.regulad.AdvancementHunt.util;

import org.bukkit.Bukkit;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public class PlayerUtil {
    public static void resetAllAdvancementProgresses(Player target) { // For some reason, there isn't an advancement share for MV-I, so we have to do this. This is a minigame plugin, so I'm not going to mess with storing old advancements.
        final @NotNull Iterator<Advancement> advancementIterator = Bukkit.getServer().advancementIterator();
        while (advancementIterator.hasNext()) {
            Advancement advancementToCheck = advancementIterator.next();
            AdvancementProgress advancementProgress = target.getAdvancementProgress(advancementToCheck);
            for (String criteria : advancementProgress.getAwardedCriteria()) {
                advancementProgress.revokeCriteria(criteria);
            }
        }
    }

    public static void resetAllAdvancementProgressesForAllPlayers() { // Most verbose function name ever?
        for (Player player : Bukkit.getOnlinePlayers()) {
            resetAllAdvancementProgresses(player);
        }
    }
}
