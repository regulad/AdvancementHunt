package xyz.regulad.advancementhunt;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bstats.bukkit.Metrics;
import org.bukkit.advancement.Advancement;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.regulad.advancementhunt.commands.GameendCommand;
import xyz.regulad.advancementhunt.commands.GamestartCommand;
import xyz.regulad.advancementhunt.commands.RegisterAdvancementCommand;
import xyz.regulad.advancementhunt.commands.RegisterSeedCommand;
import xyz.regulad.advancementhunt.database.AdvancementManager;
import xyz.regulad.advancementhunt.database.ConnectionType;
import xyz.regulad.advancementhunt.database.PlayerStats;
import xyz.regulad.advancementhunt.database.SeedManager;
import xyz.regulad.advancementhunt.exceptions.GameAlreadyStartedException;
import xyz.regulad.advancementhunt.exceptions.GameNotStartedException;
import xyz.regulad.advancementhunt.gamestate.GameEndReason;
import xyz.regulad.advancementhunt.gamestate.GameState;
import xyz.regulad.advancementhunt.gamestate.IdleState;
import xyz.regulad.advancementhunt.gamestate.PlayingState;
import xyz.regulad.advancementhunt.listener.PlayerAdvancementDoneListener;
import xyz.regulad.advancementhunt.listener.PlayerConnectionListener;
import xyz.regulad.advancementhunt.listener.PlayerDeathListener;
import xyz.regulad.advancementhunt.messages.Message;
import xyz.regulad.advancementhunt.messages.MessageManager;
import xyz.regulad.advancementhunt.tabcompleters.GamestartTabCompleter;
import xyz.regulad.advancementhunt.tabcompleters.RegisterAdvancementTabCompleter;
import xyz.regulad.advancementhunt.tabcompleters.RegisterSeedTabCompleter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;


public final class AdvancementHunt extends JavaPlugin {
    private Connection connection = null;
    private ConnectionType connectionType = null;
    private BukkitAudiences bukkitAudiences = null;

    private GameState currentGameState = new IdleState(this, GameEndReason.NONE);

    private final SeedManager seedManager = new SeedManager(this);
    private final AdvancementManager advancementManager = new AdvancementManager(this);
    private final MessageManager messageManager = new MessageManager(this);

    private final Metrics metrics = new Metrics(this, 11903); // If you make a fork of this plugin, you'll likely want to replace this.
    // Stupid, but placeholder access has forced my hand.

