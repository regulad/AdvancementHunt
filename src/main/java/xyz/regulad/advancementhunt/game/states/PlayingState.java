package xyz.regulad.advancementhunt.game.states;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import xyz.regulad.advancementhunt.AdvancementHunt;
import xyz.regulad.advancementhunt.game.tasks.CompassLocationRunnable;
import xyz.regulad.advancementhunt.game.tasks.GameEndingTask;
import xyz.regulad.advancementhunt.messages.persistent.PersistentMessage;
import xyz.regulad.advancementhunt.messages.persistent.PersistentMessageRunnable;
import xyz.regulad.advancementhunt.util.PlayerUtil;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Random;

public class PlayingState implements GameState {
    private final AdvancementHunt plugin;
    private final GameEndingTask gameEndingTask;
    private final PersistentMessageRunnable huntedRunnable;
    private final PersistentMessageRunnable hunterRunnable;
    private final CompassLocationRunnable compassLocationRunnable;

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

    public PlayingState(AdvancementHunt plugin, Player fleeingPlayer, ArrayList<Player> huntingPlayers, Advancement goalAdvancement, Instant endTime, String worldSeed, double worldSize) {
        this.plugin = plugin;
        this.gameEndingTask = new GameEndingTask(this.plugin);
        this.huntedRunnable = new PersistentMessageRunnable(this.plugin, this, PersistentMessage.HUNTED);
        this.hunterRunnable = new PersistentMessageRunnable(this.plugin, this, PersistentMessage.HUNTER);
        this.compassLocationRunnable = new CompassLocationRunnable(this.plugin, this);

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
        MVWorldManager mvWorldManager = ((MultiverseCore) this.plugin.getServer().getPluginManager().getPlugin("Multiverse-Core")).getMVWorldManager();

        mvWorldManager.addWorld(this.worldName, World.Environment.NORMAL, this.worldSeed, WorldType.NORMAL, null, null);
        World overworld = this.plugin.getServer().getWorld(this.worldName);
        overworld.getWorldBorder().setSize(this.worldSize);

        if (plugin.getConfig().getBoolean("game.world.nether")) {
            mvWorldManager.addWorld(this.worldName + "_nether", World.Environment.NETHER, this.worldSeed, WorldType.NORMAL, null, null);
            this.plugin.getServer().getWorld(this.worldName + "_nether").getWorldBorder().setSize(this.worldSize);
        }

        if (plugin.getConfig().getBoolean("game.world.end")) {
            mvWorldManager.addWorld(this.worldName + "_the_end", World.Environment.THE_END, this.worldSeed, WorldType.NORMAL, null, null);
            this.plugin.getServer().getWorld(this.worldName + "_the_end").getWorldBorder().setSize(this.worldSize);
        }

        boolean compassIsEnabled = this.plugin.getConfig().getBoolean("game.compass");

        Location spawningLocation = overworld.getSpawnLocation();
        for (Player player : this.plugin.getServer().getOnlinePlayers()) {
            PlayerUtil.resetPlayer(player);
            player.teleport(spawningLocation);
            if (compassIsEnabled) {
                player.getInventory().addItem(new ItemStack(Material.COMPASS));
            }
        }

        if (compassIsEnabled) {
            this.compassLocationRunnable.runTaskTimer(this.plugin, 20, 20);
        }

        long secondsToTask = (this.endTime.toEpochMilli() - Instant.now().toEpochMilli()) / 1000;
        this.gameEndingTask.runTaskLater(this.plugin, secondsToTask * 20);

        this.hunterRunnable.runTaskTimer(this.plugin, 20, 20);
        this.huntedRunnable.runTaskTimer(this.plugin, 20, 20);
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

        if (this.plugin.getConfig().getBoolean("game.compass") && !this.compassLocationRunnable.isCancelled()) {
            this.compassLocationRunnable.cancel();
        }

        PlayerUtil.resetAllPlayers();

        MVWorldManager mvWorldManager = ((MultiverseCore) this.plugin.getServer().getPluginManager().getPlugin("Multiverse-Core")).getMVWorldManager();

        mvWorldManager.deleteWorld(this.worldName, true, true);

        if (plugin.getConfig().getBoolean("game.world.nether")) {
            mvWorldManager.deleteWorld(this.worldName + "_nether", true, true);
        }

        if (plugin.getConfig().getBoolean("game.world.end")) {
            mvWorldManager.deleteWorld(this.worldName + "_the_end", true, true);
        }
    }
}
