package moe.evoke.application.views.controlpanel.dialogs;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import moe.evoke.application.backend.auth.UserAuthentication;
import moe.evoke.application.backend.db.User;
import moe.evoke.application.backend.db.UserRoles;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class UserActionDialog extends Dialog {

    public UserActionDialog(Runnable reloadAction, Set<User> selectedItems, UserActionType actionType) {
        VerticalLayout dialogLayout = new VerticalLayout();

        this.add(dialogLayout);

        TextField usernameField = new TextField();
        usernameField.setLabel("Username");
        dialogLayout.add(usernameField);

        EmailField emailField = new EmailField();
        emailField.setLabel("Email");
        dialogLayout.add(emailField);

        PasswordField passwordField = new PasswordField();
        passwordField.setLabel("Password");
        dialogLayout.add(passwordField);

        Button clearAvatarButton = new Button("Clear Avatar");
        clearAvatarButton.setEnabled(false);
        dialogLayout.add(clearAvatarButton);

        Map<UserRoles, Checkbox> rolesCheckboxMap = new HashMap<>();
        for (UserRoles value : UserRoles.values()) {
            Checkbox roleCheckbox = new Checkbox();
            roleCheckbox.setLabel(value.name());
            dialogLayout.add(roleCheckbox);

            rolesCheckboxMap.put(value, roleCheckbox);
        }


        if (actionType != UserActionType.CREATE && selectedItems != null) {
            clearAvatarButton.setEnabled(true);
            usernameField.setEnabled(false);

            Optional<User> user = selectedItems.stream().findFirst();
            if (user.isPresent()) {
                usernameField.setValue(user.get().getUsername());
                emailField.setValue(user.get().getEmail());

                for (UserRoles role : user.get().getRoles()) {
                    if (rolesCheckboxMap.containsKey(role)) {
                        rolesCheckboxMap.get(role).setValue(true);
                    }
                }
            }
        }


        Button confirmButton = new Button("Done", event -> {

            if (!usernameField.isEmpty()) {
                if (actionType == UserActionType.CREATE) {
                    UserAuthentication.registerUser(usernameField.getValue(), passwordField.getValue(), emailField.getValue());
                    UserAuthentication.updateRolesForUser(usernameField.getValue(), rolesCheckboxMap.entrySet().stream().filter(userRolesCheckboxEntry -> userRolesCheckboxEntry.getValue().getValue()).map(userRolesCheckboxEntry -> userRolesCheckboxEntry.getKey()).collect(Collectors.toList()));
                } else if (actionType == UserActionType.UPDATE) {
                    if (!passwordField.isEmpty()) {
                        UserAuthentication.updatePasswordForUser(usernameField.getValue(), passwordField.getValue());
                    }

                    UserAuthentication.updateRolesForUser(usernameField.getValue(), rolesCheckboxMap.entrySet().stream().filter(userRolesCheckboxEntry -> userRolesCheckboxEntry.getValue().getValue()).map(userRolesCheckboxEntry -> userRolesCheckboxEntry.getKey()).collect(Collectors.toList()));
                    UserAuthentication.updateEmailForUser(usernameField.getValue(), emailField.getValue());
                }
            }

            reloadAction.run();
            this.close();
        });

        this.add(confirmButton);

    }
}
