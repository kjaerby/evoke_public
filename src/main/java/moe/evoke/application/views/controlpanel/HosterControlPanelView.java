package moe.evoke.application.views.controlpanel;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Shortcuts;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import moe.evoke.application.backend.db.*;
import moe.evoke.application.backend.hoster.HosterFile;
import moe.evoke.application.backend.hoster.mega.MegaNZ;
import moe.evoke.application.backend.hoster.peertube.PeerTube;
import moe.evoke.application.backend.hoster.streamz.StreamZ;
import moe.evoke.application.backend.mirror.distribution.DistributionJob;
import moe.evoke.application.backend.mirror.distribution.DistributionManager;
import moe.evoke.application.backend.mirror.distribution.DistributionSource;
import moe.evoke.application.backend.mirror.distribution.DistributionTarget;
import moe.evoke.application.security.SecurityUtils;
import moe.evoke.application.views.main.MainView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Route(value = "cp-hoster/:hosterID", layout = MainView.class)
@PageTitle("Control Panel")
@CssImport("./views/controlpanel/control-panel-view.css")
@Secured("admin")
public class HosterControlPanelView extends Div implements BeforeEnterObserver {

    private static final Logger logger = LoggerFactory.getLogger(HosterControlPanelView.class);
    private final Grid<HosterFile> hosterFilesGrid;
    private final VerticalLayout hosterSettings;
    private final Database database;
    @Autowired
    private HttpServletRequest req;
    private long hosterID;

