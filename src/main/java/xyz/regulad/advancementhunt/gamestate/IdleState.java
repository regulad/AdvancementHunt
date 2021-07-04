package xyz.regulad.advancementhunt.gamestate;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import xyz.regulad.advancementhunt.AdvancementHunt;

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
            player.teleport(startingLocation);
            player.setInvulnerable(true);
            player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
            player.setFoodLevel(20);
            player.setSaturation(5);
            player.setExhaustion(0);
            player.setGameMode(GameMode.ADVENTURE);
        }
    }

    @Override
    public void end() {
        for (Player player : this.plugin.getServer().getOnlinePlayers()) {
            player.setInvulnerable(false);
            player.setGameMode(GameMode.SURVIVAL);
        }
    }
}
