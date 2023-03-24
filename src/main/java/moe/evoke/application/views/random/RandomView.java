package moe.evoke.application.views.random;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.ironlist.IronList;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import moe.evoke.application.backend.db.Anime;
import moe.evoke.application.backend.db.Database;
import moe.evoke.application.components.AnimeTile;
import moe.evoke.application.views.main.MainView;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;

@Route(value = "random", layout = MainView.class)
@PageTitle("Random")
@CssImport("./views/random/random-view.css")
public class RandomView extends VerticalLayout {

    private final List<Anime> animeList;
    private final Database database;
    private Button randomButton;
    private List<Anime> shortendList;
    private IronList<Anime> randomResultLayout;

    public RandomView(@Autowired Database database) {
        this.database = database;
        addClassName("random-view");

        createRandomButton();
        add(new Hr());
        createRandom();

        animeList = database.searchAnime(null, null, null, null, null, "SeasonYear", false, 250, 0);
        Collections.shuffle(animeList);
        shortendList = animeList.subList(0, 14);
        randomResultLayout.setItems(shortendList);
    }

    private void createRandomButton() {
        FlexLayout RandomLayout = new FlexLayout();
        RandomLayout.setJustifyContentMode(JustifyContentMode.START);
        RandomLayout.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        RandomLayout.setAlignContent(FlexLayout.ContentAlignment.CENTER);
        add(RandomLayout);

        randomButton = new Button();
        randomButton.setText("Randomize");
        randomButton.addClickListener(buttonClickEvent -> LoadRandomAnime());
        RandomLayout.add(randomButton);
    }

    private void LoadRandomAnime() {
        final UI ui = UI.getCurrent();

        Collections.shuffle(animeList);
        shortendList = animeList.subList(0, 14);

        ui.access(() -> randomResultLayout.setItems(shortendList));
    }

    private void createRandom() {
        HorizontalLayout filterLayout = new HorizontalLayout();
        filterLayout.setSizeFull();
        add(filterLayout);

        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setHorizontalComponentAlignment(Alignment.CENTER);
        add(layout);

        randomResultLayout = new IronList<>();
        randomResultLayout.setGridLayout(true);
        randomResultLayout.setHeight("100%");
        randomResultLayout.setRenderer(getAnimeRenderer());
        layout.add(randomResultLayout);
    }

    private Renderer getAnimeRenderer() {
        ComponentRenderer renderer = new ComponentRenderer<AnimeTile, Anime>(anime -> new AnimeTile(database, anime));
        return renderer;
    }

}
