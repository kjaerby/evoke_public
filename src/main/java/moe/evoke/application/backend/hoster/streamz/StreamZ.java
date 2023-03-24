package moe.evoke.application.backend.hoster.streamz;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import moe.evoke.application.backend.Config;
import moe.evoke.application.backend.hoster.HosterFile;
import moe.evoke.application.backend.hoster.streamz.data.ResponseItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class StreamZ {

    private static final Logger logger = LoggerFactory.getLogger(StreamZ.class);

    public static String uploadFile(File fileToUpload) {
        HttpResponse<String> response = Unirest.post("http://api.streamz.ws/api.upload.php")
                .field("api", Config.getStreamZAPIKey())
                .field("file", fileToUpload)
                .asString();

        if (response.isSuccess()) {
            String[] lines = response.getBody().split("\n");
            Optional<String> embedStrOpt = Arrays.stream(lines).filter(s -> s.contains("embedcrypt")).findFirst();
            if (embedStrOpt.isPresent()) {
                return embedStrOpt.get().replace("embedcrypt|", "").trim();
            }
        }

        return "n/a";
    }

    public static List<HosterFile> getFiles() {
        HttpResponse<String> response = Unirest.post("https://streamz.ws/api.dll")
                .field("api", Config.getStreamZAPIKey())
                .field("per_page", "100")
                .field("page", "0")
                .field("mode", "list")
                .field("crypt", "yes")
                .asString();

        logger.debug(response.getBody());

        Gson gson = new Gson();
        Type listType = new TypeToken<List<ResponseItem>>() {
        }.getType();
        List<ResponseItem> result = gson.fromJson(response.getBody(), listType);

        List<HosterFile> files = new ArrayList<>();
        result.forEach(responseItem -> {
            HosterFile hosterFile = new HosterFile();
            hosterFile.name = responseItem.getName();
            hosterFile.embed = responseItem.getStreamcryptembed();
            files.add(hosterFile);
        });

        return files;
    }
}
