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

import org.bukkit.Bukkit;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class PunishmentManager {

    public static final int PUNISHMENT_EXPIRE_NEVER = -1;
    public static final int PUNISHMENT_EXPIRED = 0;
    private static final String TABLE = "punishments";

    private int nextId = 1;

    /**
     * Represents a punishment
     */
    public class Punishment {

        private int id;

        private PunishmentType type;

        private UUID target;
        private UUID admin;

        private long created;
        private long expires;

        private String server;
        private String reason;

        public Punishment(int id, PunishmentType type, UUID target, UUID admin, long created, long expires, String server, String reason) {
            this.id = id;
            this.type = type;
            this.target = target;
            this.admin = admin;
            this.created = created;
            this.expires = expires;
            this.server = server;
            this.reason = reason;
        }

        /**
         * Get the ID for the punishment
         * @return Punishment ID
         */
        public int getId() {
            return id;
        }

        /**
         * Get the unix timestamp at which the punishment expires
         * @return Punishment expiration
         */
        public long getExpires() {
            return expires;
        }

        /**
         * Determine whether the punishment is expired or not
         * @return True if punishment is expired
         */
        public boolean hasExpired() {
            return expires == PUNISHMENT_EXPIRED || (expires != -1 && expires <= System.currentTimeMillis());
        }

        /**
         * Expire the punishment
         */
        public void expire() {
            setExpires(PUNISHMENT_EXPIRED);
        }

        /**
         * Set a time at which the punishment will expire
         * @param time Unix timestamp at which punishment expires
         */
        public void setExpires(long time) {
            PunishmentManager.this.updatePunishment(this, time);
            this.expires = time;
        }

        /**
         * Get the unix timestamp at which the punishment was created.
         * @return Creation time.
         */
        public long getCreated() {
            return created;
        }

        /**
         * Get the server on which the punishment was created.
         * @return Creation server.
         */
        public String getServer() {
            return server;
        }

        /**
         * Get the reason given for the punishment.
         * @return Creation reason, or false if no reason given.
         */
        public String getReason() {
            return reason;
        }

        /**
         * Get the UUID of the player who the punishment applies to.
         * @return Target UUID.
         */
        public UUID getTarget() {
            return target;
        }

        /**
         * Get the UUID of the player who the punishment was created by.
         * @return Admin UUID.
         */
        public UUID getAdmin() {
            return admin;
        }

        /**
         * Get the {@link com.puzlinc.punishments.PunishmentManager.PunishmentType} of this punishment.
         * @return Punishment type.
         */
        public PunishmentType getType() {
            return type;
        }

        /**
         * Get the message to show the user for this punishment.
         * Follows the format "{@link com.puzlinc.punishments.PunishmentManager.PunishmentType#getVerb()} until TIME for REASON."
         * @return Punishment message
         */
        public String getMessage() {
            StringBuilder builder = new StringBuilder();
            builder.append(getType().getVerb());
            if (getExpires() != PUNISHMENT_EXPIRED) {
                builder.append(" ");
                builder.append(Util.timestampToString(getExpires()));
            }
            if (getReason() != null) {
                builder.append(" for ");
                builder.append(getReason());
            }
            return builder.toString();
        }
    }

    /**
     * Represents a type of punishment that can be given
     */
    public enum PunishmentType {
        BAN("Banned"),
        KICK("Kicked"),
        MUTE("Muted");

        private String verb;

        private PunishmentType(String verb) {
            this.verb = verb;
        }

        /**
         * Get the verb for usage in sending messages.
         * For instance, BAN -> "Banned".
         * @return Verb
         */
        public String getVerb() {
            return verb;
        }
    }

    private Map<UUID, List<Punishment>> punishments = new HashMap<UUID, List<Punishment>>();
    private Configuration config;
    private Database database;
    private int threadId;

    private String sqlQueryAll;
    private String sqlQueryTarget;
    private String sqlInsert;
    private String sqlUpdate;

    private PreparedStatement updateBatch;
    private Map<UUID, List<Punishment>> updatePunishments = new HashMap<UUID, List<Punishment>>();

    public PunishmentManager(Punishments plugin, Configuration config, Database database) {
        this.config = config;
        this.database = database;

        String sqlCreate =
                "CREATE TABLE IF NOT EXISTS `" + TABLE + "` (" +
                "  `id` INT NOT NULL AUTO_INCREMENT," +
                "  `type` VARCHAR(45) NOT NULL," +
                "  `target` VARCHAR(45) NOT NULL," +
                "  `admin` VARCHAR(45) NULL," +
                "  `created` BIGINT NULL," +
                "  `expires` BIGINT NOT NULL," +
                "  `server` VARCHAR(45) NOT NULL," +
                "  `reason` VARCHAR(45) NULL," +
                "  PRIMARY KEY (`id`))";

        String sqlGetNextId = "SELECT MAX(id) AS max FROM " + TABLE;

        try {
            PreparedStatement statement = database.getConnection().prepareStatement(sqlCreate);
            statement.executeUpdate();

            statement = database.getConnection().prepareStatement(sqlGetNextId);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                nextId = rs.getInt("max")+1;
            }

            sqlQueryAll = "SELECT * FROM " + TABLE +" WHERE expires <> 0 AND expires > UNIX_TIMESTAMP()";
            sqlQueryTarget = "SELECT * FROM " + TABLE +" WHERE target = ?";
            sqlInsert = "INSERT INTO " + TABLE + "(`type`, `target`, `admin`, `created`, `expires`, `server`, `reason`) VALUES (?, ?, ?, ?, ?, ?, ?)";
            sqlUpdate = "UPDATE " + TABLE + " SET expires = ? WHERE id = ?";

            updateBatch = database.getConnection().prepareStatement(sqlInsert);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        loadActivePunishments();

        threadId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            public void run() {
                try {
                    updateBatch.executeBatch();
                    updatePunishments = new HashMap<UUID, List<Punishment>>();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }, 5 * 60 * 20L, 5 * 60 * 20L);
    }

    /**
     * Get this server's name.
     * @return Server name
     */
    public String getServer() {
        return config.getServerName();
    }

    /**
     * Loads all active remote punishments and stores them in local memory for all players
     */
    public void loadActivePunishments() {
        punishments = new HashMap<UUID, List<Punishment>>();
        try {
            PreparedStatement statement = database.getConnection().prepareStatement(sqlQueryAll);
            List<Punishment> list = resultSetToPunishments(statement.executeQuery());
            for (Punishment punishment : list) {
                localAddPunishment(punishment, punishments);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads all active remote punishments and stores them in local memory for a given player
     * @param id UUID of specified player
     */
    public void loadActivePunishmentsFor(UUID id) {
        List<Punishment> list;
        if (updatePunishments.containsKey(id)) {
            // There are some punishments pending push
            list = updatePunishments.get(id);
        } else {
            list = new ArrayList<Punishment>();
        }
        list.addAll(getPunishmentsFor(id));
        punishments.put(id, list);
    }

    /**
     * Gets all remote punishments for a given player regardless of whether or not they are active
     * @param id UUID of specified player
     * @return List of remote punishment history for player
     */
    public List<Punishment> getPunishmentsFor(UUID id) {
        try {
            PreparedStatement statement = database.getConnection().prepareStatement(sqlQueryTarget);

            statement.setString(1, id.toString());

            return resultSetToPunishments(statement.executeQuery());
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Gets all local active punishments for a given player
     * @param id UUID of specified player
     * @return List of local active punishments
     */
    public List<Punishment> getActivePunishmentsFor(UUID id) {
        return punishments.containsKey(id) ? punishments.get(id) : new ArrayList<Punishment>();
    }

    /**
     * Gets all remote and local (unsynced) punishments for a given player regardless of whether or not they are active
     * @param id UUID of specified player
     * @return List of total punishment history for player
     */
    public List<Punishment> getAllPunishmentsFor(UUID id) {
        List<Punishment> punishments = getPunishmentsFor(id);
        if (updatePunishments.containsKey(id)) {
            punishments.addAll(updatePunishments.get(id));
        }
        return punishments;
    }

    /**
     * Determines whether a player currently has an active punishment of a specified type
     * @param id UUID of specified player
     * @param type PunishmentType to check for
     * @return Punishment value, if active or null if none found
     */
    public Punishment hasActivePunishment(UUID id, PunishmentType type) {
        for (PunishmentManager.Punishment punishment : getActivePunishmentsFor(id)) {
            if (punishment.getType() == type && !punishment.hasExpired()) {
                return punishment;
            }
        }
        return null;
    }

    /**
     * Adds a new punishment to the local history, pending sync to database
     * @param type PunishmentType
     * @param target Target player's UUID
     * @param admin Admin player's UUID
     * @param created The timestamp the punishment was created
     * @param expires The timestamp the punishment will expire
     * @param server The server the punishment was created on
     * @param reason The reason for the punishment
     * @return A new punishment
     */
    public Punishment addPunishment(PunishmentType type, UUID target, UUID admin, long created, long expires, String server, String reason) {
        Punishment punishment = new Punishment(nextId, type, target, admin, created, expires, server, reason);
        nextId++;

        localAddPunishment(punishment, punishments);
        localAddPunishment(punishment, updatePunishments);
        try {
            updateBatch.setString(1, type.toString());
            updateBatch.setString(2, target.toString());
            updateBatch.setString(3, admin != null ? admin.toString() : null);
            updateBatch.setLong(4, created);
            updateBatch.setLong(5, expires);
            updateBatch.setString(6, server);
            updateBatch.setString(7, reason);

            updateBatch.addBatch();
            nextId++;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return punishment;
    }

    /**
     * Add a punishment only to the local set of punishments
     * @param punishment Punishment to add
     * @param map Map to use (either pending sync or already synced)
     */
    private void localAddPunishment(Punishment punishment, Map<UUID, List<Punishment>> map) {
        if (map.containsKey(punishment.getTarget())) {
            map.get(punishment.getTarget()).add(punishment);
        } else {
            List<Punishment> temp = new ArrayList<Punishment>();
            temp.add(punishment);
            map.put(punishment.getTarget(), temp);
        }
    }

    /**
     * Update a punishment expire status
     * @param punishment Punishment to update
     * @param expires New expire time
     */
    public void updatePunishment(Punishment punishment, long expires) {
        try {
            PreparedStatement statement = database.getConnection().prepareStatement(sqlUpdate);
            statement.setLong(1, expires);
            statement.setInt(2, punishment.getId());

            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private List<Punishment> resultSetToPunishments(ResultSet rs) {
        List<Punishment> list = new ArrayList<Punishment>();
        try {
            while (rs.next()) {
                int id = rs.getInt("id");
                PunishmentType type = PunishmentType.valueOf(rs.getString("type"));
                UUID target = UUID.fromString(rs.getString("target"));
                UUID admin = rs.getString("admin") != null ? UUID.fromString(rs.getString("admin")) : null;
                long created = rs.getLong("created");
                long expires = rs.getLong("expires");
                String server = rs.getString("server");
                String reason = rs.getString("reason");

                list.add(new Punishment(id, type, target, admin, created, expires, server, reason));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Force all pending stat updates to be executed.
     * This execution will NOT be threaded to ensure it halts the main thread until completion.
     */
    public void forceUpdate() {
        try {
            updateBatch.executeBatch();
            updatePunishments = new HashMap<UUID, List<Punishment>>();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Shut down threaded update execution.
     */
    public void stop() {
        Bukkit.getScheduler().cancelTask(threadId);
    }
}
