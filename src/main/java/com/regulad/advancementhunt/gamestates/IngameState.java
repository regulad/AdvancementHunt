package com.regulad.advancementhunt.gamestates;

import com.regulad.advancementhunt.AdvancementHunt;
import com.regulad.advancementhunt.message.MessageType;
import com.regulad.advancementhunt.teams.Team;
import com.regulad.advancementhunt.teams.TeamManager;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class IngameState extends GameState {

	private Player fleeingPlayer;

    private final AdvancementHunt plugin;
    private final TeamManager teamManager;

    public IngameState(AdvancementHunt plugin) {
        this.plugin = plugin;
        this.teamManager = plugin.getTeamManager();
    }

    @Override
    public void start() {
        Player hunter1 = teamManager.getPlayers(Team.HUNTER).get(0);
        Player hunter2 = teamManager.getPlayers(Team.HUNTER).get(1);
        Player player = teamManager.getPlayers(Team.PLAYER).get(0);

        if (!teamManager.isTeamEmpty(Team.PLAYER)) {
			this.fleeingPlayer = Bukkit.getOnlinePlayers().stream().skip((int) (Bukkit.getOnlinePlayers().size() * Math.random())).findFirst().orElse(null);
        } else {
			this.fleeingPlayer = player;
        }

		String advancement_id = plugin.getAdvancement_id();

        plugin.getMessageManager().sendMessage(fleeingPlayer, MessageType.ISFLEEING_PLAYER);

        int count = 0;

        for (Player allPlayers : Bukkit.getOnlinePlayers()) {
            if (count < 2) {
                if (teamManager.getTeams().get(allPlayers) != Team.PLAYER) {
                    for (Player all : Bukkit.getOnlinePlayers()) {
                        plugin.getMessageManager().sendMessageReplace(all, MessageType.THE_HUNTER, "%player%", teamManager.getPlayers(Team.HUNTER).get(count).getDisplayName());
                    }
                    count++;
                }
            }
        }

        for (Player all : Bukkit.getOnlinePlayers()) {
            plugin.getMessageManager().sendMessageReplace(all, MessageType.THE_FLEEING_PLAYER, "%player%", player.getDisplayName());
        }
        plugin.getActionbarManager().startTimeRemaining(AdvancementHunt.getInstance().getMinutesUntilEnd());

        // Teleport Player To worldborder
        String name = plugin.getWorldName();
        World w = Bukkit.getServer().getWorld(name);
        WorldBorder wb = Bukkit.getWorld(plugin.getWorldName()).getWorldBorder();
        wb.setCenter(0, 0);
        wb.setSize(4000);

        int distance = plugin.getDistance();

        hunter1.teleport(new Location(w, w.getSpawnLocation().getX(), w.getSpawnLocation().getY(), w.getSpawnLocation().getZ() - distance));
        hunter2.teleport(new Location(w, w.getSpawnLocation().getX(), w.getSpawnLocation().getY(), w.getSpawnLocation().getZ() - distance));
        player.teleport(new Location(w, w.getSpawnLocation().getX(), w.getSpawnLocation().getY(), w.getSpawnLocation().getZ()));

        if (plugin.isGlow()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 1));
        }

        if (plugin.isCompass()) {
            hunter1.getInventory().addItem(new ItemStack(Material.COMPASS));
            hunter2.getInventory().addItem(new ItemStack(Material.COMPASS));
        }
    }

    @Override
    public void stop() {

    }

    public Player getFleeingPlayer() {
        return fleeingPlayer;
    }
}
