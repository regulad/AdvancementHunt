package quest.ender.AdvancementHunt.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import quest.ender.AdvancementHunt.AdvancementHunt;
import quest.ender.AdvancementHunt.database.stats.PlayerStats;
import quest.ender.AdvancementHunt.database.stats.StatsColumn;
import quest.ender.AdvancementHunt.events.PostGameStateChangeEvent;
import quest.ender.AdvancementHunt.events.PreGameStateChangeEvent;
import quest.ender.AdvancementHunt.game.state.IdleState;
import quest.ender.AdvancementHunt.game.state.PlayingState;
import quest.ender.AdvancementHunt.messages.Message;

public class GameStateChangeListener implements Listener {
    private final AdvancementHunt plugin;

    public GameStateChangeListener(AdvancementHunt plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onGameStart(PostGameStateChangeEvent event) { // Post because of PAPI placeholders
        if (event.getNewGameState() instanceof PlayingState newPlayingState && event.getOldGameState() instanceof IdleState) { // Game start
            this.plugin.getMessageManager().dispatchMessage(newPlayingState.fleeingPlayer, Message.HUNTED_START);
            for (Player player : newPlayingState.huntingPlayers) {
                this.plugin.getMessageManager().dispatchMessage(player, Message.HUNTER_START);
            }
        }
    }

    @EventHandler
    public void onGameEnd(PreGameStateChangeEvent event) { // Pre because of PAPI placeholders
        if (event.getNewGameState() instanceof IdleState newIdleState && event.getOldGameState() instanceof PlayingState oldPlayingState) { // Game end
            switch (newIdleState.getGameEndReason()) {
                case HUNTED_WIN -> {
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
                }
                case HUNTER_WIN -> {
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
                }
                case HUNTED_LEAVE -> {
                    // The hunted left. Punish themmmmmmm!
                    PlayerStats huntedStats = this.plugin.getPlayerStats(oldPlayingState.fleeingPlayer);
                    huntedStats.updateColumn(StatsColumn.LOSSES, huntedStats.getColumn(StatsColumn.LOSSES) + 1);
                    for (Player player : oldPlayingState.huntingPlayers) {
                        this.plugin.getMessageManager().dispatchMessage(player, Message.LEFT);
                    }
                }
                case HUNTER_LEAVE -> {
                    // All the hunters left. Punish themmmmmmm!
                    this.plugin.getMessageManager().dispatchMessage(oldPlayingState.fleeingPlayer, Message.LEFT);
                    for (Player player : oldPlayingState.leftPlayers) {
                        PlayerStats hunterStats = this.plugin.getPlayerStats(player);
                        hunterStats.updateColumn(StatsColumn.LOSSES, hunterStats.getColumn(StatsColumn.LOSSES) + 1);
                    }
                    for (Player player : oldPlayingState.huntingPlayers) {
                        this.plugin.getMessageManager().dispatchMessage(player, Message.LEFT);
                    }
                }
                case TIME_UP -> {
                    // Time is up.
                    // Give the runner a loss
                    PlayerStats huntedStats = this.plugin.getPlayerStats(oldPlayingState.fleeingPlayer);
                    huntedStats.updateColumn(StatsColumn.LOSSES, huntedStats.getColumn(StatsColumn.LOSSES) + 1);
                    this.plugin.getMessageManager().dispatchMessage(oldPlayingState.fleeingPlayer, Message.TIME_UP);
                    // Give the hunters a loss, too
                    for (Player player : oldPlayingState.huntingPlayers) {
                        PlayerStats hunterStats = this.plugin.getPlayerStats(player);
                        hunterStats.updateColumn(StatsColumn.LOSSES, hunterStats.getColumn(StatsColumn.LOSSES) + 1);
                        this.plugin.getMessageManager().dispatchMessage(player, Message.TIME_UP);
                    }
                }
            }
        }
    }

    @EventHandler
    public void fixGameModes(final @NotNull PostGameStateChangeEvent postGameStateChangeEvent) {
        @Nullable PlayingState possiblePlayingState = null;
        if (postGameStateChangeEvent.getOldGameState() instanceof PlayingState knownPlayingState)
            possiblePlayingState = knownPlayingState;
        else if (postGameStateChangeEvent.getNewGameState() instanceof PlayingState knownPlayingState)
            possiblePlayingState = knownPlayingState;

        if (possiblePlayingState != null)
            for (final @NotNull Player player : this.plugin.getServer().getOnlinePlayers())
                player.setGameMode(possiblePlayingState.worldUtil.getMVWorldManager().getMVWorld(player.getWorld()).getGameMode()); // Janky as hell. Just to fix a small issue.
    }
}
