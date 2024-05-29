package quest.ender.AdvancementHunt.listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerListPingEvent;
import quest.ender.AdvancementHunt.AdvancementHunt;
import quest.ender.AdvancementHunt.events.PostGameStateChangeEvent;
import quest.ender.AdvancementHunt.exceptions.GameNotStartedException;
import quest.ender.AdvancementHunt.game.GameEndReason;
import quest.ender.AdvancementHunt.game.state.IdleState;
import quest.ender.AdvancementHunt.game.state.PlayingState;

public class PlayerConnectionListener implements Listener {
    private final AdvancementHunt plugin;

    private boolean maxPlayersIsZero = false;

    public PlayerConnectionListener(AdvancementHunt plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerPreJoin(AsyncPlayerPreLoginEvent event) {
        if (!(this.plugin.getCurrentGameState() instanceof IdleState))
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Component.text(this.plugin.getMessageManager().getKickMessage()).color(TextColor.color(16733525)));
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent playerQuitEvent) {
        if (this.plugin.getCurrentGameState() instanceof PlayingState currentGameState) {
            Player leftPlayer = playerQuitEvent.getPlayer();
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

    @EventHandler
    public void onGameStateChange(PostGameStateChangeEvent event) {
        if (event.getNewGameState() instanceof PlayingState && event.getOldGameState() instanceof IdleState) { // A game started.
            if (this.plugin.getConfig().getBoolean("game.max_to_zero")) this.maxPlayersIsZero = true;
        } else if (event.getNewGameState() instanceof IdleState && event.getOldGameState() instanceof PlayingState) { // A game ended.
            if (this.maxPlayersIsZero) this.maxPlayersIsZero = false;
        }
    }

    @EventHandler
    public void onPing(ServerListPingEvent event) {
        if (this.maxPlayersIsZero) event.setMaxPlayers(0);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player huntedPlayer = event.getPlayer(); // The last player to join will be the hunted.
        this.plugin.startGameUnattended(huntedPlayer, true);
    }
}
