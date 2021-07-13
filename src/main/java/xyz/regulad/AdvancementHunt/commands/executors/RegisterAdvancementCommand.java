package xyz.regulad.AdvancementHunt.commands.executors;

import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import xyz.regulad.AdvancementHunt.AdvancementHunt;

public class RegisterAdvancementCommand implements CommandExecutor {
    private final AdvancementHunt plugin;

    public RegisterAdvancementCommand(AdvancementHunt plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length != 2) return false; // Wrong number of arguments!
        Advancement possibleAdvancement = this.plugin.getServer().getAdvancement(NamespacedKey.minecraft(args[0].replaceFirst("minecraft:", "")));
        if (possibleAdvancement == null) return false; // Bad advancement!
        int advancementTime = Integer.parseInt(args[1]);
        this.plugin.getAdvancementManager().putAdvancement(possibleAdvancement, advancementTime);
        return true; // All is good!
    }
}
