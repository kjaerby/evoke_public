package moe.evoke.application.views.main;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.tabs.TabsVariant;
import com.vaadin.flow.dom.ThemeList;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.theme.lumo.Lumo;
import moe.evoke.application.backend.Broadcaster;
import moe.evoke.application.backend.db.Database;
import moe.evoke.application.backend.util.Utils;
import moe.evoke.application.components.IPFSJs;
import moe.evoke.application.security.SecurityUtils;
import moe.evoke.application.views.anime.AnimeAiringView;
import moe.evoke.application.views.anime.AnimeView;
import moe.evoke.application.views.controlpanel.ControlPanelView;
import moe.evoke.application.views.home.HomeView;
import moe.evoke.application.views.random.RandomView;
import moe.evoke.application.views.statistics.Statistics;
import moe.evoke.application.views.user.UserProfileView;
import moe.evoke.application.views.user.UserSettingsView;
import moe.evoke.application.views.watchtogether.WatchTogetherPlayerView;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The main view is a top-level placeholder for other views.
 */
@PWA(name = "evoke", shortName = "evoke", enableInstallPrompt = false)
@JsModule("./styles/shared-styles.js")
@CssImport("./views/main/main-view.css")
@Push
public class MainView extends AppLayout {

    private final Tabs menu;
    private final Database database;

    @Autowired
    private HttpServletRequest req;
    private Registration broadcasterRegistration;
    private H1 viewTitle;

    public MainView(@Autowired Database database) {
        this.database = database;

        setPrimarySection(Section.DRAWER);
        addToNavbar(true, createHeaderContent());
        menu = createMenu();
        addToDrawer(createDrawerContent(menu));

        Utils.checkDarkMode();

        IPFSMode ipfsMode = Utils.getIPFSMode();
        if (ipfsMode != null && ipfsMode.equals(IPFSMode.NODE)) {
            IPFSJs ipfs = new IPFSJs();
            ipfs.setVisible(false);
            addToDrawer(ipfs);
        } else {
            IPFSModeSelectorDialog dialog = new IPFSModeSelectorDialog();
            dialog.addDialogCloseActionListener(event -> {
                IPFSMode mode = Utils.getIPFSMode();
                if (mode != null && mode.equals(IPFSMode.NODE)) {
                    IPFSJs ipfs = new IPFSJs();
                    ipfs.setVisible(false);
                    addToDrawer(ipfs);
                }
            });
            dialog.open();
        }
    }

    private static Tab createTab(String text, Class<? extends Component> navigationTarget, RouteParameters parameters) {
        final Tab tab = new Tab();
        tab.add(new RouterLink(text, navigationTarget, parameters));
        ComponentUtil.setData(tab, Class.class, navigationTarget);
        return tab;
    }

    private static Tab createTab(String text, Class<? extends Component> navigationTarget) {
        final Tab tab = new Tab();
        tab.add(new RouterLink(text, navigationTarget));
        ComponentUtil.setData(tab, Class.class, navigationTarget);
        return tab;
    }

    private void createFeedbackButton() {

        Component component = getContent();
        if (component instanceof HasComponents) {
            HasComponents hasComponents = (HasComponents) component;

            Icon feedbackIcon = new Icon(VaadinIcon.COMMENT_ELLIPSIS_O);

            Button feedbackButton = new Button();
            feedbackButton.setIcon(feedbackIcon);
            feedbackButton.setClassName("feedback-button");
            feedbackButton.getStyle().set("cursor", "pointer");
            feedbackButton.addClickListener(event -> new FeedbackDialog().open());
            hasComponents.add(feedbackButton);
        }
    }

    private Component createHeaderContent() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setId("header");
        layout.getThemeList().set("dark", true);
        layout.setWidthFull();
        layout.setSpacing(false);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.add(new DrawerToggle());
        viewTitle = new H1();
        layout.add(viewTitle);

