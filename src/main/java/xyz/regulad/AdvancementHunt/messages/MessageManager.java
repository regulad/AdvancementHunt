package xyz.regulad.AdvancementHunt.messages;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.regulad.AdvancementHunt.AdvancementHunt;

import java.util.List;
import java.util.Map;

public class MessageManager {
    private final AdvancementHunt plugin;

    public MessageManager(AdvancementHunt plugin) {
        this.plugin = plugin;
    }

    public String getKickMessage() {
        return this.plugin.getConfig().getString("kick_message");
    }

    /**
     * Dispatch a {@code message} to a {@code recipient}.
     *
     * @param recipient The {@link Player} who will receive the {@code message}. If it is an instance of player, then PAPI placeholders will be substituted in.
     * @param message   The {@link Message} to be sent to the {@code recipient}.
     */
    public void dispatchMessage(Player recipient, Message message) {
        final @NotNull List<Map<?, ?>> messageEndpoints = this.plugin.getConfig().getMapList(message.getKey());
        for (@NotNull Map<?, ?> messageEndpoint : messageEndpoints) {
            final @NotNull String sendType = (String) messageEndpoint.get("send_type");
            final @NotNull String messageToSend = (String) messageEndpoint.get("message");
            final @NotNull Component messageComponent = Component.text(PlaceholderAPI.setPlaceholders(recipient, messageToSend));
            switch (sendType) {
                case "title" -> {
                    final @NotNull String submessageToSend = messageEndpoint.get("submessage") != null ? (String) messageEndpoint.get("submessage") : "";
                    final @NotNull Component submessageComponent = Component.text(PlaceholderAPI.setPlaceholders(recipient, submessageToSend));
                    final @NotNull Title titleToSend = Title.title(messageComponent, submessageComponent);
                    recipient.showTitle(titleToSend);
                }
                case "actionbar" -> {
                    final boolean snowyArea = switch (recipient.getLocation().getBlock().getBiome()) {
                        case ICE_SPIKES, SNOWY_BEACH, SNOWY_TAIGA, SNOWY_MOUNTAINS, SNOWY_TAIGA_HILLS, SNOWY_TUNDRA, SNOWY_TAIGA_MOUNTAINS -> true;
                        default -> false; // We needn't do anything if we aren't in the snow.
                    }; // Makes it so you can see the action bar in the snow. Very, very hard to see.
                    recipient.sendActionBar(snowyArea ? messageComponent.color(TextColor.color(0)) : messageComponent);
                }
                case "chat" -> recipient.sendMessage(messageComponent);
            }
        }
    }
}
