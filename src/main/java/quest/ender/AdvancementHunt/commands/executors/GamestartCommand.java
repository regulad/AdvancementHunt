package quest.ender.AdvancementHunt.commands.executors;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.javatuples.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import quest.ender.AdvancementHunt.AdvancementHunt;
import quest.ender.AdvancementHunt.exceptions.GameAlreadyStartedException;
import quest.ender.AdvancementHunt.util.TimeUtil;

import java.util.ArrayList;
import java.util.Collection;

public class GamestartCommand implements CommandExecutor {
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
            final @Nullable Pair<@Nullable Advancement, Integer> advancement;
            final @Nullable Pair<@NotNull String, Integer> seed;
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
                    advancement = new Pair<>(this.plugin.getServer().getAdvancement(NamespacedKey.minecraft(args[1].replaceFirst("minecraft:", ""))), Integer.valueOf(args[2]));
                    seed = this.plugin.getSeedManager().getSeed();
                    break;
                case 5:
                    huntedPlayer = this.plugin.getServer().getPlayer(args[0]);
                    advancement = new Pair<>(this.plugin.getServer().getAdvancement(NamespacedKey.minecraft(args[1].replaceFirst("minecraft:", ""))), Integer.valueOf(args[2]));
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
                        this.plugin.startGame(huntedPlayer, hunterPlayers, advancement.getValue0(), TimeUtil.instantInFuture(advancement.getValue1() * 60_000), seed.getValue0(), seed.getValue1());
                        sender.sendMessage(Component.text("The game has started."));
                    } catch (GameAlreadyStartedException exception) {
                        sender.sendMessage(Component.text("The game cannot be started, one is already ongoing."));
                    }
                }
            }
        }
        return true;
    }
}
