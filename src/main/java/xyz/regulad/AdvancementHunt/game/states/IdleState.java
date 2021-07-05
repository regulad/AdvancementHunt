package xyz.regulad.AdvancementHunt.game.states;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import xyz.regulad.AdvancementHunt.AdvancementHunt;
import xyz.regulad.AdvancementHunt.game.GameEndReason;
import xyz.regulad.AdvancementHunt.util.PlayerUtil;

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
            PlayerUtil.resetPlayer(player);
            player.teleport(startingLocation);
        }
    }

    @Override
    public void end() {
        PlayerUtil.resetAllPlayers();
    }

    public GameEndReason getGameEndReason() {
        return this.reason;
    }
}
