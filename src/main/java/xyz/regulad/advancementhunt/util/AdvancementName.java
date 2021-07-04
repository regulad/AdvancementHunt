package xyz.regulad.advancementhunt.util;

import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.advancement.Advancement;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class AdvancementName { // Copied almost verbatim from https://github.com/DiscordSRV/DiscordSRV/blob/master/src/main/java/github/scarsz/discordsrv/listeners/PlayerAdvancementDoneListener.java
    public static boolean isBlank(CharSequence cs) { // Copied from org.apache.commons.lang3.StringUtils
        int strLen = cs == null ? 0 : cs.length();
        if (strLen != 0) {
            for (int i = 0; i < strLen; ++i) {
                if (!Character.isWhitespace(cs.charAt(i))) {
                    return false;
                }
            }
        }
        return true;
    }

    public static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.builder().extractUrls().hexColors().useUnusualXRepeatedCharacterHexFormat().build();

    private static final Map<Advancement, String> ADVANCEMENT_TITLE_CACHE = new ConcurrentHashMap<>();

    public static String getAdvancementTitle(Advancement advancement) {
        return ADVANCEMENT_TITLE_CACHE.computeIfAbsent(advancement, v -> {
            try {
                Object handle = advancement.getClass().getMethod("getHandle").invoke(advancement);
                Object advancementDisplay = Arrays.stream(handle.getClass().getMethods())
                        .filter(method -> method.getReturnType().getSimpleName().equals("AdvancementDisplay"))
                        .filter(method -> method.getParameterCount() == 0)
                        .findFirst().orElseThrow(() -> new RuntimeException("Failed to find AdvancementDisplay getter for advancement handle"))
                        .invoke(handle);
                if (advancementDisplay == null) {
                    throw new RuntimeException("Advancement doesn't have display properties");
                } else {
                    try {
                        Field advancementMessageField = advancementDisplay.getClass().getDeclaredField("a");
                        advancementMessageField.setAccessible(true);
                        Object advancementMessage = advancementMessageField.get(advancementDisplay);
                        Object advancementTitle = advancementMessage.getClass().getMethod("getString").invoke(advancementMessage);
                        return (String) advancementTitle;
                    } catch (Exception exception) {
                        Field titleComponentField = Arrays.stream(advancementDisplay.getClass().getDeclaredFields())
                                .filter(field -> field.getType().getSimpleName().equals("IChatBaseComponent"))
                                .findFirst().orElseThrow(() -> new RuntimeException("Failed to find advancement display properties field"));

                        titleComponentField.setAccessible(true);
                        Object titleChatBaseComponent = titleComponentField.get(advancementDisplay);
                        String title = (String) titleChatBaseComponent.getClass().getMethod("getText").invoke(titleChatBaseComponent);

                        if (!isBlank(title)) {
                            return title;
                        } else {
                            Class<?> chatSerializerClass = Arrays.stream(titleChatBaseComponent.getClass().getDeclaredClasses())
                                    .filter(clazz -> clazz.getSimpleName().equals("ChatSerializer"))
                                    .findFirst().orElseThrow(() -> new RuntimeException("Couldn't get component ChatSerializer class"));

                            String componentJson = (String) chatSerializerClass.getMethod("a", titleChatBaseComponent.getClass()).invoke(null, titleChatBaseComponent);
                            return LEGACY_SERIALIZER.serialize(Material.getMaterial("NETHERITE_PICKAXE") == null && Bukkit.getServer().getPluginManager().getPlugin("ViaVersion") == null ? GsonComponentSerializer.colorDownsamplingGson().deserialize(componentJson) : GsonComponentSerializer.gson().deserialize(componentJson));
                            // I'm going to be real honest, I'm not very sure whats happening here.
                            // From what I can tell, we serialize the component, but we need to do pre-processing if the server does not support 1.16 hex color codes.
                        }
                    }
                }
            } catch (Exception e) {
                String rawAdvancementName = advancement.getKey().getKey();
                return Arrays.stream(rawAdvancementName.substring(rawAdvancementName.lastIndexOf("/") + 1).toLowerCase().split("_"))
                        .map(s -> s.substring(0, 1).toUpperCase() + s.substring(1))
                        .collect(Collectors.joining(" "));
            }
        });
    }
}
