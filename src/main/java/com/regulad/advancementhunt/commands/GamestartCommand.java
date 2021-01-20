package com.regulad.advancementhunt.commands;

import com.regulad.advancementhunt.AdvancementHunt;
import com.regulad.advancementhunt.gamestates.LobbyState;
import com.regulad.advancementhunt.message.MessageType;
import com.regulad.advancementhunt.mysql.MySQLManager;
import com.regulad.advancementhunt.permissions.Permission;
import com.regulad.advancementhunt.teams.Team;
import com.regulad.advancementhunt.teams.TeamManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class GamestartCommand implements CommandExecutor {

    private final AdvancementHunt plugin;
    private final MySQLManager mySQLManager;

    public GamestartCommand(AdvancementHunt plugin) {
        this.plugin = plugin;
        this.mySQLManager = plugin.getMySQLManager();
    }

    private final LobbyState lobbyState = (LobbyState) AdvancementHunt.getInstance().getGameStateManager().getCurrentGameState();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }

        if (!(plugin.getGameStateManager().getCurrentGameState() instanceof LobbyState)) {
            System.out.println("In LobbyState");
            return true;
        }

        if (lobbyState.getLobbyTimer().isStarted()) {
            System.out.println("Is Started");
            return true;
        }
        Player player = (Player) sender;

        String advancementId;
        String seed;
        int timeLimit;
        int countdownSeconds;
        String username;
        boolean glow;
        boolean compass;
        int distance;

        if (!(args.length == 8)) {
            Random random = new Random();
            advancementId = mySQLManager.getAdvancements().get(random.nextInt(mySQLManager.getAdvancements().size()));
            seed = mySQLManager.getSeeds().get(new Random().nextInt(mySQLManager.getSeeds().size())) + "";
            timeLimit = 30;
            countdownSeconds = 5;
            username = ((Player) Bukkit.getOnlinePlayers().toArray()[new Random().nextInt(Bukkit.getOnlinePlayers().toArray().length)]).getName();

            distance = 10;

            if (Bukkit.getOnlinePlayers().size() >= LobbyState.MIN_PLAYERS) {
                plugin.getMessageManager().sendMessage(player, MessageType.CREATING_WORLD);
                plugin.getUtils().getWorldUtil().worldCreate(plugin.getWorldName(), World.Environment.NORMAL, seed);

                WorldCreator normal_world = new WorldCreator(plugin.getWorldName());

                runCheck(advancementId, seed, timeLimit, username, distance, normal_world);

                plugin.setGlow(true);
                plugin.setCompass(true);

                lobbyState.getLobbyTimer().setSeconds(countdownSeconds);
                lobbyState.getLobbyTimer().start();
            } else {
                plugin.getMessageManager().sendMessageReplace(player, MessageType.NOT_ENOUGH_PLAYERS, "%count%", LobbyState.MIN_PLAYERS - Bukkit.getOnlinePlayers().size() + "");
            }
            return true;
        }


        if (!(player.hasPermission(plugin.getPermissionManager().getPermission(Permission.START)))) {
            return true;
        }

        if (!args[0].equalsIgnoreCase("random")) {
            advancementId = args[0];
        } else {
            Random random = new Random();
            advancementId = mySQLManager.getAdvancements().get(random.nextInt(mySQLManager.getAdvancements().size()));
        }

        seed = args[1];

        if (args[1].equalsIgnoreCase("random")) {
            // if seed is random set seed to random long
            seed = mySQLManager.getSeeds().get(new Random().nextInt(mySQLManager.getSeeds().size())) + "";
        }
        timeLimit = Integer.parseInt(args[2]);
        countdownSeconds = Integer.parseInt(args[3]);
        if (!args[4].equalsIgnoreCase("random")) {
            if (Bukkit.getPlayer(args[4]) != null) {
                username = args[4];
            } else {
                plugin.getMessageManager().sendMessage(player, MessageType.PLAYER_NOT_ONLINE);
                return true;
            }
        } else {
            username = null;
        }
        glow = Boolean.parseBoolean(args[5]);
        compass = Boolean.parseBoolean(args[6]);
        distance = Integer.parseInt(args[7]);

        if (Bukkit.getOnlinePlayers().size() >= LobbyState.MIN_PLAYERS) {
            // New message update
            plugin.getMessageManager().sendMessage(player, MessageType.CREATING_WORLD);
            plugin.getUtils().getWorldUtil().worldCreate(plugin.getWorldName(), World.Environment.NORMAL, seed);

            // Fist Make world and load them
            WorldCreator normal_world = new WorldCreator(plugin.getWorldName());
            runCheck(advancementId, seed, timeLimit, username, distance, normal_world);

            plugin.setGlow(glow);
            plugin.setCompass(compass);

            lobbyState.getLobbyTimer().setSeconds(countdownSeconds);
            lobbyState.getLobbyTimer().start();
        } else {
            player.sendMessage(plugin.getConfigManager().getMessageWithReplace("Game.Messages.NotEnoughPlayers", "%count%", String.valueOf((LobbyState.MIN_PLAYERS - Bukkit.getOnlinePlayers().size()))));
        }

        return true;
    }

    private void runCheck(String advancementId, String seed, int timeLimit, String username, int distance, WorldCreator normal_world) {
        normal_world.seed(Long.parseLong(seed));
        normal_world.environment(World.Environment.NORMAL);
        normal_world.createWorld();
        plugin.getMultiverseCore().getMVWorldManager().getMVWorld(plugin.getWorldName()).setSpawnLocation(Bukkit.getWorld(plugin.getWorldName()).getSpawnLocation());

        WorldCreator nether_world = new WorldCreator(plugin.getWorldName() + "_nether");
        nether_world.environment(World.Environment.NETHER);
        nether_world.createWorld();

        WorldCreator end_world = new WorldCreator(plugin.getWorldName() + "_the_end");
        end_world.environment(World.Environment.THE_END);
        end_world.createWorld();

        plugin.getTeamManager().setInTeam(Bukkit.getPlayer(username), Team.PLAYER);

        TeamManager teamManager = plugin.getTeamManager();

        for (Player allPlayers : Bukkit.getOnlinePlayers()) {
            if (teamManager.getTeams().get(allPlayers) != Team.PLAYER) {
                teamManager.setInTeam(allPlayers, Team.HUNTER);
            }
        }

        plugin.setAdvancement_id(advancementId);
        plugin.setMinutesUntilEnd(timeLimit);

        plugin.setDistance(distance);
    }
}
