package xyz.regulad.AdvancementHunt.database;

import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.javatuples.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.regulad.AdvancementHunt.AdvancementHunt;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * An interface for pulling advancements from the database.
 */
public class AdvancementManager {
    private final @NotNull AdvancementHunt plugin;

    public AdvancementManager(AdvancementHunt plugin) {
        this.plugin = plugin;
    }

    public @Nullable Pair<@Nullable Advancement, Integer> getAdvancement() {
        try {
            @Nullable PreparedStatement preparedStatement = null; // Even though this switch is exhaustive, Intellij insists it isn't.
            switch (this.plugin.getConnectionType()) {
                case MYSQL:
                    preparedStatement = this.plugin.getConnection().prepareStatement("SELECT * FROM " + this.plugin.getConfig().getString("db.prefix") + "advancements ORDER BY RAND() LIMIT 1;");
                    break;
                case SQLITE:
                    preparedStatement = this.plugin.getConnection().prepareStatement("SELECT * FROM " + this.plugin.getConfig().getString("db.prefix") + "advancements ORDER BY RANDOM() LIMIT 1;");
                    break;
            }

            final @NotNull ResultSet resultSet = preparedStatement.executeQuery();

            if (this.plugin.getConnectionType() == ConnectionType.MYSQL) {
                resultSet.next();
            }


            final @Nullable Advancement advancement = this.plugin.getServer().getAdvancement(NamespacedKey.minecraft(resultSet.getString("advancement").replaceFirst("minecraft:", "")));
            return new Pair<>(advancement, resultSet.getInt("minutes"));
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
            return null;
        }
    }

    public void putAdvancement(Advancement advancement, int time) {
        try {
            PreparedStatement preparedStatement = this.plugin.getConnection().prepareStatement("INSERT INTO " + this.plugin.getConfig().getString("db.prefix") + "advancements VALUES(?,?);");
            preparedStatement.setString(1, advancement.getKey().toString());
            preparedStatement.setInt(2, time);
            preparedStatement.execute();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
    }
}
