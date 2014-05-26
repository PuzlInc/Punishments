package com.puzlinc.punishments;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;

public class Database {

    private Punishments plugin;
    private Configuration config;

    private Connection connection;

    public Database(Punishments plugin, Configuration config) {
        this.plugin = plugin;
        this.config = config;
    }

    public void connect() {
        String url = "jdbc:mysql://"
                + config.getMysqlHostname()
                + ":"
                + config.getMysqlPort()
                + "/"
                + config.getMysqlDatabase();

        try {
            connection = DriverManager.getConnection(url, config.getMysqlUsername(), config.getMysqlPassword());

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error connecting to the database", e);
        }
    }

    public void disconnect() {
        try {
            connection.close();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error disconnecting from the database", e);
        }
    }

    public Connection getConnection() {
        return connection;
    }
}
