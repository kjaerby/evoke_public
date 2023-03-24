package moe.evoke.application.components;

import com.google.gson.Gson;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import moe.evoke.application.components.peertube.PlayerStatus;

import java.util.Timer;
import java.util.TimerTask;

@Tag("video")
@NpmPackage(value = "video.js", version = "^7.8.2")
@JsModule("video.js/dist/video.js")
@CssImport("video.js/dist/video-js.css")
public class VideoPlayer extends HtmlComponent {

    private static final PropertyDescriptor<String, String> videoTagIdProperty =
            PropertyDescriptors.propertyWithDefault("id", "");

    private static final PropertyDescriptor<String, String> classProperty =
            PropertyDescriptors.propertyWithDefault("class", "video-js");

    private static final PropertyDescriptor<String, String> posterProperty =
            PropertyDescriptors.propertyWithDefault("poster", "");

    private PlayerStatusListener listener;
    private PlayerStatus lastPlayerStatus;
    private Timer timer;

    public VideoPlayer() {
        setVideoTagId("video-js-" + this.hashCode() + "-" + System.nanoTime());
        addClassName("video-js");

        setPreload(true);
        enableControls(true);
    }

    public VideoPlayer(String srcUrl) {
        setVideoTagId("video-js-" + this.hashCode() + "-" + System.nanoTime());
        addClassName("video-js");

        setPreload(true);
        enableControls(true);
        setSourceURI(srcUrl);
    }

    public void pause() {
        getElement().executeJs("this.pause();");
    }

    public void play() {
        getElement().executeJs("this.play();");
    }

    public void seek(double timeInSeconds) {
        getElement().executeJs("this.currentTime = " + timeInSeconds + ";");
    }

    public String getPosterURI() {
        return posterProperty.get(this);
    }

    public VideoPlayer setPosterURI(String path) {
        posterProperty.set(this, path);
        return this;
    }

    public String getSourceURI() {
        return this.getElement().getAttribute("src");
    }

    public VideoPlayer setSourceURI(String path) {
        this.getElement().setAttribute("src", path);
        return this;
    }

    public void enableControls(boolean value) {
        getElement().executeJs("this.controls = " + value + ";");
    }

    public void setPreload(boolean value) {
        getElement().executeJs("this.preload = " + value + ";");
    }

    public String getVideoTagClass() {
        return classProperty.get(this);
    }

    public VideoPlayer setVideoTagClass(String value) {
        classProperty.set(this, value);
        return this;
    }

    public String getVideoTagId() {
        return videoTagIdProperty.get(this);
    }

    public VideoPlayer setVideoTagId(String value) {
        videoTagIdProperty.set(this, value);
        return this;
    }

    public void setStartPos(double progress) {
        getElement().executeJs(
                "this.addEventListener('loadedmetadata', function () { this.currentTime = " + progress + ";}, false);"
        );
    }

    public void setPlayerStatusListener(PlayerStatusListener listener) {
        this.listener = listener;
    }

    public void startListener() {
        stopListener();

        UI ui = UI.getCurrent();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                ui.access(() -> {
                    getElement().executeJs(
                            "let status = {};" +
                                    "status.paused = this.paused;" +
                                    "status.currentTime = this.currentTime;" +
                                    "status.duration = this.duration;" +
                                    "this.$server.playerStatus(JSON.stringify(status));"
                    );
                });

            }
        }, 2500, 2500);
    }

    private void stopListener() {
        if (this.timer != null) {
            this.timer.cancel();
        }
    }

    @ClientCallable
    private void playerStatus(String json) {
        System.out.println(json);
        Gson gson = new Gson();
        PlayerStatus playerStatus = gson.fromJson(json, PlayerStatus.class);
        lastPlayerStatus = playerStatus;
        listener.execute(lastPlayerStatus, playerStatus);
    }

    public String getSrc() {
        return getSourceURI();
    }

    public void setSrc(String streamURL) {
        setSourceURI(streamURL);
    }
}
