package de.teddy.advancementhunt.placeholder;

import de.teddy.advancementhunt.AdvancementHunt;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SpigotExpansion extends PlaceholderExpansion {

    private final AdvancementHunt plugin;

    public SpigotExpansion(AdvancementHunt plugin) {
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
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return "";
        }

        if (params.equals("time_remaining")) {
            return plugin.getActionbarManager().getRemainingTime();
        }

        if (params.equals("wins")) {
            if (plugin.getUtils().getFightUtil().getWins().containsKey(player)) {
                return String.valueOf(plugin.getUtils().getFightUtil().getWins().get(player));
            } else {
                return "0";
            }
        }

        if (params.equals("losses")) {
            if (plugin.getUtils().getFightUtil().getLosses().containsKey(player)) {
                return String.valueOf(plugin.getUtils().getFightUtil().getLosses().get(player));
            } else {
                return "0";
            }
        }

        if (params.equals("kills")) {
            if (plugin.getUtils().getFightUtil().getKills().containsKey(player)) {
                return plugin.getUtils().getFightUtil().getKills().get(player) + "";
            }

            return 0 + "";
        }

        if (params.equals("deaths")) {
            if (plugin.getUtils().getFightUtil().getDeaths().containsKey(player)) {
                return plugin.getUtils().getFightUtil().getDeaths().get(player) + "";
            }

            return 0 + "";
        }

        if (params.equals("id")) {
            if (plugin.getAdvancement_id() != null) {
                return plugin.getAdvancement_id();
            }
        }

        if (params.equals("name")) {
            if (AdvancementHunt.getInstance().getAdvancement_id() != null) {
                String[] advancement_name = plugin.getAdvancement_id().split("/");
                return advancement_name[1];
            }
        }

        return null;
    }
}
