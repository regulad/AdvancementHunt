package xyz.regulad.AdvancementHunt.commands.tabcompleters;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import xyz.regulad.AdvancementHunt.AdvancementHunt;

import java.util.Arrays;
import java.util.List;

public class RegisterSeedTabCompleter implements TabCompleter {
    private final AdvancementHunt plugin;

    public RegisterSeedTabCompleter(AdvancementHunt plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] arguments) {
        switch (arguments.length) {
            case 1: // World seed
                return Arrays.asList(String.valueOf(this.plugin.getServer().getWorlds().get(0).getSeed()));
            case 2: // Worldborder
                return Arrays.asList("1000", "2000", "3000000");
            default:
                return null;
        }
    }
}
