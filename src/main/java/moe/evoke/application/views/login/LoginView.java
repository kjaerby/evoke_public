package moe.evoke.application.views.login;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import moe.evoke.application.backend.db.Database;
import moe.evoke.application.backend.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;

@Route("login")
@PageTitle("Login")

public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final LoginForm login = new LoginForm();

    public LoginView(@Autowired Database database) {
        addClassName("login-view");
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        Utils.checkDarkMode();

        login.setAction("login");

        add(Utils.getLogoAsImge(), login);

        Button registerButton = new Button("Register");
        registerButton.addClickListener(buttonClickEvent -> new RegisterDialog(database).open());

        add(registerButton);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        // inform the user about an authentication error
        if (beforeEnterEvent.getLocation()
                .getQueryParameters()
                .getParameters()
                .containsKey("error")) {
            login.setError(true);
        }
    }
}