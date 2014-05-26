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

package com.puzlinc.punishments.listener;

import com.puzlinc.punishments.PunishmentManager;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerLoginEvent;

import java.util.UUID;

public class PlayerListener implements Listener {

    private PunishmentManager manager;

    public PlayerListener(PunishmentManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        UUID id = event.getPlayer().getUniqueId();

        manager.loadActivePunishmentsFor(id);

        // BAN
        PunishmentManager.Punishment punishment;
        if ((punishment = manager.hasActivePunishment(id, PunishmentManager.PunishmentType.BAN)) != null) {
            event.setResult(PlayerLoginEvent.Result.KICK_BANNED);
            event.setKickMessage(ChatColor.RED + punishment.getMessage());
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        UUID id = event.getPlayer().getUniqueId();

        // MUTE
        PunishmentManager.Punishment punishment;
        if ((punishment = manager.hasActivePunishment(id, PunishmentManager.PunishmentType.MUTE)) != null) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + punishment.getMessage());
        }
    }
}
