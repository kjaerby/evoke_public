package moe.evoke.application.backend.hoster.streamz.data;

import com.google.gson.annotations.SerializedName;

public class ResponseItem {

    @SerializedName("code")
    private String code;

    @SerializedName("size")
    private String size;

    @SerializedName("uniquecode")
    private String uniquecode;

    @SerializedName("direct")
    private String direct;

    @SerializedName("name")
    private String name;

    @SerializedName("embed")
    private String embed;

    @SerializedName("streamcryptdirect")
    private String streamcryptdirect;

    @SerializedName("streamcryptembed")
    private String streamcryptembed;

    @SerializedName("status")
    private String status;

    public String getCode() {
        return code;
    }

    public String getSize() {
        return size;
    }

    public String getUniquecode() {
        return uniquecode;
    }

    public String getDirect() {
        return direct;
    }

    public String getName() {
        return name;
    }

    public String getEmbed() {
        return embed;
    }

    public String getStreamcryptdirect() {
        return streamcryptdirect;
    }

    public String getStreamcryptembed() {
        return streamcryptembed;
    }

    public String getStatus() {
        return status;
    }
}