package com.regulad.advancementhunt.listener;

import com.destroystokyo.paper.Title;
import com.regulad.advancementhunt.AdvancementHunt;
import com.regulad.advancementhunt.gamestates.GameState;
import com.regulad.advancementhunt.message.MessageType;
import com.regulad.advancementhunt.teams.Team;
import org.bukkit.*;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerDeathListener implements Listener {

    private final AdvancementHunt plugin;

    public PlayerDeathListener(AdvancementHunt plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if(plugin.getTeamManager().getPlayers(Team.PLAYER).contains(player)) {
            World w = Bukkit.getServer().getWorld(plugin.getWorldName());

            for(Player all : Bukkit.getOnlinePlayers()) {
                plugin.getMessageManager().sendMessage(all, MessageType.HUNTERWON);
            }

            plugin.getGameStateManager().setGameState(GameState.ENDING_STATE);

            plugin.getUtils().getFightUtil().addWin(plugin.getTeamManager().getPlayers(Team.HUNTER).get(0), 1);
            plugin.getUtils().getFightUtil().addWin(plugin.getTeamManager().getPlayers(Team.HUNTER).get(1), 1);
            plugin.getUtils().getFightUtil().addLoose(plugin.getTeamManager().getPlayers(Team.PLAYER).get(0), 1);
        } else if (plugin.getTeamManager().getPlayers(Team.HUNTER).contains(player)) {
            World w = Bukkit.getServer().getWorld(plugin.getWorldName());

            WorldBorder wb = Bukkit.getWorld(plugin.getWorldName()).getWorldBorder();

            respawnWithRunnable(player, new Location(w, w.getSpawnLocation().getX(), w.getSpawnLocation().getY(), w.getSpawnLocation().getZ() - plugin.getDistance()));

            if(event.getDrops().contains(new ItemStack(Material.COMPASS))) {
                event.getDrops().remove(new ItemStack(Material.COMPASS));
            }
        }
    }

    private void respawnWithRunnable(Player player, Location location) {
        new BukkitRunnable() {
            @Override
            public void run() {
                // Removed instant respawn
                player.teleport(location);

                if (plugin.isCompass()) {
                    player.getInventory().addItem(new ItemStack(Material.COMPASS));
                }

            }
        }.runTaskLater(plugin, 3);
    }

    private void respawn(Player player) {
        (new BukkitRunnable() {
            public void run() {

                player.spigot().respawn();
                player.sendTitle(new Title("TEST","",1,1,1));
            }
        }).runTaskLater(plugin, 3L);
    }
}
