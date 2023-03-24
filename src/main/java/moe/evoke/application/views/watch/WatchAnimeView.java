package moe.evoke.application.views.watch;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.VaadinService;
import moe.evoke.application.backend.anilist.Anilist;
import moe.evoke.application.backend.anilist.data.EdgesItem;
import moe.evoke.application.backend.anilist.data.Node;
import moe.evoke.application.backend.db.Anime;
import moe.evoke.application.backend.db.Database;
import moe.evoke.application.backend.db.Episode;
import moe.evoke.application.backend.db.HostedEpisode;
import moe.evoke.application.backend.hoster.ipfs.IPFS;
import moe.evoke.application.backend.hoster.ipfs.IPFSGateway;
import moe.evoke.application.backend.malsync.MALSync;
import moe.evoke.application.backend.malsync.MALSyncProvider;
import moe.evoke.application.backend.mirror.distribution.DistributionTarget;
import moe.evoke.application.backend.util.Utils;
import moe.evoke.application.components.AnimeTile;
import moe.evoke.application.components.IPFSJs;
import moe.evoke.application.components.StyledText;
import moe.evoke.application.components.VideoPlayer;
import moe.evoke.application.security.SecurityUtils;
import moe.evoke.application.views.controlpanel.ControlPanelView;
import moe.evoke.application.views.controlpanel.EditAnimeControlPanelView;
import moe.evoke.application.views.main.IPFSMode;
import moe.evoke.application.views.main.MainView;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.Cookie;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Route(value = "watch/:animeID/:episode", layout = MainView.class)
@CssImport("./views/watch/watch-view.css")
public class WatchAnimeView extends VerticalLayout implements BeforeEnterObserver, HasDynamicTitle {

    private final Tabs tabs;
    private final Div pages;
    private final Map<Tab, Component> tabsToPages = new HashMap<>();
    private final HorizontalLayout header;
    private final Label title;
    private final StyledText description;
    private final FlexLayout linksLayout;
    private final FlexLayout episodes;
    private final FlexLayout relations;
    private final Database database;
    private Image coverImg;
    private long animeID;
    private long episode;

    private IPFSJs ipfsJs;

    public WatchAnimeView(@Autowired Database database) {
        this.database = database;
        addClassName("watch-view");

        IPFSMode mode = Utils.getIPFSMode();
        if (mode != null && mode.equals(IPFSMode.NODE)) {
            this.ipfsJs = new IPFSJs();
            this.ipfsJs.getElement().setVisible(false);
            add(this.ipfsJs);
        }

        tabs = new Tabs();
        tabs.addSelectedChangeListener(event -> {
            tabsToPages.values().forEach(page -> page.setVisible(false));
            Component selectedPage = tabsToPages.get(tabs.getSelectedTab());
            if (selectedPage != null) {
                selectedPage.setVisible(true);

                if (tabs.getSelectedTab().getLabel().equals("evoke")) {
                    importHosterPlayerFunctions();
                }

            }
        });


        header = new HorizontalLayout();
        header.setClassName("header");

        coverImg = new Image();
        coverImg.setClassName("cover-img");
        header.add(coverImg);


        VerticalLayout details = new VerticalLayout();
        header.add(details);

        title = new Label();
        title.setId("anime-title");
        details.add(title);

        description = new StyledText("");
        details.add(description);

        linksLayout = new FlexLayout();
        linksLayout.setId("provider-links");
        linksLayout.setJustifyContentMode(JustifyContentMode.START);
        linksLayout.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        details.add(linksLayout);

        pages = new Div();

        Label episodeTitle = new Label("Episodes");
        episodes = new FlexLayout();
        episodes.setJustifyContentMode(JustifyContentMode.START);
        episodes.setFlexWrap(FlexLayout.FlexWrap.WRAP);


        Label relationsTitle = new Label("Relations");
        relations = new FlexLayout();
        relations.setJustifyContentMode(JustifyContentMode.START);
        relations.setFlexWrap(FlexLayout.FlexWrap.WRAP);

        add(header, tabs, pages, new Hr(), episodeTitle, episodes, new Hr(), relationsTitle, relations);
    }

