package moe.evoke.application.backend.hoster.streamtape;

import com.google.gson.Gson;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import moe.evoke.application.backend.Config;
import moe.evoke.application.backend.db.Anime;
import moe.evoke.application.backend.hoster.streamtape.listfolders.FoldersItem;
import moe.evoke.application.backend.hoster.streamtape.listfolders.Response;
import moe.evoke.application.backend.util.Utils;

import java.io.File;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

public class Streamtape {


    public static String uploadAnime(Anime anime, File fileToUpload) {
        Map<String, String> folders = getFolders();

        String animesFolder = folders.get("animes");
        if (animesFolder == null || animesFolder.isEmpty()) {
            animesFolder = createFolder("animes");
        }

        Map<String, String> subFolders = getFolders(animesFolder);
        String animeFolder = subFolders.get(String.valueOf(anime.getAnilistID()));
        if (animeFolder == null || animeFolder.isEmpty()) {
            animeFolder = createFolder(animesFolder, String.valueOf(anime.getAnilistID()));
        }


        String uploadURL = uploadStep1(animeFolder, fileToUpload);
        String uploadID = uploadStep2(uploadURL, fileToUpload);

        return "https://streamtape.com/e/" + uploadID;
    }

    public static String uploadStep2(String uploadURL, File fileToUpload) {


        HttpResponse<String> response = Unirest.post(uploadURL).field("file1", fileToUpload).asString();

        if (response.isSuccess()) {
            Gson gson = new Gson();
            moe.evoke.application.backend.hoster.streamtape.upload2.Response upload2Response = gson.fromJson(response.getBody(), moe.evoke.application.backend.hoster.streamtape.upload2.Response.class);
            return upload2Response.getResult().getId();
        }

        return "n/a";
    }

    public static String uploadStep1(String folder, File fileToUpload) {
        try {
            MessageDigest shaDigest = MessageDigest.getInstance("SHA-256");
            String shaChecksum = Utils.getFileChecksum(shaDigest, fileToUpload);

            String requestURL = "https://api.streamtape.com/file/ul?login=" + Config.getStreamtapeAPIUser() + "&key=" + Config.getStreamtapeAPIPassword() + "&sha256=" + shaChecksum;
            if (folder != null && !folder.isEmpty()) {
                requestURL += "&folder=" + folder;
            }

            HttpResponse<String> response = Unirest.post(requestURL).asString();

            if (response.isSuccess()) {
                Gson gson = new Gson();
                moe.evoke.application.backend.hoster.streamtape.upload1.Response upload1Response = gson.fromJson(response.getBody(), moe.evoke.application.backend.hoster.streamtape.upload1.Response.class);
                return upload1Response.getResult().getUrl();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return "n/a";
    }

    public static String createFolder(String name) {
        return createFolder(null, name);
    }

    public static String createFolder(String parent, String name) {
        String requestURL = "https://api.streamtape.com/file/createfolder?login=" + Config.getStreamtapeAPIUser() + "&key=" + Config.getStreamtapeAPIPassword() + "&name=" + name;
        if (parent != null && !parent.isEmpty()) {
            requestURL += "&pid=" + parent;
        }

        HttpResponse<String> response = Unirest.post(requestURL)
                .header("Cookie", "__cfduid=d23ced9f0a906f334add4f6125bcfb81a1617625043")
                .asString();

        if (response.isSuccess()) {
            Gson gson = new Gson();
            moe.evoke.application.backend.hoster.streamtape.createfolder.Response listFolderResponse = gson.fromJson(response.getBody(), moe.evoke.application.backend.hoster.streamtape.createfolder.Response.class);
            return listFolderResponse.getResult().getFolderid();
        }

        return "n/a";
    }

    public static Map<String, String> getFolders() {
        return getFolders(null);
    }

    public static Map<String, String> getFolders(String parent) {
        Map<String, String> folderToIds = new HashMap<>();

        String requestURL = "https://api.streamtape.com/file/listfolder?login=" + Config.getStreamtapeAPIUser() + "&key=" + Config.getStreamtapeAPIPassword();
        if (parent != null && !parent.isEmpty()) {
            requestURL += "&folder=" + parent;
        }

        HttpResponse<String> response = Unirest.get(requestURL)
                .asString();

        if (response.isSuccess()) {
            Gson gson = new Gson();
            Response listFolderResponse = gson.fromJson(response.getBody(), Response.class);
            for (FoldersItem foldersItem : listFolderResponse.getResult().getFolders()) {
                folderToIds.put(foldersItem.getName(), foldersItem.getId());
            }
        }

        return folderToIds;
    }


}
