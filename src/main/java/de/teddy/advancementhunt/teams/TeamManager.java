package de.teddy.advancementhunt.teams;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;

public class TeamManager {

    private final HashMap<Player, Team> teams = new HashMap<>();

    public void setInTeam(Player player, Team team) {
        teams.put(player, team);
    }

    public Boolean isTeamEmpty(Team team) {
        return !this.teams.containsValue(team);
    }

    public ArrayList<Player> getPlayers(Team team) {
        ArrayList<Player> list = new ArrayList<>();
        for(Player key : teams.keySet()) {
            if(teams.get(key).equals(team)) {
                list.add(key);
            }
        }
        return list;
    }

    /** public Team getTeam(Player player) {
        for(Team key : teams.keySet()) {
            if(teams.get(key).equals(player)) {
                return key;
            }
        }
        return null;
    }*/

    public void clear() {
        this.teams.clear();
    }

    public HashMap<Player, Team> getTeams() { return teams; }

}
