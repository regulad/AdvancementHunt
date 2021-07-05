package xyz.regulad.AdvancementHunt.database;

import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import xyz.regulad.AdvancementHunt.AdvancementHunt;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class AdvancementManager {
    private final AdvancementHunt plugin;

    public AdvancementManager(AdvancementHunt plugin) {
        this.plugin = plugin;
    }

    public HashMap<Advancement, Integer> getAdvancement() throws SQLException { // I don't like returning a HashMap, but we don't have tuples.
        PreparedStatement preparedStatement = null;

        switch (this.plugin.getConnectionType()) {
            case MYSQL:
                preparedStatement = this.plugin.getConnection().prepareStatement("SELECT * FROM " + this.plugin.getConfig().getString("db.prefix") + "advancements ORDER BY RAND() LIMIT 1;");
                break;
            case SQLITE:
                preparedStatement = this.plugin.getConnection().prepareStatement("SELECT * FROM " + this.plugin.getConfig().getString("db.prefix") + "advancements ORDER BY RANDOM() LIMIT 1;");
                break;
        }

        try {
            ResultSet resultSet = preparedStatement.executeQuery();

            HashMap<Advancement, Integer> outputHashmap = new HashMap<>();
            outputHashmap.put(this.plugin.getServer().getAdvancement(NamespacedKey.minecraft(resultSet.getString("advancement"))), resultSet.getInt("minutes"));
            return outputHashmap;
        } catch (SQLException ignored) {
            return null;
        }
    }

    public void putAdvancement(Advancement advancement, int time) throws SQLException {
        PreparedStatement preparedStatement = this.plugin.getConnection().prepareStatement("INSERT INTO " + this.plugin.getConfig().getString("db.prefix") + "advancements VALUES(?,?);");
        preparedStatement.setString(1, advancement.toString());
        preparedStatement.setInt(2, time);
        preparedStatement.execute();
    }
}
