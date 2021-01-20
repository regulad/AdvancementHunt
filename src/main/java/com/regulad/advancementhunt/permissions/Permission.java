package com.regulad.advancementhunt.permissions;

import com.regulad.advancementhunt.AdvancementHunt;

public enum Permission {

	SET(AdvancementHunt.getInstance().getConfigManager().getMessage("Game.Permissions.SetLocation")),
	START(AdvancementHunt.getInstance().getConfigManager().getMessage("Game.Permissions.StartGame"));
	
	private final String permission;
	
	Permission(String permission) {
		this.permission = permission;
	}
	
	public String getPermission() { return permission; }
	
}
