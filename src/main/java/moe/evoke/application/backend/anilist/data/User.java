package moe.evoke.application.backend.anilist.data;

import com.google.gson.annotations.SerializedName;

public class User {

    @SerializedName("name")
    private String name;

    @SerializedName("id")
    private int id;

    @SerializedName("avatar")
    private Avatar avatar;

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public Avatar getAvatar() {
        return avatar;
    }

    @Override
    public String toString() {
        return
                "User{" +
                        "name = '" + name + '\'' +
                        ",id = '" + id + '\'' +
                        ",avatar = '" + avatar + '\'' +
                        "}";
    }
}