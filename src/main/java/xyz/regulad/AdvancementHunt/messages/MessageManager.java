package xyz.regulad.AdvancementHunt.messages;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;
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

    public void dispatchMessage(Player recipient, Message message) {
        List<Map<?, ?>> listOfMessages = this.plugin.getConfig().getMapList(message.getKey());
        Audience audience = this.plugin.getBukkitAudiences().player(recipient);
        for (Map<?, ?> messageMap : listOfMessages) {
            String sendType = (String) messageMap.get("send_type");
            String messageToSend = (String) messageMap.get("message");
            Component messageComponent = Component.text(PlaceholderAPI.setPlaceholders(recipient, messageToSend));
            switch (sendType) {
                case "title":
                    Title titleToSend;

                    String submessageToSend = messageMap.get("submessage") != null ? (String) messageMap.get("submessage") : "";
                    Component submessageComponent = Component.text(PlaceholderAPI.setPlaceholders(recipient, submessageToSend));

                    titleToSend = Title.title(messageComponent, submessageComponent);
                    audience.showTitle(titleToSend);
                    break;
                case "actionbar":
                    audience.sendActionBar(messageComponent);
                    break;
                case "chat":
                    audience.sendMessage(messageComponent);
                    break;
            }
        }
    }
}
