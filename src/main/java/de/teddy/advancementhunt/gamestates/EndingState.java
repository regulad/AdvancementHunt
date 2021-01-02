package de.teddy.advancementhunt.gamestates;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import de.teddy.advancementhunt.AdvancementHunt;
import de.teddy.advancementhunt.timer.EndingTimer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;

public class EndingState extends GameState {

    EndingTimer endingTimer = new EndingTimer();

    @Override
    public void start() {
        endingTimer.start();
        Bukkit.getScheduler().scheduleSyncDelayedTask(AdvancementHunt.getInstance(), new Runnable() {
            @Override
            public void run() {
                for(Player player : Bukkit.getOnlinePlayers()) {
                    AdvancementHunt.getInstance().getUtils().getLocationUtil().teleport(player, "LobbySpawn");
                    player.getInventory().clear();
                    player.setHealth(20);
                    player.setFoodLevel(20);
                    AdvancementHunt.getInstance().getActionbarManager().setRemainingTime(null);
                    AdvancementHunt.getInstance().getActionbarManager().stopTimeRemaining();
                }
            }
        }, 1L);
    }

    @Override
    public void stop() {
        endingTimer.cancel();
        for(Player player : Bukkit.getOnlinePlayers()) {

            // First Save Player Data to db if use db is true
            if(AdvancementHunt.getInstance().getConfigManager().getConfig().getBoolean("Game.MySQL.Use_db")) {
                try {
                    String sql = "INSERT INTO player_stat VALUES(?,?,?,?,?,?)";
                    PreparedStatement preparedStatement = AdvancementHunt.getInstance().getMysql().getConnection().prepareStatement(sql);
                    preparedStatement.setString(1, player.getUniqueId().toString());
                    preparedStatement.setString(2, player.getName());
                    preparedStatement.setInt(3, AdvancementHunt.getInstance().getUtils().getFightUtil().getKills().get(player));
                    preparedStatement.setInt(4, AdvancementHunt.getInstance().getUtils().getFightUtil().getDeaths().get(player));
                    preparedStatement.setInt(5, AdvancementHunt.getInstance().getUtils().getFightUtil().getLosses().get(player));
                    preparedStatement.setInt(6, AdvancementHunt.getInstance().getUtils().getFightUtil().getWins().get(player));
                    preparedStatement.execute();
                } catch (Exception e) {
                    AdvancementHunt.getInstance().getLogger().info("Unable to Save Player stat to db");
                }
            }

            // Instad of kicking players i am going to sent them to hub server
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Connect");
            out.writeUTF(AdvancementHunt.getInstance().getConfigManager().getMessage("Game.Extra.Hub_Server"));
            player.sendPluginMessage(AdvancementHunt.getInstance(),"BungeeCord",out.toByteArray());

            // player.kickPlayer(AdvancementHunt.getInstance().getConfigManager().getMessage("Game.Messages.GameIsOver"));
            player.getActivePotionEffects().clear();
            player.getInventory().clear();
        }

        /**
         * Old shitty code for deleting world
         */
        //Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mv delete " + AdvancementHunt.getInstance().getWorldName());
        //Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mv confirm");

        // New Method
        AdvancementHunt.getInstance().getMultiverseCore().deleteWorld(AdvancementHunt.getInstance().getWorldName());
        AdvancementHunt.getInstance().getMultiverseCore().deleteWorld(AdvancementHunt.getInstance().getWorldName() + "_the_end");
        AdvancementHunt.getInstance().getMultiverseCore().deleteWorld(AdvancementHunt.getInstance().getWorldName() + "_nether");

        AdvancementHunt.getInstance().getTeamManager().clear();
        AdvancementHunt.getInstance().getUtils().getFightUtil().clear();
    }

}
