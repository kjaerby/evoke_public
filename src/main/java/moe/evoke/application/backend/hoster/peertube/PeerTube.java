package moe.evoke.application.backend.hoster.peertube;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.gson.Gson;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import moe.evoke.application.backend.db.Database;
import moe.evoke.application.backend.db.Episode;
import moe.evoke.application.backend.hoster.HosterFile;
import moe.evoke.application.backend.hoster.peertube.data.listvideos.DataItem;
import moe.evoke.application.backend.hoster.peertube.data.listvideos.Response;
import moe.evoke.application.backend.mirror.distribution.DistributionTarget;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class PeerTube {

    private static final Logger logger = LoggerFactory.getLogger(PeerTube.class);

    private static final String baseURL = "https://<>";
    private static final int TOKEN_KEY = 1337;
    private static LoadingCache<Integer, String> tokenCache;

    private static String getToken() {
        if (tokenCache == null) {
            tokenCache = Caffeine.newBuilder()
                    .maximumSize(1)
                    .expireAfterWrite(1, TimeUnit.HOURS)
                    .refreshAfterWrite(1, TimeUnit.HOURS)
                    .build(key ->
                    {
                        HttpResponse<JsonNode> response = Unirest.post(baseURL + "/api/v1/users/token")
                                .header("Content-Type", "application/x-www-form-urlencoded")
                                .field("client_id", "awva7rpxplqpc9ky0kom4v7gh1kg6mfd")
                                .field("client_secret", "RMNoaGExLHmnIzaqrkTl1wsDOHp1xjnr")
                                .field("grant_type", "password")
                                .field("response_type", "code")
                                .field("username", "evoke")
                                .field("password", "qpb1tR72QRX2qVd1")
                                .asJson();

                        if (response.isSuccess()) {
                            return response.getBody().getObject().getString("access_token");
                        }

                        return "n/a";
                    });
        }

        return tokenCache.get(TOKEN_KEY);
    }

    public static String uploadFile(File fileToUpload) {

        HttpResponse<JsonNode> response = Unirest.post(baseURL + "/api/v1/videos/upload")
                .header("Authorization", "Bearer " + getToken())
                .field("videofile", fileToUpload)
                .field("channelId", "2")
                .field("privacy", "2")
                .field("nsfw", "false")
                .field("downloadEnabled", "false")
                .field("name", FilenameUtils.removeExtension(fileToUpload.getName()))
                .asJson();

        if (response.isSuccess()) {
            String uuid = response.getBody().getObject().getJSONObject("video").getString("uuid");
            String embedURL = "https://<>/videos/embed/" + uuid + "?title=0&peertubeLink=0&api=1";
            return embedURL;
        }

        return "n/a";
    }

    public static List<HosterFile> getFiles() {


        List<HosterFile> files = new ArrayList<>();

        HttpResponse<String> response = Unirest.get(baseURL + "/api/v1/users/me/videos?count=100")
                .header("Authorization", "Bearer " + getToken())
                .asString();

        Set<DataItem> dataItems = new HashSet<>();

        Gson gson = new Gson();
        Response result = gson.fromJson(response.getBody(), Response.class);
        dataItems.addAll(result.getData());

        int total = result.getTotal();
        if (total > 100) {
            int runs = (int) Math.round(((double) total) / 100.0) - 1;
            for (int i = 0; i < runs; i++) {
                int offset = (i + 1) * 100;
                HttpResponse<String> subResponse = Unirest.get(baseURL + "/api/v1/users/me/videos?count=100&start=" + offset)
                        .header("Authorization", "Bearer " + getToken())
                        .asString();
                Response subResult = gson.fromJson(subResponse.getBody(), Response.class);
                dataItems.addAll(subResult.getData());
            }
        }

        dataItems.parallelStream().forEach(item -> {
            try {
                HosterFile hosterFile = new HosterFile();
                hosterFile.name = item.getName();
                hosterFile.embed = baseURL + item.getEmbedPath() + "?title=0&peertubeLink=0&api=1";
                hosterFile.hostedEpisode = Database.instance().getHostedEpisodeForStreamURL(hosterFile.embed);

                if (hosterFile.hostedEpisode == null) {
                    String animeStr = item.getName().split("-")[0];
                    long animeID = Long.parseLong(animeStr);
                    String epNumStr = item.getName().split("-")[1];
                    long epNum = Long.parseLong(epNumStr);

                    Episode episodeObj = Database.instance().getAnimeByAnilistID(animeID).getEpisodes().stream().filter(episode1 -> episode1.getNumber() == epNum).findFirst().get();
                    Database.instance().createHostedEpisode(DistributionTarget.MEGA.getHoster(), episodeObj, hosterFile.embed);
                    hosterFile.hostedEpisode = Database.instance().getHostedEpisodeForStreamURL(hosterFile.embed);
                }

                if (hosterFile.hostedEpisode != null) {
                    hosterFile.episode = Database.instance().getEpisodeByID(hosterFile.hostedEpisode.getEpisodeID());
                    hosterFile.anime = Database.instance().getAnimeByID(hosterFile.episode.getAnimeID());
                }

                files.add(hosterFile);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        return files;
    }

    public static HosterFile remoteUploadVideo(String videoName, String url) {
        HttpResponse<String> response = Unirest.post(baseURL + "/api/v1/videos/imports")
                .header("Authorization", "Bearer " + getToken())
                .header("Content-Type", "application/x-www-form-urlencoded")
                .field("channelId", "2") // evoke = 2
                .field("name", videoName)
                .field("privacy", "2")
                .field("nsfw", "false")
                .field("downloadEnabled", "false")
                .field("targetUrl", url)
                .asString();

        logger.debug(response.getBody());
        Gson gson = new Gson();
        moe.evoke.application.backend.hoster.peertube.data.remoteupload.Response result = gson.fromJson(response.getBody(), moe.evoke.application.backend.hoster.peertube.data.remoteupload.Response.class);

        HosterFile hosterFile = new HosterFile();
        hosterFile.embed = baseURL + result.getVideo().getEmbedPath() + "?title=0&peertubeLink=0&api=1";
        hosterFile.name = videoName;

        return hosterFile;
    }

    public static boolean deleteVideo(String videoId) {
        HttpResponse<String> response = Unirest.delete(baseURL + "/api/v1/videos/" + videoId)
                .header("Authorization", "Bearer " + getToken())
                .asString();

        logger.info("Delete Video returned: " + response.getBody() + " [" + response.getStatus() + "]");
        return response.isSuccess() && response.getStatus() == 204;
    }
}
