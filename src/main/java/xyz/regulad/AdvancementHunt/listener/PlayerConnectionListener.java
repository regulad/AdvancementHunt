package xyz.regulad.AdvancementHunt.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import xyz.regulad.AdvancementHunt.AdvancementHunt;
import xyz.regulad.AdvancementHunt.exceptions.GameNotStartedException;
import xyz.regulad.AdvancementHunt.game.GameEndReason;
import xyz.regulad.AdvancementHunt.game.states.IdleState;
import xyz.regulad.AdvancementHunt.game.states.PlayingState;

public class PlayerConnectionListener implements Listener {
    private final AdvancementHunt plugin;

    public PlayerConnectionListener(AdvancementHunt plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerPreJoin(AsyncPlayerPreLoginEvent event) {
        if (!(this.plugin.getCurrentGameState() instanceof IdleState))
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, this.plugin.getMessageManager().getKickMessage());
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent playerQuitEvent) {
        if (this.plugin.getCurrentGameState() instanceof PlayingState) {
            Player leftPlayer = playerQuitEvent.getPlayer();
            PlayingState currentGameState = (PlayingState) this.plugin.getCurrentGameState();
            if (leftPlayer.equals(currentGameState.fleeingPlayer)) {
                try {
                    this.plugin.endGame(GameEndReason.HUNTED_LEAVE);
                } catch (GameNotStartedException ignored) {
                }
            } else {
                boolean playerIsHunter = false;
                for (Player player : currentGameState.huntingPlayers) {
                    if (leftPlayer.equals(player)) {
                        playerIsHunter = true;
                        break;
                    }
                }

                if (playerIsHunter) {
                    currentGameState.huntingPlayers.remove(leftPlayer);
                    currentGameState.leftPlayers.add(leftPlayer);
                    if (currentGameState.huntingPlayers.size() == 0) {
                        try {
                            this.plugin.endGame(GameEndReason.HUNTER_LEAVE);
                        } catch (GameNotStartedException ignored) {
                        }
                    }
                }
            }
        }
    }
}
