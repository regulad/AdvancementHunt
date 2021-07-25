package quest.ender.AdvancementHunt.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import quest.ender.AdvancementHunt.game.state.GameState;

public class PreGameStateChangeEvent extends GameStateChangeEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;

    public PreGameStateChangeEvent(GameState oldGameState, GameState newGameState) {
        super(oldGameState, newGameState);
    }

    public static @NotNull HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
