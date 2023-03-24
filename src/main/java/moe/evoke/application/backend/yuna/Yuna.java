package moe.evoke.application.backend.yuna;

import com.google.gson.Gson;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import moe.evoke.application.backend.anidb.AniDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Yuna {

    private static final Logger logger = LoggerFactory.getLogger(AniDB.class);

    public static YunaResponse getIDMapping(AnimeIDSource source, long ID) {
        HttpResponse<String> response = Unirest.get("https://relations.yuna.moe/api/ids?source=" + source.name().toLowerCase() + "&id=" + ID)
                .asString();
        logger.debug("https://relations.yuna.moe/api/ids?source=" + source.name().toLowerCase() + "&id=" + ID);

        if (response.isSuccess()) {
            String body = response.getBody();
            logger.debug(body);

            Gson gson = new Gson();
            YunaResponse yunaResponse = gson.fromJson(body, YunaResponse.class);
            return yunaResponse;
        }

        return null;
    }

}
