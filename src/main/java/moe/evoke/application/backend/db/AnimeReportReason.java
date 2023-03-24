package moe.evoke.application.backend.db;

public enum AnimeReportReason {
    COPYRIGHT("Copyright Abused"),
    MISSING_EPISODE("Episode Missing"),
    HOSTER_NOT_WORKING("Hoster not Woring");

    private final String readableReason;

    AnimeReportReason(String readableReason) {
        this.readableReason = readableReason;
    }

    public String getReadableReason() {
        return readableReason;
    }

    @Override
    public String toString() {
        return getReadableReason();
    }
}