    @Override
    public void onEnable() {
        // pAPI
        new AdvancementHuntPlaceholders(this).register();

        // Setup configuration: this will do nothing if config.yml already exists.
        this.saveDefaultConfig();

        // Database configuration
        if (this.getConfig().getString("db.type").equalsIgnoreCase("mysql")) {
            this.connectionType = ConnectionType.MYSQL;
            try {
                String jdbcUri = "jdbc:mysql://" + this.getConfig().getString("db.host") + ":" + this.getConfig().getInt("db.port") + "/" + this.getConfig().getString("db.name") + this.getConfig().getString("db.options");
                this.connection = DriverManager.getConnection(jdbcUri, this.getConfig().getString("db.user"), this.getConfig().getString("db.passwd"));
                this.getLogger().info("Successfully connected to the MySQL database at " + this.getConfig().getString("db.host") + ":" + this.getConfig().getInt("db.port"));
            } catch (SQLException exception) {
                this.getLogger().severe(exception.getMessage());
                this.getServer().getPluginManager().disablePlugin(this);
                return;
            }
        } else if (this.getConfig().getString("db.type").equalsIgnoreCase("sqlite")) {
            this.connectionType = ConnectionType.SQLITE;
            try {
                this.connection = DriverManager.getConnection("jdbc:sqlite:" + this.getDataFolder().getAbsolutePath() + (System.getProperty("os.name").toLowerCase().contains("win") ? "\\" : "/") + this.getConfig().getString("db.filename"));
                this.getLogger().info("Successfully connected to the SQLite database at " + this.getDataFolder().getAbsolutePath() + (System.getProperty("os.name").toLowerCase().contains("win") ? "\\" : "/") + this.getConfig().getString("db.filename"));
            } catch (SQLException exception) {
                this.getLogger().severe(exception.getMessage());
                this.getServer().getPluginManager().disablePlugin(this);
                return;
            }
        } else {
            this.getLogger().severe("Invalid database type!");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Write tables
        try {
            // Create stats table
            PreparedStatement preparedStatement = this.connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + this.getConfig().getString("db.prefix") + "stats (uuid VARCHAR(255) UNIQUE, kills INT NOT NULL default 0, deaths INT NOT NULL default 0, losses INT NOT NULL default 0, wins INT NOT NULL default 0, PRIMARY KEY (uuid));");
            preparedStatement.execute();

            // Create advancements table
            PreparedStatement preparedStatement2 = this.connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + this.getConfig().getString("db.prefix") + "advancements (advancement VARCHAR(255) UNIQUE, minutes INT NOT NULL, PRIMARY KEY (advancement));");
            preparedStatement2.execute();

            // Create seeds table
            PreparedStatement preparedStatement3 = this.connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + this.getConfig().getString("db.prefix") + "worlds (seed VARCHAR(255) UNIQUE, border INT NOT NULL default 60000000, PRIMARY KEY (seed));");
            preparedStatement3.execute();
        } catch (SQLException exception) {
            this.getLogger().severe(exception.getMessage());
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Register listeners
        this.getServer().getPluginManager().registerEvents(new PlayerConnectionListener(this), this);
        this.getServer().getPluginManager().registerEvents(new PlayerAdvancementDoneListener(this), this);
        this.getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);

        this.bukkitAudiences = BukkitAudiences.create(this);

        // Register commands
        PluginCommand gamestartCommand = this.getCommand("gamestart");
        gamestartCommand.setExecutor(new GamestartCommand(this));
        gamestartCommand.setTabCompleter(new GamestartTabCompleter(this));

        PluginCommand registerAdvancementCommand = this.getCommand("registeradvancement");
        registerAdvancementCommand.setExecutor(new RegisterAdvancementCommand(this));
        registerAdvancementCommand.setTabCompleter(new RegisterAdvancementTabCompleter(this));

        PluginCommand registerSeedCommand = this.getCommand("registerseed");
        registerSeedCommand.setExecutor(new RegisterSeedCommand(this));
        registerSeedCommand.setTabCompleter(new RegisterSeedTabCompleter(this));

        this.getCommand("gameend").setExecutor(new GameendCommand(this));

        // Setup gamestate
        this.currentGameState.start();
    }

    @Override
    public void onDisable() {
        // End the current state
        if (this.currentGameState != null) {
            this.currentGameState.end();
            this.currentGameState = null;
        }

        if (this.bukkitAudiences != null) {
            this.bukkitAudiences.close();
            this.bukkitAudiences = null;
        }

        // Close the SQL connection
        if (this.connection != null) {
            try {
                this.connection.close();
                this.connection = null;
            } catch (SQLException exception) {
                this.getLogger().severe(exception.getMessage());
            }
        }
    }

    public Connection getConnection() {
        return this.connection;
    }

    public PlayerStats getPlayerStats(Player player) {
        return new PlayerStats(player, this);
    }

    public void startGame(Player fleeingPlayer, ArrayList<Player> huntingPlayers, Advancement goalAdvancement, Instant endTime, String worldSeed, double worldSize) throws GameAlreadyStartedException {
        if (this.currentGameState instanceof IdleState) {
            PlayingState newPlayingState = new PlayingState(this, fleeingPlayer, huntingPlayers, goalAdvancement, endTime, worldSeed, worldSize);
            newPlayingState.start();
            this.currentGameState.end();
            this.currentGameState = newPlayingState;
            this.getMessageManager().dispatchMessage(newPlayingState.fleeingPlayer, Message.HUNTED_START);
            for (Player player : newPlayingState.huntingPlayers) {
                this.getMessageManager().dispatchMessage(player, Message.HUNTER_START);
            }
        } else {
            throw new GameAlreadyStartedException("A game has already started.");
        }
    }

    public void endGame(GameEndReason reason) throws GameNotStartedException {
        if (this.currentGameState instanceof PlayingState) {
            PlayingState currentPlayingState = (PlayingState) this.currentGameState;
            IdleState newIdleState = new IdleState(this, reason);
            newIdleState.start();
            switch (reason) {
                case HUNTED_WIN:
                    // Give the runner the win
                    PlayerStats winnerStats = this.getPlayerStats(currentPlayingState.fleeingPlayer);
                    winnerStats.setWins(winnerStats.getWins() + 1);
                    this.getMessageManager().dispatchMessage(currentPlayingState.fleeingPlayer, Message.HUNTED_WIN);
                    // Give the hunters a loss
                    for (Player player : currentPlayingState.huntingPlayers) {
                        PlayerStats hunterStats = this.getPlayerStats(player);
                        hunterStats.setLosses(hunterStats.getLosses() + 1);
                        this.getMessageManager().dispatchMessage(player, Message.HUNTED_WIN);
                    }
                    break;
                case HUNTER_WIN:
                    // Give the runner a loss
                    PlayerStats loserStats = this.getPlayerStats(currentPlayingState.fleeingPlayer);
                    loserStats.setLosses(loserStats.getLosses() + 1);
                    this.getMessageManager().dispatchMessage(currentPlayingState.fleeingPlayer, Message.HUNTER_WIN);
                    // Give the hunters a win
                    for (Player player : currentPlayingState.huntingPlayers) {
                        PlayerStats hunterStats = this.getPlayerStats(player);
                        hunterStats.setWins(hunterStats.getWins() + 1);
                        this.getMessageManager().dispatchMessage(player, Message.HUNTER_WIN);
                    }
                    break;
                case HUNTED_LEAVE:
                    // The hunted left. Punish themmmmmmm!
                    PlayerStats huntedStats = this.getPlayerStats(currentPlayingState.fleeingPlayer);
                    huntedStats.setLosses(huntedStats.getLosses() + 1);
                    this.getMessageManager().dispatchMessage(currentPlayingState.fleeingPlayer, Message.LEFT);
                    for (Player player : currentPlayingState.huntingPlayers) {
                        this.getMessageManager().dispatchMessage(player, Message.LEFT);
                    }
                    break;
                case HUNTER_LEAVE:
                    // All the hunters left. Punish themmmmmmm!
                    this.getMessageManager().dispatchMessage(currentPlayingState.fleeingPlayer, Message.LEFT);
                    for (Player player : currentPlayingState.leftPlayers) {
                        PlayerStats hunterStats = this.getPlayerStats(player);
                        hunterStats.setLosses(hunterStats.getLosses() + 1);
                    }
                    for (Player player : currentPlayingState.huntingPlayers) {
                        this.getMessageManager().dispatchMessage(player, Message.LEFT);
                    }
                    break;
                case TIME_UP:
                    // Time is up.
                    // Give the runner a loss
                    PlayerStats hunterStats1 = this.getPlayerStats(currentPlayingState.fleeingPlayer);
                    hunterStats1.setLosses(hunterStats1.getLosses() + 1);
                    this.getMessageManager().dispatchMessage(currentPlayingState.fleeingPlayer, Message.TIME_UP);
                    // Give the hunters a loss, too
                    for (Player player : currentPlayingState.huntingPlayers) {
                        PlayerStats hunterStats = this.getPlayerStats(player);
                        hunterStats.setLosses(hunterStats.getLosses() + 1);
                        this.getMessageManager().dispatchMessage(player, Message.TIME_UP);
                    }
                    break;
            }
            this.currentGameState.end();
            this.currentGameState = newIdleState;
        } else {
            throw new GameNotStartedException("A game is not ongoing.");
        }
    }

    public SeedManager getSeedManager() {
        return this.seedManager;
    }

    public AdvancementManager getAdvancementManager() {
        return this.advancementManager;
    }

    public GameState getCurrentGameState() {
        return this.currentGameState;
    }

    public ConnectionType getConnectionType() {
        return this.connectionType;
    }

    public MessageManager getMessageManager() {
        return this.messageManager;
    }

    public BukkitAudiences getBukkitAudiences() {
        return this.bukkitAudiences;
    }
}
