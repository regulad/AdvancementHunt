package de.teddy.advancementhunt.listener;

import com.destroystokyo.paper.Title;
import de.teddy.advancementhunt.AdvancementHunt;
import de.teddy.advancementhunt.gamestates.GameState;
import de.teddy.advancementhunt.message.MessageType;
import de.teddy.advancementhunt.teams.Team;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerDeathListener implements Listener {

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if(!(player instanceof Player))
        {
            return;
        }

        if(event.getEntity().getKiller() != null) {
            // event.setDeathMessage("§c" + player.getDisplayName() + " " + AdvancementHunt.getInstance().getConfigManager().getMessageWithReplace("Game.Messages.KilledBy", "%player%", event.getEntity().getKiller().getDisplayName()));
        } else {
            // event.setDeathMessage(AdvancementHunt.getInstance().getConfigManager().getMessageWithReplace("Game.Messages.Dead", "%player%", player.getDisplayName()));
        }

        if(AdvancementHunt.getInstance().getTeamManager().getPlayers(Team.PLAYER).contains(player)) {
            World w = Bukkit.getServer().getWorld(AdvancementHunt.getInstance().getWorldName());
            respawn(player);

            for(Player all : Bukkit.getOnlinePlayers())
            {
                AdvancementHunt.getInstance().getMessageManager().sendMessage(all, MessageType.HUNTERWON);
            }
            // Bukkit.broadcastMessage(AdvancementHunt.getInstance().getConfigManager().getMessage("Game.Messages.TheHuntersWon"));

            AdvancementHunt.getInstance().getGameStateManager().setGameState(GameState.ENDING_STATE);

            AdvancementHunt.getInstance().getUtils().getFightUtil().addWin(AdvancementHunt.getInstance().getTeamManager().getPlayers(Team.HUNTER).get(0), 1);
            AdvancementHunt.getInstance().getUtils().getFightUtil().addWin(AdvancementHunt.getInstance().getTeamManager().getPlayers(Team.HUNTER).get(1), 1);
            AdvancementHunt.getInstance().getUtils().getFightUtil().addLoose(AdvancementHunt.getInstance().getTeamManager().getPlayers(Team.PLAYER).get(0), 1);
        } else if(AdvancementHunt.getInstance().getTeamManager().getPlayers(Team.HUNTER).contains(player)) {
            World w = Bukkit.getServer().getWorld(AdvancementHunt.getInstance().getWorldName());

            WorldBorder wb = Bukkit.getWorld(AdvancementHunt.getInstance().getWorldName()).getWorldBorder();
            respawnWithRunnable(player, new Location(w, w.getSpawnLocation().getX(), w.getSpawnLocation().getY(), w.getSpawnLocation().getZ() - AdvancementHunt.getInstance().getDistance()));

            if(event.getDrops().contains(new ItemStack(Material.COMPASS))) {
                event.getDrops().remove(new ItemStack(Material.COMPASS));
            }
        }
    }

    private void respawnWithRunnable(Player player, Location location) {
        new BukkitRunnable() {
            @Override
            public void run() {
                player.spigot().respawn();
                player.sendTitle(new Title("TEST","",1,1,1));
                player.teleport(location);
                if (AdvancementHunt.getInstance().isCompass()) {
                    player.getInventory().addItem(new ItemStack(Material.COMPASS));
                }
            }
        }.runTaskLater(AdvancementHunt.getInstance(), 3);
    }

    private void respawn(Player player) {
        (new BukkitRunnable() {
            public void run() {
                player.spigot().respawn();
                player.sendTitle(new Title("TEST","",1,1,1));

                // Ignore this
                // player.teleport(Bukkit.getWorld(AdvancementHunt.getInstance().getWorldName()).getSpawnLocation());
            }
        }).runTaskLater(AdvancementHunt.getInstance(), 3L);
    }
}
