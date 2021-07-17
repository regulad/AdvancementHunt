package quest.ender.AdvancementHunt.game.tasks;

import org.bukkit.scheduler.BukkitRunnable;
import quest.ender.AdvancementHunt.AdvancementHunt;
import quest.ender.AdvancementHunt.exceptions.GameNotStartedException;
import quest.ender.AdvancementHunt.game.GameEndReason;

public class GameEndingTask extends BukkitRunnable {
    private final AdvancementHunt plugin;
    private boolean toExitGracefully = false;

    public GameEndingTask(AdvancementHunt plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (this.toExitGracefully) {
            this.cancel();
            return; // Might not be required, safety first!
        }
        try {
            this.plugin.endGame(GameEndReason.TIME_UP);
        } catch (GameNotStartedException ignored) {
        }
        if (this.toExitGracefully) {
            this.cancel();
        }
    }

    /**
     * Allows the task to complete it's current iteration before exiting.
     */
    public void exitGracefully() {
        this.toExitGracefully = true;
    }

    public boolean isToExitGracefully() {
        return this.toExitGracefully;
    }
}
