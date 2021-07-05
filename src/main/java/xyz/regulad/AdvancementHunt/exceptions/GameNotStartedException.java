package xyz.regulad.AdvancementHunt.exceptions;

public class GameNotStartedException extends BadGameStateException {
    public GameNotStartedException(String message) {
        super(message);
    }
}
