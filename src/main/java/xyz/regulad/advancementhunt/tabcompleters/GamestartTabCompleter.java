package xyz.regulad.advancementhunt.tabcompleters;

import org.bukkit.advancement.Advancement;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.regulad.advancementhunt.AdvancementHunt;

import java.util.*;

public class GamestartTabCompleter implements TabCompleter {
    private final AdvancementHunt plugin;

    public GamestartTabCompleter(AdvancementHunt plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] arguments) {
        switch (arguments.length) {
            case 1: // Hunted player
                Collection<? extends Player> playerCollection = this.plugin.getServer().getOnlinePlayers();
                ArrayList<String> playerList = new ArrayList<>();
                for (Player player : playerCollection) {
                    playerList.add(player.getDisplayName());
                }
                return playerList;
            case 2: // Advancement
                List<String> advancementList = new ArrayList<>();
                Iterator<Advancement> advancementIterator = this.plugin.getServer().advancementIterator();
                while (advancementIterator.hasNext()) {
                    Advancement advancement = advancementIterator.next();
                    advancementList.add(advancement.toString());
                }
                return advancementList;
            case 3: // Time
                return Arrays.asList("10", "20", "30");
            case 4: // World seed
                return Arrays.asList(String.valueOf(this.plugin.getServer().getWorlds().get(0).getSeed()));
            case 5: // Worldborder
                return Arrays.asList("1000", "2000", "3000000");
            default:
                return null;
        }
    }
}
