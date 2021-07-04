package xyz.regulad.advancementhunt.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import xyz.regulad.advancementhunt.AdvancementHunt;
import xyz.regulad.advancementhunt.database.PlayerStats;
import xyz.regulad.advancementhunt.events.gamestate.PostGameStateChangeEvent;
import xyz.regulad.advancementhunt.events.gamestate.PreGameStateChangeEvent;
import xyz.regulad.advancementhunt.game.states.IdleState;
import xyz.regulad.advancementhunt.game.states.PlayingState;
import xyz.regulad.advancementhunt.messages.Message;

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
                    winnerStats.setWins(winnerStats.getWins() + 1);
                    this.plugin.getMessageManager().dispatchMessage(oldPlayingState.fleeingPlayer, Message.HUNTED_WIN);
                    // Give the hunters a loss
                    for (Player player : oldPlayingState.huntingPlayers) {
                        PlayerStats hunterStats = this.plugin.getPlayerStats(player);
                        hunterStats.setLosses(hunterStats.getLosses() + 1);
                        this.plugin.getMessageManager().dispatchMessage(player, Message.HUNTED_WIN);
                    }
                    break;
                case HUNTER_WIN:
                    // Give the runner a loss
                    PlayerStats loserStats = this.plugin.getPlayerStats(oldPlayingState.fleeingPlayer);
                    loserStats.setLosses(loserStats.getLosses() + 1);
                    this.plugin.getMessageManager().dispatchMessage(oldPlayingState.fleeingPlayer, Message.HUNTER_WIN);
                    // Give the hunters a win
                    for (Player player : oldPlayingState.huntingPlayers) {
                        PlayerStats hunterStats = this.plugin.getPlayerStats(player);
                        hunterStats.setWins(hunterStats.getWins() + 1);
                        this.plugin.getMessageManager().dispatchMessage(player, Message.HUNTER_WIN);
                    }
                    break;
                case HUNTED_LEAVE:
                    // The hunted left. Punish themmmmmmm!
                    PlayerStats huntedStats = this.plugin.getPlayerStats(oldPlayingState.fleeingPlayer);
                    huntedStats.setLosses(huntedStats.getLosses() + 1);
                    for (Player player : oldPlayingState.huntingPlayers) {
                        this.plugin.getMessageManager().dispatchMessage(player, Message.LEFT);
                    }
                    break;
                case HUNTER_LEAVE:
                    // All the hunters left. Punish themmmmmmm!
                    this.plugin.getMessageManager().dispatchMessage(oldPlayingState.fleeingPlayer, Message.LEFT);
                    for (Player player : oldPlayingState.leftPlayers) {
                        PlayerStats hunterStats = this.plugin.getPlayerStats(player);
                        hunterStats.setLosses(hunterStats.getLosses() + 1);
                    }
                    for (Player player : oldPlayingState.huntingPlayers) {
                        this.plugin.getMessageManager().dispatchMessage(player, Message.LEFT);
                    }
                    break;
                case TIME_UP:
                    // Time is up.
                    // Give the runner a loss
                    PlayerStats huntedStats1 = this.plugin.getPlayerStats(oldPlayingState.fleeingPlayer);
                    huntedStats1.setLosses(huntedStats1.getLosses() + 1);
                    this.plugin.getMessageManager().dispatchMessage(oldPlayingState.fleeingPlayer, Message.TIME_UP);
                    // Give the hunters a loss, too
                    for (Player player : oldPlayingState.huntingPlayers) {
                        PlayerStats hunterStats = this.plugin.getPlayerStats(player);
                        hunterStats.setLosses(hunterStats.getLosses() + 1);
                        this.plugin.getMessageManager().dispatchMessage(player, Message.TIME_UP);
                    }
                    break;
            }
        }
    }
}
