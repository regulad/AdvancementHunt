# // TODO Mk II

## To be fixed now

1. BungeeCord stuff
    * Add a "Queue" system.
    * Kick players back to a selection of servers
1. Save player hashmap to DB.
	* Must use same DB as other table
	* Both tables must share the same prefix
1. Add table prefix
	* Both the playerdata table and the game storage table must share the same configurable prefix
1. After a game is over, the server still blocks players from joining with the message "game already started"
1. Plugin uses the PlayerJoinEvent instead of the superior AsyncPlayerPreLoginEvent, this is an issue on the BungeeCord network because it results in players being kicked from the network, not being unable to connect to the server
    * I think this causes crashes since I get errors like `ERROR Could not pass event PlayerJoinEvent to Advancementhunt v1.0-SNAPSHOT`, which lead to a crash
    * This happens with other events too.

## To be fixed later

1. When a game is over, the timer continues to tick down even when the "Game ends in x seconds"
1. Game does not automatically start when 3 players are sent into the server
1. "Creating World..." is hardcoded and cannot be changed with `messages.yml`
1. Add "queue" system for the messages so when 2 messages are dispatched to the same place nothing breaks

# I have also provided the original pitch. Please read over it if there is any confusion.

```I need a plugin for a Minigame on my team's BungeeCord server network. My Budget is $25.

The game will have 3 servers dedicated to it initally and I may expand this if the game is a success.

There will be 2 parts of the plugin: the game and the matchmaking.

The name of the game is Advancement Hunt.

Players are given the challenge of obtaining an advancement while being chased by 1-2 other people.
The person being hunted will have the glowing effect.
The hunters will have a compass that points towards the player.

Three worlds must be generated (one overworld, nether, and end) using a plugin like Multiverse and Multiverse-NetherPortals.

The plugin should be able to function on a standalone server with no or an intrgrated databse and in a network with a MySQL database.

This advancement must be random from a table of advancments stored on a MySQL database. The seed used to generate the set of worlds must also be random from a table stored on a MySQL database.

Another table may be used to store what servers are online for matchmaking, and another one for storing statistics like games won and lost for each player. A queue command must be able to be called from the lobby servers.

I need the plugin to support PlaceholderAPI for a few placeholders that I will place in my GUI.

%ah_wins% - The amount of games a player has won
%ah_losses% - The amount of times a player has lost
%ah_time_remaining% - The amount of time remaining.

Everything about the game should be configurable, including who the fleeing player is, if the fleeing player has the glowing effect, if the hunters have a compass, if there is a time limit & how long, how many hunters there are, if there is a worldborder and how far out, a custom world seed, and/or a custom world generator type. 

The plugin needs to have its source files provided so my team can make revisions in the future.

The MySQL intregration will need to have a configurable prefix because I will have multiple instances of the game running on my network.

The messages the plugin sends to the player must be fully configurable both in their content and where they are displayed```
