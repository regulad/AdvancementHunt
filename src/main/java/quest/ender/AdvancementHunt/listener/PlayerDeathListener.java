package quest.ender.AdvancementHunt.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import quest.ender.AdvancementHunt.AdvancementHunt;
import quest.ender.AdvancementHunt.database.stats.PlayerStats;
import quest.ender.AdvancementHunt.database.stats.StatsColumn;
import quest.ender.AdvancementHunt.exceptions.GameNotStartedException;
import quest.ender.AdvancementHunt.game.GameEndReason;
import quest.ender.AdvancementHunt.game.state.PlayingState;

public class PlayerDeathListener implements Listener {

    private final AdvancementHunt plugin;

    public PlayerDeathListener(AdvancementHunt plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (this.plugin.getCurrentGameState() instanceof PlayingState playingState) {
            if (event.getEntity().getKiller() != null) {
                PlayerStats killerStats = this.plugin.getPlayerStats(event.getEntity().getKiller());
                killerStats.updateColumn(StatsColumn.KILLS, killerStats.getColumn(StatsColumn.KILLS) + 1);
            }
            PlayerStats deadStats = this.plugin.getPlayerStats(event.getEntity());
            deadStats.updateColumn(StatsColumn.DEATHS, deadStats.getColumn(StatsColumn.DEATHS) + 1);

            if (playingState.fleeingPlayer.equals(event.getEntity())) {
                try {
                    this.plugin.endGame(GameEndReason.HUNTER_WIN);
                } catch (GameNotStartedException ignored) {
                }
            } // Maybe some logic to change the game time if a hunter dies
        }
    }
}
