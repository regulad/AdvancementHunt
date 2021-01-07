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


public final class AdvancementHunt extends JavaPlugin {

    private String prefix = "§cAdvancementHunt §8- §r";
    private String worldName = null;
    private int minutesUntilEnd;
    private String advancement_id = null;
    private int distance;
    private boolean glow;
    private boolean compass;
    private Messages messages;

    private Location compassLoc;


    private static AdvancementHunt instance;

    private MySQL mysql;
    private Utils utils = new Utils();
    private GameStateManager gameStateManager = new GameStateManager();
    private PermissionManager permissionManager = new PermissionManager();
    private ConfigManager configManager;
    private MySQLManager mySQLManager;
    private TeamManager teamManager = new TeamManager();
    private ActionbarManager actionbarManager = new ActionbarManager();
    private MessageManager messageManager;
    private AdvancementSeed advancementSeed;

    @Override
    public void onEnable() {
        instance = this;
        configManager = new ConfigManager();
        mySQLManager = new MySQLManager(configManager);
        messages = new Messages();

        worldName = getConfigManager().getMessage("Game.Extra.WorldName");

        gameStateManager = new GameStateManager();
        gameStateManager.setGameState(GameState.LOBBY_STATE);

        messageManager = new MessageManager(messages);
        this.initMySQL();

        new SpigotExpansion().register();

        this.register();

        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
    }

    private void initMySQL() {
        if(!AdvancementHunt.getInstance().getConfigManager().getConfig().getBoolean("Game.MySQL.Use_db")) {
            advancementSeed = new AdvancementSeed();
            return;
        }
       ConfigManager configManager = AdvancementHunt.getInstance().getConfigManager();
        try {
            mysql = new MySQL(configManager.getMessage("Game.MySQL.Host"),Integer.parseInt(configManager.getMessage("Game.MySQL.Port")), configManager.getMessage("Game.MySQL.Database"), configManager.getMessage("Game.MySQL.User"), configManager.getMessage("Game.MySQL.Password"));
        } catch (Exception e) {
            System.out.println("No connection (mysql)!");
        }
    }

    private void register() {
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new PlayerConnectionListener(), this);
        pluginManager.registerEvents(new PlayerAdvancementDoneListener(), this);
        pluginManager.registerEvents(new PlayerDeathListener(), this);

        this.getCommand("set").setExecutor(new SetCommand());
        this.getCommand("gamestart").setExecutor(new GamestartCommand());
        this.getCommand("debug").setExecutor(new DebugCommand());

        // set Tab compleaters
        getCommand("gamestart").setTabCompleter(new GameStartTabCompleter());
    }


    public MultiverseCore getMultiverseCore() {
        /**
         * Hmm its good to remove the Exception
         */
        Plugin plugin = getServer().getPluginManager().getPlugin("Multiverse-Core");

        if (plugin instanceof MultiverseCore) {
            return (MultiverseCore) plugin;
        }

        throw new RuntimeException("MultiVerse not found!");
    }

    public static AdvancementHunt getInstance() { return instance; }

    public MessageManager getMessageManager()
    {
        return messageManager;
    }

    public AdvancementSeed getAdvancementSeed()
    {
        return advancementSeed;
    }
    public Utils getUtils() { return utils; }

    public GameStateManager getGameStateManager() { return gameStateManager; }

    public String getPrefix() { return prefix; }

    public String getWorldName() { return worldName; }

    public PermissionManager getPermissionManager() { return permissionManager; }

    public MySQL getMysql() { return mysql; }

    public ConfigManager getConfigManager() { return configManager; }

    public MySQLManager getMySQLManager() { return mySQLManager; }

    public String getAdvancement_id() { return advancement_id; }

    public void setAdvancement_id(String advancement_id) { this.advancement_id = advancement_id; }

    public TeamManager getTeamManager() { return teamManager; }

    public ActionbarManager getActionbarManager() { return actionbarManager; }

    public int getMinutesUntilEnd() { return minutesUntilEnd; }

    public void setMinutesUntilEnd(int minutesUntilEnd) { this.minutesUntilEnd = minutesUntilEnd; }

    public int getDistance() { return distance; }

    public void setDistance(int distance) { this.distance = distance; }

    public boolean isGlow() { return glow; }

    public boolean isCompass() { return compass; }

    public void setGlow(boolean glow) { this.glow = glow; }

    public void setCompass(boolean compass) { this.compass = compass; }

    public Location getCompassLoc() { return compassLoc; }

    public void setCompassLoc(Location compassLoc) { this.compassLoc = compassLoc; }
}
