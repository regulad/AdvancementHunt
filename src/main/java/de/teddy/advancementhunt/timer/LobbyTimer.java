package de.teddy.advancementhunt.timer;

import de.teddy.advancementhunt.AdvancementHunt;
import de.teddy.advancementhunt.gamestates.GameState;
import de.teddy.advancementhunt.gamestates.LobbyState;
import de.teddy.advancementhunt.message.MessageType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class LobbyTimer extends Timer {

//	private int idleTask;

	private int seconds = 5;
	public void setSeconds(int seconds) { this.seconds = seconds; }

	//private boolean isIdling = false;
	private boolean isStarted = false;


	@Override
	public void start() {
		isStarted = true;

		counter = Bukkit.getScheduler().scheduleSyncRepeatingTask(AdvancementHunt.getInstance(), () -> {
			if (seconds == 0) {
				cancel();
				AdvancementHunt.getInstance().getGameStateManager().setGameState(GameState.INGAME_STATE);
			} else {
				for (Player player : Bukkit.getOnlinePlayers()) {
					AdvancementHunt.getInstance().getMessageManager().sendMessageReplace(player, MessageType.START_GAME, "%seconds%", seconds + "");
					// AdvancementHunt.getInstance().getActionbarManager().sendActionbar(player, AdvancementHunt.getInstance().getPrefix() + AdvancementHunt.getInstance().getConfigManager().getMessageWithReplace("Game.Messages.StartGame", "%seconds%", String.valueOf(seconds)));
				}
			}
			seconds--;
		}, 0, 20);
	}

	@Override
	public void cancel() {
		this.isStarted = false;
		Bukkit.getScheduler().cancelTask(counter);
	}
	
//	public void idle() {
//		isIdling = true;
//
//		idleTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(AdvancementHunt.getInstance(), new Runnable() {
//
//			@Override
//			public void run() {
//				for(Player player : Bukkit.getOnlinePlayers()) {
//					int missingPlayers = LobbyState.MIN_PLAYERS - Bukkit.getOnlinePlayers().size();
//					if(missingPlayers != 1) {
//						AdvancementHunt.getInstance().getActionbarManager().sendActionbar(player, AdvancementHunt.getInstance().getPrefix() + "§e" + missingPlayers + " §7players are still required to start");
//					} else {
//						AdvancementHunt.getInstance().getActionbarManager().sendActionbar(player, AdvancementHunt.getInstance().getPrefix() + "§e" + missingPlayers + " §7player is still required to start");
//					}
//				}
//			}
//		}, 5 * 20, 5 * 20);
//	}
//
//	public void cancelIdle() {
//		isIdleing = false;
//		Bukkit.getScheduler().cancelTask(idleTask);
//		System.out.println("'IdleTask' Scheduler running: " + Bukkit.getScheduler().isCurrentlyRunning(idleTask));
//	}
//
//	public void stopIdle() { }
//
//	public boolean isIdleing() { return isIdling; }

	public boolean isStarted() { return isStarted; }

	public void setStarted(boolean started) { isStarted = started; }
}
