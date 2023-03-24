package moe.evoke.application.backend.waifulabs;

import com.google.gson.Gson;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.apache.commons.codec.binary.Base64;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Waifulabs {

    private static final List<NewGirlsItem> waifuPool = new ArrayList<>(100);

    public static InputStream generateWaifu() {
        while (waifuPool.size() < 100) {
            HttpResponse<String> response = Unirest.post("https://api.waifulabs.com/generate")
                    .header("Content-Type", "application/json")
                    .body("{\"step\" : 0}")
                    .asString();

            if (response.isSuccess()) {
                Gson gson = new Gson();
                WaifuResponse waifuResponse = gson.fromJson(response.getBody(), WaifuResponse.class);

                waifuPool.addAll(waifuResponse.getNewGirls());
            }
        }

        Random rand = new Random();
        int idx = rand.nextInt(waifuPool.size());
        if (idx > 0) {
            NewGirlsItem randomElement = waifuPool.get(idx);

            byte[] data = Base64.decodeBase64(randomElement.getImage());

            return new ByteArrayInputStream(data);
        }

        return null;
    }
}
