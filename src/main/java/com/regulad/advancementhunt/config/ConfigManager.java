package com.regulad.advancementhunt.config;

import com.regulad.advancementhunt.AdvancementHunt;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigManager {

    private final File data_folder = new File(AdvancementHunt.getInstance().getDataFolder() + "");
    private final File file = new File(data_folder, "config.yml");
    private final YamlConfiguration config;

    public ConfigManager() {
        if (file.exists()) {
            config = YamlConfiguration.loadConfiguration(file);
            return;
        }

        config = YamlConfiguration.loadConfiguration(file);

        getConfig().set("Game.Permissions.StartGame", "advancementhunt.command.start");
       getConfig().set("Game.Permissions.SetLocation", "advancementhunt.command.set");

        getConfig().set("Game.MySQL.Host", "localhost");
        getConfig().set("Game.MySQL.Port", "3306");
        getConfig().set("Game.MySQL.Database", "advancementhunt");
        getConfig().set("Game.MySQL.Options", "?autoReconnect=true");
        getConfig().set("Game.MySQL.User", "root");
        getConfig().set("Game.MySQL.Password", "'passwd'");
        getConfig().set("Game.MySQL.table_prefix", "ah_");
        getConfig().set("Game.MySQL.Use_db", false);
        getConfig().set("Game.Extra.Hub_Server", "hub");
        getConfig().set("Game.Extra.WorldName", "advancementhunt");

        try {
            getConfig().save(getFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getMessageWithReplace(String configString, String replaceString, String replaceWith) {
        String message = getConfig().getString(configString);
        message = message.replace(replaceString, replaceWith);
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public String getMessage(String configString) {
        return ChatColor.translateAlternateColorCodes('&', getConfig().getString(configString));
    }

    public File getFile() {
        return file;
    }

    public YamlConfiguration getConfig() {
        return config;
    }
}

//package com.regulad.advancementhunt.config;
//
//import com.regulad.advancementhunt.AdvancementHunt;
//
//public class ConfigManager extends AbstractFile {
//
//    public ConfigManager() {
//        super(AdvancementHunt.getInstance(), "config.yml", "", true);
//    }
//}
