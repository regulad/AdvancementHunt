package xyz.regulad.AdvancementHunt.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import xyz.regulad.AdvancementHunt.AdvancementHunt;
import xyz.regulad.AdvancementHunt.exceptions.GameNotStartedException;
import xyz.regulad.AdvancementHunt.game.GameEndReason;
import xyz.regulad.AdvancementHunt.game.states.PlayingState;

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
