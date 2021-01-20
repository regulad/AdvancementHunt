# AdvancementHunt

## Requirements
1. The following plugins:
    * Multiverse-Core
    * Multiverse-NetherPortals
    * PlaceholderAPI
1. Java 11
1. [Paper](https://github.com/PaperMC/Paper), or a [fork that fully supports the API](https://github.com/topics/paper-fork), running Minecraft 1.15 or higher

## Setup

1. MySQL in config.yml
    * This can be disabled, and the plugin will work.
1. The set lobby spawn

## Use

### Commands
* `/gamestart`: Shows you informations to start the game
* `/set <LobbySpawn>`: Sets the Lobbyspawn

### Permissions

#### You can change these if you want

1. **StartGame**: `advancementhunt.command.start`
1. **SetLocation**: `advancementhunt.command.set`

### MySQL

#### You can disable this if you want to use the plugin standalone

1. You can change the host, port, database, database prefix, JDBC options, username, and password.

## Placeholder API Placeholders

### Identifier is `ah`

* `time_remaining` - The remaining time. (HH:MM:ss)
* `wins` - The total amount of wins a player has. (Pulled from DB, not cached!)
* `losses` - The total amount of losses a player has. (Pulled from DB, not cached!)
* `kills` - Shows kills of the player
* `deaths` - shows death's of the player
* `id` - The advancement id. (Only the namespaced ID)
