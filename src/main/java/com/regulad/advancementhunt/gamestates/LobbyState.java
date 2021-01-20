package com.regulad.advancementhunt.gamestates;

import com.regulad.advancementhunt.AdvancementHunt;
import com.regulad.advancementhunt.timer.LobbyTimer;

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
