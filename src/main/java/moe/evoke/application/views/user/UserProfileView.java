package moe.evoke.application.views.user;

import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import moe.evoke.application.backend.db.Anime;
import moe.evoke.application.backend.db.Database;
import moe.evoke.application.components.AnimeTile;
import moe.evoke.application.security.SecurityUtils;
import moe.evoke.application.views.main.MainView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Route(value = "user/:userID", layout = MainView.class)
@CssImport("./views/user/user-profile-view.css")
public class UserProfileView extends VerticalLayout implements BeforeEnterObserver, HasDynamicTitle {

    private static final Logger logger = LoggerFactory.getLogger(UserProfileView.class);

    private final Database database;
    private String username;


    public UserProfileView(@Autowired Database database) {
        addClassName("user-profile-view");
        this.database = database;
    }

    private void createUserPage() {

        removeAll();

        HorizontalLayout userHeaderView = new HorizontalLayout();
        add(userHeaderView);

        Avatar userAvatar = new Avatar();
        userAvatar.setHeight("10em");
        userAvatar.setWidth("10em");
        userAvatar.setImageResource(new StreamResource("img.png", () -> database.getAvatarForUser(username)));
        userHeaderView.add(userAvatar);

        Label usernameLabel = new Label();
        usernameLabel.setText(username);
        userHeaderView.add(usernameLabel);

        add(new Hr());

        Label planToWatchAnimeLabel = new Label("Plan to Watch");
        add(planToWatchAnimeLabel);

        FlexLayout planToWatchAnimesLayout = new FlexLayout();
        planToWatchAnimesLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        planToWatchAnimesLayout.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        add(planToWatchAnimesLayout);

        add(new Hr());

        Label watchingAnimeLabel = new Label("Watching");
        add(watchingAnimeLabel);

        FlexLayout watchingAnimesLayout = new FlexLayout();
        watchingAnimesLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        watchingAnimesLayout.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        add(watchingAnimesLayout);

        add(new Hr());

        Label watchedAnimeLabel = new Label("Watched");
        add(watchedAnimeLabel);

        FlexLayout watchedAnimesLayout = new FlexLayout();
        watchedAnimesLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        watchedAnimesLayout.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        add(watchedAnimesLayout);

        List<Anime> watchedAnime = database.getLastWatchedAnimeForUser(username, 250, false);

        for (Anime anime : watchedAnime) {
            double progress = database.getWatchProgressPercentageForUserAndAnime(username, anime);

            AnimeTile animeTile = new AnimeTile(database, anime);
            animeTile.setProgress(progress);
            if (progress == 1) {
                watchedAnimesLayout.add(animeTile);
            } else if (progress > 0 && progress < 1) {
                watchingAnimesLayout.add(animeTile);
            } else {
                planToWatchAnimesLayout.add(animeTile);
            }

            if (username.equals(SecurityUtils.getUsername())) {
                Button deleteWatchProgressButton = new Button();
                deleteWatchProgressButton.setIcon(VaadinIcon.CLOSE_SMALL.create());
                deleteWatchProgressButton.setClassName("delete-watched-anime-button");
                boolean finalWatched = progress == 1;
                deleteWatchProgressButton.addClickListener(buttonClickEvent -> {
                    database.removeWatchProgressForAnime(SecurityUtils.getUsername(), anime);
                    if (finalWatched) {
                        watchedAnimesLayout.remove(animeTile);
                    } else if (progress == 0) {
                        planToWatchAnimesLayout.remove(animeTile);
                    } else {
                        watchingAnimesLayout.remove(animeTile);
                    }
                });
                animeTile.add(deleteWatchProgressButton);
            }
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        username = event.getRouteParameters().get("userID").orElse("1");

        createUserPage();
    }

    @Override
    public String getPageTitle() {
        if (username != null && !username.isEmpty()) {
            return username + "'s Profile";
        }
        return "User Profile";
    }
}
