package xyz.regulad.advancementhunt.timer;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import xyz.regulad.advancementhunt.AdvancementHunt;
import xyz.regulad.advancementhunt.gamestates.GameState;
import xyz.regulad.advancementhunt.message.MessageType;

public class LobbyTimer extends Timer {

//	private int idleTask;

    private int seconds = 5;

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    //private boolean isIdling = false;
    private boolean isStarted = false;


    @Override
    public void start() {
        isStarted = true;

        counter = Bukkit.getScheduler().scheduleSyncRepeatingTask(AdvancementHunt.getInstance(), () -> {
            if (seconds == 0) {
                cancel();
                AdvancementHunt.getInstance().getGameStateManager().setGameState(GameState.INGAME_STATE);
            } else {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    AdvancementHunt.getInstance().getMessageManager().sendMessageReplace(player, MessageType.START_GAME, "%seconds%", seconds + "");
                }
            }
            seconds--;
        }, 0, 20);
    }

    @Override
    public void cancel() {
        this.isStarted = false;
        Bukkit.getScheduler().cancelTask(counter);
    }

    public boolean isStarted() {
        return isStarted;
    }

    public void setStarted(boolean started) {
        isStarted = started;
    }
}
