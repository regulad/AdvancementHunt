package com.regulad.advancementhunt.util;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class LocationUtil {

    private final File file = new File("plugins//AdvancementHunt//locations.yml");
    private final YamlConfiguration locations = YamlConfiguration.loadConfiguration(file);
	private final HashMap<String, Location> locationCacheMap = new HashMap<>();

	String root = "AdvancementHunt.";
    
	public void setLobbySpawn(Player player) {
		locations.set(root + "LobbySpawn.World", player.getLocation().getWorld().getName());
		locations.set(root + "LobbySpawn.X", player.getLocation().getX());
		locations.set(root + "LobbySpawn.Y", player.getLocation().getY());
		locations.set(root + "LobbySpawn.Z", player.getLocation().getZ());
		locations.set(root + "LobbySpawn.Pitch", player.getLocation().getPitch());
		locations.set(root + "LobbySpawn.Yaw", player.getLocation().getYaw());
		
		try {
			locations.save(this.file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 *
	 * @param player player getting teleported
	 * @param name unknown documentation.
	 *
	 * Checks if the location of name exists in memory, before attempting to grab the configuration increasing load and using more memory.
	 */
	public void teleport(Player player, String name) {

		if (!locationCacheMap.containsKey(name)) {
			World world = Bukkit.getWorld(locations.getString(root + name + ".World"));
			double x = locations.getDouble(root + name + ".X");
			double y = locations.getDouble(root + name + ".Y");
			double z = locations.getDouble(root + name + ".Z");
			float yaw = (float) locations.getDouble(root + name + ".Yaw");
			float pitch = (float) locations.getDouble(root + name + ".Pitch");

			locationCacheMap.put(name, new Location(world, x, y, z, yaw, pitch));
		}
		
		player.teleport(locationCacheMap.get(name));
	}
}
