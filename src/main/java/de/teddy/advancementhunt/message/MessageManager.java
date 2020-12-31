package de.teddy.advancementhunt.message;

import com.destroystokyo.paper.Title;
import de.teddy.advancementhunt.AdvancementHunt;
import de.teddy.advancementhunt.config.Messages;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.swing.*;
import java.util.ArrayList;

public class MessageManager {
    private ArrayList<MessageObject> messages = new ArrayList<>();
    private static Messages message_config;

    public MessageManager(Messages message_config)
    {
        this.message_config = message_config;
        loadMessages();
    }

    private void loadMessages()
    {
        for(String str : message_config.getConfig().getConfigurationSection("Messages").getKeys(false))
        {
            SendType message_send_type = getSendType(message_config.getConfig().getString("Messages." + str + ".send_type"));
            String message = message_config.getConfig().getString("Messages." + str + ".message");
            MessageType messageType = getMessageType(str);

            messages.add(new MessageObject(messageType,message_send_type,message));
        }
    }

    private SendType getSendType(String send_type)
    {
        switch (send_type)
        {
            case "TITLE":
                return SendType.TITLE;
            case "ACTION_BAR":
                return SendType.ACTION_BAR;
            case "CHAT":
                return SendType.CHAT;
        }

        AdvancementHunt.getInstance().getLogger().info("Invalid Message Send type found using default one ('CHAT')");
        return SendType.CHAT;
    }

    private MessageType getMessageType(String message_type)
    {
        switch (message_type)
        {
            case "Hunter_won":
                return MessageType.HUNTERWON;
            case "Game_over":
                return MessageType.GAME_OVER;
            case "Player_offline":
                return MessageType.PLAYER_NOT_ONLINE;
            case "Time_left":
                return MessageType.TIME_LEFT;
            case "THE_FLEEING_PLAYER":
                return MessageType.THE_FLEEING_PLAYER;
            case "the_hunter":
                return MessageType.THE_HUNTER;
            case "won":
                return MessageType.WON;
            case "not_enough_players":
                return MessageType.NOT_ENOUGH_PLAYERS;
            case "is_fleeing_player":
                return MessageType.ISFLEEING_PLAYER;
            case "Stop_Game":
                return MessageType.STOP_GAME;
            case "Start_Game":
                return MessageType.START_GAME;
        }

        AdvancementHunt.getInstance().getLogger().info("message type: " + message_type + " Not found returning random type lol");
        return MessageType.STOP_GAME;
    }

    private MessageObject getMessage(MessageType messageType)
    {
        for(MessageObject message_objects : messages)
        {
            if(message_objects.getMessageType() == messageType)
            {
                return message_objects;
            }
        }

        return null;
    }

    public void sendMessage(Player player,MessageType messageType)
    {
        MessageObject message = getMessage(messageType);

        if(message != null)
        {
            switch (message.getSendType())
            {
                case CHAT:
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',message.getMessage()));
                    return;
                case TITLE:
                    player.sendTitle(new Title(ChatColor.translateAlternateColorCodes('&',message.getMessage()),""));
                    return;
                case ACTION_BAR:
                    player.sendActionBar(ChatColor.translateAlternateColorCodes('&',message.getMessage()));
                    return;
            }
        }

        AdvancementHunt.getInstance().getLogger().info("Agh, seems like abhi has did a ooopse");
    }

    public void sendMessageReplace(Player player, MessageType messageType, String regex, String replacements)
    {
        MessageObject message = getMessage(messageType);

        if(message != null)
        {
            switch (message.getSendType())
            {
                case CHAT:
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',message.getMessage().replaceAll(regex,replacements)));
                    return;
                case TITLE:
                    player.sendTitle(new Title(ChatColor.translateAlternateColorCodes('&',message.getMessage().replaceAll(regex,replacements)),""));
                    return;
                case ACTION_BAR:
                    player.sendActionBar(ChatColor.translateAlternateColorCodes('&',message.getMessage().replaceAll(regex,replacements)));
                    return;
            }
        }

        AdvancementHunt.getInstance().getLogger().info("Agh, seems like abhi has did a ooopse");
    }

    public void sendFormatMessage(Player player,MessageType messageType,ArrayList<MessageFormat> format)
    {
        MessageObject message = getMessage(messageType);

        String text = message.getMessage();

        for(MessageFormat format1 : format)
        {
            text.replaceAll(format1.getRegex(),format1.getReplace());
        }
        if(message != null)
        {
            switch (message.getSendType())
            {
                case CHAT:
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',text));
                    return;
                case TITLE:
                    player.sendTitle(new Title(ChatColor.translateAlternateColorCodes('&',text),""));
                    return;
                case ACTION_BAR:
                    player.sendActionBar(ChatColor.translateAlternateColorCodes('&',text));
                    return;
            }
        }

        AdvancementHunt.getInstance().getLogger().info("Agh, seems like abhi has did a ooopse");
    }
    public static Messages getMessageConfig()
    {
        return message_config;
    }
}
