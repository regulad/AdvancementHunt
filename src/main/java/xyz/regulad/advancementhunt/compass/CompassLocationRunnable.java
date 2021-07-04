package xyz.regulad.advancementhunt.compass;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.regulad.advancementhunt.AdvancementHunt;
import xyz.regulad.advancementhunt.gamestate.PlayingState;

public class CompassLocationRunnable extends BukkitRunnable {
    private final AdvancementHunt plugin;
    private final PlayingState gameState;

    public CompassLocationRunnable(AdvancementHunt plugin, PlayingState gameState) {
        this.plugin = plugin;
        this.gameState = gameState;
    }

    @Override
    public void run() {
        for (Player player : this.gameState.huntingPlayers) {
            player.setCompassTarget(this.gameState.fleeingPlayer.getLocation());
        }
    }
}