    private void importHosterPlayerFunctions() {
        Page page = UI.getCurrent().getPage();
        page.addJavaScript("https://unpkg.com/@peertube/embed-api/build/player.min.js");
        page.executeJs("let player = new PeerTubePlayer(document.querySelector('iframe'));\n" +
                "player.addEventListener('playbackStatusUpdate', " +
                "function (event) {\n" +
                "  console.debug(event);\n" +
                "});");
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        animeID = Long.parseLong(event.getRouteParameters().get("animeID").
                orElse("-1"));
        episode = Long.parseLong(event.getRouteParameters().get("episode").
                orElse("1"));


        Anime anime = database.getAnimeByAnilistID(animeID);

        tabs.removeAll();
        pages.removeAll();
        tabsToPages.clear();
        episodes.removeAll();
        linksLayout.removeAll();
        relations.removeAll();

        if (anime != null) {

            title.setText(anime.getName());
            description.setText(anime.getData().getData().getMedia().getDescription());

            Image tmp = anime.getCoverAsImage();
            tmp.setClassName("cover-img");
            header.replace(coverImg, tmp);
            coverImg = tmp;

            if (SecurityUtils.isAccessGranted(ControlPanelView.class)) {
                Button settingsButton = new Button("Edit");
                settingsButton.addClickListener(buttonClickEvent ->
                {
                    getUI().ifPresent(ui -> {
                        RouteParam paramAnime = new RouteParam("animeID", Long.toString(database.getAnimeByAnilistID(animeID).getID()));
                        RouteParameters params = new RouteParameters(paramAnime);
                        ui.navigate(EditAnimeControlPanelView.class, params);
                    });
                });
                settingsButton.getStyle().set("background-color", "rgb(255,0,0)");
                settingsButton.getStyle().set("color", "rgb(255,255,255)");
                settingsButton.getStyle().set("margin", "5px");
                settingsButton.getStyle().set("cursor", "pointer");
                linksLayout.add(settingsButton);
            }

            Icon reportIcon = new Icon(VaadinIcon.FLAG);
            Button reportButton = new Button();
            reportButton.setIcon(reportIcon);
            reportButton.addClickListener(buttonClickEvent -> new ReportDialog(database, anime, episode));
            linksLayout.add(reportButton);

            List<MALSyncProvider> providerList = MALSync.getProviderForAnime(anime);
            if (providerList != null) {
                for (MALSyncProvider provider : providerList) {
                    Button providerButton = new Button(provider.getProvider());
                    providerButton.getStyle().set("cursor", "pointer");
                    providerButton.getStyle().set("margin", "5px");
                    providerButton.addClickListener(buttonClickEvent ->
                            getUI().ifPresent(ui -> ui.getPage().open(provider.getUrl())));
                    linksLayout.add(providerButton);
                }
            }


            List<Episode> watchedEpisodes = Database.instance().getWatchProgressForUserForAnime(SecurityUtils.getUsername(), anime.getID());
            Button nextEpisode = null;
            for (Episode episode : anime.getEpisodes()) {
                List<HostedEpisode> hostedEpisodes = episode.getHostedEpisodes();
                if (hostedEpisodes == null || hostedEpisodes.size() == 0) {
                    continue;
                }

                Button episodeSelector = new Button();
                episodeSelector.getStyle().set("cursor", "pointer");
                episodeSelector.getStyle().set("margin", "5px");
                episodeSelector.setText(Long.toString(episode.getNumber()));
                episodeSelector.addClickListener(buttonClickEvent ->
                        getUI().ifPresent(ui -> {
                            RouteParam paramAnime = new RouteParam("animeID", Long.toString(anime.getAnilistID()));
                            RouteParam paramEpisode = new RouteParam("episode", Long.toString(episode.getNumber()));
                            RouteParameters params = new RouteParameters(paramAnime, paramEpisode);
                            ui.navigate(WatchAnimeView.class, params);
                        }));

                Optional<Episode> watchedEpisode = watchedEpisodes.stream().filter(wep -> wep.getID() == episode.getID() && wep.isCompleted()).findAny();
                if (watchedEpisode.isPresent()) {
                    episodeSelector.getStyle().set("background-color", "rgb(35, 166, 39)");
                    episodeSelector.getStyle().set("color", "rgb(255,255,255)");
                }
                episodes.add(episodeSelector);

                if (episode.getNumber() == this.episode + 1) {
                    Shortcuts.addShortcutListener(UI.getCurrent(), episodeSelector::click, Key.KEY_N, KeyModifier.SHIFT);
                    episodeSelector.addClassName("next-episode");
                    episodeSelector.setId("next-episode");
                    nextEpisode = episodeSelector;
                }

                if (episode.getNumber() == this.episode) {
                    episodeSelector.getStyle().set("background-color", "rgb(36,51,72)");
                    episodeSelector.getStyle().set("color", "rgb(255,255,255)");
                    episodeSelector.addClassName("current-episode");
                    episodeSelector.setId("current-episode");

                    List<Checkbox> completedCheckboxes = new ArrayList<>();
                    String username = SecurityUtils.getUsername();

                    Database.instance().createWatchProgress(username, episode);

                    boolean tabSet = false;
                    for (HostedEpisode hostedEpisode : episode.getHostedEpisodes()) {
                        Tab tab = new Tab(hostedEpisode.getHoster().getName());
                        tabs.add(tab);

                        VerticalLayout page = new VerticalLayout();
                        page.setVisible(false);
                        page.setHeightFull();
                        page.setWidthFull();
                        page.setClassName("anime-player-container");
                        pages.add(page);

                        IFrame iframe = null;
                        VideoPlayer video = null;
                        if (hostedEpisode.getHoster().equals(DistributionTarget.IPFS.getHoster())) {

                            HorizontalLayout ipfsOptionsLayout = new HorizontalLayout();
                            ipfsOptionsLayout.setWidthFull();
                            page.add(ipfsOptionsLayout);

                            ComboBox<IPFSGatewayType> gatewayTypeComboBox = new ComboBox<>();
                            gatewayTypeComboBox.setLabel("IPFS Gateway:");
                            if (this.ipfsJs != null && Utils.getIPFSMode().equals(IPFSMode.NODE)) {
                                gatewayTypeComboBox.setItems(IPFSGatewayType.values());
                            } else {
                                gatewayTypeComboBox.setItems(IPFSGatewayType.EVOKE, IPFSGatewayType.CUSTOM);
                            }
                            ipfsOptionsLayout.add(gatewayTypeComboBox);

                            video = new VideoPlayer();
                            video.addClassName("anime-player-frame");
                            page.add(video);

                            TextField customGatewayField = new TextField("Custom Gateway");
                            customGatewayField.setHelperText("eg: https://ipfs.io/ipfs/");
                            customGatewayField.setVisible(false);


                            VideoPlayer finalVideo = video;
                            customGatewayField.addValueChangeListener(changeEvent -> {
                                Cookie cookie = new Cookie(IPFSGatewayType.class.getSimpleName() + "-custom", changeEvent.getValue());
                                cookie.setPath(VaadinService.getCurrentRequest().getContextPath());
                                VaadinService.getCurrentResponse().addCookie(cookie);
                                finalVideo.setSrc(changeEvent.getValue() + hostedEpisode.getStreamURL());
                            });
                            ipfsOptionsLayout.add(customGatewayField);

                            String url;
                            Optional<IPFSGateway> gateway = database.getGatewaysForHash(hostedEpisode.getStreamURL()).stream().filter(gw -> gw.isPublic()).findAny();
                            if (gateway.isPresent()) {
                                url = gateway.get().getAddress() + hostedEpisode.getStreamURL();
                            } else {
                                url = IPFS.getRandomGateway() + hostedEpisode.getStreamURL();
                            }

                            Cookie gatewayCookie = Utils.getCookieByName(IPFSGatewayType.class.getSimpleName());
                            if (gatewayCookie != null) {
                                if (gatewayCookie.getValue().equals(IPFSGatewayType.BROWSER.name())) {
                                    gatewayTypeComboBox.setValue(IPFSGatewayType.BROWSER);

                                    if (this.ipfsJs != null) {
                                        this.ipfsJs.loadVideoStreamForElement(video.getVideoTagId(), hostedEpisode.getStreamURL(), url);
                                    } else {
                                        Notification.show("This mode is not supported!", 5000, Notification.Position.TOP_END);
                                    }
                                } else if (gatewayCookie.getValue().equals(IPFSGatewayType.EVOKE.name())) {
                                    gatewayTypeComboBox.setValue(IPFSGatewayType.EVOKE);
                                    video.setSrc(url);
                                } else if (gatewayCookie.getValue().equals(IPFSGatewayType.CUSTOM.name())) {
                                    gatewayTypeComboBox.setValue(IPFSGatewayType.CUSTOM);
                                    customGatewayField.setVisible(true);

                                    String gatewayStr = "https://ipfs.io/ipfs/";
                                    Cookie customGatewayCookie = Utils.getCookieByName(IPFSGatewayType.class.getSimpleName() + "-custom");
                                    if (customGatewayCookie != null) {
                                        gatewayStr = customGatewayCookie.getValue();
                                        customGatewayField.setValue(gatewayStr);
                                    } else {
                                        customGatewayField.setValue(gatewayStr);
                                    }

                                    url = gateway + hostedEpisode.getStreamURL();
                                    video.setSrc(url);
                                }
                            } else {
                                if (this.ipfsJs != null) {
                                    gatewayTypeComboBox.setValue(IPFSGatewayType.BROWSER);
                                    this.ipfsJs.loadVideoStreamForElement(video.getVideoTagId(), hostedEpisode.getStreamURL(), url);
                                } else {
                                    gatewayTypeComboBox.setValue(IPFSGatewayType.EVOKE);
                                    Notification.show("This mode is not supported!", 5000, Notification.Position.TOP_END);
                                }
                            }

                            String finalUrl = url;
                            gatewayTypeComboBox.addValueChangeListener(changeEvent -> {
                                switch (changeEvent.getValue()) {
                                    case BROWSER:
                                        finalVideo.getElement().removeAttribute("src");
                                        if (this.ipfsJs != null) {
                                            this.ipfsJs.loadVideoStreamForElement(finalVideo.getVideoTagId(), hostedEpisode.getStreamURL(), finalUrl);
                                        } else {
                                            Notification.show("This mode is not supported!", 5000, Notification.Position.TOP_END);
                                        }

                                        customGatewayField.setVisible(false);
                                        break;
                                    case EVOKE:
                                        String videoURL;
                                        Optional<IPFSGateway> ipfsGateway = database.getGatewaysForHash(hostedEpisode.getStreamURL()).stream().filter(gw -> gw.isPublic()).findAny();
                                        if (ipfsGateway.isPresent()) {
                                            videoURL = gateway.get().getAddress() + hostedEpisode.getStreamURL();
                                        } else {
                                            videoURL = IPFS.getRandomGateway() + hostedEpisode.getStreamURL();
                                        }
                                        finalVideo.setSrc(videoURL);
                                        customGatewayField.setVisible(false);
                                        break;
                                    case CUSTOM:
                                        customGatewayField.setVisible(true);
                                        String gatewayStr = "https://ipfs.io/ipfs/";
                                        Cookie customGatewayCookie = Utils.getCookieByName(IPFSGatewayType.class.getSimpleName() + "-custom");
                                        if (customGatewayCookie != null) {
                                            gatewayStr = customGatewayCookie.getValue();
                                        } else {
                                            customGatewayField.setValue(gatewayStr);
                                        }
                                        videoURL = gatewayStr + hostedEpisode.getStreamURL();
                                        finalVideo.setSrc(videoURL);
                                        break;
                                }

                                Cookie cookie = new Cookie(IPFSGatewayType.class.getSimpleName(), changeEvent.getValue().name());
                                cookie.setPath(VaadinService.getCurrentRequest().getContextPath());
                                VaadinService.getCurrentResponse().addCookie(cookie);
                            });

                            if (nextEpisode != null) {
                                Shortcuts.addShortcutListener(video, nextEpisode::click, Key.KEY_N, KeyModifier.SHIFT);
                            }

                            final UI ui = UI.getCurrent();
                            video.setPlayerStatusListener((lastPlayerStatus, playerStatus) ->
                            {
                                if (playerStatus != null && playerStatus.getCurrentTime() > 10 && !playerStatus.isPaused()) {
                                    boolean completed = playerStatus.getDuration() * 0.8 < playerStatus.getCurrentTime();

                                    if (completed && !episode.isCompleted()) {
                                        episode.setCompleted(true);
                                        ui.access(() ->
                                        {
                                            Notification.show("Finished Episode " + episode.getNumber(), 5000, Notification.Position.TOP_END);
                                            database.updateWatchProgress(username, episode, 0.0, true);
                                            completedCheckboxes.forEach(checkbox -> checkbox.setValue(true));
                                        });
                                    } else {
                                        database.updateWatchProgress(username, episode, playerStatus.getCurrentTime(), completed);
                                    }
                                }
                            });
                        } else {
                            iframe = new IFrame();
                            iframe.setSrc(hostedEpisode.getStreamURL());
                            iframe.setAllow("autoplay; fullscreen");
                            iframe.getElement().setAttribute("scrolling", "no");
                            iframe.getElement().setAttribute("frameborder", "0");
                            iframe.getElement().setAttribute("marginwidth", "0");
                            iframe.getElement().setAttribute("marginheight", "0");
                            iframe.setClassName("anime-player-frame");
                            page.add(iframe);
                        }
                        final HtmlComponent videoComponent = iframe == null ? video : iframe;


                        HorizontalLayout playerSettingsLayout = new HorizontalLayout();
                        page.add(playerSettingsLayout);

                        Checkbox completedCheckbox = new Checkbox("Completed");
                        completedCheckbox.addValueChangeListener(changeEvent ->
                        {
                            database.updateWatchProgress(username, episode, 0, changeEvent.getValue());
                            completedCheckboxes.forEach(checkbox -> checkbox.setValue(changeEvent.getValue()));
                        });
                        completedCheckboxes.add(completedCheckbox);
                        playerSettingsLayout.add(completedCheckbox);

                        Icon expandIcon = new Icon(VaadinIcon.EXPAND_FULL);
                        Button expandButton = new Button();
                        expandButton.setIcon(expandIcon);
                        expandButton.setClassName("expand-button");
                        AtomicBoolean expandToggle = new AtomicBoolean(true);
                        expandButton.addClickListener(buttonClickEvent -> {
                            if (expandToggle.get()) {
                                videoComponent.getStyle().set("width", "100%");
                            } else {
                                videoComponent.getStyle().remove("width");
                            }
                            expandToggle.set(!expandToggle.get());
                            videoComponent.getElement().callJsFunction("scrollIntoView");
                        });
                        playerSettingsLayout.add(expandButton);

                        Optional<Episode> oldProgress = watchedEpisodes.stream().filter(wEp -> wEp.getID() == episode.getID()).findAny();
                        if (oldProgress.isPresent()) {
                            if (video != null) {
                                video.setStartPos(oldProgress.get().getProgress());
                                video.startListener();
                            }
                            completedCheckbox.setValue(oldProgress.get().isCompleted());
                        }

                        tabsToPages.put(tab, page);

                        if (!tabSet) {
                            tabSet = true;
                            tabs.setSelectedTab(tab);
                            tabsToPages.get(tab).setVisible(true);
                        }
                    }
                }
            }

            for (EdgesItem item : anime.getData().getData().getMedia().getRelations().getEdges()) {
                if (!item.getNode().getType().equalsIgnoreCase("anime")) {
                    continue;
                }

                Node node = item.getNode();
                Anime relatedAnime = Database.instance().getAnimeByAnilistID(node.getId());

                String text = node.getTitle().getUserPreferred() + "</br>" + item.getRelationType();

                AnimeTile animeTile = new AnimeTile(database, relatedAnime);
                animeTile.addClassName("anime-tile-airing-view");
                animeTile.setTextAsHTML(text);
                relations.add(animeTile);
            }

        } else {
            Label animeNotFoundLabel = new Label("Anime was not found in Database! Do you want to request this anime?");
            add(animeNotFoundLabel);

            Button requestAnimeButton = new Button("Request Anime");
            requestAnimeButton.addClickListener(buttonClickEvent -> {
                if (Anilist.getInfoForAnime(animeID) != null) {
                    Database.instance().createAnime(animeID);
                    getUI().ifPresent(ui -> ui.getPage().reload());
                }

            });
            add(requestAnimeButton);
        }

    }


    @Override
    public String getPageTitle() {

        Anime anime = Database.instance().getAnimeByAnilistID(animeID);
        if (anime != null) {
            return "Watch - " + anime.getName();
        }

        return "Watch";
    }
}
