package quest.ender.AdvancementHunt;

import org.bstats.bukkit.Metrics;
import org.bukkit.advancement.Advancement;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import quest.ender.AdvancementHunt.commands.GameendCommand;
import quest.ender.AdvancementHunt.commands.GamestartCommand;
import quest.ender.AdvancementHunt.commands.RegisterAdvancementCommand;
import quest.ender.AdvancementHunt.commands.RegisterSeedCommand;
import quest.ender.AdvancementHunt.database.AdvancementManager;
import quest.ender.AdvancementHunt.database.ConnectionType;
import quest.ender.AdvancementHunt.database.SeedManager;
import quest.ender.AdvancementHunt.database.stats.PlayerStats;
import quest.ender.AdvancementHunt.events.PostGameStateChangeEvent;
import quest.ender.AdvancementHunt.events.PreGameStateChangeEvent;
import quest.ender.AdvancementHunt.exceptions.GameAlreadyStartedException;
import quest.ender.AdvancementHunt.exceptions.GameNotStartedException;
import quest.ender.AdvancementHunt.game.GameEndReason;
import quest.ender.AdvancementHunt.game.state.GameState;
import quest.ender.AdvancementHunt.game.state.IdleState;
import quest.ender.AdvancementHunt.game.state.PlayingState;
import quest.ender.AdvancementHunt.listener.*;
import quest.ender.AdvancementHunt.messages.MessageManager;
import quest.ender.AdvancementHunt.placeholders.AdvancementHuntPlaceholderExpansion;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * The main class. All API methods should be called via an instance.
 */
public final class AdvancementHunt extends JavaPlugin implements Listener {
    private final @NotNull SeedManager seedManager = new SeedManager(this);
    private final @NotNull AdvancementManager advancementManager = new AdvancementManager(this);
    private final @NotNull MessageManager messageManager = new MessageManager(this);
    private final @NotNull Metrics metrics = new Metrics(this, 11903); // If you make a fork of this plugin, you'll likely want to replace this.
    private @Nullable Connection connection = null;
    private @Nullable ConnectionType connectionType = null;
    private @Nullable GameState currentGameState = new IdleState(this, GameEndReason.NONE);

    private final @NotNull HashMap<@NotNull Player, @NotNull PlayerStats> playerStatsHashMap = new HashMap<>();
    // Stupid, but placeholder access has forced my hand. Possible memory leak, but it won't be a big enough deal to care about.

    @Override
    public void onEnable() {
        // pAPI
        new AdvancementHuntPlaceholderExpansion(this).register();

        // Setup configuration: this will do nothing if config.yml already exists.
        this.saveDefaultConfig();

        // Database configuration
        this.connectionType = switch (this.getConfig().getString("db.type")) {
            case "mysql" -> ConnectionType.MYSQL;
            case "sqlite" -> ConnectionType.SQLITE;
            default -> null;
        };

        if (this.connectionType == null) {
            this.getLogger().severe("Invalid connection type!");
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
            this.connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + this.getConfig().getString("db.prefix") + "stats (uuid VARCHAR(255) UNIQUE, kills INT NOT NULL default 0, deaths INT NOT NULL default 0, losses INT NOT NULL default 0, wins INT NOT NULL default 0, PRIMARY KEY (uuid));").execute();

            // Create advancements table
            this.connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + this.getConfig().getString("db.prefix") + "advancements (advancement VARCHAR(255) UNIQUE, minutes INT NOT NULL, PRIMARY KEY (advancement));").execute();

