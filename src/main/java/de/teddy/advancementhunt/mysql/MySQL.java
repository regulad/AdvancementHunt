package de.teddy.advancementhunt.mysql;

import de.teddy.advancementhunt.AdvancementHunt;

import java.sql.*;
import java.util.ArrayList;

public class MySQL {

    private String host = "";
    private int port;
    private String database = "";
    private String user = "";
    private String password = "";

    private Connection connection;

    public MySQL(String host, int port, String database,String user, String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.user = user;
        this.password = password;
        connect();
    }

    private void connect() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database + AdvancementHunt.getInstance().getConfigManager().getMessage("Game.MySQL.Options"), this.user, this.password);
            System.out.println("[AdvancementHunt] MySQL DB connected");

            setUpTable();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void setUpTable() throws Exception
    {
        AdvancementHunt.getInstance().getLogger().info("Setting up player stat table....");
        Statement statement = connection.createStatement();

        String sql = "CREATE TABLE IF NOT EXISTS " + AdvancementHunt.getInstance().getConfigManager().getConfig().getString("Game.MySQL.table_prefix") + "player_stat"
                + "  (uuid             VARCHAR(255) UNIQUE,"
                + "   playername       VARCHAR(255) NOT NULL default '',"
                + "   kills            INT NOT NULL default 0,"
                + "   deaths           INT NOT NULL default 0,"
                + "   Losses           INT NOT NULL default 0,"
                + "   Wins             INT NOT NULL default 0,"
                + "   PRIMARY KEY  (uuid));";
        statement.execute(sql);
    }
    public void close() {
        if(connection != null) {
            try {
                connection.close();
                System.out.println("[AdvancementHunt] MySQL DB disconnected");
            } catch (SQLException e) {
                System.out.println("[AdvancementHunt] MySQL exception on disconnect!");
                System.out.println(e.getMessage());
            }
        }
    }

    public void update(String query) {
        try {
            Statement statement = this.connection.createStatement();
            statement.executeUpdate(query);
            statement.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public Connection getConnection()
    {
        return connection;
    }
    public ResultSet query(String query) {
        ResultSet resultSet = null;

        try {
            Statement statement = this.connection.createStatement();
            resultSet = statement.executeQuery(query);
        } catch (SQLException e) {
            this.connect();
            System.out.println(e.getMessage());
        }
        return resultSet;
    }

}
