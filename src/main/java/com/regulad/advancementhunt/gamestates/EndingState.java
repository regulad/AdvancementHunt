package com.regulad.advancementhunt.gamestates;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.regulad.advancementhunt.AdvancementHunt;

import com.regulad.advancementhunt.timer.EndingTimer;
import com.regulad.advancementhunt.util.FightUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;

public class EndingState extends GameState {

    private final EndingTimer endingTimer;
    private final AdvancementHunt plugin;

    public EndingState(AdvancementHunt plugin) {
        this.plugin = plugin;
        this.endingTimer = new EndingTimer(plugin);
    }

    @Override
    public void start() {
        endingTimer.start();

        plugin.getActionbarManager().setRemainingTime(null);

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            for(Player player : Bukkit.getOnlinePlayers()) {
                plugin.getUtils().getLocationUtil().teleport(player, "LobbySpawn");
                player.getInventory().clear();
                player.setHealth(20);
                player.setFoodLevel(20);
            }
        }, 1L);

        plugin.getActionbarManager().stopTimeRemaining();
    }

    @Override
    public void stop() {
        endingTimer.cancel();

        FightUtil fightUtil = plugin.getUtils().getFightUtil();
        String hubServer = plugin.getConfigManager().getMessage("Game.Extra.Hub_Server");

        for(Player player : Bukkit.getOnlinePlayers()) {

            // First Save Player Data to db if use db is true
            if(plugin.getConfigManager().getConfig().getBoolean("Game.MySQL.Use_db")) {
                try {
                    String sql = "INSERT INTO player_stat VALUES(?,?,?,?,?,?)";
                    PreparedStatement preparedStatement = plugin.getMysql().getConnection().prepareStatement(sql);
                    preparedStatement.setString(1, player.getUniqueId().toString());
                    preparedStatement.setString(2, player.getName());
                    preparedStatement.setInt(3, fightUtil.getKills().get(player));
                    preparedStatement.setInt(4, fightUtil.getDeaths().get(player));
                    preparedStatement.setInt(5, fightUtil.getLosses().get(player));
                    preparedStatement.setInt(6, fightUtil.getWins().get(player));
                    preparedStatement.execute();
                } catch (Exception e) {
                    plugin.getLogger().info("Unable to Save Player stat to db");
                }
            }

            // Instad of kicking players i am going to sent them to hub server
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Connect");
            out.writeUTF(hubServer);
            player.sendPluginMessage(plugin,"BungeeCord",out.toByteArray());
            player.getActivePotionEffects().clear();
            player.getInventory().clear();
        }

        String worldName = plugin.getWorldName();

        plugin.getMultiverseCore().deleteWorld(worldName);
        plugin.getMultiverseCore().deleteWorld(worldName + "_the_end");
        plugin.getMultiverseCore().deleteWorld(worldName + "_nether");

        plugin.getTeamManager().clear();
        plugin.getUtils().getFightUtil().clear();
    }
}
