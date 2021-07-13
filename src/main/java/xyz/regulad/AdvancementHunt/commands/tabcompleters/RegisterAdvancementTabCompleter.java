package xyz.regulad.AdvancementHunt.commands.tabcompleters;

import org.bukkit.advancement.Advancement;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import xyz.regulad.AdvancementHunt.AdvancementHunt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class RegisterAdvancementTabCompleter implements TabCompleter {
    private final AdvancementHunt plugin;

    public RegisterAdvancementTabCompleter(AdvancementHunt plugin) {
        this.plugin = plugin;
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
