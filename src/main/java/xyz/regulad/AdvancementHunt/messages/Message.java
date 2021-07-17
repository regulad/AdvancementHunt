package xyz.regulad.AdvancementHunt.messages;

import org.bukkit.configuration.file.YamlConfiguration;

/**
 * Represents a message the plugin may display to players.
 */
public enum Message {
    HUNTER_GAMESTART("messages.hunter_gamestart"),
    HUNTED_GAMESTART("messages.hunted_gamestart"),
    HUNTER_PERSISTENT("messages.hunter_persistent"),
    HUNTED_PERSISTENT("messages.hunted_persistent"),
    HUNTER_WIN("messages.hunter_win"),
    HUNTED_WIN("messages.hunted_win"),
    HUNTER_START("messages.hunter_start"),
    HUNTED_START("messages.hunted_start"),
    TIME_UP("messages.time_up"),
    LEFT("messages.left");

    private final String key;

    Message(String key) {
        this.key = key;
    }

    /**
     * @return A {@link String} that is the key to the message in the plugin's main {@link YamlConfiguration}.
     */
    public String getKey() {
        return this.key;
    }
}
