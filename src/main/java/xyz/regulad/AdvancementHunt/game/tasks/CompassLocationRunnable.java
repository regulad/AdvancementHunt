package xyz.regulad.AdvancementHunt.game.tasks;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.regulad.AdvancementHunt.AdvancementHunt;
import xyz.regulad.AdvancementHunt.game.states.PlayingState;

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
