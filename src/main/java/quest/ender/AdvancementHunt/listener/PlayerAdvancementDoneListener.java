package quest.ender.AdvancementHunt.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import quest.ender.AdvancementHunt.AdvancementHunt;
import quest.ender.AdvancementHunt.exceptions.GameNotStartedException;
import quest.ender.AdvancementHunt.game.GameEndReason;
import quest.ender.AdvancementHunt.game.state.PlayingState;

public class PlayerAdvancementDoneListener implements Listener {

    private final AdvancementHunt plugin;

    public PlayerAdvancementDoneListener(AdvancementHunt plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onAdvancementIsDone(PlayerAdvancementDoneEvent event) {
        Player player = event.getPlayer();
        if (this.plugin.getCurrentGameState() instanceof PlayingState playingState) {
            if (playingState.fleeingPlayer.equals(player) && playingState.goalAdvancement.equals(event.getAdvancement())) {
                try {
                    this.plugin.endGame(GameEndReason.HUNTED_WIN);
                } catch (GameNotStartedException ignored) {
                }
            }
        }
    }
}
