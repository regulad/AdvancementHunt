package de.teddy.advancementhunt.gamestates;

import com.onarandombox.MultiverseCore.MultiverseCore;
import de.teddy.advancementhunt.AdvancementHunt;
import de.teddy.advancementhunt.message.MessageType;
import de.teddy.advancementhunt.teams.Team;
import org.apache.commons.lang.reflect.FieldUtils;
import org.bukkit.*;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class IngameState extends GameState {

	private String advancement_id;
	private Player fleeingPlayer;

	@Override
	public void start() {
		Player hunter1 = AdvancementHunt.getInstance().getTeamManager().getPlayers(Team.HUNTER).get(0);
		Player hunter2 = AdvancementHunt.getInstance().getTeamManager().getPlayers(Team.HUNTER).get(1);
		Player player = AdvancementHunt.getInstance().getTeamManager().getPlayers(Team.PLAYER).get(0);
		
		if(!AdvancementHunt.getInstance().getTeamManager().isTeamEmpty(Team.PLAYER)) {
			Player fleeingPlayer = Bukkit.getOnlinePlayers().stream().skip((int) (Bukkit.getOnlinePlayers().size() * Math.random())).findFirst().orElse(null);
			this.fleeingPlayer = fleeingPlayer;
		} else {
			Player fleeingPlayer = player;
			this.fleeingPlayer = fleeingPlayer;
		}
		this.advancement_id =  AdvancementHunt.getInstance().getAdvancement_id();

		// Fixed Advancement using namspaced key ()
		AdvancementHunt.getInstance().getMessageManager().sendMessageReplace(fleeingPlayer, MessageType.ISFLEEING_PLAYER,"%id%",this.advancement_id.split("/")[1]);
		// fleeingPlayer.sendMessage(AdvancementHunt.getInstance().getConfigManager().getMessageWithReplace("Game.Messages.IsFleeingPlayer", "%id%", this.advancement_id));

		int count = 0;
		for(Player allPlayers : Bukkit.getOnlinePlayers()) {
			if (count < 2) {
				if (AdvancementHunt.getInstance().getTeamManager().getTeams().get(allPlayers) != Team.PLAYER) {
					for(Player all : Bukkit.getOnlinePlayers())
					{
						AdvancementHunt.getInstance().getMessageManager().sendMessageReplace(all,MessageType.THE_HUNTER,"%player%",AdvancementHunt.getInstance().getTeamManager().getPlayers(Team.HUNTER).get(count).getDisplayName());
					}
					//Bukkit.broadcastMessage(AdvancementHunt.getInstance().getConfigManager().getMessageWithReplace("Game.Messages.TheHunter", "%player%",
							//AdvancementHunt.getInstance().getTeamManager().getPlayers(Team.HUNTER).get(count).getDisplayName()));
					count++;
				}
			}
		}

		for(Player all : Bukkit.getOnlinePlayers())
		{
			AdvancementHunt.getInstance().getMessageManager().sendMessageReplace(all,MessageType.THE_FLEEING_PLAYER,"%player%",player.getDisplayName());
		}
		//Bukkit.broadcastMessage(AdvancementHunt.getInstance().getConfigManager().getMessageWithReplace("Game.Messages.TheFleeingPlayer", "%player%",
				//player.getDisplayName()));

		AdvancementHunt.getInstance().getActionbarManager().startTimeRemaining(AdvancementHunt.getInstance().getMinutesUntilEnd());

		// Teleport Player To worldboarder
		String name = AdvancementHunt.getInstance().getWorldName();
		World w = Bukkit.getServer().getWorld(name);
		WorldBorder wb = Bukkit.getWorld(AdvancementHunt.getInstance().getWorldName()).getWorldBorder();
		wb.setCenter(0, 0);
		wb.setSize(4000);
		hunter1.teleport(new Location(w, w.getSpawnLocation().getX(), w.getSpawnLocation().getY(), w.getSpawnLocation().getZ() - AdvancementHunt.getInstance().getDistance()));
		hunter2.teleport(new Location(w, w.getSpawnLocation().getX(), w.getSpawnLocation().getY(), w.getSpawnLocation().getZ() - AdvancementHunt.getInstance().getDistance()));
		player.teleport(new Location(w, w.getSpawnLocation().getX(), w.getSpawnLocation().getY(), w.getSpawnLocation().getZ()));
		//System.out.println("Size: " + wb.getSize() + " CenterX: " + wb.getCenter());

		if (AdvancementHunt.getInstance().isGlow()) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 1));
		}
		if (AdvancementHunt.getInstance().isCompass()) {
			hunter1.getInventory().addItem(new ItemStack(Material.COMPASS));
			hunter2.getInventory().addItem(new ItemStack(Material.COMPASS));
		}
	}

	@Override
	public void stop() {

	}

	public Player getFleeingPlayer() { return fleeingPlayer; }
}
