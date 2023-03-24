package moe.evoke.application.backend.util;

import com.github.kokorin.jaffree.ffmpeg.FFmpeg;
import com.github.kokorin.jaffree.ffmpeg.UrlInput;
import com.github.kokorin.jaffree.ffmpeg.UrlOutput;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.dom.ThemeList;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.theme.lumo.Lumo;
import moe.evoke.application.backend.Broadcaster;
import moe.evoke.application.backend.Config;
import moe.evoke.application.backend.anilist.Anilist;
import moe.evoke.application.backend.anilist.data.EdgesItem;
import moe.evoke.application.backend.anilist.data.Node;
import moe.evoke.application.backend.anilist.data.Relations;
import moe.evoke.application.backend.anilist.data.Title;
import moe.evoke.application.backend.db.Anime;
import moe.evoke.application.backend.db.Database;
import moe.evoke.application.backend.db.Episode;
import moe.evoke.application.backend.db.NewsType;
import moe.evoke.application.backend.discord.Discord;
import moe.evoke.application.views.main.IPFSMode;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Cookie;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.*;

public class Utils {

    private static final Logger logger = LoggerFactory.getLogger(Utils.class);
    private static final Map<Long, StreamResource> animeCoverResouceMap = new HashMap<>();

    public static IPFSMode getIPFSMode() {
        Cookie cookie = getCookieByName(IPFSMode.class.getSimpleName());
        if (cookie != null) {
            return IPFSMode.valueOf(cookie.getValue());
        }
        return null;
    }

