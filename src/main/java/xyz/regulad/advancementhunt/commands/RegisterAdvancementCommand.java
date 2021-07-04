package xyz.regulad.advancementhunt.commands;

import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import xyz.regulad.advancementhunt.AdvancementHunt;

import java.sql.SQLException;

public class RegisterAdvancementCommand implements CommandExecutor {
    private final AdvancementHunt plugin;

    public RegisterAdvancementCommand(AdvancementHunt plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length != 2) return false; // Wrong number of arguments!
        Advancement possibleAdvancement = this.plugin.getServer().getAdvancement(NamespacedKey.minecraft(args[0]));
        if (possibleAdvancement == null) return false; // Bad advancement!
        int advancementTime = Integer.parseInt(args[1]);
        try {
            this.plugin.getAdvancementManager().putAdvancement(possibleAdvancement, advancementTime);
        } catch (SQLException exception) {
            this.plugin.getLogger().severe(exception.getMessage());
            sender.sendMessage("§4AdvancementHunt was unable to read the database. Please check the console!");
        }
        return true; // All is good!
    }
}
