package moe.evoke.application.views.anime;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.ironlist.IronList;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import moe.evoke.application.backend.anilist.Anilist;
import moe.evoke.application.backend.db.Database;
import moe.evoke.application.backend.monthly.moe.AiringAnime;
import moe.evoke.application.components.AnimeTile;
import moe.evoke.application.views.main.MainView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Route(value = "airing", layout = MainView.class)
@PageTitle("Airing Anime")
@CssImport("./views/anime/anime-airing-view.css")
public class AnimeAiringView extends VerticalLayout implements BeforeEnterObserver {

    private static final Logger logger = LoggerFactory.getLogger(AnimeAiringView.class);

    private final Map<Integer, List<AiringAnime.AiringEpisode>> dayToAiringAnime = new HashMap<>();
    private final Map<Integer, VerticalLayout> weekDayToLayout = new HashMap<Integer, VerticalLayout>();

    private final Database database;

    public AnimeAiringView(@Autowired Database database) {
        this.database = database;
        addClassName("animes-airing-view");

        populateMap();

        createAiringAnime();

    }

    private void populateMap() {

        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date startDay = calendar.getTime();

        calendar.add(Calendar.DAY_OF_YEAR, 6);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        Date endDay = calendar.getTime();

        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        logger.debug("Start Day: " + dateFormat.format(startDay));
        logger.debug("End Day: " + dateFormat.format(endDay));

        List<AiringAnime.AiringEpisode> airingAnimeList = Anilist.getAiring();
        for (AiringAnime.AiringEpisode airingEpisode : airingAnimeList) {
            if (airingEpisode.getAiringDate().before(endDay) && airingEpisode.getAiringDate().after(startDay)) {
                calendar.setTime(airingEpisode.getAiringDate());
                int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
                if (!dayToAiringAnime.containsKey(dayOfYear)) {
                    dayToAiringAnime.put(dayOfYear, new ArrayList<>());
                }

                dayToAiringAnime.get(dayOfYear).add(airingEpisode);
            }
        }
    }

    private void createAiringAnime() {
        Label airingAnimeLabel = new Label("Airing Anime");
        add(airingAnimeLabel);

        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);

        for (int i = 0; i < 7; i++) {
            createWeekdayRow(calendar.get(Calendar.DAY_OF_YEAR), calendar.get(Calendar.DAY_OF_WEEK));

            if (i + 1 < 6) {
                add(new Hr());
            }

            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

    }

    private void createWeekdayRow(int dayOfYear, int dayOfWeek) {

        VerticalLayout weekdayRow = new VerticalLayout();
        weekdayRow.setMaxWidth("80%");
        weekDayToLayout.put(dayOfWeek, weekdayRow);
        add(weekdayRow);

        Label dayOfWeekLabel = new Label();
        dayOfWeekLabel.setText(getDayNameByDayOfWeek(dayOfWeek));
        weekdayRow.add(dayOfWeekLabel);

        List<AiringAnime.AiringEpisode> animes = dayToAiringAnime.get(dayOfYear);
        if (animes == null) {
            return;
        }

        animes.sort(Comparator.comparing(AiringAnime.AiringEpisode::getAiringDate));

        IronList<AiringAnime.AiringEpisode> airingAnimeIronList = new IronList<>();
        airingAnimeIronList.setGridLayout(true);
        airingAnimeIronList.setSizeFull();
        airingAnimeIronList.setItems(animes);
        airingAnimeIronList.setRenderer(getAnimeRenderer());
        weekdayRow.add(airingAnimeIronList);
    }

    private String getDayNameByDayOfWeek(int dayOfWeek) {
        switch (dayOfWeek) {
            case Calendar.MONDAY:
                return "Monday";
            case Calendar.TUESDAY:
                return "Tuesday";
            case Calendar.WEDNESDAY:
                return "Wednesday";
            case Calendar.THURSDAY:
                return "Thursday";
            case Calendar.FRIDAY:
                return "Friday";
            case Calendar.SATURDAY:
                return "Saturday";
            case Calendar.SUNDAY:
                return "Sunday";
            default:
                return "unknown";
        }
    }

    private Renderer getAnimeRenderer() {
        ComponentRenderer renderer = new ComponentRenderer<AnimeTile, AiringAnime.AiringEpisode>(airingEpisode ->
        {
            AnimeTile animeTile = new AnimeTile(database, airingEpisode.getEpisode().getAnime(), airingEpisode.getEpisode());
            animeTile.addClassName("anime-tile-airing-view");

            DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
            String value =
                    airingEpisode.getEpisode().getAnime().getName() +
                            "</br>" +
                            airingEpisode.getEpisode().getNumber() +
                            "/" +
                            airingEpisode.getEpisode().getAnime().getEpisodeCount() +
                            " - " +
                            dateFormat.format(airingEpisode.getAiringDate());

            animeTile.setTextAsHTML(value);

            return animeTile;
        });

        return renderer;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {

    }
}
