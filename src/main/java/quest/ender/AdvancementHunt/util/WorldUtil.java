package quest.ender.AdvancementHunt.util;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiverseNetherPortals.MultiverseNetherPortals;
import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.WorldGroup;
import com.onarandombox.multiverseinventories.profile.WorldGroupManager;
import com.onarandombox.multiverseinventories.share.Sharables;
import org.bukkit.*;
import org.jetbrains.annotations.NotNull;

public class WorldUtil {
    private final Server server;

    public WorldUtil(Server server) {
        this.server = server;
    }

    public MultiverseCore getMultiverseCore() {
        return (MultiverseCore) this.server.getPluginManager().getPlugin("Multiverse-Core");
    }

    public MultiverseInventories getMultiverseInventories() {
        return (MultiverseInventories) this.server.getPluginManager().getPlugin("Multiverse-Inventories");
    }

    public MultiverseNetherPortals getMultiverseNetherPortals() {
        return (MultiverseNetherPortals) this.server.getPluginManager().getPlugin("Multiverse-NetherPortals");
    }

    public MVWorldManager getMVWorldManager() {
        return this.getMultiverseCore().getMVWorldManager();
    }

    public WorldGroupManager getWorldGroupManager() {
        return this.getMultiverseInventories().getGroupManager();
    }

    public MultiverseWorld[] createWorlds(final @NotNull String worldName, final @NotNull String worldSeed) {
        this.getMVWorldManager().addWorld(worldName, World.Environment.NORMAL, worldSeed, WorldType.NORMAL, true, null);
        this.getMVWorldManager().addWorld(worldName + "_nether", World.Environment.NETHER, worldSeed, WorldType.NORMAL, true, null);
        this.getMVWorldManager().addWorld(worldName + "_the_end", World.Environment.THE_END, worldSeed, WorldType.NORMAL, true, null);

        this.getMultiverseNetherPortals().addWorldLink(worldName, worldName + "_nether", PortalType.NETHER);
        this.getMultiverseNetherPortals().addWorldLink(worldName + "_nether", worldName, PortalType.NETHER);

        this.getMultiverseNetherPortals().addWorldLink(worldName, worldName + "_the_end", PortalType.ENDER);
        this.getMultiverseNetherPortals().addWorldLink(worldName + "_the_end", worldName, PortalType.ENDER);

        final WorldGroup newGroup = this.getWorldGroupManager().newEmptyGroup(worldName);

        newGroup.addWorld(worldName);
        newGroup.addWorld(worldName + "_nether");
        newGroup.addWorld(worldName + "_the_end");

        newGroup.getShares().addAll(Sharables.allOf());

        this.getWorldGroupManager().updateGroup(newGroup);

        final @NotNull MultiverseWorld[] worlds = new MultiverseWorld[]{this.getMVWorldManager().getMVWorld(worldName), this.getMVWorldManager().getMVWorld(worldName + "_nether"), this.getMVWorldManager().getMVWorld(worldName + "_the_end")};

        for (MultiverseWorld world : worlds) {
            world.setGameMode(GameMode.SURVIVAL); // Weird in the case that the main world has a different GameMode from survival.
            world.setDifficulty(Difficulty.HARD); // Same as the above case.
            // If for some reason someone installs this plugin on a regular survival server, this won't completely break it.
        }

        return worlds;
    }

    public void deleteWorlds(final @NotNull String worldName) {
        this.getMVWorldManager().removePlayersFromWorld(worldName);
        this.getMVWorldManager().removePlayersFromWorld(worldName + "_nether");
        this.getMVWorldManager().removePlayersFromWorld(worldName + "_the_end");

        this.getMultiverseNetherPortals().removeWorldLink(worldName, worldName + "_nether", PortalType.NETHER);
        this.getMultiverseNetherPortals().removeWorldLink(worldName + "_nether", worldName, PortalType.NETHER);

        this.getMultiverseNetherPortals().removeWorldLink(worldName, worldName + "_the_end", PortalType.ENDER);
        this.getMultiverseNetherPortals().removeWorldLink(worldName + "_the_end", worldName, PortalType.ENDER);

        this.getWorldGroupManager().removeGroup(this.getWorldGroupManager().getGroup(worldName));

        this.getMVWorldManager().deleteWorld(worldName, true, true);
        this.getMVWorldManager().deleteWorld(worldName + "_nether", true, true);
        this.getMVWorldManager().deleteWorld(worldName + "_the_end", true, true);
    }
}
