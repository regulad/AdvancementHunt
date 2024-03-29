package quest.ender.AdvancementHunt.messages.persistent;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import quest.ender.AdvancementHunt.AdvancementHunt;
import quest.ender.AdvancementHunt.game.state.PlayingState;
import quest.ender.AdvancementHunt.messages.Message;

public class PersistentMessageRunnable extends BukkitRunnable {
    private final AdvancementHunt plugin;
    private final PlayingState playingState;
    private final PersistentMessage persistentMessage;

    public PersistentMessageRunnable(AdvancementHunt plugin, PlayingState playingState, PersistentMessage persistentMessage) {
        this.plugin = plugin;
        this.playingState = playingState;
        this.persistentMessage = persistentMessage;
    }

    @Override
    public void run() {
        switch (this.persistentMessage) {
            case HUNTED:
                this.plugin.getMessageManager().dispatchMessage(this.playingState.fleeingPlayer, Message.HUNTED_PERSISTENT);
                break;
            case HUNTER:
                for (Player player : this.playingState.huntingPlayers) {
                    this.plugin.getMessageManager().dispatchMessage(player, Message.HUNTER_PERSISTENT);
                }
                break;
        }
    }
}