    public HosterControlPanelView(@Autowired Database database) {
        this.database = database;
        addClassName("hoster-control-panel-view");

        VerticalLayout layout = new VerticalLayout();
        add(layout);

        hosterFilesGrid = new Grid<>(HosterFile.class);
        hosterFilesGrid.setPageSize(Integer.MAX_VALUE);
        hosterFilesGrid.addItemClickListener(hosterItemClickEvent -> {
            if (hosterItemClickEvent.getClickCount() == 2) {
                {
                    Label animeLabel = new Label("Anime");
                    ComboBox<Anime> animeComboBox = new ComboBox<>();
                    animeComboBox.setItems(database.getAnime());

                    Label episodeLabel = new Label("Episode");
                    ComboBox<Episode> episodeComboBox = new ComboBox<>();

                    animeComboBox.addValueChangeListener(comboBoxAnimeComponentValueChangeEvent -> episodeComboBox.setItems(comboBoxAnimeComponentValueChangeEvent.getValue().getEpisodes()));

                    HostedEpisode hostedEpisode = database.getHostedEpisodeForStreamURL(hosterItemClickEvent.getItem().getEmbed());
                    if (hostedEpisode != null) {
                        Episode episode = database.getEpisodeByID(hostedEpisode.getEpisodeID());
                        Anime anime = database.getAnimeByID(episode.getAnimeID());
                        animeComboBox.setValue(anime);
                        episodeComboBox.setValue(episode);
                    }

                    Dialog dialog = new Dialog();
                    dialog.add(animeLabel, animeComboBox, episodeLabel, episodeComboBox);
                    dialog.setCloseOnEsc(false);
                    dialog.setCloseOnOutsideClick(false);
                    Span message = new Span();

                    Button confirmButton = new Button("Confirm", event -> {
                        message.setText("Confirmed!");

                        if (hostedEpisode != null) {
                            database.removeHostedEpisode(hostedEpisode);
                        }
                        database.createHostedEpisode(database.getHosterByID(hosterID), episodeComboBox.getValue(), hosterItemClickEvent.getItem().getEmbed());

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
                }
            }
        });
        layout.add(hosterFilesGrid);

        hosterSettings = new VerticalLayout();
        layout.add(hosterSettings);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {

        if (!SecurityUtils.isAccessGranted(ControlPanelView.class)) {
            event.rerouteTo("home");
        }

        hosterID = Long.parseLong(event.getRouteParameters().get("hosterID").
                orElse("-1"));

        if (hosterID != -1) {
            hosterSettings.removeAll();
            Hoster hoster = database.getHosterByID(hosterID);
            switch (hoster.getName().toLowerCase()) {
                case "streamz":
                    hosterFilesGrid.setItems(StreamZ.getFiles());
                    break;
                case "mega":
                    //hosterFilesGrid.setItems(MegaNZ.getFiles());
                    createMegaSettings();
                    break;
                case "evoke":
                    hosterFilesGrid.setItems(PeerTube.getFiles());
                    break;
            }


        }


    }

    private void createMegaSettings() {

        Button calculateDistribution = new Button("Distribute all Episodes to other Hosters");
        hosterSettings.add(calculateDistribution);
        calculateDistribution.addClickListener(buttonClickEvent -> {

            VerticalLayout dialogLayout = new VerticalLayout();

            Map<DistributionTarget, Checkbox> targetCheckboxMap = new HashMap<>();
            for (DistributionTarget distributionTarget : DistributionTarget.values()) {
                // No distribution from MEGA -> MEGA
                if (distributionTarget == DistributionTarget.MEGA) {
                    continue;
                }

                Checkbox distributeCheckbox = new Checkbox(distributionTarget.getLabel());
                distributeCheckbox.setValue(true);
                dialogLayout.add(distributeCheckbox);
                targetCheckboxMap.put(distributionTarget, distributeCheckbox);
            }


            Button runDistributionButton = new Button("Distribute Episode", event -> {

                Label progressBarStatusLabel = new Label("Calculating missing episodes...");
                dialogLayout.add(progressBarStatusLabel);

                ProgressBar progressBar = new ProgressBar();
                progressBar.setIndeterminate(true);
                dialogLayout.add(progressBar);

                List<DistributionTarget> targets = new ArrayList<>();
                for (Map.Entry<DistributionTarget, Checkbox> entry : targetCheckboxMap.entrySet()) {
                    if (entry.getValue().getValue()) {
                        targets.add(entry.getKey());
                    }
                }

                final UI ui = UI.getCurrent();

                // 1. Get all animes available on MEGA
                Runnable runnable = () -> {

                    List<HosterFile> filesOnMega = MegaNZ.getFiles();

                    filesOnMega.stream().filter(hosterFile -> hosterFile.getEpisode() == null).forEach(hosterFile -> logger.debug("Missing episode for: " + hosterFile.getName()));

                    List<HosterFile> filteredFilesOnMega = filesOnMega.stream().filter(hosterFile -> hosterFile.getEpisode() != null).collect(Collectors.toList());
                    Set<Episode> episodesOnMega = filteredFilesOnMega.parallelStream().map(hosterFile -> hosterFile.getEpisode()).collect(Collectors.toSet());

                    // Calculate Episodes to distribute
                    Map<Hoster, DistributionTarget> hosterDistributionTargetMap = new HashMap<>();
                    targets.forEach(distributionTarget -> hosterDistributionTargetMap.put(distributionTarget.getHoster(), distributionTarget));

                    List<DistributionJob> distributionJobs = new ArrayList<>();
                    episodesOnMega.parallelStream().forEach(episode ->
                    {
                        List<HostedEpisode> hostedEpisodes = episode.getHostedEpisodes();

                        Set<Hoster> hostersForEpisode = new HashSet<>();
                        for (HostedEpisode hostedEpisode : hostedEpisodes) {
                            hostersForEpisode.add(hostedEpisode.getHoster());
                        }


                        List<DistributionTarget> distributionTargets = new ArrayList<>();
                        for (Map.Entry<Hoster, DistributionTarget> entry : hosterDistributionTargetMap.entrySet()) {
                            if (!hostersForEpisode.contains(entry.getKey())) {
                                // File is not on hoster, but distribution is selected!
                                distributionTargets.add(entry.getValue());
                            }
                        }

                        if (distributionTargets.size() > 0) {
                            DistributionJob distributionJob = new DistributionJob();
                            distributionJob.anime = database.getAnimeByID(episode.getAnimeID());
                            distributionJob.episode = episode;
                            distributionJob.source = DistributionSource.MEGA;
                            distributionJob.targets = distributionTargets;
                            distributionJobs.add(distributionJob);
                        }
                    });

                    AtomicBoolean continueProcess = new AtomicBoolean(false);
                    ui.access(() ->
                    {
                        progressBarStatusLabel.setText("Finished calculation! " + distributionJobs.size() + " distribution jobs needed!");
                        progressBar.setIndeterminate(false);
                        progressBar.setValue(0);

                        Button continueProcessButton = new Button("Continue");
                        continueProcessButton.addClickListener(buttonClickEvent1 -> continueProcess.set(true));
                        dialogLayout.add(continueProcessButton);
                    });

                    while (!continueProcess.get()) {
                        try {
                            Thread.sleep(2500);
                        } catch (InterruptedException e) {
                        }
                    }

                    ui.access(() ->
                    {
                        progressBarStatusLabel.setText("Starting distribution...");
                        progressBar.setIndeterminate(true);
                        progressBar.setValue(0);

                        // remove button
                        dialogLayout.remove(dialogLayout.getComponentAt(dialogLayout.getComponentCount() - 1));
                    });

                    ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);

                    List<Future<DistributionJob>> finishedJobs = new ArrayList<>();
                    List<Future<DistributionJob>> runningJobs = new ArrayList<>();

                    for (DistributionJob job : distributionJobs) {
                        Future<DistributionJob> future = executor.submit(() ->
                        {
                            DistributionManager.submitJob(job);
                            return job;
                        });
                        runningJobs.add(future);
                    }


                    long startTime = System.currentTimeMillis();
                    while (runningJobs.size() != finishedJobs.size()) {
                        for (Future<DistributionJob> runningJob : runningJobs) {
                            if (runningJob.isDone() && !finishedJobs.contains(runningJob)) {
                                finishedJobs.add(runningJob);
                            }
                        }

                        if (finishedJobs.size() % 10 == 0 && finishedJobs.size() > 0) {
                            long currentTime = System.currentTimeMillis();
                            long timePerElement = (currentTime - startTime) / finishedJobs.size();
                            long timeLeft = (runningJobs.size() - finishedJobs.size()) * timePerElement;
                            String eta = String.format("%d min, %d sec",
                                    TimeUnit.MILLISECONDS.toMinutes(timeLeft),
                                    TimeUnit.MILLISECONDS.toSeconds(timeLeft) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeLeft)));
                            logger.info("Current " + finishedJobs.size() + " of " + runningJobs.size() + " ETA: " + eta);
                        }

                        try {
                            Thread.sleep(2500);
                        } catch (InterruptedException e) {
                        }

                        updateProgress(ui, progressBarStatusLabel, progressBar, distributionJobs.size(), finishedJobs.size());
                    }

                    logger.info("Finished distribution");
                };

                Thread backgroundThread = new Thread(runnable);
                backgroundThread.start();

            });
            dialogLayout.add(runDistributionButton);

            Dialog dialog = new Dialog();
            dialog.add(dialogLayout);
            dialog.setCloseOnEsc(false);
            dialog.setCloseOnOutsideClick(false);

            Button confirmButton = new Button("Done", event -> {
                dialog.close();
            });

            dialog.add(confirmButton);
            dialog.open();
        });

    }

    private void updateProgress(UI ui, Label progressBarStatusLabel, ProgressBar progressBar, int totalJobs, int finishedJobs) {
        ui.access(() ->
        {
            progressBarStatusLabel.setText("Running: " + finishedJobs + " / " + totalJobs);
            progressBar.setIndeterminate(false);
            progressBar.setValue(Double.valueOf(finishedJobs) / Double.valueOf(totalJobs));
        });
    }


}
