package moe.evoke.application.views.anime;

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import moe.evoke.application.backend.db.Anime;
import moe.evoke.application.backend.db.Database;
import moe.evoke.application.components.AnimeTile;
import moe.evoke.application.views.main.MainView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.gatanaso.MultiselectComboBox;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Route(value = "anime", layout = MainView.class)
@PageTitle("Anime Search")
@CssImport("./views/anime/anime-view.css")
public class AnimeView extends VerticalLayout {

    private static final Logger logger = LoggerFactory.getLogger(AnimeView.class);

    private static final String SORT_NAME = "Name";
    private static final String SORT_NAME_COLUMN = "Title";
    private static final String SORT_YEAR = "Year";
    private static final String SORT_YEAR_COLUMN = "SeasonYear";
    private static final String SORT_SCORE = "Score";
    private static final String SORT_SCORE_COLUMN = "AverageScore";

    private static final Map<String, String> SORT_MAP = new HashMap<>();

    static {
        SORT_MAP.put(SORT_NAME, SORT_NAME_COLUMN);
        SORT_MAP.put(SORT_YEAR, SORT_YEAR_COLUMN);
        SORT_MAP.put(SORT_SCORE, SORT_SCORE_COLUMN);
    }

    private final AtomicBoolean scrollRunning = new AtomicBoolean(false);
    private final Database database;
    private TextField animeNameTextField;
    private MultiselectComboBox<String> genresComboBox;
    private MultiselectComboBox<String> tagsComboBox;
    private MultiselectComboBox<String> releaseYearComboBox;
    private MultiselectComboBox<String> formatComboBox;
    private Select<String> sortFieldSelect;
    private FlexLayout searchResultLayout;

    public AnimeView(@Autowired Database database) {
        addClassName("anime-view");

        createAnimeSearchBar();
        add(new Hr());
        createAnimeSearch();

        this.database = database;

        List<Anime> result = database.searchAnime(null, null, null, null, null, SORT_MAP.get(SORT_YEAR), false, 100, 0);
        result.forEach(anime -> addAnime(searchResultLayout, anime));

        Set<String> genres = database.getAnimeGenres();
        genresComboBox.setItems(genres.stream().sorted().collect(Collectors.toList()));

        Set<String> tags = database.getAnimeTags();
        tagsComboBox.setItems(tags.stream().sorted().collect(Collectors.toList()));

        Set<String> formats = database.getAnimeFormats();
        formatComboBox.setItems(formats.stream().sorted().collect(Collectors.toList()));

        Set<String> years = database.getAnimeYear();
        List<String> yearsList = years.stream().sorted().collect(Collectors.toList());
        Collections.reverse(yearsList);
        releaseYearComboBox.setItems(yearsList);
    }

    private void createAnimeSearchBar() {

        FlexLayout animeSearchLayout = new FlexLayout();
        animeSearchLayout.setJustifyContentMode(JustifyContentMode.START);
        animeSearchLayout.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        animeSearchLayout.setAlignContent(FlexLayout.ContentAlignment.SPACE_BETWEEN);
        add(animeSearchLayout);

        animeNameTextField = new TextField();
        animeNameTextField.getStyle().set("margin", "5px");
        animeNameTextField.setLabel("Name");
        animeNameTextField.addValueChangeListener(changeEvent -> doSearch());
        animeSearchLayout.add(animeNameTextField);

        genresComboBox = new MultiselectComboBox<>();
        genresComboBox.getStyle().set("margin", "5px");
        genresComboBox.setLabel("Genres");
        genresComboBox.addValueChangeListener(changeEvent -> doSearch());
        animeSearchLayout.add(genresComboBox);

        tagsComboBox = new MultiselectComboBox<>();
        tagsComboBox.getStyle().set("margin", "5px");
        tagsComboBox.setLabel("Tags");
        tagsComboBox.addValueChangeListener(changeEvent -> doSearch());
        animeSearchLayout.add(tagsComboBox);

        releaseYearComboBox = new MultiselectComboBox<>();
        releaseYearComboBox.getStyle().set("margin", "5px");
        releaseYearComboBox.setLabel("Year");
        releaseYearComboBox.addValueChangeListener(changeEvent -> doSearch());
        animeSearchLayout.add(releaseYearComboBox);

        formatComboBox = new MultiselectComboBox<>();
        formatComboBox.getStyle().set("margin", "5px");
        formatComboBox.setLabel("Format");
        formatComboBox.addValueChangeListener(changeEvent -> doSearch());
        animeSearchLayout.add(formatComboBox);

        sortFieldSelect = new Select<>();
        sortFieldSelect.setLabel("Sort");
        sortFieldSelect.getStyle().set("margin", "5px");
        sortFieldSelect.setItems(SORT_MAP.keySet());
        sortFieldSelect.setValue(SORT_YEAR);
        sortFieldSelect.addValueChangeListener(changeEvent -> doSearch());
        animeSearchLayout.add(sortFieldSelect);

    }

