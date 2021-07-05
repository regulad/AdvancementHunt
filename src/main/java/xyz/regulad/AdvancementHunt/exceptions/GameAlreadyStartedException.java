package xyz.regulad.AdvancementHunt.exceptions;

public class GameAlreadyStartedException extends BadGameStateException {
    public GameAlreadyStartedException(String message) {
        super(message);
    }
}
