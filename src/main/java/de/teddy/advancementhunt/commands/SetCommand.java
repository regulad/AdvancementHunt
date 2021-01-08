package de.teddy.advancementhunt.commands;

import de.teddy.advancementhunt.AdvancementHunt;
import de.teddy.advancementhunt.mysql.MySQLManager;
import de.teddy.advancementhunt.permissions.Permission;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SetCommand implements CommandExecutor {

    private final AdvancementHunt plugin;

    public SetCommand(AdvancementHunt plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if(!(sender instanceof Player)) {
            return false;
        }

        Player player = (Player) sender;

        if(!(args.length == 1)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b/set <TYPE>"));
            return false;
        }

        if(!(player.hasPermission(plugin.getPermissionManager().getPermission(Permission.SET)))) {
            return false;
        }

        if(args[0].equalsIgnoreCase("LobbySpawn")) {
            plugin.getUtils().getLocationUtil().setLobbySpawn(player);
            player.sendMessage(plugin.getPrefix() + ChatColor.translateAlternateColorCodes('&', "&e'LobbySpawn' &8» &a✔"));
        } else {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bSet&8: &eLobbySpawn"));
        }

        return false;
    }

}
