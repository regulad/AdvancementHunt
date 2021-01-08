package de.teddy.advancementhunt.gamestates;

public class GameStateManager {

	private final GameState[] gameStates = new GameState[3];
	private GameState currentGameState;
	
	public GameStateManager() {
		gameStates[GameState.LOBBY_STATE] = new LobbyState();
		gameStates[GameState.INGAME_STATE] = new IngameState();
		gameStates[GameState.ENDING_STATE] = new EndingState();
	}
	
	public void setGameState(int gameStateIndex) {
		
		if(currentGameState != null)
			currentGameState.stop();
		currentGameState = gameStates[gameStateIndex];
		currentGameState.start();
		
	}
	
	public void stopCurrentGameState() {
		currentGameState.stop();
		currentGameState = null;
	}

	public void resetGameStates() {
		currentGameState.stop();
		currentGameState = gameStates[GameState.LOBBY_STATE];
		currentGameState.start();
	}
	
	public GameState getCurrentGameState() {
		return currentGameState;
	}
	
}
