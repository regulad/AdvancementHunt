package quest.ender.AdvancementHunt.game.state;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
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
        final @NotNull World primaryWorld = this.plugin.getServer().getWorlds().get(0);

        for (Player player : this.plugin.getServer().getOnlinePlayers()) {
            PlayerUtil.resetAllAdvancementProgresses(player);
            player.spigot().respawn();
            player.teleport(primaryWorld.getSpawnLocation());
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
