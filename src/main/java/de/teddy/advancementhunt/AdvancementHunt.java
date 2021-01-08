package de.teddy.advancementhunt;

import com.onarandombox.MultiverseCore.MultiverseCore;
import de.teddy.advancementhunt.actionbar.ActionbarManager;
import de.teddy.advancementhunt.commands.*;
import de.teddy.advancementhunt.config.AdvancementSeed;
import de.teddy.advancementhunt.config.ConfigManager;
import de.teddy.advancementhunt.config.Messages;
import de.teddy.advancementhunt.gamestates.GameState;
import de.teddy.advancementhunt.gamestates.GameStateManager;
import de.teddy.advancementhunt.listener.PlayerAdvancementDoneListener;
import de.teddy.advancementhunt.listener.PlayerConnectionListener;
import de.teddy.advancementhunt.listener.PlayerDeathListener;
import de.teddy.advancementhunt.message.MessageManager;
import de.teddy.advancementhunt.mysql.MySQL;
import de.teddy.advancementhunt.mysql.MySQLManager;
import de.teddy.advancementhunt.permissions.PermissionManager;
import de.teddy.advancementhunt.placeholder.SpigotExpansion;
import de.teddy.advancementhunt.teams.TeamManager;
import de.teddy.advancementhunt.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;


public final class AdvancementHunt extends JavaPlugin {

    private static AdvancementHunt instance;

    private String worldName = null;
    private int minutesUntilEnd;
    private String advancement_id = null;
    private int distance;
    private boolean glow;
    private boolean compass;

    private Location compassLoc;

    private MySQL mysql;
    private final Utils utils = new Utils();
    private GameStateManager gameStateManager;
    private final PermissionManager permissionManager = new PermissionManager();
    private ConfigManager configManager;
    private MySQLManager mySQLManager;
    private final TeamManager teamManager = new TeamManager();
    private ActionbarManager actionbarManager;
    private MessageManager messageManager;
    private AdvancementSeed advancementSeed;

    @Override
    public void onEnable() {
        instance = this;

        actionbarManager = new ActionbarManager(instance);
        configManager = new ConfigManager();
        mySQLManager = new MySQLManager(configManager);

        Messages messages = new Messages();

        worldName = configManager.getMessage("Game.Extra.WorldName");

        gameStateManager = new GameStateManager(instance);
        gameStateManager.setGameState(GameState.LOBBY_STATE);

        messageManager = new MessageManager(messages);

        this.initMySQL();

        new SpigotExpansion().register();

        this.register();

        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
    }

    private void initMySQL() {
        if(!configManager.getConfig().getBoolean("Game.MySQL.Use_db")) {
            advancementSeed = new AdvancementSeed();
            return;
        }

       ConfigManager configManager = instance.getConfigManager();

        try {
            mysql = new MySQL(configManager.getMessage("Game.MySQL.Host"),Integer.parseInt(configManager.getMessage("Game.MySQL.Port")), configManager.getMessage("Game.MySQL.Database"), configManager.getMessage("Game.MySQL.User"), configManager.getMessage("Game.MySQL.Password"));
        } catch (Exception e) {
            System.out.println("No connection (mysql)!");
        }
    }

    private void register() {
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new PlayerConnectionListener(), this);
        pluginManager.registerEvents(new PlayerAdvancementDoneListener(instance), this);
        pluginManager.registerEvents(new PlayerDeathListener(), this);

        this.getCommand("set").setExecutor(new SetCommand(instance));
        this.getCommand("gamestart").setExecutor(new GamestartCommand(instance));
        this.getCommand("debug").setExecutor(new DebugCommand(instance));

        // set Tab compleaters
        getCommand("gamestart").setTabCompleter(new GameStartTabCompleter());
    }


    /**
     * Get the {@link MultiverseCore} instance.
     *
     * @return the multiverse core instance.
     */
    public MultiverseCore getMultiverseCore() {
        Plugin plugin = getServer().getPluginManager().getPlugin("Multiverse-Core");

        if (plugin instanceof MultiverseCore) {
            return (MultiverseCore) plugin;
        }

        throw new RuntimeException("MultiVerse not found!");
    }

    public static AdvancementHunt getInstance() { return instance; }

    /**
     * Get the {@link MessageManager} instance.
     *
     * @return the message manager
     */
    @NotNull
    public MessageManager getMessageManager()
    {
        return messageManager;
    }

    /**
     * Get the {@link AdvancementSeed} seed.
     *
     * @return the world seed.
     */
    @NotNull
    public AdvancementSeed getAdvancementSeed()
    {
        return advancementSeed;
    }

    /**
     * Get the {@link Utils} instance.
     *
     * @return the utilities instance.
     */
    @NotNull
    public Utils getUtils() { return utils; }

    public GameStateManager getGameStateManager() { return gameStateManager; }

    public String getPrefix() {
        return "§cAdvancementHunt §8- §r"; }

    public String getWorldName() { return worldName; }

    /**
     * Get the {@link PermissionManager} instance.
     *
     * @return the permission manager
     */
    @NotNull
    public PermissionManager getPermissionManager() { return permissionManager; }

    /**
     * Get the {@link MySQL} instance.
     *
     * @return the mysql instance.
     */
    @NotNull
    public MySQL getMysql() { return mysql; }

    /**
     * Get the {@link ConfigManager} instance.
     *
     * @return the config manager
     */
    @NotNull
    public ConfigManager getConfigManager() { return configManager; }

    /**
     * Get the {@link MySQLManager} instance.
     *
     * @return the mysql manager
     */
    @NotNull
    public MySQLManager getMySQLManager() { return mySQLManager; }

    public String getAdvancement_id() { return advancement_id; }

    public void setAdvancement_id(String advancement_id) { this.advancement_id = advancement_id; }

    /**
     * Get the {@link TeamManager} instance.
     *
     * @return the team manager
     */
    @NotNull
    public TeamManager getTeamManager() { return teamManager; }

    /**
     * Get the {@link ActionbarManager} instance.
     *
     * @return the action bar manager
     */
    @NotNull
    public ActionbarManager getActionbarManager() { return actionbarManager; }

    public int getMinutesUntilEnd() { return minutesUntilEnd; }

    public void setMinutesUntilEnd(int minutesUntilEnd) { this.minutesUntilEnd = minutesUntilEnd; }

    public int getDistance() { return distance; }

    public void setDistance(int distance) { this.distance = distance; }

    public boolean isGlow() { return glow; }

    public boolean isCompass() { return compass; }

    public void setGlow(boolean glow) { this.glow = glow; }

    public void setCompass(boolean compass) { this.compass = compass; }

    /**
     * Get the {@link Location} location.
     *
     * @return the compass's location.
     */
    @NotNull
    public Location getCompassLoc() { return compassLoc; }

    public void setCompassLoc(Location compassLoc) { this.compassLoc = compassLoc; }
}
