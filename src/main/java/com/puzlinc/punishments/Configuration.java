/*
* Punishments
* Copyright (C) 2014 Puzl Inc.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.puzlinc.punishments;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class Configuration {

    private static final String CONFIG_FILE_NAME = "config.yml";

    private File configFile;
    private YamlConfiguration config;

    private String mysqlHostname;
    private String mysqlPort;
    private String mysqlUsername;
    private String mysqlPassword;
    private String mysqlDatabase;
    private String mysqlTable;

    private String serverName;
    private String syncInterval;

    public Configuration(Punishments plugin) {
        configFile = new File(plugin.getDataFolder(), CONFIG_FILE_NAME);

        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);

        load();
    }

    private void load() {
        mysqlHostname = config.getString("mysql.hostname");
        mysqlPort = config.getString("mysql.port");
        mysqlUsername = config.getString("mysql.username");
        mysqlPassword = config.getString("mysql.password");
        mysqlDatabase = config.getString("mysql.database");
        mysqlTable = config.getString("mysql.table");

        serverName = config.getString("server-name");
        syncInterval = config.getString("sync-interval");
    }

    public String getMysqlHostname() {
        return mysqlHostname;
    }

    public String getMysqlPort() {
        return mysqlPort;
    }

    public String getMysqlUsername() {
        return mysqlUsername;
    }

    public String getMysqlPassword() {
        return mysqlPassword;
    }

    public String getMysqlDatabase() {
        return mysqlDatabase;
    }

    public String getMysqlTable() {
        return mysqlTable;
    }

    public String getServerName() {
        return serverName;
    }

    public long getSyncInterval() {
        return Util.lengthToSeconds(syncInterval);
    }
}
