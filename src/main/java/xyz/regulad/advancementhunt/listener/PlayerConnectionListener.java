package xyz.regulad.advancementhunt.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import xyz.regulad.advancementhunt.AdvancementHunt;
import xyz.regulad.advancementhunt.gamestates.EndingState;
import xyz.regulad.advancementhunt.gamestates.GameState;
import xyz.regulad.advancementhunt.gamestates.IngameState;
import xyz.regulad.advancementhunt.gamestates.LobbyState;
import xyz.regulad.advancementhunt.message.MessageType;

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