    private void doSearch() {
        searchResultLayout.removeAll();

        UI.getCurrent().getPage().executeJs("" +
                "var element = document.getElementById(\"animeSearchResult\");" +
                "element.scrollTop = 0;"
        );

        loadAnime();
    }

    private void createAnimeSearch() {
        HorizontalLayout filterLayout = new HorizontalLayout();
        filterLayout.setSizeFull();
        add(filterLayout);

        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setHorizontalComponentAlignment(Alignment.CENTER);
        add(layout);

        searchResultLayout = new FlexLayout();
        searchResultLayout.setWidth("100%");
        searchResultLayout.setHeight("100%");
        searchResultLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        searchResultLayout.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        searchResultLayout.setAlignContent(FlexLayout.ContentAlignment.SPACE_BETWEEN);
        layout.add(searchResultLayout);

        searchResultLayout.setId("animeSearchResult");

        setId(UUID.randomUUID().toString());
        UI.getCurrent().getPage().executeJs(
                "var element = document.querySelector(\"body > vaadin-app-layout\").shadowRoot.querySelector(\"div:nth-child(5)\");" +
                        "var vaadinObject = document.getElementById('" + getId().get() + "');" +
                        "var h = element, b = document.body, st = 'scrollTop', sh = 'scrollHeight';" +
                        "element.onscroll = function(){" +
                        "var percent = (h[st]||b[st]) / ((h[sh]||b[sh]) - h.clientHeight) * 100;" +
                        "vaadinObject.$server.scrollPercent(percent)" +
                        "};"
        );
    }

    @ClientCallable
    private void scrollPercent(String value) {
        if (!scrollRunning.get()) {
            scrollRunning.set(true);
            double scrollPercent = Double.parseDouble(value);
            if (scrollPercent > 80) {
                loadAnime();
            }
            scrollRunning.set(false);
        }
    }

    private void loadAnime() {
        String searchName = animeNameTextField.getValue();
        List<String> searchGenres = new ArrayList<>(genresComboBox.getSelectedItems());
        List<String> searchTags = new ArrayList<>(tagsComboBox.getSelectedItems());
        List<String> searchReleaseYears = new ArrayList<>(releaseYearComboBox.getSelectedItems());
        List<String> searchFormats = new ArrayList<>(formatComboBox.getSelectedItems());

        List<Anime> result = database.searchAnime(searchName, searchGenres, searchTags, searchReleaseYears, searchFormats, SORT_MAP.get(sortFieldSelect.getValue()), sortFieldSelect.getValue().equals(SORT_NAME), 100, (int) searchResultLayout.getChildren().count());

        result.forEach(anime -> addAnime(searchResultLayout, anime));
    }

    private void addAnime(FlexLayout layout, Anime anime) {
        AnimeTile animeTile = new AnimeTile(database, anime);
        layout.add(animeTile);
    }

}
