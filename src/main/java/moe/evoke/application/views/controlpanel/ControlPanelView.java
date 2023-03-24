package moe.evoke.application.views.controlpanel;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import moe.evoke.application.backend.db.Anime;
import moe.evoke.application.backend.db.Database;
import moe.evoke.application.backend.db.Hoster;
import moe.evoke.application.security.SecurityUtils;
import moe.evoke.application.views.controlpanel.dialogs.*;
import moe.evoke.application.views.main.MainView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Route(value = "cp", layout = MainView.class)
@PageTitle("Control Panel")
@CssImport("./views/controlpanel/control-panel-view.css")
@Secured("admin")
public class ControlPanelView extends Div implements BeforeEnterObserver {

    private static final Logger logger = LoggerFactory.getLogger(ControlPanelView.class);
    private final Database database;
    private Grid<Anime> animeGrid;
    private Grid<Hoster> hosterGrid;

    public ControlPanelView(@Autowired Database database) {
        this.database = database;
        addClassName("control-panel-view");

        VerticalLayout layout = new VerticalLayout();
        add(layout);

        createAnimeGrid(layout);
        createHosterGrid(layout);

        createUtils(layout);
    }

    private void createUtils(VerticalLayout layout) {

        layout.add(new Hr());
        layout.add(new Label("Utility Section"));

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        layout.add(horizontalLayout);

        Button findMissingEpisodesButton = new Button("Find missing episodes");
        findMissingEpisodesButton.addClickListener(buttonClickEvent -> new FindMissingEpisodesDialog(database).open());
        horizontalLayout.add(findMissingEpisodesButton);

        Button showReportsButton = new Button("Show Reports");
        showReportsButton.addClickListener(buttonClickEvent -> new ReportsDialog(database).open());
        horizontalLayout.add(showReportsButton);

        Button showUsersButton = new Button("Show Users");
        showUsersButton.addClickListener(buttonClickEvent -> new UserDialog(database).open());
        horizontalLayout.add(showUsersButton);

        Button showJobsButton = new Button("Show Distribution Jobs");
        showJobsButton.addClickListener(buttonClickEvent -> new DistributionJobsDialog().open());
        horizontalLayout.add(showJobsButton);

        Button inviteCodesButton = new Button("Show Invite Codes");
        inviteCodesButton.addClickListener(buttonClickEvent -> new UserInviteCodeDialog(database).open());
        horizontalLayout.add(inviteCodesButton);

    }

    private void createHosterGrid(VerticalLayout layout) {
        Label hoster = new Label("Hoster");
        layout.add(hoster);

        hosterGrid = new Grid<>(Hoster.class);
        hosterGrid.setItems(database.getHoster());
        hosterGrid.addItemClickListener(hosterItemClickEvent -> {
            if (hosterItemClickEvent.getClickCount() == 2) {
                getUI().ifPresent(ui -> {
                    RouteParam paramHoster = new RouteParam("hosterID", Long.toString(hosterItemClickEvent.getItem().getID()));
                    RouteParameters params = new RouteParameters(paramHoster);
                    ui.navigate(HosterControlPanelView.class, params);
                });
            }
        });
        layout.add(hosterGrid);

        HorizontalLayout editHosterLayout = new HorizontalLayout();
        layout.add(editHosterLayout);

        Button addHoster = new Button("Add");
        addHoster.addClickListener(buttonClickEvent -> new AddHosterDialog(database, () -> reloadHoster()).open());
        editHosterLayout.add(addHoster);

        Button removeHoster = new Button("Remove");
        removeHoster.addClickListener(buttonClickEvent -> {
            for (Hoster hosterObj : hosterGrid.getSelectedItems()) {
                database.removeHoster(hosterObj);
            }
            reloadHoster();
        });
        editHosterLayout.add(removeHoster);
    }

    private void reloadAnime() {
        animeGrid.setItems(database.getAnime());
    }

    private void reloadHoster() {
        hosterGrid.setItems(database.getHoster());
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!SecurityUtils.isAccessGranted(ControlPanelView.class)) {
            event.rerouteTo("home");
        }
    }

    private void createAnimeGrid(VerticalLayout layout) {

        Label animeLabel = new Label("Animes");
        layout.add(animeLabel);

        animeGrid = new Grid<>(Anime.class);
        animeGrid.setPageSize(Integer.MAX_VALUE);
        animeGrid.removeColumnByKey("episodes");
        animeGrid.removeColumnByKey("data");
        animeGrid.removeColumnByKey("cover");
        animeGrid.removeColumnByKey("coverAsImage");
        animeGrid.addItemClickListener(animeItemClickEvent -> {
            if (animeItemClickEvent.getClickCount() == 2) {
                getUI().ifPresent(ui -> {
                    RouteParam paramAnime = new RouteParam("animeID", Long.toString(animeItemClickEvent.getItem().getID()));
                    RouteParameters params = new RouteParameters(paramAnime);
                    ui.navigate(EditAnimeControlPanelView.class, params);
                });
            }
        });
        layout.add(animeGrid);

        final UI ui = UI.getCurrent();
        CompletableFuture.runAsync(() -> {
            List<Anime> allAnimeList = Database.instance().getAnime();
            ui.access(() -> animeGrid.setItems(allAnimeList));
        });

        HorizontalLayout editAnimeLayout = new HorizontalLayout();
        layout.add(editAnimeLayout);

        Button addAnime = new Button("Add");
        addAnime.addClickListener(buttonClickEvent -> new AddAnimeDialog(database, () -> reloadAnime()).open());
        editAnimeLayout.add(addAnime);

        Button removeAnime = new Button("Remove");
        removeAnime.addClickListener(buttonClickEvent -> {
            for (Anime anime : animeGrid.getSelectedItems()) {
                database.removeAnime(anime);
            }

            reloadAnime();
        });
        editAnimeLayout.add(removeAnime);

    }
}
