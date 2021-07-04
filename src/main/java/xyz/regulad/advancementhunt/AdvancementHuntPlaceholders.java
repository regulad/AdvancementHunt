package xyz.regulad.advancementhunt;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import xyz.regulad.advancementhunt.gamestate.PlayingState;
import xyz.regulad.advancementhunt.util.AdvancementName;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AdvancementHuntPlaceholders extends PlaceholderExpansion {
    private final AdvancementHunt plugin;

    public AdvancementHuntPlaceholders(AdvancementHunt plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "ah";
    }

    @Override
    public String getAuthor() {
        return "regulad";
    }

    @Override
    public String getVersion() {
        return "${project.version}";
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public List<String> getPlaceholders() {
        return Arrays.asList("wins", "losses", "kills", "deaths", "id", "advancement", "runner");
    }

    @Override
    public String onPlaceholderRequest(Player player, String placeholder) {
        switch (placeholder) {
            case "wins":
                if (player != null) {
                    return String.valueOf(this.plugin.getPlayerStats(player).getWins());
                } else {
                    return "";
                }
            case "losses":
                if (player != null) {
                    return String.valueOf(this.plugin.getPlayerStats(player).getLosses());
                } else {
                    return "";
                }
            case "kills":
                if (player != null) {
                    return String.valueOf(this.plugin.getPlayerStats(player).getKills());
                } else {
                    return "";
                }
            case "deaths":
                if (player != null) {
                    return String.valueOf(this.plugin.getPlayerStats(player).getDeaths());
                } else {
                    return "";
                }
            case "id":
                if (this.plugin.getCurrentGameState() instanceof PlayingState) {
                    PlayingState currentGameState = (PlayingState) this.plugin.getCurrentGameState();
                    return currentGameState.goalAdvancement.toString();
                } else {
                    return "";
                }
            case "advancement":
                if (this.plugin.getCurrentGameState() instanceof PlayingState) {
                    PlayingState currentGameState = (PlayingState) this.plugin.getCurrentGameState();
                    return AdvancementName.getAdvancementTitle(currentGameState.goalAdvancement);
                } else {
                    return "";
                }
            case "hunted":
                if (this.plugin.getCurrentGameState() instanceof PlayingState) {
                    PlayingState currentGameState = (PlayingState) this.plugin.getCurrentGameState();
                    return currentGameState.fleeingPlayer.getDisplayName();
                } else {
                    return "";
                }
            case "hunters":
                if (this.plugin.getCurrentGameState() instanceof PlayingState) {
                    PlayingState currentGameState = (PlayingState) this.plugin.getCurrentGameState();
                    ArrayList<String> playerList = new ArrayList<>();
                    for (Player hunter : currentGameState.huntingPlayers) {
                        playerList.add(hunter.getDisplayName());
                    }
                    return String.join(", ", playerList); // Having "and" be the second to last one would be cool.
                } else {
                    return "";
                }
            case "time":
                if (this.plugin.getCurrentGameState() instanceof PlayingState) {
                    PlayingState currentGameState = (PlayingState) this.plugin.getCurrentGameState();
                    long seconds = (currentGameState.endTime.toEpochMilli() - Instant.now().toEpochMilli()) / 1000;

                    long minutes = seconds / 60;
                    seconds -= minutes * 60; // Could be replaced with a modulo operation, but there is no quantifiable benefit from doing so.

                    long hours = minutes / 60;
                    minutes -= hours * 60;

                    return (hours != 0 ? (hours < 10 ? "0" + hours : String.valueOf(hours)) + ":" : "") + (minutes != 0 || hours != 0 ? (minutes < 10 ? "0" + minutes : String.valueOf(minutes)) + ":" : "") + (seconds < 10 ? "0" + seconds : String.valueOf(seconds));
                } else {
                    return "";
                }
            default:
                return null;
        }
    }
}
