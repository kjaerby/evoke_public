package moe.evoke.application.views.controlpanel;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Shortcuts;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import moe.evoke.application.backend.anilist.Anilist;
import moe.evoke.application.backend.db.*;
import moe.evoke.application.backend.hoster.mega.MegaNZ;
import moe.evoke.application.backend.malsync.MALSync;
import moe.evoke.application.backend.mirror.distribution.DistributionJob;
import moe.evoke.application.backend.mirror.distribution.DistributionManager;
import moe.evoke.application.backend.mirror.distribution.DistributionSource;
import moe.evoke.application.backend.mirror.distribution.DistributionTarget;
import moe.evoke.application.backend.torrent.TorrentManager;
import moe.evoke.application.backend.util.CoverUtil;
import moe.evoke.application.security.SecurityUtils;
import moe.evoke.application.views.controlpanel.dialogs.ImportEpisodeDialog;
import moe.evoke.application.views.controlpanel.dialogs.TorrentDialog;
import moe.evoke.application.views.main.MainView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Route(value = "cp-edit-anime/:animeID", layout = MainView.class)
@PageTitle("Control Panel")
@CssImport("./views/controlpanel/control-panel-view.css")
@Secured("admin")
public class EditAnimeControlPanelView extends Div implements BeforeEnterObserver {

    private final Grid<Episode> episodeGrid;
    private final Label currentAnime;
    private final Database database;
    @Autowired
    private HttpServletRequest req;
    private long animeID;
    private Grid<HostedEpisode> hostedEpisodeGrid;

    public EditAnimeControlPanelView(@Autowired Database database) {
        this.database = database;
        addClassName("edit-anime-control-panel-view");

        VerticalLayout layout = new VerticalLayout();
        add(layout);

        currentAnime = new Label("Edit: ");
        layout.add(currentAnime);


        HorizontalLayout animeActionsLayout = new HorizontalLayout();
        layout.add(animeActionsLayout);

        Button torrentButton = new Button("Torrent");
        torrentButton.addClickListener(buttonClickEvent -> new TorrentDialog(database.getAnimeByID(animeID)).open());
        animeActionsLayout.add(torrentButton);

        Button refreshAnilistCacheButton = new Button("Refresh Anilist Cache");
        refreshAnilistCacheButton.addClickListener(buttonClickEvent -> {
            Anime anime = database.getAnimeByID(animeID);
            Anilist.refreshCacheForAnime(anime);
        });
        animeActionsLayout.add(refreshAnilistCacheButton);

        Button refreshMALSyncCacheButton = new Button("Refresh MAL-Sync Cache");
        refreshMALSyncCacheButton.addClickListener(buttonClickEvent -> {
            Anime anime = database.getAnimeByID(animeID);
            MALSync.refreshCacheForAnime(anime);
        });
        animeActionsLayout.add(refreshMALSyncCacheButton);

        Button megaImportButton = new Button("Import from MEGA");
        megaImportButton.addClickListener(buttonClickEvent -> {
            Runnable runnable = () -> {
                Anime anime = database.getAnimeByID(animeID);
                MegaNZ.getFiles(anime);
            };
            new Thread(runnable).start();
        });
        animeActionsLayout.add(megaImportButton);

        Button refreshCoverButton = new Button("Refresh Cover");
        refreshCoverButton.addClickListener(buttonClickEvent -> {
            Anime anime = database.getAnimeByID(animeID);
            CoverUtil.deleteCoverForAnime(anime);
            CoverUtil.getCoverForAnime(anime);
        });
        animeActionsLayout.add(refreshCoverButton);

        Label episodes = new Label("Episodes");
        layout.add(episodes);
        episodeGrid = new Grid<>(Episode.class);
        episodeGrid.addSelectionListener(selectionEvent ->
        {
            selectionEvent.getFirstSelectedItem().ifPresent(episode -> hostedEpisodeGrid.setItems(episode.getHostedEpisodes()));
        });
        layout.add(episodeGrid);

        {
            HorizontalLayout editEpisodeLayout = new HorizontalLayout();
            layout.add(editEpisodeLayout);

            Button addEpisode = new Button("Add");
            addEpisode.addClickListener(buttonClickEvent -> {

                Dialog dialog = new Dialog();

                Label numberLabel = new Label("Number:");
                dialog.add(numberLabel);

                TextField numberField = new TextField();
                dialog.add(numberField);

                dialog.setCloseOnEsc(false);
                dialog.setCloseOnOutsideClick(false);
                Span message = new Span();

                Button confirmButton = new Button("Confirm", event -> {
                    message.setText("Confirmed!");
                    database.createEpisode(database.getAnimeByID(animeID), Long.parseLong(numberField.getValue()));
                    reloadEpisode();
                    dialog.close();
                });
                Button cancelButton = new Button("Cancel", event -> {
                    message.setText("Cancelled...");
                    dialog.close();
                });

                Shortcuts.addShortcutListener(dialog, () -> {
                    message.setText("Cancelled...");
                    dialog.close();
                }, Key.ESCAPE);

                dialog.add(new Div(confirmButton, cancelButton));
                dialog.open();
            });
            editEpisodeLayout.add(addEpisode);

            Button removeEpisode = new Button("Remove");
            removeEpisode.addClickListener(buttonClickEvent -> {
                for (Episode episode : episodeGrid.getSelectedItems()) {
                    database.removeEpisode(episode);
                }
                reloadEpisode();
            });
            editEpisodeLayout.add(removeEpisode);

            Button importEpisodeButton = new Button("Import");
            importEpisodeButton.addClickListener(buttonClickEvent -> {
                new ImportEpisodeDialog(database, animeID, episodeGrid.getSelectedItems()).open();
            });
            editEpisodeLayout.add(importEpisodeButton);

            Button createEpisodesButton = new Button("Create Episodes");
            createEpisodesButton.addClickListener(buttonClickEvent -> {
                createEpisodesButtonAction(buttonClickEvent);
            });
            editEpisodeLayout.add(createEpisodesButton);

            Button importMissingEpisodesButton = new Button("Import Missing Episodes from GoGo");
            importMissingEpisodesButton.addClickListener(buttonClickEvent -> {
                importMissingEpisodesButtonAction(buttonClickEvent);
            });
            editEpisodeLayout.add(importMissingEpisodesButton);
        }

        Label hoster = new Label("Hoster");
        layout.add(hoster);
        hostedEpisodeGrid = new Grid<>(HostedEpisode.class);
        layout.add(hostedEpisodeGrid);

        {
            HorizontalLayout editHostedEpisodeLayout = new HorizontalLayout();
            layout.add(editHostedEpisodeLayout);

            Button addHostedEpisode = new Button("Add");
            addHostedEpisode.addClickListener(buttonClickEvent -> {

                Dialog dialog = new Dialog();

                Label hosterLabel = new Label("Hoster:");
                dialog.add(hosterLabel);

                ComboBox<Hoster> hosterComboBox = new ComboBox<>();
                hosterComboBox.setItems(database.getHoster());
                dialog.add(hosterComboBox);

                Label streamURLLabel = new Label("Stream-URL:");
                dialog.add(streamURLLabel);

                TextField streamURLField = new TextField();
                dialog.add(streamURLField);

                dialog.setCloseOnEsc(false);
                dialog.setCloseOnOutsideClick(false);
                Span message = new Span();

                Button confirmButton = new Button("Confirm", event -> {
                    message.setText("Confirmed!");
                    for (Episode episode : episodeGrid.getSelectedItems()) {
                        database.createHostedEpisode(hosterComboBox.getValue(), episode, streamURLField.getValue());
                    }
                    reloadHostedEpisode();
                    dialog.close();
                });
                Button cancelButton = new Button("Cancel", event -> {
                    message.setText("Cancelled...");
                    dialog.close();
                });

                Shortcuts.addShortcutListener(dialog, () -> {
                    message.setText("Cancelled...");
                    dialog.close();
                }, Key.ESCAPE);

                dialog.add(new Div(confirmButton, cancelButton));
                dialog.open();
            });
            editHostedEpisodeLayout.add(addHostedEpisode);

            Button removeHostedEpisode = new Button("Remove");
            removeHostedEpisode.addClickListener(buttonClickEvent -> {
                for (HostedEpisode hostedEpisode : hostedEpisodeGrid.getSelectedItems()) {
                    database.removeHostedEpisode(hostedEpisode);
                }
                reloadHostedEpisode();
            });
            editHostedEpisodeLayout.add(removeHostedEpisode);
        }


    }

