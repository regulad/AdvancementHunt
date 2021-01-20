package com.regulad.advancementhunt.listener;

import com.regulad.advancementhunt.AdvancementHunt;
import com.regulad.advancementhunt.gamestates.EndingState;
import com.regulad.advancementhunt.gamestates.GameState;
import com.regulad.advancementhunt.gamestates.IngameState;
import com.regulad.advancementhunt.gamestates.LobbyState;
import com.regulad.advancementhunt.message.MessageType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerConnectionListener implements Listener {

    private final AdvancementHunt plugin;
    private final LobbyState lobbyState;

    public PlayerConnectionListener(AdvancementHunt plugin) {
        this.plugin = plugin;
        this.lobbyState = (LobbyState) plugin.getGameStateManager().getCurrentGameState();
    }

    @EventHandler
    public void onPlayerPreJoin(AsyncPlayerPreLoginEvent event) {
        /*
         * Disallow player join if game is already in progress.
         */
        if (plugin.getGameStateManager().getCurrentGameState() instanceof EndingState) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, plugin.getMessageManager().getMessage(MessageType.IN_PROGRESS_GAME).getMessage());
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        /*
         * for some reason Player object was null so lets try adding some delay.
         */
        Bukkit.getScheduler().runTaskLater(AdvancementHunt.getInstance(), () -> {
            Player player = event.getPlayer();

            if (!(plugin.getGameStateManager().getCurrentGameState() instanceof EndingState)) {
                plugin.getUtils().getLocationUtil().teleport(player, "LobbySpawn");
            }
        }, 100);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (plugin.getGameStateManager().getCurrentGameState() instanceof IngameState) {
            plugin.getGameStateManager().setGameState(GameState.ENDING_STATE);
        }

        // What is this? because there is no point to keep it locally if you are checking it above.
        if (lobbyState.getLobbyTimer().isStarted()) {
            lobbyState.getLobbyTimer().cancel();
        }
    }

}