        Icon darModeIcon = new Icon(VaadinIcon.MOON);
        Button darkModeToggleButton = new Button();
        darkModeToggleButton.setIcon(darModeIcon);
        darkModeToggleButton.setId("darkmode-toggle-button");
        darkModeToggleButton.addClickListener(iconClickEvent -> {
            ThemeList themeList = UI.getCurrent().getElement().getThemeList();
            if (themeList.contains(Lumo.DARK)) {
                themeList.remove(Lumo.DARK);
                Cookie darkModeCookie = new Cookie("darkmode", "false");
                darkModeCookie.setPath(VaadinService.getCurrentRequest().getContextPath());
                VaadinService.getCurrentResponse().addCookie(darkModeCookie);
            } else {
                themeList.add(Lumo.DARK);
                Cookie darkModeCookie = new Cookie("darkmode", "true");
                darkModeCookie.setPath(VaadinService.getCurrentRequest().getContextPath());
                VaadinService.getCurrentResponse().addCookie(darkModeCookie);
            }
        });
        layout.add(darkModeToggleButton);


        MenuBar menuBar = new MenuBar();
        menuBar.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);
        layout.add(menuBar);

        Avatar avatar = new Avatar();
        final String username = SecurityUtils.getUsername();
        avatar.setImageResource(new StreamResource("img.png", () -> database.getAvatarForUser(username)));
        MenuItem menuItem = menuBar.addItem(avatar);

        SubMenu subMenu = menuItem.getSubMenu();

        MenuItem profileItem = subMenu.addItem("Profile");
        profileItem.addClickListener(menuItemClickEvent ->
        {
            UI.getCurrent().navigate(UserProfileView.class, new RouteParameters("userID", username));
        });

        MenuItem settingsItem = subMenu.addItem("Settings");
        settingsItem.addClickListener(menuItemClickEvent -> UI.getCurrent().navigate(UserSettingsView.class));

        MenuItem logoutItem = subMenu.addItem("Logout");
        logoutItem.addClickListener(menuItemClickEvent -> UI.getCurrent().getPage().setLocation("/logout"));

        return layout;
    }

    private Component createDrawerContent(Tabs menu) {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.getThemeList().set("spacing-s", true);
        layout.setAlignItems(FlexComponent.Alignment.STRETCH);
        HorizontalLayout logoLayout = new HorizontalLayout();
        logoLayout.setId("logo");
        logoLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        Image siteLogo = Utils.getIconAsImge();
        siteLogo.getStyle().set("margin", "auto");
        logoLayout.add(siteLogo);
        logoLayout.addClickListener(event -> getUI().ifPresent(ui -> ui.navigate("")));
        layout.setSizeFull();
        layout.add(logoLayout, menu);
        return layout;
    }

    private Tabs createMenu() {
        final Tabs tabs = new Tabs();
        tabs.setOrientation(Tabs.Orientation.VERTICAL);
        tabs.addThemeVariants(TabsVariant.LUMO_MINIMAL);
        tabs.setId("tabs");
        tabs.add(createMenuItems());
        return tabs;
    }

    private Component[] createMenuItems() {

        List<Tab> tabs = new ArrayList<>();
        tabs.add(createTab("Home", HomeView.class));
        tabs.add(createTab("Airing", AnimeAiringView.class));
        tabs.add(createTab("Anime", AnimeView.class));
        tabs.add(createTab("Random", RandomView.class));
        tabs.add(createTab("Statistics", Statistics.class));

        if (SecurityUtils.isAccessGranted(WatchTogetherPlayerView.class)) {
            RouteParam param = new RouteParam("username", SecurityUtils.getUsername());
            RouteParameters params = new RouteParameters(param);
            tabs.add(createTab("Watch Together", WatchTogetherPlayerView.class, params));
        }
        if (SecurityUtils.isAccessGranted(ControlPanelView.class)) {
            tabs.add(createTab("Control Panel", ControlPanelView.class));
        }

        Tab[] tabsArray = new Tab[tabs.size()];
        tabsArray = tabs.toArray(tabsArray);
        return tabsArray;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        getTabForComponent(getContent()).ifPresent(menu::setSelectedTab);
        viewTitle.setText(getCurrentPageTitle());

        createFeedbackButton();
    }

    private Optional<Tab> getTabForComponent(Component component) {
        return menu.getChildren().filter(tab -> ComponentUtil.getData(tab, Class.class).equals(component.getClass()))
                .findFirst().map(Tab.class::cast);
    }

    private String getCurrentPageTitle() {
        PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
        return title == null ? "" : title.value();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        UI ui = attachEvent.getUI();
        broadcasterRegistration = Broadcaster.register(newMessage -> {
            ui.access(() -> Notification.show(newMessage, 5000, Notification.Position.TOP_END));
        });
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        broadcasterRegistration.remove();
        broadcasterRegistration = null;
    }
}
