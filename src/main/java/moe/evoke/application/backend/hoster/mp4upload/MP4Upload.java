package moe.evoke.application.backend.hoster.mp4upload;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import moe.evoke.application.backend.Config;
import moe.evoke.application.backend.hoster.mp4upload.upload.ResponseItem;
import moe.evoke.application.backend.mirror.distribution.DistributionTarget;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MP4Upload {

    private static final Logger logger = LoggerFactory.getLogger(MP4Upload.class);

    private static final int TOKEN_KEY = 1337;
    private static LoadingCache<Integer, MP4UploadData> tokenCache;

    public static String uploadFile(File fileToUpload) {
        long fileSizeInBytes = fileToUpload.length();
        long fileSizeInKB = fileSizeInBytes / 1024;
        long fileSizeInMB = fileSizeInKB / 1024;

        if (fileSizeInMB > 500) {
            logger.warn("File is bigger then 500mb (" + fileSizeInMB + "). Cannot upload to " + DistributionTarget.MP4UPLOAD.getLabel() + "!");
        }

        MP4UploadData uploadData = getUploadData();

        HttpResponse<String> response = Unirest.post(uploadData.uploadURL)
                .field("sess_id", uploadData.sessionID)
                .field("file", fileToUpload)
                .asString();

        if (response.isSuccess()) {
            Gson gson = new Gson();
            Type listOfMyClassObject = new TypeToken<ArrayList<ResponseItem>>() {
            }.getType();
            List<ResponseItem> outputList = gson.fromJson(response.getBody(), listOfMyClassObject);

            return "https://www.mp4upload.com/embed-" + outputList.get(0).getFileCode() + ".html";
        } else {
            logger.error(response.getBody());
        }

        return "n/a";
    }

    private static MP4UploadData getUploadData() {
        if (tokenCache == null) {
            tokenCache = Caffeine.newBuilder()
                    .maximumSize(1)
                    .expireAfterWrite(1, TimeUnit.HOURS)
                    .build(key ->
                    {
                        final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 11_2_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.114 Safari/537.36";
                        final String LOGIN_FORM_URL = "https://www.mp4upload.com/?op=login";
                        final String LOGIN_ACTION_URL = "https://www.mp4upload.com/";

                        Connection.Response loginForm = Jsoup.connect(LOGIN_FORM_URL)
                                .method(Connection.Method.GET)
                                .userAgent(USER_AGENT)
                                .execute();
                        HashMap<String, String> cookies = new HashMap<>(loginForm.cookies());

                        HashMap<String, String> formData = new HashMap<>();
                        formData.put("op", "login");
                        formData.put("redirect", "https://www.google.com/");
                        formData.put("login", Config.getMP4UploadUsername());
                        formData.put("password", Config.getMP4UploadPassword());

                        Connection.Response myFilesPage = Jsoup.connect(LOGIN_ACTION_URL)
                                .cookies(cookies)
                                .data(formData)
                                .method(Connection.Method.POST)
                                .userAgent(USER_AGENT)
                                .execute();

                        cookies = new HashMap<>(myFilesPage.cookies());
                        Connection.Response uploadPage = Jsoup.connect("https://www.mp4upload.com/?op=upload")
                                .cookies(cookies)
                                .method(Connection.Method.GET)
                                .userAgent(USER_AGENT)
                                .execute();

                        Document uploadPageDocument = uploadPage.parse();
                        String uploadAction = uploadPageDocument.select("#uploadfile").attr("action");
                        String sessID = uploadPageDocument.select("#uploadfile > input[type=hidden]:nth-child(1)").attr("value");
                        logger.debug("mp4upload data found: " + uploadAction + " - " + sessID);

                        MP4UploadData uploadData = new MP4UploadData();
                        uploadData.uploadURL = uploadAction;
                        uploadData.sessionID = sessID;
                        return uploadData;
                    });
        }

        return tokenCache.get(TOKEN_KEY);
    }

    private static class MP4UploadData {
        String sessionID;
        String uploadURL;
    }
}
