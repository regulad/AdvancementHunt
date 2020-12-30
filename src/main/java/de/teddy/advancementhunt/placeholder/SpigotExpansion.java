package de.teddy.advancementhunt.placeholder;

import de.teddy.advancementhunt.AdvancementHunt;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SpigotExpansion extends PlaceholderExpansion {
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
        return "1.1-SNAPSHOT";
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
        if(player == null) {
            return "";
        }
        if(params.equals("time_remaining")) {
            return AdvancementHunt.getInstance().getActionbarManager().getRemainingTime();
        }
        if(params.equals("wins")) {
            if(AdvancementHunt.getInstance().getUtils().getFightUtil().getWins().containsKey(player)) {
                return String.valueOf(AdvancementHunt.getInstance().getUtils().getFightUtil().getWins().get(player));
            } else {
                return "0";
            }
        }
        if(params.equals("losses")) {
            if(AdvancementHunt.getInstance().getUtils().getFightUtil().getLooses().containsKey(player)) {
                return String.valueOf(AdvancementHunt.getInstance().getUtils().getFightUtil().getLooses().get(player));
            } else {
                return "0";
            }
        }

        if(params.equals("kills"))
        {
            if(AdvancementHunt.getInstance().getUtils().getFightUtil().getKills().containsKey(player))
            {
                return AdvancementHunt.getInstance().getUtils().getFightUtil().getKills().get(player) + "";
            }

            return 0 + "";
        }

        if(params.equals("deaths"))
        {
            if(AdvancementHunt.getInstance().getUtils().getFightUtil().getDeaths().containsKey(player))
            {
                return AdvancementHunt.getInstance().getUtils().getFightUtil().getDeaths().get(player) + "";
            }

            return 0 + "";
        }

        if(params.equals("id")) {
            if(AdvancementHunt.getInstance().getAdvancement_id() != null) {
                String[] advancement_name = AdvancementHunt.getInstance().getAdvancement_id().split("/");
                return advancement_name[1];
            }
        }
        return null;
    }
}
