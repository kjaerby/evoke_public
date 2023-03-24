package moe.evoke.application.views.home;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import moe.evoke.application.backend.db.Anime;
import moe.evoke.application.backend.db.Database;
import moe.evoke.application.backend.db.News;
import moe.evoke.application.backend.db.NewsType;
import moe.evoke.application.components.AnimeTile;
import moe.evoke.application.components.StyledText;
import moe.evoke.application.security.SecurityUtils;
import moe.evoke.application.views.main.MainView;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Route(value = "home", layout = MainView.class)
@RouteAlias(value = "", layout = MainView.class)
@PageTitle("Home")
@CssImport("./views/home/home-view.css")
public class HomeView extends VerticalLayout {

    private final FlexLayout animeNewsLayout;
    private final VerticalLayout customNewsLayout;

    private final Database database;

    public HomeView(@Autowired Database database) {
        this.database = database;

        addClassName("home-view");
        setHorizontalComponentAlignment(Alignment.CENTER);

        createContinueWatching();

        animeNewsLayout = new FlexLayout();
        animeNewsLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        animeNewsLayout.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        animeNewsLayout.setAlignContent(FlexLayout.ContentAlignment.SPACE_BETWEEN);

        customNewsLayout = new VerticalLayout();

        createNewsItems();
    }

    private void createContinueWatching() {

        List<Anime> lastWatchedAnime = database.getLastWatchedAnimeForUser(SecurityUtils.getUsername(), 10, true);

        if (lastWatchedAnime.size() > 0) {

            add(new Label("Continue Watching"));

            FlexLayout continueWatchingLayout = new FlexLayout();
            continueWatchingLayout.setJustifyContentMode(JustifyContentMode.CENTER);
            continueWatchingLayout.setFlexWrap(FlexLayout.FlexWrap.WRAP);
            continueWatchingLayout.setAlignContent(FlexLayout.ContentAlignment.SPACE_BETWEEN);
            add(continueWatchingLayout);

            for (Anime anime : lastWatchedAnime) {
                AnimeTile animeTile = new AnimeTile(database, anime);
                continueWatchingLayout.add(animeTile);
            }

            add(new Hr());

        }
    }

    private void createNewsItems() {
        List<News> animeNewsList = database.getNews(10, NewsType.ANIME, NewsType.EPISODE);
        List<News> customNewsList = database.getNews(5, NewsType.CUSTOM);

        if (animeNewsList.size() > 0) {
            add(new Label("Anime News"));
            add(animeNewsLayout);

            for (News news : animeNewsList) {
                switch (news.getNewsType()) {
                    case ANIME:
                        createAnimeNews(news);
                        break;
                    case EPISODE:
                        createEpisodeNews(news);
                        break;
                }
            }
        }

        if (animeNewsList.size() > 0 && customNewsList.size() > 0) {
            add(new Hr());
        }

        if (customNewsList.size() > 0) {
            add(new Label("Site News"));
            add(customNewsLayout);
            for (News news : customNewsList) {
                createCustomNews(news);
            }
        }
    }

    private void createAnimeNews(News news) {

        Anime anime = news.getAnime();

        AnimeTile animeTile = new AnimeTile(database, anime);
        animeTile.addClassName("anime-tile-airing-view");
        animeTile.setTextAsHTML(anime.getName() + "</br>Anime was added.");
        animeNewsLayout.add(animeTile);

    }

    private void createEpisodeNews(News news) {
        Anime anime = news.getAnime();

        AnimeTile animeTile = new AnimeTile(database, anime, news.getEpisode());
        animeTile.addClassName("anime-tile-airing-view");
        animeTile.setTextAsHTML(anime.getName() + "</br>Episode " + news.getEpisode().getNumber() + " was added.");
        animeNewsLayout.add(animeTile);

    }

    private void createCustomNews(News news) {
        VerticalLayout newsLayout = new VerticalLayout();
        newsLayout.setClassName("news-item");
        newsLayout.setHorizontalComponentAlignment(Alignment.CENTER);
        customNewsLayout.add(newsLayout);

        Label headline = new Label();
        headline.setTitle(news.getTitle());
        newsLayout.add(headline);

        StyledText newsContent = new StyledText(news.getContent());
        newsContent.getContent().setSizeFull();
        newsLayout.add(newsContent);
    }

}
