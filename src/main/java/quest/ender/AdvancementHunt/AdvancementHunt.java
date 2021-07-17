package quest.ender.AdvancementHunt;

import org.bstats.bukkit.Metrics;
import org.bukkit.advancement.Advancement;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import quest.ender.AdvancementHunt.commands.executors.GameendCommand;
import quest.ender.AdvancementHunt.commands.executors.GamestartCommand;
import quest.ender.AdvancementHunt.commands.executors.RegisterAdvancementCommand;
import quest.ender.AdvancementHunt.commands.executors.RegisterSeedCommand;
import quest.ender.AdvancementHunt.commands.tabcompleters.GamestartTabCompleter;
import quest.ender.AdvancementHunt.commands.tabcompleters.RegisterAdvancementTabCompleter;
import quest.ender.AdvancementHunt.commands.tabcompleters.RegisterSeedTabCompleter;
import quest.ender.AdvancementHunt.database.AdvancementManager;
import quest.ender.AdvancementHunt.database.ConnectionType;
import quest.ender.AdvancementHunt.database.SeedManager;
import quest.ender.AdvancementHunt.database.stats.PlayerStats;
import quest.ender.AdvancementHunt.events.PostGameStateChangeEvent;
import quest.ender.AdvancementHunt.events.PreGameStateChangeEvent;
import quest.ender.AdvancementHunt.exceptions.GameAlreadyStartedException;
import quest.ender.AdvancementHunt.exceptions.GameNotStartedException;
import quest.ender.AdvancementHunt.game.GameEndReason;
import quest.ender.AdvancementHunt.game.states.GameState;
import quest.ender.AdvancementHunt.game.states.IdleState;
import quest.ender.AdvancementHunt.game.states.PlayingState;
import quest.ender.AdvancementHunt.listener.*;
import quest.ender.AdvancementHunt.messages.MessageManager;
import quest.ender.AdvancementHunt.placeholders.AdvancementHuntPlaceholders;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;

/**
 * The main class. All API methods should be called via an instance.
 */
public final class AdvancementHunt extends JavaPlugin {
    private final SeedManager seedManager = new SeedManager(this);
    private final AdvancementManager advancementManager = new AdvancementManager(this);
    private final MessageManager messageManager = new MessageManager(this);
    private final Metrics metrics = new Metrics(this, 11903); // If you make a fork of this plugin, you'll likely want to replace this.
    private Connection connection = null;
    private ConnectionType connectionType = null;
    private GameState currentGameState = new IdleState(this, GameEndReason.NONE);
    // Stupid, but placeholder access has forced my hand.

    @Override
    public void onEnable() {
        // pAPI
        new AdvancementHuntPlaceholders(this).register();

        // Setup configuration: this will do nothing if config.yml already exists.
        this.saveDefaultConfig();

        // Database configuration
        switch (this.getConfig().getString("db.type")) {
            case "mysql":
                this.connectionType = ConnectionType.MYSQL;
                break;
            case "sqlite":
                this.connectionType = ConnectionType.SQLITE;
                break;
            default:
                this.getLogger().severe("Invalid database type!");
                this.getServer().getPluginManager().disablePlugin(this);
                return;
        }

        switch (this.connectionType) {
            case MYSQL:
                try {
                    String jdbcUri = "jdbc:mysql://" + this.getConfig().getString("db.host") + ":" + this.getConfig().getInt("db.port") + "/" + this.getConfig().getString("db.name") + this.getConfig().getString("db.options");
                    this.connection = DriverManager.getConnection(jdbcUri, this.getConfig().getString("db.user"), this.getConfig().getString("db.passwd"));
                    this.getLogger().info("Successfully connected to the MySQL database at " + this.getConfig().getString("db.host") + ":" + this.getConfig().getInt("db.port"));
                } catch (SQLException exception) {
                    this.getLogger().severe(exception.getMessage());
                    this.getServer().getPluginManager().disablePlugin(this);
                    return;
                }
                break;
            case SQLITE:
                try {
                    final @NotNull File sqliteFile = new File(this.getDataFolder(), this.getConfig().getString("db.filename"));
                    this.connection = DriverManager.getConnection("jdbc:sqlite:" + sqliteFile.getPath());
                    this.getLogger().info("Successfully connected to the SQLite database at " + sqliteFile.getPath());
                } catch (SQLException exception) {
                    this.getLogger().severe(exception.getMessage());
                    this.getServer().getPluginManager().disablePlugin(this);
                    return;
                }
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
        this.getServer().getPluginManager().registerEvents(new CountdownListener(this), this);
        this.getServer().getPluginManager().registerEvents(new PlayerConnectionListener(this), this);
        this.getServer().getPluginManager().registerEvents(new PlayerAdvancementDoneListener(this), this);
        this.getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);
        this.getServer().getPluginManager().registerEvents(new GameStateChangeListener(this), this);

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

    /**
     * @deprecated
     */
    @Deprecated
    public void startGame(PlayingState newPlayingState) throws GameAlreadyStartedException {
        if (this.currentGameState instanceof IdleState currentIdleState) {
            PreGameStateChangeEvent preGameStateChangeEvent = new PreGameStateChangeEvent(currentIdleState, newPlayingState);
            this.getServer().getPluginManager().callEvent(preGameStateChangeEvent);
            if (!preGameStateChangeEvent.isCancelled()) {
                newPlayingState.start();
                currentIdleState.end();
                this.currentGameState = newPlayingState;
                this.getServer().getPluginManager().callEvent(new PostGameStateChangeEvent(currentIdleState, newPlayingState));
            }
        } else {
            throw new GameAlreadyStartedException("A game has already started.");
        }

    }

    public void startGame(Player fleeingPlayer, ArrayList<Player> huntingPlayers, Advancement goalAdvancement, Instant endTime, String worldSeed, double worldSize) throws GameAlreadyStartedException {
        this.startGame(new PlayingState(this, fleeingPlayer, huntingPlayers, goalAdvancement, endTime, worldSeed, worldSize));
    }

    /**
     * @deprecated
     */
    @Deprecated
    public void endGame(IdleState newIdleState) throws GameNotStartedException {
        if (this.currentGameState instanceof PlayingState) {
            PlayingState currentPlayingState = (PlayingState) this.currentGameState;

            PreGameStateChangeEvent preGameStateChangeEvent = new PreGameStateChangeEvent(currentPlayingState, newIdleState);
            this.getServer().getPluginManager().callEvent(preGameStateChangeEvent);
            if (!preGameStateChangeEvent.isCancelled()) {
                newIdleState.start();
                this.currentGameState.end();
                this.currentGameState = newIdleState;
                this.getServer().getPluginManager().callEvent(new PostGameStateChangeEvent(currentPlayingState, newIdleState));
            }
        } else {
            throw new GameNotStartedException("A game is not ongoing.");
        }
    }

    public void endGame(GameEndReason reason) throws GameNotStartedException {
        this.endGame(new IdleState(this, reason));
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
}
