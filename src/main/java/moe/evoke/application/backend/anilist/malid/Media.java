package moe.evoke.application.backend.anilist.malid;

import com.google.gson.annotations.SerializedName;

public class Media {

    @SerializedName("id")
    private int id;

    @SerializedName("idMal")
    private int idMal;

    public int getId() {
        return id;
    }

    public int getIdMal() {
        return idMal;
    }
}