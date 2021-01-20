package com.regulad.advancementhunt.message;

import com.destroystokyo.paper.Title;
import com.regulad.advancementhunt.AdvancementHunt;
import com.regulad.advancementhunt.config.Messages;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class MessageManager {
    private static Messages message_config;
    private final ArrayList<MessageObject> messages = new ArrayList<>();

    public MessageManager(Messages message_config) {
        MessageManager.message_config = message_config;
        loadMessages();
    }

    public static Messages getMessageConfig() {
        return message_config;
    }

    private void loadMessages() {
        for (String str : message_config.getConfig().getConfigurationSection("Messages").getKeys(false)) {
            SendType message_send_type = getSendType(message_config.getConfig().getString("Messages." + str + ".send_type"));
            String message = message_config.getConfig().getString("Messages." + str + ".message");
            MessageType messageType = getMessageType(str);

            messages.add(new MessageObject(messageType, message_send_type, message));
        }
    }

    private SendType getSendType(String send_type) {
        switch (send_type) {
            case "TITLE":
                return SendType.TITLE;
            case "ACTION_BAR":
                return SendType.ACTION_BAR;
            case "CHAT":
                return SendType.CHAT;
            case "SUBTITLE":
                return SendType.SUBTITLE;
        }

        AdvancementHunt.getInstance().getLogger().info("Invalid Message Send type found using default one ('CHAT')");

        return SendType.CHAT;
    }

    private MessageType getMessageType(String message_type) {
        switch (message_type.toLowerCase()) {
            case "hunter_won":
                return MessageType.HUNTERWON;
            case "game_over":
                return MessageType.GAME_OVER;
            case "player_offline":
                return MessageType.PLAYER_NOT_ONLINE;
            case "time_left":
                return MessageType.TIME_LEFT;
            case "the_fleeing_player":
                return MessageType.THE_FLEEING_PLAYER;
            case "the_hunter":
                return MessageType.THE_HUNTER;
            case "won":
                return MessageType.WON;
            case "not_enough_players":
                return MessageType.NOT_ENOUGH_PLAYERS;
            case "is_fleeing_player":
                return MessageType.ISFLEEING_PLAYER;
            case "stop_game":
                return MessageType.STOP_GAME;
            case "in_progress_game":
                return MessageType.IN_PROGRESS_GAME;
            case "start_game":
                return MessageType.START_GAME;
            case "create_world":
                return MessageType.CREATING_WORLD;
        }

        AdvancementHunt.getInstance().getLogger().info("message type: " + message_type + " Not found returning random type lol");
        return MessageType.STOP_GAME;
    }

    public MessageObject getMessage(MessageType messageType) {
        for (MessageObject message_objects : messages) {
            if (message_objects.getMessageType() == messageType) {
                return message_objects;
            }
        }

        return null;
    }

    public void sendMessage(Player player, MessageType messageType) {
        MessageObject message = getMessage(messageType);

        if (message != null) {
            switch (message.getSendType()) {
                case CHAT:
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(player, message.getMessage())));
                    return;
                case TITLE:
                    player.sendTitle(new Title(ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(player, message.getMessage())), ""));
                    return;
                case ACTION_BAR:
                    player.sendActionBar(ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(player, message.getMessage())));
                    return;
                case SUBTITLE:
                    player.sendTitle(new Title("", ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(player, message.getMessage()))));
                    return;
            }
        }

        AdvancementHunt.getInstance().getLogger().info("Unable to send message to player");
    }

    public void sendMessageReplace(Player player, MessageType messageType, String regex, String replacements) {
        MessageObject message = getMessage(messageType);

        if (message != null) {
            switch (message.getSendType()) {
                case CHAT:
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(player, message.getMessage().replaceAll(regex, replacements))));
                    return;
                case TITLE:
                    player.sendTitle(new Title(ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(player, message.getMessage().replaceAll(regex, replacements))), ""));
                    return;
                case ACTION_BAR:
                    player.sendActionBar(ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(player, message.getMessage().replaceAll(regex, replacements))));
                    return;
                case SUBTITLE:
                    player.sendTitle(new Title("", ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(player, message.getMessage()))));
                    return;
            }
        }

        AdvancementHunt.getInstance().getLogger().info("Unable to send message to player");
    }
}