    private void importMissingEpisodesButtonAction(ClickEvent<Button> buttonClickEvent) {
        Anime anime = database.getAnimeByID(animeID);

        if (!DistributionSource.GOGO.isAvailable(anime))
        {
            Notification.show("GoGo is not available for this anime!", 5000, Notification.Position.TOP_END);
            return;
        }

        List<Episode> missing = new ArrayList<>();
        List<Episode> episodes = anime.getEpisodes();
        for (Episode episode : episodes) {
            var hostedEpisodes = episode.getHostedEpisodes();
            if (hostedEpisodes == null || hostedEpisodes.isEmpty())
            {
                missing.add(episode);
            }
        }

        List<DistributionTarget> targets = new ArrayList<>();
        targets.add(DistributionTarget.MEGA);
        targets.add(DistributionTarget.STREAMTAPE);

        for (Episode episode : missing) {
            DistributionJob distributionJob = new DistributionJob();
            distributionJob.anime = anime;
            distributionJob.episode = episode;
            distributionJob.source = DistributionSource.GOGO;
            distributionJob.targets = targets;

            DistributionManager.submitJob(distributionJob);
        }
    }

    private void createEpisodesButtonAction(ClickEvent<Button> buttonClickEvent) {

        Anime anime = database.getAnimeByID(animeID);
        List<Episode> episodes = anime.getEpisodes();
        int episodeCount = anime.getEpisodeCount();

        if (episodeCount != episodes.size()) {
            for (int i = 0; i < episodeCount; i++) {
                int finalI = i;
                Optional<Episode> episodeInList = episodes.stream().filter(episode -> episode.getNumber() == (finalI + 1)).findFirst();
                if (episodeInList.isPresent()) {
                    continue;
                }

                database.createEpisode(anime, i + 1);
            }
        }

        reloadEpisode();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {

        if (!SecurityUtils.isAccessGranted(ControlPanelView.class)) {
            event.rerouteTo("home");
        }

        animeID = Long.parseLong(event.getRouteParameters().get("animeID").
                orElse("-1"));

        if (animeID != -1) {
            Anime anime = database.getAnimeByID(animeID);
            episodeGrid.setItems(anime.getEpisodes());

            currentAnime.setText(anime.getName());
        }
    }

    private void reloadEpisode() {
        if (animeID != -1) {
            Anime anime = database.getAnimeByID(animeID);
            episodeGrid.setItems(anime.getEpisodes());
        }
    }

    private void reloadHostedEpisode() {
        if (animeID != -1) {
            for (Episode episode : episodeGrid.getSelectedItems()) {
                hostedEpisodeGrid.setItems(episode.getHostedEpisodes());
            }
        }
    }

}
