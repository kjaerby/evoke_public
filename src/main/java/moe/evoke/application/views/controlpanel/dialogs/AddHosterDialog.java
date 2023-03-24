package moe.evoke.application.views.controlpanel.dialogs;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Shortcuts;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.TextField;
import moe.evoke.application.backend.db.Database;

public class AddHosterDialog extends Dialog {

    public AddHosterDialog(Database database, Runnable reloadAction) {
        Label hosterNameLabel = new Label("Hoster:");
        TextField hosterNameField = new TextField();

        this.add(hosterNameLabel);
        this.add(hosterNameField);
        this.setCloseOnEsc(false);
        this.setCloseOnOutsideClick(false);
        Span message = new Span();

        Button confirmButton = new Button("Confirm", event -> {
            message.setText("Confirmed!");
            database.createHoster(hosterNameField.getValue());
            reloadAction.run();
            this.close();
        });
        Button cancelButton = new Button("Cancel", event -> {
            message.setText("Cancelled...");
            this.close();
        });

        Shortcuts.addShortcutListener(this, () -> {
            message.setText("Cancelled...");
            this.close();
        }, Key.ESCAPE);

        this.add(new Div(confirmButton, cancelButton));
    }

}
