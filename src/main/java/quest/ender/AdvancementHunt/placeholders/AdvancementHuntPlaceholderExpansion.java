package quest.ender.AdvancementHunt.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import quest.ender.AdvancementHunt.AdvancementHunt;
import quest.ender.AdvancementHunt.database.stats.StatsColumn;
import quest.ender.AdvancementHunt.game.state.PlayingState;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class AdvancementHuntPlaceholderExpansion extends PlaceholderExpansion {
    private final @NotNull AdvancementHunt plugin;

    public AdvancementHuntPlaceholderExpansion(final @NotNull AdvancementHunt plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "ah";
    }

    @Override
    public @NotNull String getAuthor() {
        return "regulad";
    }

    @Override
    public @NotNull String getVersion() {
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
    public @NotNull List<String> getPlaceholders() {
        return Arrays.asList("wins", "losses", "kills", "deaths", "id", "advancement", "runner");
    }

    @Override
    public @Nullable String onPlaceholderRequest(final @Nullable Player player, final @NotNull String placeholder) {
        switch (placeholder) {
            case "wins":
                if (player != null)
                    return String.valueOf(this.plugin.getPlayerStats(player).getColumn(StatsColumn.WINS));
                else
                    return "";
            case "losses":
                if (player != null)
                    return String.valueOf(this.plugin.getPlayerStats(player).getColumn(StatsColumn.LOSSES));
                else
                    return "";
            case "kills":
                if (player != null)
                    return String.valueOf(this.plugin.getPlayerStats(player).getColumn(StatsColumn.KILLS));
                else
                    return "";
            case "deaths":
                if (player != null)
                    return String.valueOf(this.plugin.getPlayerStats(player).getColumn(StatsColumn.DEATHS));
                else
                    return "";
            case "id":
                if (this.plugin.getCurrentGameState() instanceof PlayingState currentGameState)
                    return currentGameState.goalAdvancement.toString();
                else
                    return "";
            case "advancement":
                if (this.plugin.getCurrentGameState() instanceof PlayingState currentGameState) {
                    final @NotNull Advancement advancement = currentGameState.goalAdvancement;

                    try { // NMS: Fix!
                        final @NotNull Class<? extends Advancement> craftAdvancementClass = advancement.getClass();
                        final @NotNull Class<?> advancementDisplayClass = craftAdvancementClass.getMethod("getDisplay").getReturnType(); // org.bukkit.advancement.AdvancementDisplay
                        final @Nullable Object advancementDisplay = craftAdvancementClass.getMethod("getDisplay").invoke(advancement); // An instance of AdvancementDisplay
                        return (String) Objects.requireNonNull(advancementDisplayClass.getMethod("getTitle").invoke(advancementDisplay));
                    } catch (Exception exception) {
                        exception.printStackTrace();

                        final @NotNull String rawAdvancementName = advancement.getKey().getKey();
                        return Arrays.stream(rawAdvancementName.substring(rawAdvancementName.lastIndexOf("/") + 1).toLowerCase().split("_"))
                                .map(s -> s.substring(0, 1).toUpperCase() + s.substring(1))
                                .collect(Collectors.joining(" "));
                    }
                } else
                    return "";
            case "hunted":
                if (this.plugin.getCurrentGameState() instanceof PlayingState currentGameState)
                    return LegacyComponentSerializer.legacySection().serialize(currentGameState.fleeingPlayer.displayName());
                else
                    return "";
            case "hunters":
                if (this.plugin.getCurrentGameState() instanceof PlayingState currentGameState) {
                    ArrayList<String> playerList = new ArrayList<>();
                    for (Player hunter : currentGameState.huntingPlayers) {
                        playerList.add(LegacyComponentSerializer.legacySection().serialize(hunter.displayName()));
                    }
                    return String.join(", ", playerList); // Having "and" be the second to last one would be cool.
                } else
                    return "";
            case "time":
                if (this.plugin.getCurrentGameState() instanceof PlayingState currentGameState) {
                    final @NotNull Duration duration = Duration.between(currentGameState.endTime, Instant.now());
                    long seconds = duration.get(ChronoUnit.SECONDS);

                    long minutes = seconds / 60;
                    seconds = seconds % 60;

                    long hours = minutes / 60;
                    minutes = minutes % 60;

                    return (hours != 0 ? (hours < 10 ? "0" + hours : String.valueOf(hours)) + ":" : "") + (minutes != 0 || hours != 0 ? (minutes < 10 ? "0" + minutes : String.valueOf(minutes)) + ":" : "") + (seconds < 10 ? "0" + seconds : String.valueOf(seconds));
                } else
                    return "";
            default:
                return null;
        }
    }
}