    public static Cookie getCookieByName(String name) {
        Cookie[] cookies = VaadinService.getCurrentRequest().getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (name.equals(cookie.getName())) {
                    return cookie;
                }
            }
        }
        return null;
    }

    public static boolean isDarkModeEnabled() {
        Cookie darkModeCookie = getCookieByName("darkmode");

        if (darkModeCookie != null) {
            boolean darkMode = Boolean.parseBoolean(darkModeCookie.getValue());
            return darkMode;
        }

        return false;
    }

    public static void checkDarkMode() {
        ThemeList themeList = UI.getCurrent().getElement().getThemeList();
        if (isDarkModeEnabled()) {
            themeList.add(Lumo.DARK);
        } else {
            themeList.remove(Lumo.DARK);
        }

    }

    public static String getFileChecksum(MessageDigest digest, File file) throws IOException {
        //Get file input stream for reading the file content
        FileInputStream fis = new FileInputStream(file);

        //Create byte array to read data in chunks
        byte[] byteArray = new byte[1024];
        int bytesCount = 0;

        //Read file data and update in message digest
        while ((bytesCount = fis.read(byteArray)) != -1) {
            digest.update(byteArray, 0, bytesCount);
        }

        //close the stream; We don't need it now.
        fis.close();

        //Get the hash's bytes
        byte[] bytes = digest.digest();

        //This bytes[] has bytes in decimal format;
        //Convert it to hexadecimal format
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }

        //return complete hash
        return sb.toString();
    }

    public static void generateEpisodeNews(Episode episode) {
        Anime anime = episode.getAnime();

        String title = "";
        String content = "";

        Database.instance().createNews(NewsType.EPISODE, title, content, null, episode);
        Broadcaster.broadcast("Added Episode " + episode.getNumber() + " for Anime " + anime.getName());
        Discord.announceEpisode(anime, episode);
    }

    public static void generateAnimeNews(Anime anime) {

        String title = "";
        String content = "";

        Database.instance().createNews(NewsType.ANIME, title, content, anime, null);
        Broadcaster.broadcast("Added Anime " + anime.getName());
    }

    public static boolean animeTitleContains(String searchName, Anime anime) {
        Title title = anime.getData().getData().getMedia().getTitle();
        boolean match = false;
        if (title.getEnglish() != null) {
            if (!title.getEnglish().isEmpty() && title.getEnglish().toLowerCase().contains(searchName.toLowerCase())) {
                match = true;
            }
        }
        if (!match && title.getUserPreferred() != null) {
            if (!title.getUserPreferred().isEmpty() && title.getUserPreferred().toLowerCase().contains(searchName.toLowerCase())) {
                match = true;
            }
        }
        if (!match && title.getRomaji() != null) {
            if (!title.getRomaji().isEmpty() && title.getRomaji().toLowerCase().contains(searchName.toLowerCase())) {
                match = true;
            }
        }
        if (!match && title.getNative() != null) {
            if (!title.getNative().isEmpty() && title.getNative().toLowerCase().contains(searchName.toLowerCase())) {
                match = true;
            }
        }

        return match;
    }

    public static void fillAnimeMetadata(long anilistID) {
        Anime anime = Database.instance().getAnimeByAnilistID(anilistID);
        CoverUtil.getCoverForAnime(anime);
    }

    public static Image generateAnimeCoverImage(Anime anime) {

        StreamResource resource;
        if (!animeCoverResouceMap.containsKey(anime.getID())) {
            StreamResource sr = new StreamResource(anime.getAnilistID() + "-cover", () -> CoverUtil.getCoverForAnime(anime));
            sr.setCacheTime(60 * 60 * 1000);
            animeCoverResouceMap.put(anime.getID(), sr);
        }

        resource = animeCoverResouceMap.get(anime.getID());

        Image image = new Image(resource, String.valueOf(anime.getAnilistID()));
        return image;
    }

    public static boolean downloadFileWithCurl(String url, String fileName) {
        logger.debug("Download '" + fileName + "' from '" + url + "'");

        try {
            String[] commandNormal = {"curl", "-L", "-o", fileName, "-C", "-", url, "-H", "user-agent: " + Config.getRequestUserAgent()};
            String[] commandTwist = {"curl", "-L", "-o", fileName, "-C", "-", url, "-H", "user-agent: " + Config.getRequestUserAgent(), "-H", "Referer: https://twist.moe/"};
            var processBuilder = new ProcessBuilder();
            int returnCode;
            if (url.contains("twist.moe")) {
                returnCode = processBuilder.command(commandTwist).inheritIO().start().waitFor();
            } else {
                returnCode = processBuilder.command(commandNormal).inheritIO().start().waitFor();
            }

            if (returnCode != 0) {
                logger.error("curl returned: " + returnCode);
                return false;
            }

            File downloadFile = new File(fileName);
            long fileSizeInBytes = downloadFile.length();
            long fileSizeInKB = fileSizeInBytes / 1024;
            long fileSizeInMB = fileSizeInKB / 1024;
            if (fileSizeInMB < 25) {
                downloadFile.delete();
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return new File(fileName).exists();
    }

    public static File downloadFileWithAria(String url) {
        logger.debug("Download from '" + url + "'");

        String tmpFolderName = UUID.randomUUID().toString();
        File tmpFolder = new File(tmpFolderName);
        tmpFolder.mkdirs();

        try {
            String[] command = {"aria2c", "--seed-time=0", url};
            var processBuilder = new ProcessBuilder();
            int returnCode = processBuilder.command(command).inheritIO().directory(tmpFolder).start().waitFor();

            if (returnCode != 0) {
                logger.error("aria2c returned: " + returnCode);
                tmpFolder.delete();
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            tmpFolder.delete();
            return null;
        }


        return tmpFolder;
    }

    public static float getVideoLength(File video) {
        String[] command = {"./misc/extract_time_from_video.sh", video.getAbsolutePath()};

        try {

            File outputFile = new File(video.getName() + ".time");

            ProcessBuilder processBuilder = new ProcessBuilder();
            int returnCode = processBuilder.command(command).redirectOutput(outputFile).start().waitFor();

            if (returnCode != 0) {
                return 0;
            }

            if (outputFile.exists()) {
                List<String> lines = Files.readAllLines(outputFile.toPath());
                Collections.reverse(lines);
                for (String line : lines) {
                    logger.debug(line);
                    if (line.contains("time=")) {
                        int startIdx = line.indexOf("time=");
                        int endIdx = line.indexOf("bitrate=");
                        String timeStr = line.substring(startIdx, endIdx).replace("time=", "").trim();

                        String[] units = timeStr.split(":");
                        String hoursStr = units[0];
                        String minutesStr = units[1];
                        String secondsStr = units[2];

                        if (secondsStr.indexOf(".") > -1) {
                            secondsStr = secondsStr.substring(0, secondsStr.indexOf("."));
                        }

                        int hours = Integer.parseInt(hoursStr);
                        int minutes = Integer.parseInt(minutesStr);
                        int seconds = Integer.parseInt(secondsStr);

                        minutes += hours * 60;
                        seconds += minutes * 60;

                        outputFile.delete();
                        return seconds;
                    }
                }

                outputFile.delete();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public static boolean isWithinRange(double input1, double input2, double deviation) {
        return Math.abs(input1 - input2) <= deviation;
    }

    public static File extractSubtitle(File video) {

        File output = new File(video.getName() + ".ass");

        // ffmpeg -i 1-1.mp4 -c:v libx264 -x264-params nal-hrd=cbr -b:v 5M -minrate 5M -maxrate 5M -bufsize 10M -c:a copy -vf "ass=1-1.ass" -movflags +faststart 1-1_burned.mp4

        FFmpeg.atPath()
                .addInput(UrlInput.fromPath(video.toPath()))
                .addOutput(UrlOutput.toPath(output.toPath()))
                .setOutputListener(line -> logger.debug(line))
                .execute();

        return output;
    }

    public static File burninSubtitle(File video, File assSubtitle) {

        File output = new File(video.getName() + "_burnin.mp4");


        // ffmpeg -i 1-1.mp4 -c:v libx264 -x264-params nal-hrd=cbr -b:v 5M -minrate 5M -maxrate 5M -bufsize 10M -c:a copy -vf "ass=1-1.ass" -movflags +faststart 1-1_burned.mp4

        FFmpeg.atPath()
                .addInput(UrlInput.fromPath(video.toPath()))
                .addOutput(UrlOutput.toPath(output.toPath()))
                .addArguments("-c:v", "libx264")
                .addArguments("-preset", "slow")
                .addArguments("-crf", "23")
                .addArguments("-tune", "animation")
                .addArguments("-c:a", "aac")
                .addArguments("-ac", "2")
                .addArguments("-b:a", "320k")
                .addArguments("-ar", "48000")
                .addArguments("-vf", "format=yuv420p, scale=-1:1080, ass='" + assSubtitle.getAbsolutePath() + "'")
                .addArguments("-movflags", "+faststart")
                .setOutputListener(line -> logger.debug(line))
                .execute();

        return output;
    }

    public static Image getLogoAsImge() {
        String logoFile = isDarkModeEnabled() ? "images/logo-dark.png" : "images/logo.png";
        Image logo = new Image(logoFile, "evoke logo");
        return logo;
    }

    public static Image getIconAsImge() {
        String logoFile = isDarkModeEnabled() ? "icons/icon-dark.png" : "icons/icon.png";
        Image logo = new Image(logoFile, "evoke logo");
        return logo;
    }

    public static Episode findSequelForEpisode(Anime anime, int targetEpisode) {

        logger.debug("Searching Sequel for " + anime.getName() + " with episode " + targetEpisode);

        int delta = targetEpisode - anime.getEpisodeCount();
        if (delta < 0) {
            return null;
        }

        Relations relations = anime.getData().getData().getMedia().getRelations();
        for (EdgesItem edge : relations.getEdges()) {
            if (!edge.getNode().getType().equalsIgnoreCase("anime") || !edge.getRelationType().equalsIgnoreCase("SEQUEL")) {
                continue;
            }

            Node node = edge.getNode();
            Anime relatedAnime = Database.instance().getAnimeByAnilistID(node.getId());
            Anilist.refreshCacheForAnime(relatedAnime);
            relatedAnime = Database.instance().getAnimeByAnilistID(node.getId());

            Optional<Episode> episode = Database.instance().getEpisodesForAnime(relatedAnime).stream().filter(ep -> ep.getNumber() == delta).findFirst();
            if (relatedAnime.getEpisodeCount() == 0 && relatedAnime.getStatus().equalsIgnoreCase("RELEASING")) {
                if (episode.isPresent()) {
                    return episode.get();
                } else {
                    Database.instance().createEpisode(relatedAnime, delta);
                    Optional<Episode> newEpisode = Database.instance().getEpisodesForAnime(relatedAnime).stream().filter(ep -> ep.getNumber() == delta).findFirst();
                    if (newEpisode.isPresent()) {
                        return newEpisode.get();
                    }
                }
            }

            if (relatedAnime.getEpisodeCount() > delta) {
                if (episode.isPresent()) {
                    return episode.get();
                }
            }

            return findSequelForEpisode(relatedAnime, delta);
        }

        return null;
    }

    public static String generateInviteCode() {
        String generatedString = RandomStringUtils.random(10, true, true);
        generatedString += "-";
        generatedString += RandomStringUtils.random(10, true, true);
        generatedString += "-";
        generatedString += RandomStringUtils.random(10, true, true);

        Database.instance().createInviteCode(generatedString);

        return generatedString;
    }

    public static String getLocation() {
        VaadinServletRequest request = (VaadinServletRequest) VaadinService.getCurrentRequest();
        StringBuffer uriString = request.getRequestURL();
        return uriString.toString();
    }

    public static String milliToETA(long milliseconds) {
        int seconds = (int) (milliseconds / 1000) % 60;
        int minutes = (int) ((milliseconds / (1000 * 60)) % 60);
        int hours = (int) ((milliseconds / (1000 * 60 * 60)) % 24);
        return String.format("%dh, %dmin, %ds", hours, minutes, seconds);
    }

}
