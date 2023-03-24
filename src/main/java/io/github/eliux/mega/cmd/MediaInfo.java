package io.github.eliux.mega.cmd;

import io.github.eliux.mega.error.MegaInvalidResponseException;

public class MediaInfo {

    private final String remotePath;
    private final int width;
    private final int height;
    private final int fps;
    private final int playtime;

    public MediaInfo(String remotePath, int width, int height, int fps, int playtime) {
        this.remotePath = remotePath;
        this.width = width;
        this.height = height;
        this.fps = fps;
        this.playtime = playtime;
    }

    public static MediaInfo parseMediaInfo(String mediaInfoStr) {
        final String[] tokens = parseTokens(mediaInfoStr);
        try {
            return parseMediaInfo(tokens);
        } catch (Exception ex) {
            final MegaInvalidResponseException megaEx =
                    new MegaInvalidResponseException(
                            "Error while parsing file info from %s", mediaInfoStr
                    );
            megaEx.addSuppressed(ex);
            throw megaEx;
        }
    }

    private static final String[] parseTokens(String fileInfoStr) {
        final String[] tokens = fileInfoStr.replace("\\t", "\\s").split("\\s+");

        if (tokens.length != 5) {
            throw new MegaInvalidResponseException(
                    "The gotten file format is incorrect: Should have 6 tokens"
            );
        }

        return tokens;
    }

    private static final MediaInfo parseMediaInfo(String[] tokens) {

        String path = tokens[0];

        final int width = Integer.parseInt(tokens[1]);
        final int height = Integer.parseInt(tokens[2]);
        final int fps = Integer.parseInt(tokens[3]);
        final int playtime = parsePlaytime(tokens[4]);

        return new MediaInfo(path, width, height, fps, playtime);
    }

    private static int parsePlaytime(String token) {

        int result = 0;

        int minuteIdx = token.indexOf("M");
        if (minuteIdx > -1) {
            String minuteStr = token.substring(0, minuteIdx);
            int minutes = Integer.parseInt(minuteStr);
            result += minutes * 60;

            String secondsStr = token.substring(minuteIdx + 1).replace("s", "");
            int seconds = Integer.parseInt(secondsStr);
            result += seconds;
        }

        return result;
    }

    public String getRemotePath() {
        return remotePath;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getFps() {
        return fps;
    }

    public int getPlaytime() {
        return playtime;
    }
}
