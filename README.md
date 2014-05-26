Punishments
=============

Punishments is a MySQL-based UUID logging framework for network-wide punishments.

Included in the Punishments plugin are muting, banning, and kicking, however the plugin can be easily expanded to support many other actions.

Commmands
-------

By default, the following commands are implemented:
* ```/kick <player> [reason]``` - punishments.kick
* ```/ban <player> [time] [reason]``` - punishments.ban
* ```/mute <player> [time] [reason]``` - punishments.mute
* ```/unban <player>``` - punishments.unban
* ```/unmute <player>```  - punishments.unmute
* ```/history <player>```  - punishments.history

### Commmand notes
* Brackets indicate a required argument while braces indicate an optional argument
* Time can be denoted as any combination of d, h, m, s. For instance, 1d2h5m30s, 1d, 30s, and 1d30s are all valid times.

Configuration
-------

When setting up the plugin, config.yml's "mysql" options should be populated with valid connection credentials to a MySQL server for use with the plugin.

The database name chosen should exist on the server already, but any necessary tables will be created by the plugin.

### Database notes
* Entries in the database are stored in a table called "punishments"
* Entries are saved to the database are saved every 5 minutes or whenever the server restarts / reloads
* Entries in the database can be manually expired by setting the "expired" field to a value of 0
* -1 denotes an infinite (never expiring) entry


Expansion
-------
New punishments can be added simply by adding a new value to the PunishmentManager.PunishmentType enum.

You will also need to implement any necessary commands and listeners in the same manner that the existing punishments have been added.

