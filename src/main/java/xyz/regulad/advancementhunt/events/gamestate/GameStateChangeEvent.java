package xyz.regulad.advancementhunt.events.gamestate;

import org.bukkit.event.Event;
import xyz.regulad.advancementhunt.game.states.GameState;

public abstract class GameStateChangeEvent extends Event {
    private final GameState oldGameState;
    private final GameState newGameState;

    public GameStateChangeEvent(GameState oldGameState, GameState newGameState) {
        this.oldGameState = oldGameState;
        this.newGameState = newGameState;
    }

    public GameState getOldGameState() {
        return this.oldGameState;
    }

    public GameState getNewGameState() {
        return this.newGameState;
    }
}
