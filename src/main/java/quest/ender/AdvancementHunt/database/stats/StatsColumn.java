package quest.ender.AdvancementHunt.database.stats;

public enum StatsColumn {
    KILLS("kills"),
    DEATHS("deaths"),
    LOSSES("losses"),
    WINS("wins");

    private final String columnTitle;

    StatsColumn(String columnTitle) {
        this.columnTitle = columnTitle;
    }

    public String getColumnTitle() {
        return this.columnTitle;
    }
}
