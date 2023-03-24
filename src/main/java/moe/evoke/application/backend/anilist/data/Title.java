package moe.evoke.application.backend.anilist.data;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

public class Title {

    @SerializedName("userPreferred")
    private String userPreferred;

    @SerializedName("romaji")
    private String romaji;

    @SerializedName("english")
    private String english;

    @SerializedName("native")
    private String nativE;

    public String getUserPreferred() {
        return userPreferred;
    }

    public void setUserPreferred(String userPreferred) {
        this.userPreferred = userPreferred;
    }

    public String getRomaji() {
        return romaji;
    }

    public void setRomaji(String romaji) {
        this.romaji = romaji;
    }

    public String getEnglish() {
        return english;
    }

    public void setEnglish(String english) {
        this.english = english;
    }

    public String getNative() {
        return nativE;
    }

    public void setNative(String nativE) {
        this.nativE = nativE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Title title = (Title) o;
        return Objects.equals(userPreferred, title.userPreferred) && Objects.equals(romaji, title.romaji) && Objects.equals(english, title.english) && Objects.equals(nativE, title.nativE);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userPreferred, romaji, english, nativE);
    }

    @Override
    public String toString() {
        return "Title{" +
                "userPreferred='" + userPreferred + '\'' +
                ", romaji='" + romaji + '\'' +
                ", english='" + english + '\'' +
                ", nativE='" + nativE + '\'' +
                '}';
    }
}