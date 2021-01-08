package de.teddy.advancementhunt.teams;

public enum Team {

    HUNTER("Hunter"),
    PLAYER("Player");

    private final String teamName;

    Team(String teamName) {
        this.teamName = teamName;
    }

    public String getTeamName() { return teamName; }

}
