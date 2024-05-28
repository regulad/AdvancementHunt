package quest.ender.AdvancementHunt.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import quest.ender.AdvancementHunt.AdvancementHunt;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class RegisterAdvancementCommand implements CommandExecutor, TabCompleter {
    private final AdvancementHunt plugin;

    public RegisterAdvancementCommand(AdvancementHunt plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length != 2) return false; // Wrong number of arguments!

        final @Nullable Advancement possibleAdvancement = this.plugin.getServer().getAdvancement(NamespacedKey.minecraft(args[0].replaceFirst("minecraft:", "")));
        final @NotNull Duration advancementTime = Duration.of(Integer.parseInt(args[1]), ChronoUnit.MINUTES);

        if (possibleAdvancement == null) return false; // Bad advancement!

        this.plugin.getAdvancementManager().putAdvancement(possibleAdvancement, advancementTime);

        // send a confirmation message
        sender.sendMessage(Component.text("Advancement registered!").color(NamedTextColor.GREEN));

        return true; // All is good!
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] arguments) {
        switch (arguments.length) {
            case 1: // Advancement
                List<String> advancementList = new ArrayList<>();
                Iterator<Advancement> advancementIterator = this.plugin.getServer().advancementIterator();
                while (advancementIterator.hasNext()) {
                    Advancement advancement = advancementIterator.next();
                    advancementList.add(advancement.getKey().toString());
                }
                return advancementList;
            case 2: // Time
                return Arrays.asList("10", "20", "30");
            default:
                return null;
        }
    }
}
