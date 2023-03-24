package moe.evoke.application.views.watchtogether;

import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import moe.evoke.application.backend.db.Anime;
import moe.evoke.application.backend.db.Database;
import moe.evoke.application.backend.db.Episode;
import moe.evoke.application.backend.db.HostedEpisode;
import moe.evoke.application.backend.hoster.ipfs.IPFS;
import moe.evoke.application.backend.mirror.distribution.DistributionTarget;
import moe.evoke.application.backend.util.Utils;
import moe.evoke.application.backend.watchtogether.WatchTogether;
import moe.evoke.application.backend.watchtogether.WatchTogetherSession;
import moe.evoke.application.components.VideoPlayer;
import moe.evoke.application.components.peertube.PlayerStatus;
import moe.evoke.application.security.SecurityUtils;
import moe.evoke.application.views.main.MainView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;

@Route(value = "watchTogether/:username", layout = MainView.class)
@PageTitle("Watch Together")
@CssImport("./views/watchtogether/watch-together-view.css")
@Secured("watchtogether")
public class WatchTogetherPlayerView extends VerticalLayout implements BeforeEnterObserver {

    private final ListBox<Anime> animeListBox;
    private final ListBox<Episode> episodeListBox;
    private final ListBox<HostedEpisode> hosterListBox;
    private final VideoPlayer playerFrame;
    private final Label currentHostLabel;

    @Autowired
    private final Database database;

    private String username;
    private PlayerStatus localPlayerStatus = new PlayerStatus();
    private boolean detach = false;

