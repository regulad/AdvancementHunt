package de.teddy.advancementhunt.commands;

import de.teddy.advancementhunt.AdvancementHunt;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class DebugCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if(!(sender instanceof Player)) {
            return false;
        }
        Player player = (Player) sender;

        player.sendMessage(AdvancementHunt.getInstance().getTeamManager().getTeams().get(player).getTeamName());

        return false;
    }
}
