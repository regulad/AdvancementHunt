package de.teddy.advancementhunt.listener;

import de.teddy.advancementhunt.AdvancementHunt;
import de.teddy.advancementhunt.gamestates.GameState;
import de.teddy.advancementhunt.message.MessageManager;
import de.teddy.advancementhunt.message.MessageType;
import de.teddy.advancementhunt.teams.Team;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

public class PlayerAdvancementDoneListener implements Listener {

    private final AdvancementHunt plugin;

    public PlayerAdvancementDoneListener(AdvancementHunt plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onAdvancementIsDone(PlayerAdvancementDoneEvent event) {
        Player player = event.getPlayer();
        if (plugin.getAdvancement_id().contains(event.getAdvancement().getKey().getKey())) {
            plugin.getGameStateManager().setGameState(GameState.ENDING_STATE);

            MessageManager messageManager = plugin.getMessageManager();

            for (Player all : Bukkit.getOnlinePlayers()) {
                messageManager.sendMessageReplace(all, MessageType.WON,"%player%",player.getDisplayName());
            }
            // Bukkit.broadcastMessage(AdvancementHunt.getInstance().getConfigManager().getMessageWithReplace("Game.Messages.Won", "%player%", player.getDisplayName()));

            plugin.getUtils().getFightUtil().addLoose(plugin.getTeamManager().getPlayers(Team.HUNTER).get(0), 1);
            plugin.getUtils().getFightUtil().addLoose(plugin.getTeamManager().getPlayers(Team.HUNTER).get(1), 1);
            plugin.getUtils().getFightUtil().addWin(plugin.getTeamManager().getPlayers(Team.PLAYER).get(0), 1);
        }
        //player.sendMessage("Du hast ein neues Advancement: " + event.getAdvancement().getKey().getKey());
    }

}
