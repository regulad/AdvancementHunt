package xyz.regulad.AdvancementHunt.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import xyz.regulad.AdvancementHunt.AdvancementHunt;
import xyz.regulad.AdvancementHunt.database.stats.PlayerStats;
import xyz.regulad.AdvancementHunt.database.stats.StatsColumn;
import xyz.regulad.AdvancementHunt.events.PostGameStateChangeEvent;
import xyz.regulad.AdvancementHunt.events.PreGameStateChangeEvent;
import xyz.regulad.AdvancementHunt.game.states.IdleState;
import xyz.regulad.AdvancementHunt.game.states.PlayingState;
import xyz.regulad.AdvancementHunt.messages.Message;

public class GameStateChangeListener implements Listener {
    private final AdvancementHunt plugin;

    public GameStateChangeListener(AdvancementHunt plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onGameStart(PostGameStateChangeEvent event) { // Post because of PAPI placeholders
        if (event.getNewGameState() instanceof PlayingState && event.getOldGameState() instanceof IdleState) { // Game start
            PlayingState newPlayingState = (PlayingState) event.getNewGameState();

            this.plugin.getMessageManager().dispatchMessage(newPlayingState.fleeingPlayer, Message.HUNTED_START);
            for (Player player : newPlayingState.huntingPlayers) {
                this.plugin.getMessageManager().dispatchMessage(player, Message.HUNTER_START);
            }
        }
    }

    @EventHandler
    public void onGameEnd(PreGameStateChangeEvent event) { // Pre because of PAPI placeholders
        if (event.getNewGameState() instanceof IdleState && event.getOldGameState() instanceof PlayingState) { // Game end
            PlayingState oldPlayingState = (PlayingState) event.getOldGameState();
            IdleState newIdleState = (IdleState) event.getNewGameState();

            switch (newIdleState.getGameEndReason()) {
                case HUNTED_WIN:
                    // Give the runner the win
                    PlayerStats winnerStats = this.plugin.getPlayerStats(oldPlayingState.fleeingPlayer);
                    winnerStats.updateColumn(StatsColumn.WINS, winnerStats.getColumn(StatsColumn.WINS) + 1);
                    this.plugin.getMessageManager().dispatchMessage(oldPlayingState.fleeingPlayer, Message.HUNTED_WIN);
                    // Give the hunters a loss
                    for (Player player : oldPlayingState.huntingPlayers) {
                        PlayerStats hunterStats = this.plugin.getPlayerStats(player);
                        hunterStats.updateColumn(StatsColumn.LOSSES, hunterStats.getColumn(StatsColumn.LOSSES) + 1);
                        this.plugin.getMessageManager().dispatchMessage(player, Message.HUNTED_WIN);
                    }
                    break;
                case HUNTER_WIN:
                    // Give the runner a loss
                    PlayerStats loserStats = this.plugin.getPlayerStats(oldPlayingState.fleeingPlayer);
                    loserStats.updateColumn(StatsColumn.LOSSES, loserStats.getColumn(StatsColumn.LOSSES) + 1);
                    this.plugin.getMessageManager().dispatchMessage(oldPlayingState.fleeingPlayer, Message.HUNTER_WIN);
                    // Give the hunters a win
                    for (Player player : oldPlayingState.huntingPlayers) {
                        PlayerStats hunterStats = this.plugin.getPlayerStats(player);
                        hunterStats.updateColumn(StatsColumn.WINS, hunterStats.getColumn(StatsColumn.WINS) + 1);
                        this.plugin.getMessageManager().dispatchMessage(player, Message.HUNTER_WIN);
                    }
                    break;
                case HUNTED_LEAVE:
                    // The hunted left. Punish themmmmmmm!
                    PlayerStats huntedStats = this.plugin.getPlayerStats(oldPlayingState.fleeingPlayer);
                    huntedStats.updateColumn(StatsColumn.LOSSES, huntedStats.getColumn(StatsColumn.LOSSES) + 1);
                    for (Player player : oldPlayingState.huntingPlayers) {
                        this.plugin.getMessageManager().dispatchMessage(player, Message.LEFT);
                    }
                    break;
                case HUNTER_LEAVE:
                    // All the hunters left. Punish themmmmmmm!
                    this.plugin.getMessageManager().dispatchMessage(oldPlayingState.fleeingPlayer, Message.LEFT);
                    for (Player player : oldPlayingState.leftPlayers) {
                        PlayerStats hunterStats = this.plugin.getPlayerStats(player);
                        hunterStats.updateColumn(StatsColumn.LOSSES, hunterStats.getColumn(StatsColumn.LOSSES) + 1);
                    }
                    for (Player player : oldPlayingState.huntingPlayers) {
                        this.plugin.getMessageManager().dispatchMessage(player, Message.LEFT);
                    }
                    break;
                case TIME_UP:
                    // Time is up.
                    // Give the runner a loss
                    PlayerStats huntedStats1 = this.plugin.getPlayerStats(oldPlayingState.fleeingPlayer);
                    huntedStats1.updateColumn(StatsColumn.LOSSES, huntedStats1.getColumn(StatsColumn.LOSSES) + 1);
                    this.plugin.getMessageManager().dispatchMessage(oldPlayingState.fleeingPlayer, Message.TIME_UP);
                    // Give the hunters a loss, too
                    for (Player player : oldPlayingState.huntingPlayers) {
                        PlayerStats hunterStats = this.plugin.getPlayerStats(player);
                        hunterStats.updateColumn(StatsColumn.LOSSES, hunterStats.getColumn(StatsColumn.LOSSES) + 1);
                        this.plugin.getMessageManager().dispatchMessage(player, Message.TIME_UP);
                    }
                    break;
            }
        }
    }
}