            // Create seeds table
            this.connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + this.getConfig().getString("db.prefix") + "worlds (seed VARCHAR(255) UNIQUE, border INT NOT NULL default 60000000, PRIMARY KEY (seed));").execute();
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
        this.getServer().getPluginManager().registerEvents(this, this);

        // Register commands
        final @Nullable PluginCommand gamestartCommand = this.getCommand("gamestart");
        final @NotNull GamestartCommand gamestartCommandExecutor = new GamestartCommand(this);
        gamestartCommand.setExecutor(gamestartCommandExecutor);
        gamestartCommand.setTabCompleter(gamestartCommandExecutor);

        final @Nullable PluginCommand registerAdvancementCommand = this.getCommand("registeradvancement");
        final @NotNull RegisterAdvancementCommand registerAdvancementCommandExecutor = new RegisterAdvancementCommand(this);
        registerAdvancementCommand.setExecutor(registerAdvancementCommandExecutor);
        registerAdvancementCommand.setTabCompleter(registerAdvancementCommandExecutor);

        final @Nullable PluginCommand registerSeedCommand = this.getCommand("registerseed");
        final @NotNull RegisterSeedCommand registerSeedCommandExecutor = new RegisterSeedCommand(this);
        registerSeedCommand.setExecutor(registerSeedCommandExecutor);
        registerSeedCommand.setTabCompleter(registerSeedCommandExecutor);

        this.getCommand("gameend").setExecutor(new GameendCommand(this));

        // Setup gamestate
        if (this.currentGameState != null)
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

    @EventHandler
    public void resetPlayerStats(PlayerQuitEvent playerQuitEvent) {
        this.playerStatsHashMap.remove(playerQuitEvent.getPlayer());
    }

    @EventHandler
    public void resetPlayerStats(PlayerDeathEvent playerDeathEvent) {
        final @NotNull Player dead = playerDeathEvent.getEntity();
        final @Nullable Player killer = dead.getKiller();

        if (killer != null) {
            this.playerStatsHashMap.remove(dead);
            this.playerStatsHashMap.remove(killer);
        }
    }

    @EventHandler
    public void resetPlayerStats(PreGameStateChangeEvent preGameStateChangeEvent) {
        this.playerStatsHashMap.clear();
    }

    public @NotNull PlayerStats getPlayerStats(Player player) {
        return this.playerStatsHashMap.computeIfAbsent(player, k -> new PlayerStats(k, this));
    }

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

    @Deprecated
    public void endGame(IdleState newIdleState) throws GameNotStartedException {
        if (this.currentGameState instanceof PlayingState currentPlayingState) {
            final @NotNull PreGameStateChangeEvent preGameStateChangeEvent = new PreGameStateChangeEvent(currentPlayingState, newIdleState);
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


    /**
     * Get the connection to the database.
     *
     * @return The {@link Connection} that the plugin will use to communicate to the SQL server.
     */
    public @Nullable Connection getConnection() {
        return this.connection;
    }

    /**
     * Gets the plugin's {@link SeedManager}.
     *
     * @return The plugin's {@link SeedManager}.
     */
    public @NotNull SeedManager getSeedManager() {
        return this.seedManager;
    }

    /**
     * Gets the plugin's {@link AdvancementManager}.
     *
     * @return The plugin's {@link AdvancementManager}.
     */
    public @NotNull AdvancementManager getAdvancementManager() {
        return this.advancementManager;
    }

    /**
     * Gets the plugin's {@link MessageManager}.
     *
     * @return The plugin's {@link MessageManager}.
     */
    public @NotNull MessageManager getMessageManager() {
        return this.messageManager;
    }

    /**
     * Gets the current {@link GameState}.
     *
     * @return The current {@link GameState}. If the plugin is enabled, it won't be null.
     */
    public @Nullable GameState getCurrentGameState() {
        return this.currentGameState;
    }

    /**
     * Gets the {@link ConnectionType} of the plugin's {@link Connection}.
     *
     * @return The {@link ConnectionType} of the plugin's {@link Connection}. Provided the {@link Connection} obtained from {@link AdvancementHunt#getConnection()} isn't null, this won't be either.
     */
    public @Nullable ConnectionType getConnectionType() {
        return this.connectionType;
    }
}
