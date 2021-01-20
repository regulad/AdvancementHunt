package com.regulad.advancementhunt.actionbar;

import com.regulad.advancementhunt.AdvancementHunt;
import com.regulad.advancementhunt.gamestates.GameState;
import com.regulad.advancementhunt.message.MessageType;
import com.regulad.advancementhunt.teams.Team;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class ActionbarManager {

    private BukkitTask task;
    private String remainingTime;

    private final AdvancementHunt plugin;

    public ActionbarManager(AdvancementHunt plugin) {
        this.plugin = plugin;
    }

    public void startTimeRemaining(int minutes) {
        Player hunter1 = plugin.getTeamManager().getPlayers(Team.HUNTER).get(0);
        Player hunter2 = plugin.getTeamManager().getPlayers(Team.HUNTER).get(1);

        task = new BukkitRunnable() {
            int count = minutes * 60;

            @Override
            public void run() {
                remainingTime = format(count, true);

                for (Player player : Bukkit.getOnlinePlayers()) {
                    // I think this is what you mean about placeholder....
                    plugin.getMessageManager().sendMessage(player, MessageType.TIME_LEFT);
                    //player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(AdvancementHunt.getInstance().getConfigManager().getMessageWithReplace(
                    //  "Game.Messages.TimeLeft", "%minutes%", PlaceholderAPI.setPlaceholders(player, "%advancement_time_remaining%"))));
                }

                if (plugin.isCompass()) {
                    plugin.setCompassLoc(plugin.getTeamManager().getPlayers(Team.PLAYER).get(0).getLocation());

                    hunter1.setCompassTarget(plugin.getCompassLoc());
                    hunter2.setCompassTarget(plugin.getCompassLoc());
                }

                if (count == 0) {
                    plugin.getGameStateManager().setGameState(GameState.ENDING_STATE);
                    cancel();
                }

                count--;
            }

        }.runTaskTimerAsynchronously(plugin, 0, 20);
    }

    public void sendActionbar(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
    }

    public void stopTimeRemaining() {
        task.cancel();
    }

    private String format(int time, boolean useHours) {
        int seconds = 0, minutes = 0, hours = 0;

        while (time != 0) {
            time--;
            seconds++;
            if (seconds >= 60) {
                seconds = 0;
                minutes++;
            }
            if (minutes >= 60) {
                minutes = 0;
                hours++;
            }
        }
        StringBuilder builder = new StringBuilder();

        if (useHours) {
            builder.append(check(hours)).append(":");
        }

        builder.append(check(minutes))
                .append(":")
                .append(check(seconds));

        return builder.toString();
    }

    private String check(int input) {
        return (input >= 10) ? ("" + input) : ("0" + input);
    }

    public String getRemainingTime() {
        return remainingTime;
    }

    public void setRemainingTime(String remainingTime) {
        this.remainingTime = remainingTime;
    }

    public BukkitTask getTask() {
        return task;
    }
}
