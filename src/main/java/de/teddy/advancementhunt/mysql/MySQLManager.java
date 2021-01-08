package de.teddy.advancementhunt.mysql;

import de.teddy.advancementhunt.AdvancementHunt;
import de.teddy.advancementhunt.config.ConfigManager;

import java.sql.ResultSet;
import java.util.ArrayList;
public class MySQLManager {
    private final ConfigManager configManager;
    private String table_name;

    public MySQLManager(ConfigManager configManager)
    {
        this.configManager = configManager;
    }


    public ArrayList<String> getAdvancements() {
        if(!AdvancementHunt.getInstance().getConfigManager().getConfig().getBoolean("Game.MySQL.Use_db")) {
            return (ArrayList<String>) AdvancementHunt.getInstance().getAdvancementSeed().getConfig().getStringList("Advancement-ids");
        }
        this.table_name = configManager.getMessage("Game.MySQL.table_prefix") + "advancements";
        ArrayList<String> list = new ArrayList<String>();
        ResultSet resultSet = AdvancementHunt.getInstance().getMysql().query("SELECT * FROM " + this.table_name);
        try {
            while(resultSet.next()) {
                list.add(resultSet.getString("advancement_id"));
            }
            return list;
        } catch (Exception ignored) { }
        return null;
    }

    public ArrayList<Long> getSeeds() {
        if(!AdvancementHunt.getInstance().getConfigManager().getConfig().getBoolean("Game.MySQL.Use_db")) {
            return (ArrayList<Long>) AdvancementHunt.getInstance().getAdvancementSeed().getConfig().getLongList("World_Seeds");
        }
        this.table_name = configManager.getMessage("Game.MySQL.table_prefix") + "seeds";
        ArrayList<Long> list = new ArrayList<Long>();
        ResultSet resultSet = AdvancementHunt.getInstance().getMysql().query("SELECT * FROM " + this.table_name);
        try {
            while(resultSet.next()) {
                list.add(resultSet.getLong("world_seeds"));
            }
            return list;
        } catch (Exception ignored) { }
        return null;
    }
}
