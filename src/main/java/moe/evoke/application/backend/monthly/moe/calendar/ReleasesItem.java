package moe.evoke.application.backend.monthly.moe.calendar;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ReleasesItem {

    @SerializedName("date")
    private String date;

    @SerializedName("anime_ids")
    private List<Integer> animeIds;

    @SerializedName("catalog")
    private Object catalog;

    @SerializedName("name")
    private String name;

    @SerializedName("id")
    private int id;

    @SerializedName("source_id")
    private int sourceId;

    @SerializedName("format_short")
    private String formatShort;

    public String getDate() {
        return date;
    }

    public List<Integer> getAnimeIds() {
        return animeIds;
    }

    public Object getCatalog() {
        return catalog;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public int getSourceId() {
        return sourceId;
    }

    public String getFormatShort() {
        return formatShort;
    }

    @Override
    public String toString() {
        return
                "ReleasesItem{" +
                        "date = '" + date + '\'' +
                        ",anime_ids = '" + animeIds + '\'' +
                        ",catalog = '" + catalog + '\'' +
                        ",name = '" + name + '\'' +
                        ",id = '" + id + '\'' +
                        ",source_id = '" + sourceId + '\'' +
                        ",format_short = '" + formatShort + '\'' +
                        "}";
    }
}