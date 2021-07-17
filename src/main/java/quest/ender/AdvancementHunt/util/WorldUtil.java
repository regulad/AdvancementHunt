package quest.ender.AdvancementHunt.util;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiverseNetherPortals.MultiverseNetherPortals;
import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.WorldGroup;
import com.onarandombox.multiverseinventories.profile.WorldGroupManager;
import com.onarandombox.multiverseinventories.share.Sharables;
import org.bukkit.PortalType;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.WorldType;

public class WorldUtil {
    private final Server server;

    public WorldUtil(Server server) {
        this.server = server;
    }

    private MultiverseCore getMultiverseCore() {
        return (MultiverseCore) this.server.getPluginManager().getPlugin("Multiverse-Core");
    }

    private MultiverseInventories getMultiverseInventories() {
        return (MultiverseInventories) this.server.getPluginManager().getPlugin("Multiverse-Inventories");
    }

    private MultiverseNetherPortals getMultiverseNetherPortals() {
        return (MultiverseNetherPortals) this.server.getPluginManager().getPlugin("Multiverse-NetherPortals");
    }

    private MVWorldManager getMVWorldManager() {
        return this.getMultiverseCore().getMVWorldManager();
    }

    private WorldGroupManager getWorldGroupManager() {
        return this.getMultiverseInventories().getGroupManager();
    }

    public MultiverseWorld[] createWorlds(String worldName, String worldSeed) {
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

        return new MultiverseWorld[]{this.getMVWorldManager().getMVWorld(worldName), this.getMVWorldManager().getMVWorld(worldName + "_nether"), this.getMVWorldManager().getMVWorld(worldName + "_the_end")};
    }

    public void deleteWorlds(String worldName) {
        this.getWorldGroupManager().removeGroup(this.getWorldGroupManager().getGroup(worldName));

        this.getMultiverseNetherPortals().removeWorldLink(worldName, worldName + "_nether", PortalType.NETHER);
        this.getMultiverseNetherPortals().removeWorldLink(worldName + "_nether", worldName, PortalType.NETHER);

        this.getMultiverseNetherPortals().removeWorldLink(worldName, worldName + "_the_end", PortalType.ENDER);
        this.getMultiverseNetherPortals().removeWorldLink(worldName + "_the_end", worldName, PortalType.ENDER);

        this.getMVWorldManager().deleteWorld(worldName, true, true);
        this.getMVWorldManager().deleteWorld(worldName + "_nether", true, true);
        this.getMVWorldManager().deleteWorld(worldName + "_the_end", true, true);
    }
}
