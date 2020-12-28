package de.teddy.advancementhunt.config;

import de.teddy.advancementhunt.AdvancementHunt;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigManager {

    private File data_folder = new File(AdvancementHunt.getInstance().getDataFolder() + "");
    private File file = new File(data_folder,"config.yml");
    private YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

    public ConfigManager() {
        if(file.exists()) { return; }
        try
        {
            file.createNewFile();
        }catch (Exception ez)
        {
            AdvancementHunt.getInstance().getLogger().info("Unable to create Config");
        }
        //getConfig().set("Game.Messages.StartGame", "&cThe game starts in %seconds% seconds");
        //getConfig().set("Game.Messages.StopGame", "&cThe game stops in %seconds% seconds");
        // getConfig().set("Game.Messages.IsFleeingPlayer", "&cThe hunters are nearly behind you...! Make advancement: %id%");
        // getConfig().set("Game.Messages.NotEnoughPlayers", "&cThere are &a%count% &cplayers missing until the start");
        // getConfig().set("Game.Messages.Won", "&c%player% won!");
        // getConfig().set("Game.Messages.TheHunter", "&cHunter: %player%");
        // getConfig().set("Game.Messages.TheFleeingPlayer", "&cThe Fleeing Player: %player%");
        //getConfig().set("Game.Messages.TimeLeft", "&cTime left: %minutes%");
        // getConfig().set("Game.Messages.NotOnline", "&cThis player is not online!");
        // getConfig().set("Game.Messages.GameIsOver", "&cThe game is over!");
        // getConfig().set("Game.Messages.TheHuntersWon", "&cWinner: The Hunters");
        // getConfig().set("Game.Messages.KilledBy", "&cwas killed by %player%");
        // getConfig().set("Game.Messages.Dead", "&c%player% is dead.");

        getConfig().set("Game.Permissions.StartGame", "advancementhunt.start");
        getConfig().set("Game.Permissions.SetLocation", "advancementhunt.set");

        getConfig().set("Game.MySQL.Host", "localhost");
        getConfig().set("Game.MySQL.Port", "3306");
        getConfig().set("Game.MySQL.Database", "advancementhunt");
        getConfig().set("Game.MySQL.Options", "?autoReconnect=true");
        getConfig().set("Game.MySQL.User", "root");
        getConfig().set("Game.MySQL.Password", "passwd");
        getConfig().set("Game.MySQL.Table_Name", "mysupertable");
        getConfig().set("Game.MySQL.Use_db",true);
        getConfig().set("Game.Extra.Hub_Server","myhubservername");

        getConfig().set("Game.Extra.WorldName", "TheWorld");
        try {
            getConfig().save(getFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getMessageWithReplace(String configString, String replaceString, String replaceWith) {
        String message = getConfig().getString(configString);
        message = message.replace(replaceString, replaceWith);
        message = ChatColor.translateAlternateColorCodes('&', message);
        return message;
    }

    public String getMessage(String configString) {
        String message = getConfig().getString(configString);
        message = ChatColor.translateAlternateColorCodes('&', message);
        return message;
    }

    public File getFile() { return file; }

    public YamlConfiguration getConfig() { return config; }
}
