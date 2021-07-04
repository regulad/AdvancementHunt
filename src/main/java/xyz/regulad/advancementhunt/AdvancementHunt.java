package xyz.regulad.advancementhunt;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bstats.bukkit.Metrics;
import org.bukkit.advancement.Advancement;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.regulad.advancementhunt.commands.executors.GameendCommand;
import xyz.regulad.advancementhunt.commands.executors.GamestartCommand;
import xyz.regulad.advancementhunt.commands.executors.RegisterAdvancementCommand;
import xyz.regulad.advancementhunt.commands.executors.RegisterSeedCommand;
import xyz.regulad.advancementhunt.commands.tabcompleters.GamestartTabCompleter;
import xyz.regulad.advancementhunt.commands.tabcompleters.RegisterAdvancementTabCompleter;
import xyz.regulad.advancementhunt.commands.tabcompleters.RegisterSeedTabCompleter;
import xyz.regulad.advancementhunt.database.AdvancementManager;
import xyz.regulad.advancementhunt.database.ConnectionType;
import xyz.regulad.advancementhunt.database.PlayerStats;
import xyz.regulad.advancementhunt.database.SeedManager;
import xyz.regulad.advancementhunt.events.gamestate.PostGameStateChangeEvent;
import xyz.regulad.advancementhunt.events.gamestate.PreGameStateChangeEvent;
import xyz.regulad.advancementhunt.exceptions.GameAlreadyStartedException;
import xyz.regulad.advancementhunt.exceptions.GameNotStartedException;
import xyz.regulad.advancementhunt.game.GameEndReason;
import xyz.regulad.advancementhunt.game.states.GameState;
import xyz.regulad.advancementhunt.game.states.IdleState;
import xyz.regulad.advancementhunt.game.states.PlayingState;
import xyz.regulad.advancementhunt.listener.GameStateChangeListener;
import xyz.regulad.advancementhunt.listener.PlayerAdvancementDoneListener;
import xyz.regulad.advancementhunt.listener.PlayerConnectionListener;
import xyz.regulad.advancementhunt.listener.PlayerDeathListener;
import xyz.regulad.advancementhunt.messages.MessageManager;
import xyz.regulad.advancementhunt.placeholders.AdvancementHuntPlaceholders;

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
        this.getServer().getPluginManager().registerEvents(new GameStateChangeListener(this), this);

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
            IdleState currentIdleState = (IdleState) this.currentGameState;

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

    public void endGame(GameEndReason reason) throws GameNotStartedException {
        if (this.currentGameState instanceof PlayingState) {
            IdleState newIdleState = new IdleState(this, reason);
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