    public WatchTogetherPlayerView(@Autowired Database database) {
        this.database = database;
        addClassName("watch-together-view");

        setId("watch-together-view");

        setHorizontalComponentAlignment(Alignment.CENTER);

        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setMaxWidth("60%");
        layout.getStyle().set("margin", "auto");
        layout.setHorizontalComponentAlignment(Alignment.CENTER);
        add(layout);

        HorizontalLayout playerLayout = new HorizontalLayout();
        playerLayout.setWidthFull();
        layout.add(playerLayout);

        playerFrame = new VideoPlayer("");
        playerFrame.setHeight("720px");
        playerFrame.setWidth("1280px");
        playerFrame.getStyle().set("margin", "auto");
        playerFrame.getStyle().set("background-color", "black");
        playerFrame.addClassName("anime-player-frame");
        playerFrame.startListener();
        playerLayout.add(playerFrame);

        VerticalLayout playerInfoLayout = new VerticalLayout();
        playerLayout.setWidth("250px");
        playerLayout.add(playerInfoLayout);

        Label hostLabel = new Label("Host");
        playerInfoLayout.add(hostLabel);

        currentHostLabel = new Label();
        currentHostLabel.setTitle("Host");
        playerInfoLayout.add(currentHostLabel);

        HorizontalLayout animeSelectionLayout = new HorizontalLayout();
        layout.add(animeSelectionLayout);

        VerticalLayout searchLayout = new VerticalLayout();
        animeSelectionLayout.add(searchLayout);

        TextField searchAnimeField = new TextField();
        searchAnimeField.setLabel("Search Anime");
        searchLayout.add(searchAnimeField);

        animeListBox = new ListBox<>();
        animeListBox.setItems(database.searchAnime(null, null, null, null, null, null, true, 25, 0));
        searchAnimeField.addValueChangeListener(event -> animeListBox.setItems(database.searchAnime(event.getValue(), null, null, null, null, null, true, 25, 0)));
        searchLayout.add(animeListBox);

        VerticalLayout selectEpisodeLayout = new VerticalLayout();
        animeSelectionLayout.add(selectEpisodeLayout);

        Label selectEpisodeLabel = new Label("Select Episode");
        selectEpisodeLayout.add(selectEpisodeLabel);

        episodeListBox = new ListBox<>();
        animeListBox.addValueChangeListener(event ->
                {
                    if (event != null && event.getValue() != null && event.getValue().getEpisodes() != null) {
                        episodeListBox.setItems(
                                event.getValue().getEpisodes().stream().filter(episode ->
                                        episode.getHostedEpisodes().stream().filter(hostedEpisode -> hostedEpisode.getHoster().equals(DistributionTarget.IPFS.getHoster())).count() > 0)
                        );
                    } else {
                        episodeListBox.clear();
                    }
                }
        );
        selectEpisodeLayout.add(episodeListBox);

        VerticalLayout selectHosterLayout = new VerticalLayout();
        animeSelectionLayout.add(selectHosterLayout);

        Label selectHosterLabel = new Label("Select Hoster");
        selectHosterLayout.add(selectHosterLabel);

        hosterListBox = new ListBox<>();
        hosterListBox.setRenderer(new ComponentRenderer<>(item -> new Label(item.getHoster().getName())));
        episodeListBox.addValueChangeListener(event ->
        {
            if (event != null && event.getValue() != null && event.getValue().getHostedEpisodes() != null) {
                hosterListBox.setItems(event.getValue().getHostedEpisodes().stream().filter(hostedEpisode -> hostedEpisode.getHoster().equals(DistributionTarget.IPFS.getHoster())));
            } else {
                hosterListBox.clear();
            }
        });
        animeListBox.addValueChangeListener(event -> hosterListBox.clear());
        hosterListBox.addValueChangeListener(event -> {
            if (event != null && event.getValue() != null) {
                playerFrame.setSrc(IPFS.getRandomGateway() + event.getValue().getStreamURL());
                WatchTogether.getSession(username).setCurrentEpisode(event.getValue());
            }
        });
        selectHosterLayout.add(hosterListBox);

        playerFrame.setPlayerStatusListener((lastPlayerStatus, playerStatus) ->
        {
            localPlayerStatus = playerStatus;

            WatchTogetherSession session = WatchTogether.getSession(username);
            if (session.getUser().equalsIgnoreCase(SecurityUtils.getUsername())) {
                session.setPlayerStatus(playerStatus);
            }
        });
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        detach = true;
        super.onDetach(detachEvent);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        username = beforeEnterEvent.getRouteParameters().get("username").
                orElse("-1");

        if (!SecurityUtils.isAccessGranted(WatchTogetherPlayerView.class)) {
            beforeEnterEvent.rerouteTo("home");
        }

        WatchTogetherSession sessionTest = WatchTogether.getSession(username);
        currentHostLabel.setText(sessionTest.getUser());

        if (sessionTest.getCurrentEpisode() != null) {
            HostedEpisode hostedEpisode = sessionTest.getCurrentEpisode();
            Episode episode = database.getEpisodeByID(hostedEpisode.getEpisodeID());
            Anime anime = episode.getAnime();

            playerFrame.setSrc(IPFS.getRandomGateway() + hostedEpisode.getStreamURL());
            animeListBox.setItems(anime);
            animeListBox.setValue(anime);
            episodeListBox.setItems(episode);
            episodeListBox.setValue(episode);
            hosterListBox.setItems(hostedEpisode);
            hosterListBox.setValue(hostedEpisode);
        }

        if (!sessionTest.getUser().equals(SecurityUtils.getUsername())) {
            new Thread(() -> {
                while (!detach) {
                    WatchTogetherSession session = WatchTogether.getSession(username);

                    if (session.getCurrentEpisode() == null) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        continue;
                    }

                    if (session.getCurrentEpisode() != null && !playerFrame.getSrc().equals(session.getCurrentEpisode().getStreamURL())) {
                        beforeEnterEvent.getUI().access(() ->
                        {
                            playerFrame.setSrc(IPFS.getRandomGateway() + session.getCurrentEpisode().getStreamURL());
                        });
                    }

                    if (playerFrame.getSrc().substring(playerFrame.getSrc().lastIndexOf("/") + 1).equals(session.getCurrentEpisode().getStreamURL())) {
                        beforeEnterEvent.getUI().access(() ->
                        {
                            if (localPlayerStatus.isPaused() != session.getPlayerStatus().isPaused()) {
                                if (session.getPlayerStatus().isPaused()) {
                                    playerFrame.pause();
                                } else {
                                    playerFrame.play();
                                }
                            }

                            if (localPlayerStatus != null && !Utils.isWithinRange(localPlayerStatus.getCurrentTime(), session.getPlayerStatus().getCurrentTime(), 10)) {
                                playerFrame.seek(session.getPlayerStatus().getCurrentTime());
                            }
                        });
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }
}
