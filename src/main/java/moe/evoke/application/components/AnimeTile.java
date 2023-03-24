package moe.evoke.application.components;

import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.progressbar.ProgressBarVariant;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import moe.evoke.application.backend.db.Anime;
import moe.evoke.application.backend.db.Database;
import moe.evoke.application.backend.db.Episode;
import moe.evoke.application.security.SecurityUtils;
import moe.evoke.application.views.watch.WatchAnimeView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@CssImport("./components/anime-tile.css")
public class AnimeTile extends Div {

    private static final Logger logger = LoggerFactory.getLogger(AnimeTile.class);

    private final Anime anime;
    private final Episode episode;
    private final Image cover;
    private final Paragraph paragraph;
    private final ProgressBar statusBar;

    private final Database database;

    public AnimeTile(Database database, Anime anime, Episode episode) {
        this(database, anime, episode, true);
    }

    public AnimeTile(Database database, Anime anime) {
        this(database, anime, null, true);
    }

    public AnimeTile(Database database, Anime anime, boolean autoLoadProgress) {
        this(database, anime, null, true);
    }

    public AnimeTile(Database database, Anime anime, Episode episode, boolean autoLoadProgress) {
        this.anime = anime;
        this.episode = episode;
        this.database = database;

        setClassName("anime-tile");

        addClickListener(divClickEvent -> getUI().ifPresent(ui -> {
            long episodeNum = 1;
            if (episode != null) {
                episodeNum = episode.getNumber();
            }

            RouteParam paramAnime = new RouteParam("animeID", Long.toString(anime.getAnilistID()));
            RouteParam paramEpisode = new RouteParam("episode", Long.toString(episodeNum));
            RouteParameters params = new RouteParameters(paramAnime, paramEpisode);
            ui.navigate(WatchAnimeView.class, params);
        }));

        cover = anime.getCoverAsImage();
        add(cover);

        paragraph = new Paragraph(anime.getName());
        add(paragraph);


        statusBar = new ProgressBar();
        add(statusBar);

        if (autoLoadProgress) {
            double progress = database.getWatchProgressPercentageForUserAndAnime(SecurityUtils.getUsername(), anime);
            setProgress(progress);
        }

        createContextMenu();
    }

    private void createContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        contextMenu.setTarget(this);

        contextMenu.addItem("Open in New Tab", event -> getUI().ifPresent(ui -> {
            ui.getPage().executeJs("return window.location.origin").then(String.class, origin -> {
                String target = origin + "/watch/" + this.anime.getAnilistID() + "/" + (this.episode == null ? 1 : this.episode.getNumber());
                ui.getPage().open(target, "_blank");
            });
        }));
        contextMenu.addItem("Remove Watch Progress", event -> getUI().ifPresent(ui -> {
            database.removeWatchProgressForAnime(SecurityUtils.getUsername(), this.anime);
            ui.getPage().reload();
        }));

    }

    public void setTextAsHTML(String value) {
        this.paragraph.getElement().setProperty("innerHTML", value);
    }

    @Override
    public void setText(String text) {
        // do nothing
    }

    public void setProgress(double progress) {
        if (progress < 0.0 || progress > 1.0)
        {
            return;
        }

        statusBar.setValue(progress);
        if (progress == 1) {
            statusBar.addThemeVariants(ProgressBarVariant.LUMO_SUCCESS);
        }
    }
}
