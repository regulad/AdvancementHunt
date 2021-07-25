package quest.ender.AdvancementHunt.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.javatuples.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import quest.ender.AdvancementHunt.AdvancementHunt;
import quest.ender.AdvancementHunt.exceptions.GameAlreadyStartedException;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class GamestartCommand implements CommandExecutor, TabCompleter {
    private final AdvancementHunt plugin;

    public GamestartCommand(AdvancementHunt plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (this.plugin.getServer().getOnlinePlayers().size() <= 1) {
            sender.sendMessage(Component.text("You cannot play the game with only one player.").color(TextColor.color(11141120)));
        } else {
            final @Nullable Player huntedPlayer;
            final @Nullable Pair<@Nullable Advancement, @NotNull Duration> advancement;
            final @Nullable Pair<@NotNull String, @NotNull Integer> seed;
            switch (args.length) {
                case 0:
                    if (sender instanceof Player) {
                        huntedPlayer = (Player) sender;
                    } else {
                        return false; // Not a Player, and one was not provided.
                    }
                    advancement = this.plugin.getAdvancementManager().getAdvancement();
                    seed = this.plugin.getSeedManager().getSeed();
                    break;
                case 1:
                    huntedPlayer = this.plugin.getServer().getPlayer(args[0]);
                    advancement = this.plugin.getAdvancementManager().getAdvancement();
                    seed = this.plugin.getSeedManager().getSeed();
                    break;
                case 3:
                    huntedPlayer = this.plugin.getServer().getPlayer(args[0]);
                    advancement = new Pair<>(this.plugin.getServer().getAdvancement(NamespacedKey.minecraft(args[1].replaceFirst("minecraft:", ""))), Duration.of(Integer.parseInt(args[2]), ChronoUnit.MINUTES));
                    seed = this.plugin.getSeedManager().getSeed();
                    break;
                case 5:
                    huntedPlayer = this.plugin.getServer().getPlayer(args[0]);
                    advancement = new Pair<>(this.plugin.getServer().getAdvancement(NamespacedKey.minecraft(args[1].replaceFirst("minecraft:", ""))), Duration.of(Integer.parseInt(args[2]), ChronoUnit.MINUTES));
                    seed = new Pair<>(args[3], Integer.valueOf(args[4]));
                    break;
                default:
                    return false;
            }

            if (huntedPlayer == null) {
                sender.sendMessage(Component.text("Couldn't find the player that you asked for.").color(TextColor.color(11141120)));
            } else {
                Collection<? extends Player> onlinePlayers = this.plugin.getServer().getOnlinePlayers();
                ArrayList<Player> hunterPlayers = new ArrayList<>();

                for (Player player : onlinePlayers) if (!player.equals(huntedPlayer)) hunterPlayers.add(player);

                if (advancement == null || seed == null) {
                    sender.sendMessage(Component.text("AdvancementHunt was unable to read the database. Please check the console!").color(TextColor.color(11141120)));
                } else {
                    try {
                        this.plugin.startGame(huntedPlayer, hunterPlayers, advancement.getValue0(), Instant.now().plus(advancement.getValue1()), seed.getValue0(), seed.getValue1());
                        sender.sendMessage(Component.text("The game has started."));
                    } catch (GameAlreadyStartedException exception) {
                        sender.sendMessage(Component.text("The game cannot be started, one is already ongoing."));
                    }
                }
            }
        }
        return true;
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
                    advancementList.add(advancement.getKey().toString());
                }
                return advancementList;
            case 3: // Time
                return Arrays.asList("10", "20", "30");
            case 4: // World seed
                return Collections.singletonList(String.valueOf(this.plugin.getServer().getWorlds().get(0).getSeed()));
            case 5: // Worldborder
                return Arrays.asList("1000", "2000", "3000000");
            default:
                return null;
        }
    }
}
