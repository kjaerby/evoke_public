package moe.evoke.application.views.controlpanel.dialogs;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.server.VaadinSession;
import moe.evoke.application.backend.crawler.Crunchyroll;
import moe.evoke.application.backend.db.Anime;
import moe.evoke.application.backend.db.Database;
import moe.evoke.application.backend.db.Episode;
import moe.evoke.application.backend.malsync.MALSync;
import moe.evoke.application.backend.malsync.MALSyncProvider;
import moe.evoke.application.backend.mirror.distribution.DistributionJob;
import moe.evoke.application.backend.mirror.distribution.DistributionManager;
import moe.evoke.application.backend.mirror.distribution.DistributionSource;
import moe.evoke.application.backend.mirror.distribution.DistributionTarget;
import org.apache.commons.fileupload.util.Streams;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImportEpisodeDialog extends Dialog {

    private final VerticalLayout crunchyrollSettingsLayout;
    private final VerticalLayout manualSettingsLayout;
    private final VerticalLayout torrentSettingsLayout;

    private final Anime anime;
    private final Episode episode;

    // Crunchyroll
    private final TextField userAgentField = new TextField();
    private final TextField episodeURLField = new TextField();

    // Torrent
    private final TextField magnetLinkField = new TextField();

    public ImportEpisodeDialog(Database database, long animeID, Set<Episode> selectedItems) {
        this.anime = database.getAnimeByID(animeID);
        this.episode = selectedItems.stream().findFirst().get();

        VerticalLayout dialogLayout = new VerticalLayout();

        TextField selectedAnimeField = new TextField();
        selectedAnimeField.setLabel("Selected Anime");
        selectedAnimeField.setValue(anime.getName());
        selectedAnimeField.setEnabled(false);
        dialogLayout.add(selectedAnimeField);

        TextField selectedAEpisodeField = new TextField();
        selectedAEpisodeField.setLabel("Selected Episode");
        selectedAEpisodeField.setValue(String.valueOf(episode.getNumber()));
        dialogLayout.add(selectedAEpisodeField);

        ComboBox<DistributionSource> distributionSource = new ComboBox<>();
        distributionSource.setLabel("Distribution Source");
        distributionSource.setItems(DistributionSource.availableModes(anime));
        distributionSource.setValue(DistributionSource.MANUAL);
        dialogLayout.add(distributionSource);


        manualSettingsLayout = new VerticalLayout();
        manualSettingsLayout.setVisible(true);
        createManual();
        dialogLayout.add(manualSettingsLayout);

        crunchyrollSettingsLayout = new VerticalLayout();
        crunchyrollSettingsLayout.setVisible(false);
        createCrunchyroll();
        dialogLayout.add(crunchyrollSettingsLayout);

        torrentSettingsLayout = new VerticalLayout();
        torrentSettingsLayout.setVisible(false);
        createTorrent();
        dialogLayout.add(torrentSettingsLayout);


        distributionSource.addValueChangeListener(comboBoxDistributionModeComponentValueChangeEvent ->
        {
            DistributionSource distributionMode = comboBoxDistributionModeComponentValueChangeEvent.getValue();

            manualSettingsLayout.setVisible(distributionMode == DistributionSource.MANUAL);
            crunchyrollSettingsLayout.setVisible(distributionMode == DistributionSource.CRUNCHYROLL);
            torrentSettingsLayout.setVisible(distributionMode == DistributionSource.TORRENT);
        });


        Map<DistributionTarget, Checkbox> targetCheckboxMap = new HashMap<>();
        for (DistributionTarget distributionTarget : DistributionTarget.values()) {
            Checkbox distributeCheckbox = new Checkbox(distributionTarget.getLabel());
            distributeCheckbox.setValue(true);
            dialogLayout.add(distributeCheckbox);
            targetCheckboxMap.put(distributionTarget, distributeCheckbox);
        }

        Button runDistributionButton = new Button("Distribute Episode", event -> {

            List<DistributionTarget> targets = new ArrayList<>();
            for (Map.Entry<DistributionTarget, Checkbox> entry : targetCheckboxMap.entrySet()) {
                if (entry.getValue().getValue()) {
                    targets.add(entry.getKey());
                }
            }

            ExecutorService executorService = Executors.newFixedThreadPool(1);

            String selectedEpisode = selectedAEpisodeField.getValue();
            if (selectedEpisode.contains("-")) {
                String[] split = selectedEpisode.split("-");
                int startEp = Integer.parseInt(split[0]);
                int endEp = Integer.parseInt(split[1]);

                List<Episode> episodes = anime.getEpisodes();
                for (int i = startEp; i <= endEp; i++) {
                    int finalI = i;
                    Optional<Episode> newEpisode = episodes.stream().filter(episode1 -> episode1.getNumber() == finalI).findFirst();
                    if (!newEpisode.isPresent()) {
                        database.createEpisode(anime, i);
                        newEpisode = database.getEpisodesForAnime(anime).stream().filter(episode1 -> episode1.getNumber() == finalI).findFirst();
                    }

                    Optional<Episode> finalNewEpisode = newEpisode;
                    DistributionJob job = new DistributionJob();
                    job.anime = anime;
                    job.episode = finalNewEpisode.get();
                    job.source = distributionSource.getValue();
                    job.targets = targets;

                    DistributionManager.submitJob(job);
                }

            } else {
                executorService.submit(() ->
                {
                    DistributionJob job = new DistributionJob();
                    job.anime = anime;
                    job.episode = episode;
                    job.source = distributionSource.getValue();
                    job.targets = targets;
                    if (distributionSource.getValue() == DistributionSource.CRUNCHYROLL) {
                        Map<String, String> sourceOptions = new HashMap<>();
                        sourceOptions.put(Crunchyroll.OPTION_COOKIES_PATH, anime.getAnilistID() + "-" + episode.getNumber() + ".cookies");
                        sourceOptions.put(Crunchyroll.OPTION_USERAGENT, userAgentField.getValue());
                        sourceOptions.put(Crunchyroll.OPTION_EPISODE_URL, episodeURLField.getValue());
                        job.sourceOptions = sourceOptions;
                    } else if (distributionSource.getValue() == DistributionSource.TORRENT) {
                        Map<String, String> sourceOptions = new HashMap<>();
                        sourceOptions.put(DistributionSource.MAGNET_LINK, magnetLinkField.getValue());
                        job.sourceOptions = sourceOptions;
                    }

                    DistributionManager.submitJob(job);
                });
            }

            Notification.show("Started distribution of Episode!");
        });
        dialogLayout.add(runDistributionButton);

        this.add(dialogLayout);
        this.setCloseOnEsc(false);
        this.setCloseOnOutsideClick(false);

        Button confirmButton = new Button("Done", event -> {
            this.close();
        });

        this.add(confirmButton);
        this.open();
    }

    private void createManual() {
        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setMaxFileSize(Integer.MAX_VALUE);
        upload.setVisible(true);

        upload.addSucceededListener(event -> {
            try {
                String filename = anime.getAnilistID() + "-" + episode.getNumber() + ".mp4";
                FileOutputStream fileOutputStream = new FileOutputStream(filename);
                Streams.copy(buffer.getInputStream(), fileOutputStream, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        manualSettingsLayout.add(upload);
    }

    private void createCrunchyroll() {
        Button openCrunchyrollLinkButton = new Button("Open Crunchyroll");
        openCrunchyrollLinkButton.addClickListener(buttonClickEvent1 -> {
            List<MALSyncProvider> providerList = MALSync.getProviderForAnime(anime);
            Optional<MALSyncProvider> providerOptional = providerList.stream().filter(provider -> provider.getProvider().toLowerCase().contains("crunchyroll")).findFirst();

            String url = providerOptional.get().getUrl();
            UI.getCurrent().getPage().open(url);
        });
        crunchyrollSettingsLayout.add(openCrunchyrollLinkButton);

        episodeURLField.setLabel("URL to Episode");
        crunchyrollSettingsLayout.add(episodeURLField);

        userAgentField.setValue(VaadinSession.getCurrent().getBrowser().getBrowserApplication());
        userAgentField.setLabel("Specify User-Agent. Must match cookies.txt!");
        crunchyrollSettingsLayout.add(userAgentField);

        Label cookiesLabel = new Label("Select cookies.txt");
        crunchyrollSettingsLayout.add(cookiesLabel);

        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);

        upload.setMaxFileSize(Integer.MAX_VALUE);
        upload.setVisible(true);

        upload.addSucceededListener(event -> {
            try {
                String filename = anime.getAnilistID() + "-" + episode.getNumber() + ".cookies";
                FileOutputStream fileOutputStream = new FileOutputStream(filename);
                Streams.copy(buffer.getInputStream(), fileOutputStream, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        crunchyrollSettingsLayout.add(upload);
    }

    private void createTorrent() {
        magnetLinkField.setLabel("MAGNET Link");
        torrentSettingsLayout.add(magnetLinkField);
    }
}
