package moe.evoke.application.components;

import com.google.gson.Gson;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;
import moe.evoke.application.backend.db.Anime;
import moe.evoke.application.backend.db.Episode;
import moe.evoke.application.components.peertube.PlayerStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsModule("./components/playerstatusinterface.js")
public class PlayerStatusInterfaceComponent extends Div {

    private static final Logger logger = LoggerFactory.getLogger(PlayerStatusInterfaceComponent.class);

    private final Anime anime;
    private final Episode episode;
    private final Gson gson = new Gson();

    private boolean extensionInstalled = false;

    private PlayerStatusListener listener;
    private PlayerStatus lastPlayerStatus;

    public PlayerStatusInterfaceComponent(Anime anime, Episode episode) {
        setMaxHeight("0px");
        setMaxWidth("0px");
        getStyle().set("position", "absolute");
        getStyle().set("top", "0px");
        getStyle().set("left", "0px");

        this.setId("player-status-interface");

        this.anime = anime;
        this.episode = episode;
    }

    @ClientCallable
    private void updateStatus(String jsonData) {
        logger.debug(jsonData);

        if (listener != null) {
            PlayerStatus playerStatus = gson.fromJson(jsonData, PlayerStatus.class);
            listener.execute(lastPlayerStatus, playerStatus);
            lastPlayerStatus = playerStatus;
        }
    }

    @ClientCallable
    private void extensionAvailable(String jsonData) {
        extensionInstalled = jsonData.equals("true");
    }

    public void play() {

    }

    public void pause() {

    }

    public boolean isExtensionInstalled() {
        return this.extensionInstalled;
    }

    public void seek(double seconds) {
        getElement().executeJs("seekVideo('" + seconds + "')");
    }

    public void setPlayerStatusListener(PlayerStatusListener listener) {
        this.listener = listener;
    }
}
