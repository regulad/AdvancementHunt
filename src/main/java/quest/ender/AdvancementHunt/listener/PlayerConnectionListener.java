package quest.ender.AdvancementHunt.listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.javatuples.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import quest.ender.AdvancementHunt.AdvancementHunt;
import quest.ender.AdvancementHunt.events.PostGameStateChangeEvent;
import quest.ender.AdvancementHunt.exceptions.BadGameStateException;
import quest.ender.AdvancementHunt.exceptions.GameNotStartedException;
import quest.ender.AdvancementHunt.game.GameEndReason;
import quest.ender.AdvancementHunt.game.states.IdleState;
import quest.ender.AdvancementHunt.game.states.PlayingState;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;

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
        if (this.plugin.getConfig().getBoolean("game.start_on_join") && this.plugin.getCurrentGameState() instanceof IdleState && this.plugin.getServer().getOnlinePlayers().size() >= Bukkit.getMaxPlayers()) {
            Player huntedPlayer = event.getPlayer(); // The last player to join will be the hunted.
            Collection<? extends Player> onlinePlayers = this.plugin.getServer().getOnlinePlayers();
            ArrayList<Player> hunterPlayers = new ArrayList<>();

            for (Player player : onlinePlayers) {
                if (!player.equals(huntedPlayer)) hunterPlayers.add(player);
            }

            try {
                final @Nullable Pair<@Nullable Advancement, Integer> advancement = this.plugin.getAdvancementManager().getAdvancement();
                final @Nullable Pair<@NotNull String, Integer> seed = this.plugin.getSeedManager().getSeed();

                if (advancement == null || seed == null)
                    throw new IllegalStateException("Unable to get data from the database!");

                this.plugin.startGame(huntedPlayer, hunterPlayers, advancement.getValue0(), Instant.ofEpochMilli(System.currentTimeMillis() + advancement.getValue1() * 60_000), seed.getValue0(), seed.getValue1());
            } catch (BadGameStateException | IllegalStateException exception) {
                exception.printStackTrace();
                this.plugin.getServer().sendMessage(Component.text("An error was encountered while starting the game: ").color(TextColor.color(11141120)).append(Component.text(exception.getClass().getName())));
            }
        }
    }
}
