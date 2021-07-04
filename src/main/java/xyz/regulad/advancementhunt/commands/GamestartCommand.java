package xyz.regulad.advancementhunt.commands;

import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.regulad.advancementhunt.AdvancementHunt;
import xyz.regulad.advancementhunt.exceptions.GameAlreadyStartedException;

import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

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
            if (args.length == 1) {
                Player huntedPlayer = this.plugin.getServer().getPlayer(args[0]);
                if (huntedPlayer == null) return false; // Player isn't real. Oops!
                Collection<? extends Player> onlinePlayers = this.plugin.getServer().getOnlinePlayers();
                ArrayList<Player> hunterPlayers = new ArrayList<>();

                for (Player player : onlinePlayers) {
                    if (!player.equals(huntedPlayer)) hunterPlayers.add(player);
                }

                final HashMap<Advancement, Integer> advancementHashMap;
                final HashMap<String, Integer> seedHashMap;
                try {
                    advancementHashMap = this.plugin.getAdvancementManager().getAdvancement();
                    seedHashMap = this.plugin.getSeedManager().getSeed();

                    try {
                        this.plugin.startGame(huntedPlayer, hunterPlayers, (Advancement) advancementHashMap.keySet().toArray()[0], Instant.ofEpochMilli(System.currentTimeMillis() + (Integer) advancementHashMap.values().toArray()[0] * 1000), (String) seedHashMap.keySet().toArray()[0], (Double) seedHashMap.values().toArray()[0]);
                        sender.sendMessage("The game has started.");
                    } catch (GameAlreadyStartedException exception) {
                        sender.sendMessage("The game cannot be started, one is already ongoing.");
                    }
                } catch (SQLException exception) {
                    this.plugin.getLogger().severe(exception.getMessage());
                    sender.sendMessage("§4AdvancementHunt was unable to read the database. Please check the console!");
                }
                return true;
            } else if (args.length == 3) {
                Player huntedPlayer = this.plugin.getServer().getPlayer(args[0]);
                if (huntedPlayer == null) return false; // Player isn't real. Oops!
                Collection<? extends Player> onlinePlayers = this.plugin.getServer().getOnlinePlayers();
                ArrayList<Player> hunterPlayers = new ArrayList<>();

                for (Player player : onlinePlayers) {
                    if (!player.equals(huntedPlayer)) hunterPlayers.add(player);
                }

                final HashMap<String, Integer> seedHashMap;
                try {
                    seedHashMap = this.plugin.getSeedManager().getSeed();

                    try {
                        this.plugin.startGame(huntedPlayer, hunterPlayers, this.plugin.getServer().getAdvancement(NamespacedKey.minecraft(args[1])), Instant.ofEpochMilli(System.currentTimeMillis() + Integer.parseInt(args[2]) * 60_000L), (String) seedHashMap.keySet().toArray()[0], (Double) seedHashMap.values().toArray()[0]);
                        sender.sendMessage("The game has started.");
                    } catch (GameAlreadyStartedException exception) {
                        sender.sendMessage("The game cannot be started, one is already ongoing.");
                    }
                } catch (SQLException exception) {
                    this.plugin.getLogger().severe(exception.getMessage());
                    sender.sendMessage("§4AdvancementHunt was unable to read the database. Please check the console!");
                }
                return true;
            } else if (args.length == 5) {
                Player huntedPlayer = this.plugin.getServer().getPlayer(args[0]);
                if (huntedPlayer == null) return false; // Player isn't real. Oops!
                Collection<? extends Player> onlinePlayers = this.plugin.getServer().getOnlinePlayers();
                ArrayList<Player> hunterPlayers = new ArrayList<>();

                for (Player player : onlinePlayers) {
                    if (!player.equals(huntedPlayer)) hunterPlayers.add(player);
                }

                try {
                    this.plugin.startGame(huntedPlayer, hunterPlayers, this.plugin.getServer().getAdvancement(NamespacedKey.minecraft(args[1])), Instant.ofEpochMilli(System.currentTimeMillis() + Integer.parseInt(args[2]) * 60_000L), args[3], Double.parseDouble(args[4]));
                    sender.sendMessage("The game has started.");
                } catch (GameAlreadyStartedException exception) {
                    sender.sendMessage("The game cannot be started, one is already ongoing.");
                }
                return true;
            } else {
                return false;
            }
        }
    }
}
