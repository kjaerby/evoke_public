package moe.evoke.application.backend.hoster.vivio;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import moe.evoke.application.backend.Config;

import java.io.File;

public class Vivo {

    public static String uploadFile(File fileToUpload) {

        HttpResponse<JsonNode> response = Unirest.get("https://vivo.sx/api/v1/upload/" + fileToUpload.length())
                .header("X-AUTH", Config.getVivoAPIKey())
                .asJson();

        if (response.isSuccess()) {
            String uploadUrl = response.getBody().getObject().get("upload_url").toString();
            HttpResponse<String> response2 = Unirest.post(uploadUrl)
                    .field("session", Config.getVivoAPIKey())
                    .field("action", "push")
                    .field("file", fileToUpload)
                    .asString();

            if (response2.isSuccess()) {
                return response2.getBody();
            }
        }

        return "n/a";
    }
}
