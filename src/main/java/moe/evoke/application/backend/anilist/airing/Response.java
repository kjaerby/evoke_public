package moe.evoke.application.backend.anilist.airing;

import com.google.gson.annotations.SerializedName;

public class Response {

    @SerializedName("data")
    private Data data;

    public Data getData() {
        return data;
    }

    @Override
    public String toString() {
        return
                "Response{" +
                        "data = '" + data + '\'' +
                        "}";
    }
}