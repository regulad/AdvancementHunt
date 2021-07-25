package quest.ender.AdvancementHunt.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import quest.ender.AdvancementHunt.AdvancementHunt;

import java.util.Arrays;
import java.util.List;

public class RegisterSeedCommand implements CommandExecutor, TabCompleter {
    private final AdvancementHunt plugin;

    public RegisterSeedCommand(AdvancementHunt plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length != 2) return false; // Wrong number of arguments!
        String worldSeed = args[0];
        int worldBorder = Integer.parseInt(args[1]);
        this.plugin.getSeedManager().putSeed(worldSeed, worldBorder);
        return true; // All is good!
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] arguments) {
        return switch (arguments.length) {
            case 1 -> // World seed
                    Arrays.asList(String.valueOf(this.plugin.getServer().getWorlds().get(0).getSeed()));
            case 2 -> // Worldborder
                    Arrays.asList("1000", "2000", "3000000");
            default -> null;
        };
    }
}
