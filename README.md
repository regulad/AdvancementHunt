# AdvancementHunt

## Setup

1. Install the following plugins:
    * Multiverse-Core
    * Multiverse-NetherPortals
    * PlaceholderAPI
1. MySQL in config.yml
    * This can be disabled, and the plugin will work.
1. The set lobby spawn

## Use

### Commands
* `/gamestart`: Shows you informations to start the game
* `/set <LobbySpawn>`: Sets the Lobbyspawn

### Permissions
* Have a look at the config.yml

### MySQL
* Have a look at the config.yml

## Placeholder API Placeholders

### Identifier is `ah`


* `%ah_time_remaining%` - The remaining time. (HH:MM:ss)
* `%ah_wins%` - The total amount of wins a player has. (Pulled from DB, not cached!)
* `%ah_losses%` - The total amount of losses a player has. (Pulled from DB, not cached!)
* `%ah_kills%` - Shows kills of the player
* `%ah_deaths%` - shows death's of the player
* `%ah_id%` - The advancement id. (Only the namespaced ID)
