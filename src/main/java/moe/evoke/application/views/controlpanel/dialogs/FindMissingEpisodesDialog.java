package moe.evoke.application.views.controlpanel.dialogs;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import moe.evoke.application.backend.db.Anime;
import moe.evoke.application.backend.db.Database;
import moe.evoke.application.backend.db.Episode;
import moe.evoke.application.backend.mirror.distribution.*;
import moe.evoke.application.backend.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FindMissingEpisodesDialog extends Dialog {

    private static final Logger logger = LoggerFactory.getLogger(FindMissingEpisodesDialog.class);

    public FindMissingEpisodesDialog(Database database) {
        List<Episode> missingEpisodes = database.findMissingEpisodesForAnimes(10000);

        this.setWidth("60vw");

        VerticalLayout dialogLayout = new VerticalLayout();

        TextField searchAnimeTextField = new TextField();
        searchAnimeTextField.setLabel("Search For Anime");
        dialogLayout.add(searchAnimeTextField);

        Grid<Episode> missingEpisodeGrid = new Grid<>();
        missingEpisodeGrid.addColumn(Episode::getAnime);
        missingEpisodeGrid.addColumn(Episode::getNumber);
        missingEpisodeGrid.setItems(missingEpisodes);
        missingEpisodeGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        missingEpisodeGrid.setPageSize(Integer.MAX_VALUE);
        dialogLayout.add(missingEpisodeGrid);

        searchAnimeTextField.addValueChangeListener(event -> {
            List<Episode> filteredEpisodes = missingEpisodes.stream().filter(episode -> Utils.animeTitleContains(searchAnimeTextField.getValue(), episode.getAnime())).collect(Collectors.toList());
            missingEpisodeGrid.setItems(filteredEpisodes);
        });

        Map<DistributionTarget, Checkbox> targetCheckboxMap = new HashMap<>();
        for (DistributionTarget distributionTarget : DistributionTarget.values()) {
            Checkbox distributeCheckbox = new Checkbox(distributionTarget.getLabel());
            distributeCheckbox.setValue(true);
            dialogLayout.add(distributeCheckbox);
            targetCheckboxMap.put(distributionTarget, distributeCheckbox);
        }

        HorizontalLayout actionsLayout = new HorizontalLayout();
        dialogLayout.add(actionsLayout);

        Button importAllMissingEpisodesButton = new Button("Import All Episodes");
        importAllMissingEpisodesButton.addClickListener(buttonClickEvent -> importMissingEpisodes(targetCheckboxMap, missingEpisodes));
        actionsLayout.add(importAllMissingEpisodesButton);

        Button autoImportSelectedEpisodesButton = new Button("Import Selected Episodes");
        autoImportSelectedEpisodesButton.addClickListener(buttonClickEvent -> importMissingEpisodes(targetCheckboxMap, new ArrayList<>(missingEpisodeGrid.getSelectedItems())));
        actionsLayout.add(autoImportSelectedEpisodesButton);

        this.add(dialogLayout);
        this.setCloseOnEsc(false);
        this.setCloseOnOutsideClick(false);

        Button confirmButton = new Button("Done", event -> {
            this.close();
        });

        this.add(confirmButton);
    }

    private void importMissingEpisodes(Map<DistributionTarget, Checkbox> targetCheckboxMap, List<Episode> missingEpisodes) {
        List<DistributionTarget> targets = new ArrayList<>();
        for (Map.Entry<DistributionTarget, Checkbox> entry : targetCheckboxMap.entrySet()) {
            if (entry.getValue().getValue()) {
                targets.add(entry.getKey());
            }
        }

        for (Episode episode : missingEpisodes) {
            Anime anime = episode.getAnime();

            DistributionSource source = DistributionHelper.getBestSourceForAnime(anime, episode);

            if (source != null) {
                DistributionJob job = new DistributionJob();
                job.anime = anime;
                job.episode = episode;
                job.source = source;
                job.targets = targets;

                DistributionManager.submitJob(job);
            }
        }

    }
}
