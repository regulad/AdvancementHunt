package quest.ender.AdvancementHunt.game.tasks;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import quest.ender.AdvancementHunt.AdvancementHunt;
import quest.ender.AdvancementHunt.game.states.PlayingState;

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
