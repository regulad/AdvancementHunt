package quest.ender.AdvancementHunt.events;

import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import quest.ender.AdvancementHunt.game.state.GameState;

public class PostGameStateChangeEvent extends GameStateChangeEvent {
    private static final HandlerList handlers = new HandlerList();

    public PostGameStateChangeEvent(GameState oldGameState, GameState newGameState) {
        super(oldGameState, newGameState);
    }

    public static @NotNull HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
}
