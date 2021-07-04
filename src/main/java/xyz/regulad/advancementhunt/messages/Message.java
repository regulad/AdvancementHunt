package xyz.regulad.advancementhunt.messages;

public enum Message {
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

    public String getKey() {
        return this.key;
    }
}
