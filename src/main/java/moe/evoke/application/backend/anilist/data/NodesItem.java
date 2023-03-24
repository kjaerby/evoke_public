package moe.evoke.application.backend.anilist.data;

import com.google.gson.annotations.SerializedName;

public class NodesItem {

    @SerializedName("summary")
    private String summary;

    @SerializedName("rating")
    private int rating;

    @SerializedName("id")
    private int id;

    @SerializedName("ratingAmount")
    private int ratingAmount;

    @SerializedName("user")
    private User user;

    public String getSummary() {
        return summary;
    }

    public int getRating() {
        return rating;
    }

    public int getId() {
        return id;
    }

    public int getRatingAmount() {
        return ratingAmount;
    }

    public User getUser() {
        return user;
    }

    @Override
    public String toString() {
        return
                "NodesItem{" +
                        "summary = '" + summary + '\'' +
                        ",rating = '" + rating + '\'' +
                        ",id = '" + id + '\'' +
                        ",ratingAmount = '" + ratingAmount + '\'' +
                        ",user = '" + user + '\'' +
                        "}";
    }
}