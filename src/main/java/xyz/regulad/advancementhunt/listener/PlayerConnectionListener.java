package xyz.regulad.advancementhunt.listener;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import xyz.regulad.advancementhunt.AdvancementHunt;
import xyz.regulad.advancementhunt.exceptions.GameNotStartedException;
import xyz.regulad.advancementhunt.game.GameEndReason;
import xyz.regulad.advancementhunt.game.states.IdleState;
import xyz.regulad.advancementhunt.game.states.PlayingState;
import xyz.regulad.advancementhunt.util.PlayerUtil;

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
    public void onPlayerJoin(PlayerJoinEvent playerJoinEvent) {
        if (this.plugin.getCurrentGameState() instanceof IdleState) {
            Player player = playerJoinEvent.getPlayer();
            player.teleport(this.plugin.getServer().getWorlds().get(0).getSpawnLocation()); // Assumed to be the starting location
            PlayerUtil.resetPlayer(player, GameMode.ADVENTURE, true);
        }
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
