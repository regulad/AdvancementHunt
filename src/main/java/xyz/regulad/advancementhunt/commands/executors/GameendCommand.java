package xyz.regulad.advancementhunt.commands.executors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import xyz.regulad.advancementhunt.AdvancementHunt;
import xyz.regulad.advancementhunt.exceptions.GameNotStartedException;
import xyz.regulad.advancementhunt.game.GameEndReason;

public class GameendCommand implements CommandExecutor {
    private final AdvancementHunt plugin;

    public GameendCommand(AdvancementHunt plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        try {
            this.plugin.endGame(GameEndReason.NONE);
            sender.sendMessage("The game has ended.");
        } catch (GameNotStartedException exception) {
            sender.sendMessage("A game is not ongoing.");
        }
        return true;
    }
}
