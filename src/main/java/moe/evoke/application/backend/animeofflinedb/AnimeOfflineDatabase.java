package moe.evoke.application.backend.animeofflinedb;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import moe.evoke.application.backend.util.Utils;

import java.io.FileReader;

public class AnimeOfflineDatabase {

    public static OfflineAnime loadOfflineAnimeData() {
        try {
            boolean downloaded = Utils.downloadFileWithCurl("https://raw.githubusercontent.com/manami-project/anime-offline-database/master/anime-offline-database.json", "anime-offline-database.json");

            if (downloaded) {
                Gson gson = new Gson();
                JsonReader reader = new JsonReader(new FileReader("anime-offline-database.json"));

                OfflineAnime offlineAnime = gson.fromJson(reader, OfflineAnime.class);
                return offlineAnime;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }


}
