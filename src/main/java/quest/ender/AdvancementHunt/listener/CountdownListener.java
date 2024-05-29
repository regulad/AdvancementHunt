package quest.ender.AdvancementHunt.listener;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import quest.ender.AdvancementHunt.AdvancementHunt;
import quest.ender.AdvancementHunt.events.PostGameStateChangeEvent;
import quest.ender.AdvancementHunt.events.PreGameStateChangeEvent;
import quest.ender.AdvancementHunt.exceptions.GameAlreadyStartedException;
import quest.ender.AdvancementHunt.game.state.IdleState;
import quest.ender.AdvancementHunt.game.state.PlayingState;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class CountdownListener implements Listener {
    private final @NotNull AdvancementHunt plugin;

    private boolean countingDown = false;
    private boolean skipCountdown = false;

    public CountdownListener(@NotNull AdvancementHunt plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (this.plugin.getCurrentGameState() instanceof PlayingState playingState && !((playingState.fleeingPlayer.equals(event.getPlayer()) && playingState.huntedCanMove()) || playingState.canMove()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onGameStateChange(PreGameStateChangeEvent event) {
        if (this.skipCountdown) {
            this.skipCountdown = false;
            return; // Just to skip recursion.
        }
        final Integer[] countdown = {this.plugin.getConfig().getInt("game.startup")};
        if (countdown[0] != 0 && event.getNewGameState() instanceof PlayingState playingState) {
            this.countingDown = true;

            event.setCancelled(true); // We are just "postponing" it.

            new BukkitRunnable() { // A little bit janky.
                @Override
                public void run() {
                    if (countdown[0] <= 0) { // Primitive method to cancel.
                        CountdownListener.this.skipCountdown = true;
                        try {
                            CountdownListener.this.plugin.startGame(playingState);
                        } catch (GameAlreadyStartedException ignored) {
                        }
                        CountdownListener.this.countingDown = false;
                        if (!this.isCancelled()) {
                            this.cancel();
                        }
                    } else if (!CountdownListener.this.countingDown) {
                        CountdownListener.this.skipCountdown = true;
                        if (!this.isCancelled()) {
                            this.cancel();
                        }
                    } else {
                        final @NotNull Server serverAudience = CountdownListener.this.plugin.getServer();
                        serverAudience.playSound(Sound.sound(Key.key("block.note_block.snare"), Sound.Source.BLOCK, 1f, 0.5f));
                        serverAudience.showTitle(Title.title(Component.text(String.valueOf(countdown[0])), Component.text("⚠ Lag incoming! ⚠", NamedTextColor.RED)));

                        countdown[0] -= 1;
                    }
                }
            }.runTaskTimer(this.plugin, 20L, 20L); // One second later.
        }
    }

    @EventHandler
    public void cancelCountdown(PlayerQuitEvent event) {
        if (this.countingDown) this.countingDown = false;
    }

    @EventHandler
    public void onGameEnd(PostGameStateChangeEvent event) {
        if (this.plugin.getServer().getPluginManager().isPluginEnabled("Matchmaker") && event.getOldGameState() instanceof PlayingState && event.getNewGameState() instanceof IdleState) { // A game ended.
            final @Nullable quest.ender.MatchmakerBukkit.MatchmakerBukkit matchmakerBukkit = (quest.ender.MatchmakerBukkit.MatchmakerBukkit) this.plugin.getServer().getPluginManager().getPlugin("Matchmaker");
            Objects.requireNonNull(matchmakerBukkit);
            // A typing error might be raised here, but it's fine because we already checked to see if it's enabled.
            new BukkitRunnable() {
                @Override
                public void run() {
                    for (Player player : CountdownListener.this.plugin.getServer().getOnlinePlayers()) {
                        final @NotNull CompletableFuture<String> playerSendFuture = matchmakerBukkit.sendToGame(player, CountdownListener.this.plugin.getConfig().getString("matchmaker.after_game"));
                        playerSendFuture.thenApply(game -> {
                            if (game.equals("null"))
                                player.sendMessage(Component.text("Couldn't connect you back to a " + CountdownListener.this.plugin.getConfig().getString("matchmaker.after_game") + ".").color(TextColor.color(16733525)));

                            return game;
                        });
                    }
                }
            }
                    .runTaskLater(this.plugin, this.plugin.getConfig().getLong("matchmaker.rampdown") * 20);
        }
    }
}
