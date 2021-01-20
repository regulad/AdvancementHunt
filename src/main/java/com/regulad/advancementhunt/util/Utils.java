package com.regulad.advancementhunt.util;

public class Utils {

    private final WorldUtil worldUtil = new WorldUtil();
    private final LocationUtil locationUtil = new LocationUtil();
    private final FightUtil fightUtil = new FightUtil();

    public WorldUtil getWorldUtil() {
        return worldUtil;
    }

    public LocationUtil getLocationUtil() { return locationUtil; }

    public FightUtil getFightUtil() { return fightUtil; }
}
