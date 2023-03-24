package moe.evoke.application.backend.anilist.data;

import com.google.gson.annotations.SerializedName;

public class EndDate {

    @SerializedName("month")
    private int month;

    @SerializedName("year")
    private int year;

    @SerializedName("day")
    private int day;

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }

    public int getDay() {
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