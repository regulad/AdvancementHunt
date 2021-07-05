package xyz.regulad.AdvancementHunt.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import xyz.regulad.AdvancementHunt.AdvancementHunt;
import xyz.regulad.AdvancementHunt.database.PlayerStats;
import xyz.regulad.AdvancementHunt.exceptions.GameNotStartedException;
import xyz.regulad.AdvancementHunt.game.GameEndReason;
import xyz.regulad.AdvancementHunt.game.states.PlayingState;

public class PlayerDeathListener implements Listener {

    private final AdvancementHunt plugin;

    public PlayerDeathListener(AdvancementHunt plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (this.plugin.getCurrentGameState() instanceof PlayingState) {
            PlayingState playingState = (PlayingState) this.plugin.getCurrentGameState();

            if (event.getEntity().getKiller() != null) {
                PlayerStats killerStats = this.plugin.getPlayerStats(event.getEntity().getKiller());
                killerStats.setKills(killerStats.getKills() + 1);
            }
            PlayerStats deadStats = this.plugin.getPlayerStats(event.getEntity());
            deadStats.setDeaths(deadStats.getDeaths() + 1);

            if (playingState.fleeingPlayer.equals(event.getEntity())) {
                try {
                    this.plugin.endGame(GameEndReason.HUNTER_WIN);
                } catch (GameNotStartedException ignored) {
                }
            } // Maybe some logic to change the game time if a hunter dies
        }
    }
}
