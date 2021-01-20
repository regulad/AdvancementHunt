package com.regulad.advancementhunt.mysql;

import com.regulad.advancementhunt.AdvancementHunt;
import com.regulad.advancementhunt.config.ConfigManager;

import java.sql.ResultSet;
import java.util.ArrayList;

public class MySQLManager {
    private final AdvancementHunt plugin;
    private final ConfigManager configManager;
    private String table_name;

    public MySQLManager(AdvancementHunt plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
    }

    public ArrayList<String> getAdvancements() {
        if (!configManager.getConfig().getBoolean("Game.MySQL.Use_db")) {
            return (ArrayList<String>) plugin.getAdvancementSeed().getConfig().getStringList("Advancement-ids");
        }
        this.table_name = configManager.getMessage("Game.MySQL.table_prefix") + "advancements";
        ArrayList<String> list = new ArrayList<String>();
        ResultSet resultSet = plugin.getMysql().query("SELECT * FROM " + this.table_name);
        try {
            while (resultSet.next()) {
                list.add(resultSet.getString("advancement_id"));
            }
            return list;
        } catch (Exception ignored) {
        }
        return null;
    }

    public ArrayList<Long> getSeeds() {
        if (!configManager.getConfig().getBoolean("Game.MySQL.Use_db")) {
            return (ArrayList<Long>) plugin.getAdvancementSeed().getConfig().getLongList("World_Seeds");
        }
        this.table_name = configManager.getMessage("Game.MySQL.table_prefix") + "seeds";
        ArrayList<Long> list = new ArrayList<Long>();
        ResultSet resultSet = plugin.getMysql().query("SELECT * FROM " + this.table_name);
        try {
            while (resultSet.next()) {
                list.add(resultSet.getLong("world_seeds"));
            }
            return list;
        } catch (Exception ignored) {
        }
        return null;
    }
}
