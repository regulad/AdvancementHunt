package xyz.regulad.AdvancementHunt.game.states;

import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import xyz.regulad.AdvancementHunt.AdvancementHunt;
import xyz.regulad.AdvancementHunt.game.tasks.CompassLocationRunnable;
import xyz.regulad.AdvancementHunt.game.tasks.GameEndingTask;
import xyz.regulad.AdvancementHunt.messages.persistent.PersistentMessage;
import xyz.regulad.AdvancementHunt.messages.persistent.PersistentMessageRunnable;
import xyz.regulad.AdvancementHunt.util.PlayerUtil;
import xyz.regulad.AdvancementHunt.util.WorldUtil;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Random;

public class PlayingState implements GameState {
    public final Player fleeingPlayer;
    public final ArrayList<Player> huntingPlayers;
    public final Advancement goalAdvancement;
    public final Instant endTime;
    public final String worldSeed;
    public final double worldSize;
    public final String worldName = (new Random()).ints('a', 'z' + 1)
            .limit(10)
            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
            .toString(); // This just gives us a random string.
    public final ArrayList<Player> leftPlayers = new ArrayList<>();
    private final AdvancementHunt plugin;
    private final GameEndingTask gameEndingTask;
    private final PersistentMessageRunnable huntedRunnable;
    private final PersistentMessageRunnable hunterRunnable;
    private final CompassLocationRunnable compassLocationRunnable;
    private final WorldUtil worldUtil;

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

    @Override
    public void start() {
        // Create worlds
        final MultiverseWorld[] worlds = this.worldUtil.createWorlds(this.worldName, this.worldSeed);

        for (MultiverseWorld multiverseWorld : worlds) {
            multiverseWorld.getCBWorld().getWorldBorder().setSize(this.worldSize);
        }

        Location spawningLocation = worlds[0].getSpawnLocation();
        for (Player player : this.plugin.getServer().getOnlinePlayers()) {
            PlayerUtil.resetAllAdvancementProgresses(player);
            player.teleport(spawningLocation);
            if (this.plugin.getConfig().getBoolean("game.compass")) {
                player.getInventory().addItem(new ItemStack(Material.COMPASS));
            }
        }

        long secondsToTask = (this.endTime.toEpochMilli() - Instant.now().toEpochMilli()) / 1000;
        this.gameEndingTask.runTaskLater(this.plugin, secondsToTask * 20);

        this.hunterRunnable.runTaskTimer(this.plugin, 20, 20);
        this.huntedRunnable.runTaskTimer(this.plugin, 20, 20);
        this.compassLocationRunnable.runTaskTimer(this.plugin, 20, 10);
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
