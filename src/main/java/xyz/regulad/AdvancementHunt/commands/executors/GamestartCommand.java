package xyz.regulad.AdvancementHunt.commands.executors;

import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.javatuples.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.regulad.AdvancementHunt.AdvancementHunt;
import xyz.regulad.AdvancementHunt.exceptions.GameAlreadyStartedException;
import xyz.regulad.AdvancementHunt.util.TimeUtil;

import java.util.ArrayList;
import java.util.Collection;

public class GamestartCommand implements CommandExecutor {
    private final AdvancementHunt plugin;

    public GamestartCommand(AdvancementHunt plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (this.plugin.getServer().getOnlinePlayers().size() == 1) {
            sender.sendMessage("You cannot play the game with only one player.");
            return true;
        } else {
            final @NotNull Player huntedPlayer;
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

            Collection<? extends Player> onlinePlayers = this.plugin.getServer().getOnlinePlayers();
            ArrayList<Player> hunterPlayers = new ArrayList<>();

            for (Player player : onlinePlayers) {
                if (!player.equals(huntedPlayer)) hunterPlayers.add(player);
            }

            if (advancement == null || seed == null) {
                sender.sendMessage("§4AdvancementHunt was unable to read the database. Please check the console!");
            } else {
                try {
                    this.plugin.startGame(huntedPlayer, hunterPlayers, advancement.getValue0(), TimeUtil.instantInFuture(advancement.getValue1() * 60_000), seed.getValue0(), seed.getValue1());
                    sender.sendMessage("The game has started.");
                } catch (GameAlreadyStartedException exception) {
                    sender.sendMessage("The game cannot be started, one is already ongoing.");
                }
            }

            return true;
        }
    }
}
