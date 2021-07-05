package xyz.regulad.AdvancementHunt.commands.executors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import xyz.regulad.AdvancementHunt.AdvancementHunt;

import java.sql.SQLException;

public class RegisterSeedCommand implements CommandExecutor {
    private final AdvancementHunt plugin;

    public RegisterSeedCommand(AdvancementHunt plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length != 2) return false; // Wrong number of arguments!
        String worldSeed = args[0];
        int worldBorder = Integer.parseInt(args[1]);
        try {
            this.plugin.getSeedManager().putSeed(worldSeed, worldBorder);
        } catch (SQLException exception) {
            this.plugin.getLogger().severe(exception.getMessage());
            sender.sendMessage("§4AdvancementHunt was unable to read the database. Please check the console!");
        }
        return true; // All is good!
    }
}
