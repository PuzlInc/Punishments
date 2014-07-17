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

package com.puzlinc.punishments.command;

import com.puzlinc.punishments.PunishmentManager;
import com.puzlinc.punishments.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class CommandHandler implements CommandExecutor {

    private PunishmentManager manager;

    public CommandHandler(PunishmentManager manager) {
        this.manager = manager;
    }

    public boolean onCommand(CommandSender cs, Command cmd, String alias, String[] args) {
        String label = cmd.getLabel();
        UUID admin = (cs instanceof Player) ? ((Player)cs).getUniqueId() : null;

        if(label.equalsIgnoreCase("kick")) {
            if (cs.hasPermission("punishments.kick")) {
                if (args.length > 0) {
                    String name = args[0];
                    Player player = Bukkit.getPlayer(name);

                    if (player == null) {
                        cs.sendMessage("Cannot find player: " + name);
                        return true;
                    }

                    String reason = null;
                    if (args.length >= 2) {
                        reason = Util.argsToString(args, 1, args.length);
                    }

                    PunishmentManager.Punishment punishment = manager.addPunishment(
                            PunishmentManager.PunishmentType.KICK,
                            player.getUniqueId(),
                            admin,
                            System.currentTimeMillis(),
                            manager.PUNISHMENT_EXPIRED,
                            manager.getServer(),
                            reason
                    );

                    player.kickPlayer(punishment.getMessage());
                    cs.sendMessage("Player: " + player.getName() + " kicked.");
                } else {
                    cs.sendMessage("Usage: /kick <player> [reason]");
                }
            } else {
                cs.sendMessage("Insufficient permissions.");
            }
        } else if(label.equalsIgnoreCase("ban")) {
            if (cs.hasPermission("punishments.ban")) {
                if (args.length > 0) {
                    String name = args[0];
                    OfflinePlayer player = Bukkit.getOfflinePlayer(name);

                    if (player == null) {
                        cs.sendMessage("Cannot find player: " + name);
                        return true;
                    }

                    String reason = null;
                    long length = manager.PUNISHMENT_EXPIRE_NEVER;
                    if (args.length >= 2) {
                        long givenTime = Util.lengthToMiliseconds(args[1]);
                        if (givenTime != 0) {
                            length = System.currentTimeMillis() + givenTime;
                            reason = Util.argsToString(args, 2, args.length);
                        } else {
                            reason = Util.argsToString(args, 1, args.length);
                        }
                    }

                    PunishmentManager.Punishment punishment = manager.addPunishment(
                            PunishmentManager.PunishmentType.BAN,
                            player.getUniqueId(),
                            admin,
                            System.currentTimeMillis(),
                            length,
                            manager.getServer(),
                            reason
                    );

                    if (player.isOnline()) {
                        ((Player)player).kickPlayer(punishment.getMessage());
                    }

                    cs.sendMessage("Player: " + player.getName() + " banned.");
                } else {
                    cs.sendMessage("Usage: /ban <player> [length] [reason]");
                }
            } else {
                cs.sendMessage("Insufficient permissions.");
            }
        } else if(label.equalsIgnoreCase("mute")) {
            if (cs.hasPermission("punishments.mute")) {
                if (args.length > 0) {
                    String name = args[0];
                    OfflinePlayer player = Bukkit.getOfflinePlayer(name);

                    if (player == null) {
                        cs.sendMessage("Cannot find player: " + name);
                        return true;
                    }

                    String reason = null;
                    long length = manager.PUNISHMENT_EXPIRE_NEVER;
                    if (args.length >= 2) {
                        long givenTime = Util.lengthToMiliseconds(args[1]);
                        if (givenTime != 0) {
                            length = System.currentTimeMillis() + givenTime;
                            reason = Util.argsToString(args, 2, args.length);
                        } else {
                            reason = Util.argsToString(args, 1, args.length);
                        }
                    }

                    PunishmentManager.Punishment punishment = manager.addPunishment(
                            PunishmentManager.PunishmentType.MUTE,
                            player.getUniqueId(),
                            admin,
                            System.currentTimeMillis(),
                            length,
                            manager.getServer(),
                            reason
                    );

                    if (player.isOnline()) {
                        ((Player)player).sendMessage(punishment.getMessage());
                    }
                    cs.sendMessage("Player: " + player.getName() + " muted.");
                } else {
                    cs.sendMessage("Usage: /mute <player> [length] [reason]");
                }
            } else {
                cs.sendMessage("Insufficient permissions.");
            }
        } else if(label.equalsIgnoreCase("unban")) {
            if (cs.hasPermission("punishments.unban")) {
                if (args.length == 1) {
                    String name = args[0];
                    OfflinePlayer player = Bukkit.getOfflinePlayer(name);

                    if (player == null) {
                        cs.sendMessage("Cannot find player: " + name);
                        return true;
                    }

                    PunishmentManager.Punishment punishment;
                    if ((punishment = manager.hasActivePunishment(player.getUniqueId(), PunishmentManager.PunishmentType.BAN)) != null) {
                        punishment.expire();
                        cs.sendMessage("Player: " + player.getName() + " unbanned");
                    } else {
                        cs.sendMessage("Player: " + player.getName() + " not banned");
                    }
                } else {
                    cs.sendMessage("Usage: /unban <player>");
                }
            } else {
                cs.sendMessage("Insufficient permissions.");
            }
        } else if(label.equalsIgnoreCase("unmute")) {
            if (cs.hasPermission("punishments.unmute")) {
                if (args.length == 1) {
                    String name = args[0];
                    OfflinePlayer player = Bukkit.getOfflinePlayer(name);

                    if (player == null) {
                        cs.sendMessage("Cannot find player: " + name);
                        return true;
                    }

                    PunishmentManager.Punishment punishment;
                    if ((punishment = manager.hasActivePunishment(player.getUniqueId(), PunishmentManager.PunishmentType.MUTE)) != null) {
                        punishment.expire();
                        cs.sendMessage("Player: " + player.getName() + " unmuted");
                    } else {
                        cs.sendMessage("Player: " + player.getName() + " is not muted");
                    }
                } else {
                    cs.sendMessage("Usage: /unmute <player>");
                }
            } else {
                cs.sendMessage("Insufficient permissions.");
            }
        } else if(label.equalsIgnoreCase("history")) {
            if (cs.hasPermission("punishments.history")) {
                if (args.length == 1) {
                    String name = args[0];
                    OfflinePlayer player = Bukkit.getOfflinePlayer(name);

                    if (player == null) {
                        cs.sendMessage("Cannot find player: " + name);
                        return true;
                    }

                    cs.sendMessage("History for: " + player.getName());

                    for (PunishmentManager.Punishment punishment : manager.getAllPunishmentsFor(player.getUniqueId())) {
                        cs.sendMessage(
                                ChatColor.GOLD +
                                "(" + punishment.getId() + ") " +
                                ChatColor.GRAY +
                                Util.formatTimestamp(punishment.getCreated()) +
                                ": " +
                                ChatColor.WHITE +
                                punishment.getMessage() +
                                " by " +
                                ChatColor.GRAY +
                                (punishment.getAdmin() == null ? "CONSOLE" : Bukkit.getOfflinePlayer(punishment.getAdmin()).getName())
                        );
                    }
                } else {
                    cs.sendMessage("Usage: /history <player>");
                }
            } else {
                cs.sendMessage("Insufficient permissions.");
            }
        }
        return true;
    }
}
