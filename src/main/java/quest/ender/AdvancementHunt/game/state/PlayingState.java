package quest.ender.AdvancementHunt.game.state;

import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Material;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import quest.ender.AdvancementHunt.AdvancementHunt;
import quest.ender.AdvancementHunt.game.runnable.CompassLocationRunnable;
import quest.ender.AdvancementHunt.game.runnable.GameEndingTask;
import quest.ender.AdvancementHunt.messages.Message;
import quest.ender.AdvancementHunt.messages.persistent.PersistentMessage;
import quest.ender.AdvancementHunt.messages.persistent.PersistentMessageRunnable;
import quest.ender.AdvancementHunt.util.PlayerUtil;
import quest.ender.AdvancementHunt.util.WorldUtil;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class PlayingState implements GameState {
    public final Player fleeingPlayer;
    public final ArrayList<Player> huntingPlayers;
    public final Advancement goalAdvancement;
    public final Instant endTime;
    public final String worldSeed;
    public final double worldSize;

    public final WorldUtil worldUtil;

    public final String worldName = new Random().ints('a', 'z' + 1)
            .limit(10)
            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
            .toString(); // This just gives us a random string.
    public final ArrayList<Player> leftPlayers = new ArrayList<>();

    private final AdvancementHunt plugin;
    private final GameEndingTask gameEndingTask;
    private final PersistentMessageRunnable huntedRunnable;
    private final PersistentMessageRunnable hunterRunnable;
    private final CompassLocationRunnable compassLocationRunnable;

    private boolean canMove = true;
    private boolean huntedCanMove = true;

    public PlayingState(AdvancementHunt plugin, Player fleeingPlayer, ArrayList<Player> huntingPlayers, Advancement goalAdvancement, Instant endTime, String worldSeed, double worldSize) {
        this.plugin = plugin;
        this.gameEndingTask = new GameEndingTask(this.plugin);
        this.huntedRunnable = new PersistentMessageRunnable(this.plugin, this, PersistentMessage.HUNTED);
        this.hunterRunnable = new PersistentMessageRunnable(this.plugin, this, PersistentMessage.HUNTER);
        this.compassLocationRunnable = new CompassLocationRunnable(this.plugin, this);
        this.worldUtil = new WorldUtil(this.plugin.getServer());

        this.fleeingPlayer = fleeingPlayer;
        this.huntingPlayers = huntingPlayers;
        this.goalAdvancement = goalAdvancement;
        this.endTime = endTime;
        this.worldSeed = worldSeed;
        this.worldSize = worldSize;
    }

    public boolean canMove() {
        return this.canMove;
    }

    public boolean huntedCanMove() {
        return this.huntedCanMove;
    }

    @Override
    public void start() {
        // Create worlds
        final MultiverseWorld[] worlds = this.worldUtil.createWorlds(this.worldName, this.worldSeed);
        // apparently, the hanging here is caused by the selection of a world spawn, but idk how to change that

        for (MultiverseWorld multiverseWorld : worlds) {
            multiverseWorld.getCBWorld().getWorldBorder().setSize(this.worldSize);
        }

        final long secondsToTask = (this.endTime.toEpochMilli() - Instant.now().toEpochMilli()) / 1000;
        this.gameEndingTask.runTaskLater(this.plugin, secondsToTask * 20);

        this.hunterRunnable.runTaskTimer(this.plugin, 20, 20);
        this.huntedRunnable.runTaskTimer(this.plugin, 20, 20);
        this.compassLocationRunnable.runTaskTimer(this.plugin, 20, 10);

        final @NotNull Set<CompletableFuture<Boolean>> teleportationFutures = this.plugin.getServer().getOnlinePlayers().stream().map(player ->
                player.teleportAsync(worlds[0].getSpawnLocation()).thenApply(
                        (result) -> {
                            if (!result) {
                                throw new RuntimeException("Teleportation was not successful, terminating chain.");
                            } else {
                                return true;
                            }
                        }
                ).thenCompose( // this will not run exceptionally
                        // doing this async allows us to wait for the world to load before we begin the game
                        (result) -> CompletableFuture.supplyAsync(() -> {
                            // we can only run these once the player has arrived in the new world
                            PlayerUtil.resetAllAdvancementProgresses(player);
                            player.setInvulnerable(true);
                            return result;
                        }, this.plugin.getServer().getScheduler().getMainThreadExecutor(this.plugin))
                        // we need to nest this future so that this can be executed on the main thread after the player is teleported
                ).thenCompose( // this will also not run exceptionally
                        (result) -> {
                            // now we need to wait for the chunks to actually be sent to the client before we let people run amok
                            final @NotNull CompletableFuture<Boolean> chunkSentFuture = new CompletableFuture<>();
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    if (player.isChunkSent(worlds[0].getSpawnLocation().getChunk())) {
                                        // i could theoretically also check that all of the nearby chunks are loaded, but that's a bit overkill and this works good enough
                                        chunkSentFuture.complete(true);
                                        this.cancel();
                                    }
                                }
                            }.runTaskTimer(this.plugin, 0, 1);
                            return chunkSentFuture;
                        }
                ).thenCompose(
                        (result) -> {
                            // finally, we wait 3 seconds to ensure that the client has rendered the chunks that we sent them
                            final @NotNull CompletableFuture<Boolean> chunkRenderedFuture = new CompletableFuture<>();
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    chunkRenderedFuture.complete(true);
                                    this.cancel();
                                }
                            }.runTaskLater(this.plugin, 3L * 20L);
                            return chunkRenderedFuture;
                        }
                )
        ).collect(Collectors.toUnmodifiableSet());

        this.canMove = false;
        this.huntedCanMove = false;

        final int[] countdownSingleton = {this.plugin.getConfig().getInt("game.countdown")};
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!PlayingState.this.huntedCanMove)
                    PlayingState.this.huntedCanMove = true;

                if (!teleportationFutures.stream().allMatch(CompletableFuture::isDone)) {
                    // wait for the world to load here:
                    PlayingState.this.plugin.getServer().showTitle(Title.title(Component.text("Loading world..."), Component.text("")));
                } else if (countdownSingleton[0] != 0) {
                    // Counting down
                    PlayingState.this.plugin.getServer().showTitle(Title.title(Component.text(String.valueOf(countdownSingleton[0])), Component.text("")));
                    countdownSingleton[0]--;
                } else {
                    // Start the game
                    for (Player player : PlayingState.this.plugin.getServer().getOnlinePlayers()) { // Shouldn't apply to all players, what if there are spectators.
                        player.setInvulnerable(false);
                        if (PlayingState.this.plugin.getConfig().getBoolean("game.compass") && PlayingState.this.huntingPlayers.contains(player))
                            player.getInventory().addItem(new ItemStack(Material.COMPASS));
                    }

                    PlayingState.this.plugin.getMessageManager().dispatchMessage(PlayingState.this.fleeingPlayer, Message.HUNTED_START);

                    for (Player player : PlayingState.this.huntingPlayers) {
                        PlayingState.this.plugin.getMessageManager().dispatchMessage(player, Message.HUNTER_START);
                    }

                    PlayingState.this.canMove = true;

                    this.cancel();
                }
            }
        }.runTaskTimer(this.plugin, this.plugin.getConfig().getLong("game.before") * 20L, 20L);
    }

    @Override
    public void end() {
        if (!this.gameEndingTask.isToExitGracefully()) {
            this.gameEndingTask.exitGracefully();
        }

        if (!this.hunterRunnable.isCancelled()) {
            this.hunterRunnable.cancel();
        }

        if (!this.huntedRunnable.isCancelled()) {
            this.huntedRunnable.cancel();
        }

        if (!this.compassLocationRunnable.isCancelled()) {
            this.compassLocationRunnable.cancel();
        }

        PlayerUtil.resetAllAdvancementProgressesForAllPlayers();

        this.worldUtil.deleteWorlds(this.worldName);
    }
}
