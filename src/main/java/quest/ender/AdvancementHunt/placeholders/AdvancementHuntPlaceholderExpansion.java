package quest.ender.AdvancementHunt.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import quest.ender.AdvancementHunt.AdvancementHunt;
import quest.ender.AdvancementHunt.database.stats.StatsColumn;
import quest.ender.AdvancementHunt.game.state.PlayingState;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

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
        return this.plugin.getDescription().getVersion();
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
                    // this is definitely the best way to do this
                    final @NotNull TranslatableComponent advancementTitle = (TranslatableComponent) Objects.requireNonNull(currentGameState.goalAdvancement.getDisplay()).title();
                    final @NotNull Component renderedComponent = GlobalTranslator.render(advancementTitle, Locale.US); // don't use the player locale if available, server only has Locale.US
                    final @NotNull String advancementTitleString = PlainTextComponentSerializer.plainText().serialize(renderedComponent);
                    return advancementTitleString;
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
                    long seconds = duration.getSeconds();

                    long minutes = seconds / 60;
                    seconds -= minutes * 60;

                    long hours = minutes / 60;
                    minutes -= hours * 60;

                    seconds = Math.abs(seconds); // ???
                    minutes = Math.abs(minutes); // ???
                    hours = Math.abs(hours); // ???

                    return (minutes == 0 && hours == 0 && seconds > 0 ? ChatColor.RED : "")
                            + (
                            hours != 0 ? (hours < 10 ? "0" + hours : String.valueOf(hours)) + ":" : "")
                            + (minutes != 0 || hours != 0 ? (minutes < 10 ? "0" + minutes : String.valueOf(minutes)) + ":" : "")
                            + (seconds < 10 ? "0" + seconds : String.valueOf(seconds)
                    )
                            + (minutes == 0 && hours == 0 && seconds > 0 ? ChatColor.RESET : ""); // I kinda hate this case, but it's alright since it's the definition of internal code.
                } else
                    return "";
            default:
                return null;
        }
    }
}
