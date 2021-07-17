package quest.ender.AdvancementHunt.exceptions;

public class GameNotStartedException extends BadGameStateException {
    public GameNotStartedException(String message) {
        super(message);
    }
}
