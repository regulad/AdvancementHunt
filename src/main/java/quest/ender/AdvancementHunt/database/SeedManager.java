package quest.ender.AdvancementHunt.database;

import org.javatuples.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import quest.ender.AdvancementHunt.AdvancementHunt;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * An interface for pulling seeds from the database.
 */
public class SeedManager {
    private final AdvancementHunt plugin;

    public SeedManager(AdvancementHunt plugin) {
        this.plugin = plugin;
    }

    public @Nullable Pair<@NotNull String, Integer> getSeed() {
        try {
            @Nullable PreparedStatement preparedStatement = null; // Even though this switch is exhaustive, Intellij insists it isn't.
            switch (this.plugin.getConnectionType()) {
                case MYSQL:
                    preparedStatement = this.plugin.getConnection().prepareStatement("SELECT * FROM " + this.plugin.getConfig().getString("db.prefix") + "worlds ORDER BY RAND() LIMIT 1;");
                    break;
                case SQLITE:
                    preparedStatement = this.plugin.getConnection().prepareStatement("SELECT * FROM " + this.plugin.getConfig().getString("db.prefix") + "worlds ORDER BY RANDOM() LIMIT 1;");
                    break;
            }

            final @NotNull ResultSet resultSet = preparedStatement.executeQuery();

            if (this.plugin.getConnectionType() == ConnectionType.MYSQL) {
                resultSet.next();
            }

            return new Pair<>(resultSet.getString("seed"), resultSet.getInt("border"));
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
            return null;
        }
    }

    public void putSeed(String seed, int border) {
        try {
            PreparedStatement preparedStatement = this.plugin.getConnection().prepareStatement("INSERT INTO " + this.plugin.getConfig().getString("db.prefix") + "worlds VALUES(?,?);");
            preparedStatement.setString(1, seed);
            preparedStatement.setInt(2, border);
            preparedStatement.execute();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
    }
}
