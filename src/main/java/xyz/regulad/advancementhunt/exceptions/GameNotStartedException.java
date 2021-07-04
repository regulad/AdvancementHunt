package xyz.regulad.advancementhunt.exceptions;

public class GameNotStartedException extends BadGameStateException {
    public GameNotStartedException(String message) {
        super(message);
    }
}
