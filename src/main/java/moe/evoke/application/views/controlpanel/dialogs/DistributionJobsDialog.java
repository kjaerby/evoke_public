package moe.evoke.application.views.controlpanel.dialogs;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import moe.evoke.application.backend.mirror.distribution.DistributionJob;
import moe.evoke.application.backend.mirror.distribution.DistributionManager;

public class DistributionJobsDialog extends Dialog {

    public DistributionJobsDialog() {
        VerticalLayout dialogLayout = new VerticalLayout();

        Grid<DistributionJob> jobGrid = new Grid<>(DistributionJob.class);
        jobGrid.setItems(DistributionManager.getAllJobs());
        dialogLayout.add(jobGrid);

        this.setWidth("80vw");
        this.add(dialogLayout);
    }
}
