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
%ah_time% | %ah_advancement%
```

Will produce:

```
10:10 | Stone Age
```

(Provided there are 10 minutes and 10 seconds left, and the current advancement is Stone Age)

## Developers

### Maven

Insert the following snippets into your POM.xml.

For the repository:

```xml
<repositories>
    ...
    <repository>
        <id>regulad-releases</id>
        <url>https://nexus.regulad.xyz/repository/maven-releases/</url>
    </repository>
    ...
</repositories>
```

For the dependency:

```xml
<dependencies>
    ...
    <dependency>
        <groupId>xyz.regulad.advancementhunt</groupId>
        <artifactId>AdvancementHunt</artifactId>
        <version>{version}</version>
    </dependency>
    ...
</dependencies>
```

Replace `{version}` with the current version. You can see the current version below.

![Current Version](https://img.shields.io/github/v/release/regulad/AdvancementHunt)

### Use

You can get the instance of the AdvancementHunt plugin like any other plugin.

```java
final AdvancementHunt plugin = (AdvancementHunt) Bukkit.getServer().getPluginManager().getPlugin("AdvancementHunt");
```

If you want to change the game state, see the `startGame()` or `endGame()` methods of `xyz.regulad.advancementhunt.AdvancementHunt`.

If you only want to listen to events, look at the events `xyz.regulad.advancementhunt.events.gamestate.PreGameStateChangeEvent` or `xyz.regulad.advancementhunt.events.gamestate.PostGameStateChangeEvent`.
