package moe.evoke.application.backend.malsync;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import moe.evoke.application.backend.anilist.Anilist;
import moe.evoke.application.backend.db.Anime;
import moe.evoke.application.backend.db.Database;
import moe.evoke.application.backend.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.util.*;

public class MALSync {

    private static final Logger logger = LoggerFactory.getLogger(MALSync.class);
    private static final Map<Long, List<MALSyncProvider>> malSyncProviderMap = new HashMap<>();

    public static void importOffline() {
        if (true || Utils.downloadFileWithCurl("https://raw.githubusercontent.com/MALSync/MAL-Sync-Backup/master/mal.json", "mal.json")) {
            Gson gson = new Gson();
            try {
                JsonReader reader = new JsonReader(new FileReader("mal.json"));
                JsonElement element = gson.fromJson(reader, JsonElement.class);

                if (element.isJsonObject()) {
                    JsonObject rootObj = element.getAsJsonObject();
                    if (rootObj.has("anime")) {
                        JsonObject animeObj = rootObj.getAsJsonObject("anime");

                        animeObj.keySet().parallelStream().forEach(animeKey -> {
                            JsonObject obj = animeObj.getAsJsonObject(animeKey);

                            JsonElement pages = obj.get("Pages");
                            obj.remove("Pages");
                            obj.add("Sites", pages);

                            long malID = obj.get("id").getAsLong();
                            long anilistID = Database.instance().getAnilistIDForMALID(malID);

                            String cache = Database.instance().getMALSyncCache(anilistID);
                            if (cache != null) {
                                logger.info("Skipping " + anilistID);
                                return;
                            }

                            if (anilistID > -1) {
                                String json = gson.toJson(obj);

                                logger.info("Adding " + anilistID + " - " + json);
                                Database.instance().createMALSyncCache(anilistID, json);
                            }

                        });
                    }
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static List<MALSyncProvider> getProviderForAnime(Anime anime) {
        if (malSyncProviderMap.containsKey(anime.getAnilistID())) {
            return malSyncProviderMap.get(anime.getAnilistID());
        }

        String cache = Database.instance().getMALSyncCache(anime.getAnilistID());
        if (cache != null) {
            List<MALSyncProvider> result = jsonToResult(cache);
            malSyncProviderMap.put(anime.getAnilistID(), result);
            return result;
        }

        long malId = Anilist.getMalIDForAnime(anime);

        HttpResponse<String> response = Unirest.get("https://api.malsync.moe/mal/anime/" + malId).asString();

        logger.debug(response.getStatus() + ": " + response.getStatusText());

        if (response.isSuccess()) {
            Database.instance().createMALSyncCache(anime.getAnilistID(), response.getBody());
            List<MALSyncProvider> result = jsonToResult(response.getBody());
            malSyncProviderMap.put(anime.getAnilistID(), result);
            return result;
        }
        if (response.getStatus() == 400) {
            Database.instance().createMALSyncCache(anime.getAnilistID(), "{}");
        }

        return new ArrayList<>();
    }

    private static List<MALSyncProvider> jsonToResult(String cache) {
        List<MALSyncProvider> result = new ArrayList<>();

        Gson gson = new Gson();
        JsonObject json = gson.fromJson(cache, JsonObject.class);

        if (json != null) {
            JsonObject sites = json.getAsJsonObject("Sites");
            if (sites != null) {
                for (Map.Entry<String, JsonElement> entry : sites.entrySet()) {
                    JsonObject providerList = entry.getValue().getAsJsonObject();
                    for (String providerKey : providerList.keySet()) {
                        JsonObject provider = providerList.get(providerKey).getAsJsonObject();

                        if (provider.has("title") && provider.get("title").getAsString().toLowerCase().contains("(dub)")) {
                            continue;
                        }

                        MALSyncProvider malSyncProvider = new MALSyncProvider();
                        malSyncProvider.provider = entry.getKey();
                        if (provider.has("identifier")) {
                            malSyncProvider.identifier = provider.get("identifier").getAsString();
                        }
                        malSyncProvider.url = provider.get("url").getAsString();
                        result.add(malSyncProvider);
                    }
                }
            }
        }

        return result;
    }

    public static void refreshCacheForAnime(Anime anime) {
        Calendar nowCal = GregorianCalendar.getInstance();
        nowCal.setFirstDayOfWeek(Calendar.MONDAY);
        nowCal.add(Calendar.DAY_OF_YEAR, -1);

        Date date = Database.instance().getMALSyncCacheLastModified(anime.getAnilistID());

        Calendar cacheCal = GregorianCalendar.getInstance();
        cacheCal.setFirstDayOfWeek(Calendar.MONDAY);
        cacheCal.setTime(date);

        if (cacheCal.before(nowCal)) {
            Database.instance().removeMALSyncCache(anime.getAnilistID());
            malSyncProviderMap.remove(anime.getAnilistID());
            MALSync.getProviderForAnime(anime);
        }

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
