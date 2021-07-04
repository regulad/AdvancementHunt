package xyz.regulad.advancementhunt.database;

import xyz.regulad.advancementhunt.AdvancementHunt;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class SeedManager {
    private final AdvancementHunt plugin;

    public SeedManager(AdvancementHunt plugin) {
        this.plugin = plugin;
    }

    public HashMap<String, Integer> getSeed() throws SQLException { // I don't like returning a HashMap, but we don't have tuples.
        PreparedStatement preparedStatement = null;

        switch (this.plugin.getConnectionType()) {
            case MYSQL:
                preparedStatement = this.plugin.getConnection().prepareStatement("SELECT * FROM " + this.plugin.getConfig().getString("db.prefix") + "worlds ORDER BY RAND() LIMIT 1;");
                break;
            case SQLITE:
                preparedStatement = this.plugin.getConnection().prepareStatement("SELECT * FROM " + this.plugin.getConfig().getString("db.prefix") + "worlds ORDER BY RANDOM() LIMIT 1;");
                break;
        }

        try {
            ResultSet resultSet = preparedStatement.executeQuery();

            HashMap<String, Integer> outputHashmap = new HashMap<>();
            outputHashmap.put(resultSet.getString("seed"), resultSet.getInt("border"));
            return outputHashmap;
        } catch (SQLException ignored) {
            return null;
        }
    }

    public void putSeed(String seed, int border) throws SQLException {
        PreparedStatement preparedStatement = this.plugin.getConnection().prepareStatement("INSERT INTO " + this.plugin.getConfig().getString("db.prefix") + "worlds VALUES(?,?);");
        preparedStatement.setString(1, seed);
        preparedStatement.setInt(2, border);
        preparedStatement.execute();
    }
}
