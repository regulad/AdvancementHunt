package quest.ender.AdvancementHunt.database;

import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.javatuples.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import quest.ender.AdvancementHunt.AdvancementHunt;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * An interface for pulling advancements from the database.
 */
public class AdvancementManager {
    private final @NotNull AdvancementHunt plugin;

    public AdvancementManager(final @NotNull AdvancementHunt plugin) {
        this.plugin = plugin;
    }

    public @Nullable Pair<@Nullable Advancement, @NotNull Duration> getAdvancement() {
        try {
            final @Nullable PreparedStatement preparedStatement = switch (this.plugin.getConnectionType()) {
                case MYSQL -> this.plugin.getConnection().prepareStatement("SELECT * FROM " + this.plugin.getConfig().getString("db.prefix") + "advancements ORDER BY RAND() LIMIT 1;");
                case SQLITE -> this.plugin.getConnection().prepareStatement("SELECT * FROM " + this.plugin.getConfig().getString("db.prefix") + "advancements ORDER BY RANDOM() LIMIT 1;");
            };

            final @NotNull ResultSet resultSet = preparedStatement.executeQuery();

            if (this.plugin.getConnectionType() == ConnectionType.MYSQL)
                resultSet.next();

            final @Nullable Advancement advancement = this.plugin.getServer().getAdvancement(NamespacedKey.minecraft(resultSet.getString("advancement").replaceFirst("minecraft:", "")));
            final @NotNull Duration duration = Duration.of(resultSet.getInt("minutes"), ChronoUnit.MINUTES);
            return new Pair<>(advancement, duration);
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
            return null;
        }
    }

    public void putAdvancement(final @NotNull Advancement advancement, final @NotNull Duration duration) {
        try {
            PreparedStatement preparedStatement = this.plugin.getConnection().prepareStatement("INSERT INTO " + this.plugin.getConfig().getString("db.prefix") + "advancements VALUES(?,?);");
            preparedStatement.setString(1, advancement.getKey().toString());
            preparedStatement.setInt(2, (int) duration.get(ChronoUnit.MINUTES));
            preparedStatement.execute();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
    }
}
