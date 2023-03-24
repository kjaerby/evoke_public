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

public class AddAnimeDialog extends Dialog {

    public AddAnimeDialog(Database database, Runnable reloadAction) {
        Label anilistIDLabel = new Label("Anilist ID:");
        TextField anilistIDField = new TextField();

        this.add(anilistIDLabel);
        this.add(anilistIDField);
        this.setCloseOnEsc(false);
        this.setCloseOnOutsideClick(false);
        Span message = new Span();

        Button confirmButton = new Button("Confirm", event -> {
            message.setText("Confirmed!");
            database.createAnime(Long.parseLong(anilistIDField.getValue()));
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
