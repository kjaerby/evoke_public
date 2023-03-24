package moe.evoke.application.components.peertube;

import com.google.gson.annotations.SerializedName;

public class PlayerStatus {

    @SerializedName("paused")
    private boolean paused;

    @SerializedName("currentTime")
    private double currentTime;

    @SerializedName("duration")
    private double duration;

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public double getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(double currentTime) {
        this.currentTime = currentTime;
    }
}