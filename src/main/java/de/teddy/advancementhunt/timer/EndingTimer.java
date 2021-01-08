package de.teddy.advancementhunt.timer;

import de.teddy.advancementhunt.AdvancementHunt;

import de.teddy.advancementhunt.message.MessageType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class EndingTimer extends Timer {

    private int seconds = 5;

    private final AdvancementHunt plugin;

    public EndingTimer(AdvancementHunt plugin) {
        this.plugin = plugin;
    }

    @Override
    public void start() {

        counter = Bukkit.getScheduler().scheduleSyncRepeatingTask(AdvancementHunt.getInstance(), () -> {
            if (seconds == 0) {
                cancel();
                plugin.getGameStateManager().resetGameStates();
            } else {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    plugin.getMessageManager().sendMessageReplace(player, MessageType.STOP_GAME, "%seconds%", seconds + "");
                }
            }
            seconds--;
        }, 0, 20);
    }

    @Override
    public void cancel() {
        this.seconds = 5;
        System.out.println("EndingSeconds: " + this.seconds);
        Bukkit.getScheduler().cancelTask(counter);
    }
}
