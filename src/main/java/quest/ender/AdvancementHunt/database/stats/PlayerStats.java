package quest.ender.AdvancementHunt.database.stats;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import quest.ender.AdvancementHunt.AdvancementHunt;
import quest.ender.AdvancementHunt.database.ConnectionType;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class PlayerStats { // This isn't the best thing ever, but it's considerably better than the old system.
    private final AdvancementHunt plugin;
    private final Player player;

    public PlayerStats(Player player, AdvancementHunt plugin) {
        this.player = player;
        this.plugin = plugin;
    }

    private @Nullable ResultSet cachedResultSet = null;
    private @Nullable Instant expiresAt = null;

    private boolean rowExists() {
        try {
            PreparedStatement preparedStatement = this.plugin.getConnection().prepareStatement("SELECT * FROM " + this.plugin.getConfig().getString("db.prefix") + "stats WHERE uuid = ?;");

            preparedStatement.setString(1, this.player.getUniqueId().toString());

            final ResultSet resultSet = preparedStatement.executeQuery();

            if (this.plugin.getConnectionType() == ConnectionType.MYSQL) resultSet.next();

            return resultSet.getString("uuid") != null;
        } catch (SQLException ignored) {
            return false;
        }
    }

    public void updateColumn(StatsColumn column, int value) {
        try {
            if (!this.rowExists()) {
                final PreparedStatement preparedStatement = this.plugin.getConnection().prepareStatement("INSERT INTO " + this.plugin.getConfig().getString("db.prefix") + "stats(uuid) VALUES(?);");
                preparedStatement.setString(1, this.player.getUniqueId().toString());
                preparedStatement.execute();
            }

            PreparedStatement preparedStatement = this.plugin.getConnection().prepareStatement("UPDATE " + this.plugin.getConfig().getString("db.prefix") + "stats SET " + column.getColumnTitle() + " = ? WHERE uuid = ?;");

            preparedStatement.setInt(1, value);
            preparedStatement.setString(2, this.player.getUniqueId().toString());

            preparedStatement.execute();

            this.cachedResultSet = null;
            this.expiresAt = null;
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
    }

    public int getColumn(StatsColumn statsColumn) {
        try {
            if (this.cachedResultSet == null || this.expiresAt == null || this.expiresAt.isBefore(Instant.now())) {
                PreparedStatement preparedStatement = this.plugin.getConnection().prepareStatement("SELECT * FROM " + this.plugin.getConfig().getString("db.prefix") + "stats WHERE uuid = ?;");

                preparedStatement.setString(1, this.player.getUniqueId().toString());

                this.cachedResultSet = preparedStatement.executeQuery();
                this.expiresAt = Instant.now().plus(10L, ChronoUnit.MINUTES);

                if (this.plugin.getConnectionType() == ConnectionType.MYSQL)
                    this.cachedResultSet.next(); // Implementation weirdness.
            }

            return this.cachedResultSet.getInt(statsColumn.getColumnTitle());
        } catch (SQLException sqlException) {
            if (!sqlException.getMessage().equals("Illegal operation on empty result set."))
                sqlException.printStackTrace();
            return 0;
        }
    }
}
