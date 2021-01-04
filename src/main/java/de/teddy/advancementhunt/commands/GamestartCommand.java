package de.teddy.advancementhunt.commands;

import de.teddy.advancementhunt.AdvancementHunt;
import de.teddy.advancementhunt.gamestates.GameState;
import de.teddy.advancementhunt.gamestates.LobbyState;
import de.teddy.advancementhunt.message.MessageType;
import de.teddy.advancementhunt.permissions.Permission;
import de.teddy.advancementhunt.teams.Team;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Random;

public class GamestartCommand implements CommandExecutor {

    private String advancementId;
    private String seed;
    private int timeLimit;
    private int countdownSeconds;
    private String username;
    private boolean glow;
    private boolean compass;
    private int distance;

    private LobbyState lobbyState = (LobbyState) AdvancementHunt.getInstance().getGameStateManager().getCurrentGameState();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) {
            return true;
        }
        if(!(AdvancementHunt.getInstance().getGameStateManager().getCurrentGameState() instanceof LobbyState)) {
            System.out.println("In LobbyState");
            return true;
        }
        if(lobbyState.getLobbyTimer().isStarted()) {
            System.out.println("Is Started");
            return true;
        }
        Player player = (Player) sender;

        /*
            advancement id | Advancement to win the game
            -ws | Seed
            -tl | time limit in minutes
            -c | countdown in seconds
            -p | username of fleeing player
            -g | true/false (glow on fleeing player)
            -com | true/false (give the hunters a compass to find the fleeing player)
         */
        if(!(args.length == 8)) {
            Random random = new Random();
            this.advancementId = AdvancementHunt.getInstance().getMySQLManager().getAdvancements().get(random.nextInt(AdvancementHunt.getInstance().getMySQLManager().getAdvancements().size()));
            this.seed = AdvancementHunt.getInstance().getMySQLManager().getSeeds().get(new Random().nextInt(AdvancementHunt.getInstance().getMySQLManager().getSeeds().size())) + "";
            this.timeLimit = 30;
            this.countdownSeconds = 5;
            this.username = ((Player) Bukkit.getOnlinePlayers().toArray()[new Random().nextInt(Bukkit.getOnlinePlayers().toArray().length)]).getName();

            this.glow = true;
            this.compass = true;
            this.distance = 10;

            if(Bukkit.getOnlinePlayers().size() >= LobbyState.MIN_PLAYERS) {
                AdvancementHunt.getInstance().getMessageManager().sendMessage(player,MessageType.CREATING_WORLD);
                // player.sendMessage("§cCreating World...");
                AdvancementHunt.getInstance().getUtils().getWorldUtil().worldCreate(AdvancementHunt.getInstance().getWorldName(), World.Environment.NORMAL, this.seed);

                // Fist Make world and load them
                // #createWorld() create world and loads it
                WorldCreator normal_world = new WorldCreator(AdvancementHunt.getInstance().getWorldName());
                normal_world.seed(Long.parseLong(this.seed));
                normal_world.environment(World.Environment.NORMAL);
                normal_world.createWorld();
                AdvancementHunt.getInstance().getMultiverseCore().getMVWorldManager().getMVWorld(AdvancementHunt.getInstance().getWorldName()).setSpawnLocation(Bukkit.getWorld(AdvancementHunt.getInstance().getWorldName()).getSpawnLocation());

                WorldCreator nether_world = new WorldCreator(AdvancementHunt.getInstance().getWorldName() + "_nether");
                nether_world.environment(World.Environment.NETHER);
                nether_world.createWorld();

                WorldCreator end_world = new WorldCreator(AdvancementHunt.getInstance().getWorldName() + "_the_end");
                end_world.environment(World.Environment.THE_END);
                end_world.createWorld();

                AdvancementHunt.getInstance().getTeamManager().setInTeam(Bukkit.getPlayer(username), Team.PLAYER);
                for(Player allPlayers : Bukkit.getOnlinePlayers()) {
                    if(AdvancementHunt.getInstance().getTeamManager().getTeams().get(allPlayers) != Team.PLAYER) {
                        AdvancementHunt.getInstance().getTeamManager().setInTeam(allPlayers, Team.HUNTER);
                    }
                }
                AdvancementHunt.getInstance().setAdvancement_id(this.advancementId);
                AdvancementHunt.getInstance().setMinutesUntilEnd(this.timeLimit);

                AdvancementHunt.getInstance().setDistance(this.distance);

                AdvancementHunt.getInstance().setGlow(this.glow);
                AdvancementHunt.getInstance().setCompass(this.compass);

                lobbyState.getLobbyTimer().setSeconds(this.countdownSeconds);
                lobbyState.getLobbyTimer().start();
            } else {
                AdvancementHunt.getInstance().getMessageManager().sendMessageReplace(player, MessageType.NOT_ENOUGH_PLAYERS,"%count%",lobbyState.MIN_PLAYERS - Bukkit.getOnlinePlayers().size() + "");
                // player.sendMessage(AdvancementHunt.getInstance().getConfigManager().getMessageWithReplace("Game.Messages.NotEnoughPlayers", "%count%", String.valueOf((lobbyState.MIN_PLAYERS - Bukkit.getOnlinePlayers().size()))));
            }
            return true;
        }


        if(!(player.hasPermission(AdvancementHunt.getInstance().getPermissionManager().getPermission(Permission.START)))) {
            return true;
        }

        if(!args[0].equalsIgnoreCase("random")) {
            this.advancementId = args[0];
        } else {
            Random random = new Random();
            this.advancementId = AdvancementHunt.getInstance().getMySQLManager().getAdvancements().get(random.nextInt(AdvancementHunt.getInstance().getMySQLManager().getAdvancements().size()));
        }
        this.seed = args[1];

        if(args[1].equalsIgnoreCase("random"))
        {
            // if seed is random set seed to random long
            this.seed = AdvancementHunt.getInstance().getMySQLManager().getSeeds().get(new Random().nextInt(AdvancementHunt.getInstance().getMySQLManager().getSeeds().size())) + "";
        }
        this.timeLimit = Integer.parseInt(args[2]);
        this.countdownSeconds = Integer.parseInt(args[3]);
        if(!args[4].equalsIgnoreCase("random")) {
            if(Bukkit.getPlayer(args[4]) != null) {
                this.username = args[4];
            } else {
                AdvancementHunt.getInstance().getMessageManager().sendMessage(player,MessageType.PLAYER_NOT_ONLINE);
                // player.sendMessage(AdvancementHunt.getInstance().getConfigManager().getMessage("Game.Messages.NotOnline"));
                return true;
            }
        } else {
            this.username = null;
        }
        this.glow = Boolean.parseBoolean(args[5]);
        this.compass = Boolean.parseBoolean(args[6]);
        this.distance = Integer.parseInt(args[7]);

        if(Bukkit.getOnlinePlayers().size() >= LobbyState.MIN_PLAYERS) {
            AdvancementHunt.getInstance().getMessageManager().sendMessage(player,MessageType.CREATING_WORLD);
            // player.sendMessage("§cCreating World...");
            AdvancementHunt.getInstance().getUtils().getWorldUtil().worldCreate(AdvancementHunt.getInstance().getWorldName(), World.Environment.NORMAL, this.seed);

            // Fist Make world and load them
            // #createWorld() create world and loads it
            WorldCreator normal_world = new WorldCreator(AdvancementHunt.getInstance().getWorldName());
            normal_world.seed(Long.parseLong(this.seed));
            normal_world.environment(World.Environment.NORMAL);
            normal_world.createWorld();
            AdvancementHunt.getInstance().getMultiverseCore().getMVWorldManager().getMVWorld(AdvancementHunt.getInstance().getWorldName()).setSpawnLocation(Bukkit.getWorld(AdvancementHunt.getInstance().getWorldName()).getSpawnLocation());

            WorldCreator nether_world = new WorldCreator(AdvancementHunt.getInstance().getWorldName() + "_nether");
            nether_world.environment(World.Environment.NETHER);
            nether_world.createWorld();

            WorldCreator end_world = new WorldCreator(AdvancementHunt.getInstance().getWorldName() + "_the_end");
            end_world.environment(World.Environment.THE_END);
            end_world.createWorld();

            AdvancementHunt.getInstance().getTeamManager().setInTeam(Bukkit.getPlayer(username), Team.PLAYER);
            for(Player allPlayers : Bukkit.getOnlinePlayers()) {
                if(AdvancementHunt.getInstance().getTeamManager().getTeams().get(allPlayers) != Team.PLAYER) {
                    AdvancementHunt.getInstance().getTeamManager().setInTeam(allPlayers, Team.HUNTER);
                }
            }
            AdvancementHunt.getInstance().setAdvancement_id(this.advancementId);
            AdvancementHunt.getInstance().setMinutesUntilEnd(this.timeLimit);

            AdvancementHunt.getInstance().setDistance(this.distance);

            AdvancementHunt.getInstance().setGlow(this.glow);
            AdvancementHunt.getInstance().setCompass(this.compass);

            lobbyState.getLobbyTimer().setSeconds(this.countdownSeconds);
            lobbyState.getLobbyTimer().start();
        } else {
            player.sendMessage(AdvancementHunt.getInstance().getConfigManager().getMessageWithReplace("Game.Messages.NotEnoughPlayers", "%count%", String.valueOf((lobbyState.MIN_PLAYERS - Bukkit.getOnlinePlayers().size()))));
        }

        return true;
    }
}
