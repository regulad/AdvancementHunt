package xyz.regulad.advancementhunt.exceptions;

public class GameAlreadyStartedException extends BadGameStateException {
    public GameAlreadyStartedException(String message) {
        super(message);
    }
}
