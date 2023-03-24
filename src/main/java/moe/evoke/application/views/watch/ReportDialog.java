package moe.evoke.application.views.watch;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import moe.evoke.application.backend.db.Anime;
import moe.evoke.application.backend.db.AnimeReportReason;
import moe.evoke.application.backend.db.Database;
import moe.evoke.application.security.SecurityUtils;

public class ReportDialog extends Dialog {

    public ReportDialog(Database database, Anime anime, long episode) {
        VerticalLayout dialogLayout = new VerticalLayout();

        ComboBox<AnimeReportReason> reportReasonComboBox = new ComboBox();
        reportReasonComboBox.setLabel("Reason");
        reportReasonComboBox.setItems(AnimeReportReason.values());
        reportReasonComboBox.setValue(AnimeReportReason.HOSTER_NOT_WORKING);
        dialogLayout.add(reportReasonComboBox);

        TextArea reportDescription = new TextArea();
        reportDescription.setLabel("Description");
        dialogLayout.add(reportDescription);

        this.add(dialogLayout);
        this.setCloseOnEsc(true);
        this.setCloseOnOutsideClick(true);

        String username = SecurityUtils.getUsername();
        Button confirmButton = new Button("Done", confirmEvent -> {
            database.createAnimeReport(reportReasonComboBox.getValue(), reportDescription.getValue(), username, anime, anime.getEpisodes().get(Math.toIntExact(episode - 1)));
            this.close();
        });

        this.add(confirmButton);
        this.open();
    }
}
