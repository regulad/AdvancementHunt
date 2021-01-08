package de.teddy.advancementhunt.gamestates;

import de.teddy.advancementhunt.AdvancementHunt;
import de.teddy.advancementhunt.timer.LobbyTimer;
import org.bukkit.*;
import org.bukkit.entity.Player;

public class LobbyState extends GameState {

	public static final int MIN_PLAYERS = 3;

	private final LobbyTimer lobbyTimer = new LobbyTimer();
	
	@Override
	public void start() {
		AdvancementHunt.getInstance().getMultiverseCore();
	}

	@Override
	public void stop() {
	}

	public LobbyTimer getLobbyTimer() { return lobbyTimer; }

}
