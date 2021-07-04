# AdvancementHunt

AdvancementHunt is a minigame plugin.

The objective of the game is to obtain a certain advancement within the time limit without dying.

There are two teams, the Hunters and the Hunted.

At the beginning of the game, the Hunted will recieve the name of an advancement and are tasked with completing it. 

They must first figure out what the advancement is and then obtain it.

Also, at the beginning of the game, the Hunters are tasked with killing the Hunted before they obtain the advancement. The Hunters may not know what the advancement is.

All games have a time limit, and if it is reached, the game will stalemate.

Game stats are logged and can be accessed via PlaceholderAPI placeholders.

## Requirements

1. The following plugins:
    * Multiverse-Core
    * PlaceholderAPI
2. Java 8
3. Spigot 1.13+

Optional plugins:
   * Multiverse-NetherPortals

## Setup

AdvancementHunt is ready to go out of the box. 

## Use

### Commands

* `/gamestart [Hunted player] [Advancement] <Time (Minutes)> [World seed] <Worldborder>`: Allows you to start the game.
* `/gameend`: Allows you to end the game.
* `/registeradvancement <Advancement> <Time (Minutes)>`: Allows you to add an advancement and time limit to the database.
* `/registerseed <World seed> <Worldborder>`: Allows you to add a seed and worldborder size to the database.

## Placeholders

### Identifier is `ah`

* `wins`: The total amount of wins a player has.
* `losses`: The total amount of losses a player has.
* `kills`: Shows total kills of the player.
* `deaths`: Shows total deaths of the player.
* `id`: The advancement ID.
* `advancement`: The name of the advancement.
* `hunted`: Shows the display name of the player being hunted.
* `hunters`: Shows a list of display names for the players that are hunting. Seperated by commas.
* `time`: Shows time remaining, in `HH:MM:SS` format.

Example:

```
%ah_time% | %ah_heading%
```
