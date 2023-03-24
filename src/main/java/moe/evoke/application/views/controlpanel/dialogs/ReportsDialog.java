package moe.evoke.application.views.controlpanel.dialogs;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import moe.evoke.application.backend.db.AnimeReport;
import moe.evoke.application.backend.db.Database;

public class ReportsDialog extends Dialog {

    public ReportsDialog(Database database) {
        VerticalLayout dialogLayout = new VerticalLayout();

        Grid<AnimeReport> reportGrid = new Grid<>(AnimeReport.class);
        reportGrid.setItems(database.getAnimeReports());
        dialogLayout.add(reportGrid);

        Button markAsDoneButton = new Button("Mark As Done");
        markAsDoneButton.addClickListener(buttonClickEvent -> {
            if (reportGrid.getSelectedItems().size() > 0) {
                database.markAnimeReportAsDone(reportGrid.getSelectedItems().iterator().next());
                reportGrid.setItems(database.getAnimeReports());
            }
        });
        dialogLayout.add(markAsDoneButton);

        this.setWidth("80vw");
        this.add(dialogLayout);
        this.setCloseOnEsc(false);
        this.setCloseOnOutsideClick(false);

        Button confirmButton = new Button("Done", event -> {
            this.close();
        });

        this.add(confirmButton);
    }
}
