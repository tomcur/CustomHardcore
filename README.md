# CustomHardcore
CustomHardcore is a [Craft]bukkit/Spigot plugin providing highly customizable support for creating custom hardcore world setups. It allows creation of multiple hardcore world groups that are kept track of separately. Each group can have its own configuration.

### Configuration
A configuration can look like this.
```
timezone: "Europe/Amsterdam"
worldConfig:
    groups:
        default:
            alias: Default
            config:
                enabled: false
                lives: 1
                banishTime: 3.0
                banishLocation:
                    world: world
                    x: 240.5
                    y: 65.0
                    z: 272.0
        hardcoreGroup:
            alias: Hardcore Worlds
            worlds:
            - HardcoreWorld
            - HardcoreWorld_nether
            - HardcoreWorld_the_end
            config:
                enabled: true
```

Use the `timezone` key to specify your server's timezone. These correspond with the timezones found [here](http://en.wikipedia.org/wiki/List_of_tz_database_time_zones).

Next, `worldConfig.groups` defines the hardcore groups that will be used on your server. It is a mapping of group names and those groups' settings. Each group can have an alias (`alias`), a list of worlds (`worlds`) and a group configuration (`config`). All keys are optional.

##### `alias`
Use the alias of a group to define the display name of that group. It can be any string and does not have to be unique. If no alias is set, the display name will be the group's unique name (i.e., its key).

##### `worlds`
Use the `worlds` key to specify the worlds the group consists of. If no worlds are specified, the group will effectively go unused. Any worlds that are not specified to belong to a group, will be assigned to the default group (`worldConfig.groups.default`).

##### `config`
The configuration options are: 
- `enabled`: whether hardcore mode is enabled for this group or not.
- `lives`: the number times a player can die before being banished.
- `banishTime`: the length of time a player is banished after having spent all lives (measusured in days, e.g. 0.5 is 12 hours).
- `banishLocation`: the location a player is sent to when the banishment is enforced. They can move away from the location at will, but will be sent to the banishment location again when they attempt to join the world group they were banished from.

__Note__: The default group, `worldConfig.groups.default`, must always be present; it is used as a "fall-back"-configuration group if other groups do not specify certain configuration options.

### Commands and Permissions
- `/chc info [player]`: Get banishment information of a player.
 - Aliases: `/chc i`
 - Permissions: `customhardcore.info.player` _(Default: true)_ to be able to perform the command on a target.
- `/chc list`: List all the world groups with hardcore mode enabled.
 - Permissions: `customhardcore.info.list` _(Default: true)_
- `/chc lives [player]`: Get the number of lives a player has left in the hardcore groups.
 - Aliases: `/chc l`
 - Permissions: `customhardcore.info.player` _(default: true)_ to be able to perform the command on a target.
- `/chc groupinfo [group]`: See the configuration of a world group
 - Aliases: `/chc ginfo`, `/chc gi`
 - Permissions: `customhardcore.info.group` _(default: true)_
- `/chc worlds [group]`: See the worlds that belong to a world group.
 - Aliases: `/chc w`
 - Permissions: `customhardcore.info.group` _(default: true)_
- `/chc banish <player> [group]`: Banish a player from a group.
 - Aliases: `/chc b`
 - Permissions: `customhardcore.admin` _(default: op)_
- `/chc unbanish <player> [group]`: Unbanish a player from a group.
 - Aliases: `/chc unb`, `/chc u`
 - Permissions: `customhardcore.admin` _(default: op)_

For all commands where `player` is optional, the command will target the sender. For all commands where `group` is optional, the command will target the world group the sender is currently in.

### Multiverse
The plugin has optional Multiverse integration. If Multiverse is enabled on your server, world names will be styled in accordance with your Multiverse world alias settings.
