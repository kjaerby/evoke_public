package moe.evoke.application.backend.anidb;

import com.github.underscore.lodash.U;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import moe.evoke.application.backend.anidb.data.AniDBResponse;
import moe.evoke.application.backend.anidb.data.Anime;
import moe.evoke.application.backend.anidb.data.ResourceItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AniDB {

    private static final Logger logger = LoggerFactory.getLogger(AniDB.class);

    public static long getMalID(long aniDbID) {
        HttpResponse<String> response = Unirest.get("http://api.anidb.net:9001/httpapi?request=anime&client=malhttpclient&clientver=1&protover=1&aid=" + aniDbID)
                .asString();

        try {
            if (response.isSuccess()) {
                String body = response.getBody();
                body = body.replace("<title ", "<title array=\"true\" ");
                body = body.replace("<resource ", "<resource array=\"true\" ");
                body = body.replace("<externalentity ", "<externalentity array=\"true\" ");
                body = body.replace("xml:lang", "lang");

                logger.debug(body);

                String jsonPrettyPrintString = U.xmlToJson(body);

                Gson gson = new Gson();
                AniDBResponse aniDBResponse = gson.fromJson(jsonPrettyPrintString, AniDBResponse.class);

                Anime value = aniDBResponse.getAnime();

                System.out.println(value.getId());
                System.out.println(value.getTitles().getTitle().get(0).getText());

                long malID = -1;
                for (ResourceItem resource : value.getResources().getResource()) {
                    if (resource.getType().equals("2")) {
                        JsonElement element = resource.getExternalentity();
                        if (element.isJsonArray()) {
                            for (JsonElement jsonElement : element.getAsJsonArray()) {
                                malID = getMalID(jsonElement);
                            }
                        } else {
                            malID = getMalID(element);
                        }
                    }
                }

                return malID;
            }
        } catch (Exception ex) {

        }

        return -1;
    }

    private static long getMalID(JsonElement element) {
        if (element.isJsonObject()) {
            JsonObject jsonObject = element.getAsJsonObject();

            if (jsonObject.has("identifier")) {
                return jsonObject.get("identifier").getAsLong();
            }
        }
        return -1;
    }

}
