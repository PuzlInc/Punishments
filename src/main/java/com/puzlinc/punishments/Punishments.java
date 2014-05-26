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

import com.puzlinc.punishments.command.CommandHandler;
import com.puzlinc.punishments.listener.PlayerListener;
import org.bukkit.plugin.java.JavaPlugin;

public class Punishments extends JavaPlugin {

    private Configuration config;
    private Database database;
    private PunishmentManager manager;
    CommandHandler handler;

    public void onEnable() {
        config = new Configuration(this);
        database = new Database(this, config);
        database.connect();
        manager = new PunishmentManager(this, config, database);
        handler = new CommandHandler(manager);

        getCommand("kick").setExecutor(handler);
        getCommand("ban").setExecutor(handler);
        getCommand("mute").setExecutor(handler);
        getCommand("unban").setExecutor(handler);
        getCommand("unmute").setExecutor(handler);
        getCommand("history").setExecutor(handler);

        getServer().getPluginManager().registerEvents(new PlayerListener(manager), this);
    }

    public void onDisable() {
        manager.forceUpdate();
        database.disconnect();
        manager.stop();
    }
}
