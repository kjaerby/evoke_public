package moe.evoke.application.views.user;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import moe.evoke.application.backend.auth.UserAuthentication;
import moe.evoke.application.backend.db.Database;
import moe.evoke.application.security.SecurityUtils;
import moe.evoke.application.views.main.MainView;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "user-settings", layout = MainView.class)
@PageTitle("User Settings")
@CssImport("./views/user/user-settings-view.css")
public class UserSettingsView extends VerticalLayout {

    private final Database database;

    public UserSettingsView(@Autowired Database database) {
        this.database = database;
        addClassName("user-settings-view");

        createChangeUserAvatar();
        createChangeUserPassword();
    }

    private void createChangeUserPassword() {
        PasswordField passwordField = new PasswordField("New Password");
        passwordField.setId("password");
        passwordField.getStyle().set("margin", "10px");
        add(passwordField);

        Button save = new Button("Save");
        save.getStyle().set("margin", "10px");
        save.addClickListener(buttonClickEvent -> {
            UserAuthentication.updatePasswordForUser(SecurityUtils.getUsername(), passwordField.getValue());
            Notification.show("Updated Password!");
        });
        add(save);
    }

    private void createChangeUserAvatar() {
        Label avatarUpload = new Label("Upload Avatar");
        add(avatarUpload);

        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setMaxFileSize(Integer.MAX_VALUE);

        final String username = SecurityUtils.getUsername();
        upload.addSucceededListener(event -> database.setAvatarForUser(username, buffer.getInputStream()));

        add(upload);
    }


}
