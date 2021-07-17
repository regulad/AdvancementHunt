package quest.ender.AdvancementHunt.game.states;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import quest.ender.AdvancementHunt.AdvancementHunt;
import quest.ender.AdvancementHunt.game.GameEndReason;
import quest.ender.AdvancementHunt.util.PlayerUtil;

public class IdleState implements GameState {
    private final AdvancementHunt plugin;
    private final GameEndReason reason;

    public IdleState(AdvancementHunt plugin, GameEndReason reason) {
        this.plugin = plugin;
        this.reason = reason;
    }

    @Override
    public void start() {
        Location startingLocation = this.plugin.getServer().getWorlds().get(0).getSpawnLocation();
        for (Player player : this.plugin.getServer().getOnlinePlayers()) {
            PlayerUtil.resetAllAdvancementProgresses(player);
            player.teleport(startingLocation);
        }
    }

    @Override
    public void end() {
        PlayerUtil.resetAllAdvancementProgressesForAllPlayers();
    }

    public GameEndReason getGameEndReason() {
        return this.reason;
    }
}
