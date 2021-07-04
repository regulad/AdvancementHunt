package xyz.regulad.advancementhunt.game.states;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import xyz.regulad.advancementhunt.AdvancementHunt;
import xyz.regulad.advancementhunt.game.GameEndReason;
import xyz.regulad.advancementhunt.util.PlayerUtil;

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
            PlayerUtil.resetPlayer(player, GameMode.ADVENTURE, true);
            player.teleport(startingLocation);
        }
    }

    @Override
    public void end() {
        PlayerUtil.resetAllPlayers(GameMode.SURVIVAL, false);
    }

    public GameEndReason getGameEndReason() {
        return this.reason;
    }
}
