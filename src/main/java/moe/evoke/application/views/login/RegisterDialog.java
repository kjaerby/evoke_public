package moe.evoke.application.views.login;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.server.InitialPageSettings;
import com.vaadin.flow.server.PageConfigurator;
import moe.evoke.application.backend.auth.UserAuthentication;
import moe.evoke.application.backend.db.Database;

public class RegisterDialog extends Dialog implements PageConfigurator {

    public RegisterDialog(Database database) {
        VerticalLayout dialogLayout = new VerticalLayout();

        TextField usernameField = new TextField();
        usernameField.setLabel("Username");
        usernameField.setId("username");
        usernameField.setErrorMessage("Username already taken");
        usernameField.addValueChangeListener(event -> usernameField.setInvalid(!database.isUsernameAvailable(event.getValue())));
        dialogLayout.add(usernameField);

        PasswordField passwordField = new PasswordField();
        passwordField.setLabel("Password");
        passwordField.setId("password");
        dialogLayout.add(passwordField);

        EmailField emailField = new EmailField();
        emailField.setLabel("Email");
        emailField.setId("email");
        emailField.setClearButtonVisible(true);
        emailField.setErrorMessage("Please enter a valid email address");
        dialogLayout.add(emailField);

        TextField inviteCodeField = new TextField();
        inviteCodeField.setLabel("Invite Code");
        inviteCodeField.setErrorMessage("Invalid Invite Code");
        dialogLayout.add(inviteCodeField);

        this.add(dialogLayout);
        this.setCloseOnEsc(true);
        this.setCloseOnOutsideClick(true);

        Button confirmButton = new Button("Register", event -> {
            if (database.isInviteCodeValid(inviteCodeField.getValue()) && database.isUsernameAvailable(usernameField.getValue())) {
                UserAuthentication.registerUser(usernameField.getValue(), passwordField.getValue(), emailField.getValue());
                database.updateInviteCode(inviteCodeField.getValue(), usernameField.getValue());
                this.close();
            } else {
                inviteCodeField.setInvalid(true);
            }
        });

        this.add(confirmButton);
    }

    @Override
    public void configurePage(InitialPageSettings initialPageSettings) {
        initialPageSettings.addInlineWithContents(
                InitialPageSettings.Position.PREPEND, "window.customElements=window.customElements||{};"
                        + "window.customElements.forcePolyfill=true;" + "window.ShadyDOM={force:true};",
                InitialPageSettings.WrapMode.JAVASCRIPT);
    }
}
