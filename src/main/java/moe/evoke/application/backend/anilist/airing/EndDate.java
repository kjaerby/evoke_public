package moe.evoke.application.backend.anilist.airing;

import com.google.gson.annotations.SerializedName;

public class EndDate {

    @SerializedName("month")
    private Object month;

    @SerializedName("year")
    private Object year;

    @SerializedName("day")
    private Object day;

    public Object getMonth() {
        return month;
    }

    public Object getYear() {
        return year;
    }

    public Object getDay() {
        return day;
    }

    @Override
    public String toString() {
        return
                "EndDate{" +
                        "month = '" + month + '\'' +
                        ",year = '" + year + '\'' +
                        ",day = '" + day + '\'' +
                        "}";
    }
}