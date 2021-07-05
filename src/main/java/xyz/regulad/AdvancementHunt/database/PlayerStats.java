package xyz.regulad.AdvancementHunt.database;

import org.bukkit.entity.Player;
import xyz.regulad.AdvancementHunt.AdvancementHunt;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PlayerStats { // This isn't the best thing ever, but it's considerably better than the old system.
    private final AdvancementHunt plugin;
    private final Player player;

    public PlayerStats(Player player, AdvancementHunt plugin) {
        this.player = player;
        this.plugin = plugin;
    }

    private ResultSet getRow() throws SQLException {
        PreparedStatement preparedStatement = this.plugin.getConnection().prepareStatement("SELECT * FROM " + this.plugin.getConfig().getString("db.prefix") + "stats WHERE uuid = ?;");

        preparedStatement.setString(1, this.player.getUniqueId().toString());

        try {
            return preparedStatement.executeQuery();
        } catch (SQLException exception) {
            this.writeRow(0, 0, 0, 0); // Not that messy. This will be raised.
            return preparedStatement.executeQuery(); // Shouldn't cause the issue, again.
        }
    }

    private void writeRow(int kills, int deaths, int losses, int wins) throws SQLException { // Stupid, like pretty bad.
        PreparedStatement preparedStatement = null;
        switch (this.plugin.getConnectionType()) {
            case MYSQL:
                preparedStatement = this.plugin.getConnection().prepareStatement("DELETE IGNORE FROM " + this.plugin.getConfig().getString("db.prefix") + "stats WHERE uuid = ?; INSERT INTO " + this.plugin.getConfig().getString("db.prefix") + "stats VALUES(?,?,?,?,?);");
                preparedStatement.setString(1, this.player.getUniqueId().toString()); // uuid
                preparedStatement.setString(2, this.player.getUniqueId().toString()); // uuid
                preparedStatement.setInt(3, kills); // kills
                preparedStatement.setInt(4, deaths); // deaths
                preparedStatement.setInt(5, losses); // losses
                preparedStatement.setInt(6, wins); // wins
                break;
            case SQLITE:
                preparedStatement = this.plugin.getConnection().prepareStatement("INSERT OR REPLACE INTO " + this.plugin.getConfig().getString("db.prefix") + "stats VALUES(?,?,?,?,?);");
                preparedStatement.setString(1, this.player.getUniqueId().toString()); // uuid
                preparedStatement.setInt(2, kills); // kills
                preparedStatement.setInt(3, deaths); // deaths
                preparedStatement.setInt(4, losses); // losses
                preparedStatement.setInt(5, wins); // wins
                break;
        }

        preparedStatement.execute();

        this.getRow();
    }

    public int getKills() {
        try {
            return this.getRow().getInt("kills");
        } catch (SQLException exception) {
            this.plugin.getLogger().severe(exception.getMessage());
            return 0;
        }
    }

    public void setKills(int kills) {
        try {
            this.writeRow(kills, this.getDeaths(), this.getLosses(), this.getWins());
        } catch (SQLException exception) {
            this.plugin.getLogger().severe(exception.getMessage());
        }
    }

    public int getDeaths() {
        try {
            return this.getRow().getInt("deaths");
        } catch (SQLException exception) {
            this.plugin.getLogger().severe(exception.getMessage());
            return 0;
        }
    }

    public void setDeaths(int deaths) {
        try {
            this.writeRow(this.getKills(), deaths, this.getLosses(), this.getWins());
        } catch (SQLException exception) {
            this.plugin.getLogger().severe(exception.getMessage());
        }
    }

    public int getLosses() {
        try {
            return this.getRow().getInt("losses");
        } catch (SQLException exception) {
            this.plugin.getLogger().severe(exception.getMessage());
            return 0;
        }
    }

    public void setLosses(int losses) {
        try {
            this.writeRow(this.getKills(), this.getDeaths(), losses, this.getWins());
        } catch (SQLException exception) {
            this.plugin.getLogger().severe(exception.getMessage());
        }
    }

    public int getWins() {
        try {
            return this.getRow().getInt("wins");
        } catch (SQLException exception) {
            this.plugin.getLogger().severe(exception.getMessage());
            return 0;
        }
    }

    public void setWins(int wins) {
        try {
            this.writeRow(this.getKills(), this.getDeaths(), this.getLosses(), wins);
        } catch (SQLException exception) {
            this.plugin.getLogger().severe(exception.getMessage());
        }
    }
}
