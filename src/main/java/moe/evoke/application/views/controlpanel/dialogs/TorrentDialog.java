package moe.evoke.application.views.controlpanel.dialogs;

import com.apptastic.rssreader.Item;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.function.ValueProvider;
import moe.evoke.application.backend.db.Anime;
import moe.evoke.application.backend.db.Episode;
import moe.evoke.application.backend.mirror.distribution.DistributionJob;
import moe.evoke.application.backend.mirror.distribution.DistributionManager;
import moe.evoke.application.backend.mirror.distribution.DistributionSource;
import moe.evoke.application.backend.mirror.distribution.DistributionTarget;
import moe.evoke.application.backend.torrent.RssClient;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class TorrentDialog extends Dialog {

    private final Anime anime;
    private final Map<Episode, Item> episodeItemMap = new HashMap<>();
    private final Map<DistributionTarget, Checkbox> targetCheckboxMap = new HashMap<>();

    public TorrentDialog(Anime anime) {
        this.anime = anime;
        VerticalLayout dialogLayout = new VerticalLayout();
        this.add(dialogLayout);

        Grid<Item> rssGrid = new Grid<>();
        rssGrid.addColumn((ValueProvider<Item, String>) item -> item.getGuid().get());
        rssGrid.addColumn((ValueProvider<Item, String>) item -> item.getTitle().get());
        rssGrid.addColumn((ValueProvider<Item, String>) item -> item.getLink().get());
        for (Grid.Column<Item> column : rssGrid.getColumns()) {
            column.setAutoWidth(true);
        }
        rssGrid.setItems(RssClient.searchNyaa(anime, null, false));
        dialogLayout.add(rssGrid);

        GridContextMenu<Item> contextMenu = rssGrid.addContextMenu();
        for (Episode episode : anime.getEpisodes()) {
            contextMenu.addItem("As Episode " + episode.getNumber(),
                    (ComponentEventListener<GridContextMenu.GridContextMenuItemClickEvent<Item>>) clickEvent ->
                            episodeItemMap.put(episode, clickEvent.getItem().get())
            );
        }

        dialogLayout.add(new Hr(), new Label("Distribution Targets"));

        for (DistributionTarget distributionTarget : DistributionTarget.values()) {
            Checkbox distributeCheckbox = new Checkbox(distributionTarget.getLabel());
            distributeCheckbox.setValue(true);
            dialogLayout.add(distributeCheckbox);
            targetCheckboxMap.put(distributionTarget, distributeCheckbox);
        }

        Button createJobsButton = new Button("Create Jobs");
        createJobsButton.addClickListener(clickEvent ->
        {
            Dialog dialog = new Dialog();
            dialog.setWidth("60vw");

            Grid<Map.Entry<Episode, Item>> summaryGrid = new Grid<>();
            summaryGrid.addColumn((ValueProvider<Map.Entry<Episode, Item>, String>) entry -> String.valueOf(entry.getKey().getNumber()));
            summaryGrid.addColumn((ValueProvider<Map.Entry<Episode, Item>, String>) entry -> entry.getValue().getTitle().get());
            for (Grid.Column<Item> column : rssGrid.getColumns()) {
                column.setAutoWidth(true);
            }
            summaryGrid.setItems(episodeItemMap.entrySet());
            dialog.add(summaryGrid);

            Button confirmButton = new Button("Confirm");
            confirmButton.getStyle().set("margin", "5px");
            confirmButton.addClickListener(event ->
            {
                submitJobs();
                dialog.close();
            });
            dialog.add(confirmButton);

            Button abortButton = new Button("Abort");
            abortButton.getStyle().set("margin", "5px");
            abortButton.addClickListener(event -> dialog.close());
            dialog.add(abortButton);

            dialog.open();
        });
        dialogLayout.add(createJobsButton);

        this.setWidth("80vw");
        this.add(dialogLayout);
        this.setCloseOnEsc(false);
        this.setCloseOnOutsideClick(false);


        Button confirmButton = new Button("Done", event -> {
            this.close();
        });

        this.add(confirmButton);
    }

    private void submitJobs() {
        for (Map.Entry<Episode, Item> episodeItemEntry : episodeItemMap.entrySet()) {

            DistributionJob job = new DistributionJob();
            job.anime = this.anime;
            job.episode = episodeItemEntry.getKey();
            job.source = DistributionSource.TORRENT;
            job.sourceOptions = new HashMap<>();
            job.sourceOptions.put(DistributionSource.MAGNET_LINK, episodeItemEntry.getValue().getLink().get());
            job.targets = targetCheckboxMap.entrySet().stream().filter(entry -> entry.getValue().getValue()).map(Map.Entry::getKey).collect(Collectors.toList());

            DistributionManager.submitJob(job);


        }
    }

}
