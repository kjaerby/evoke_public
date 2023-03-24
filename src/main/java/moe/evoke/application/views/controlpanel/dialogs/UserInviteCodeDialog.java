package moe.evoke.application.views.controlpanel.dialogs;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.internal.Pair;
import moe.evoke.application.backend.db.Database;
import moe.evoke.application.backend.util.Utils;

public class UserInviteCodeDialog extends Dialog {

    public UserInviteCodeDialog(Database database) {
        VerticalLayout dialogLayout = new VerticalLayout();

        Grid<Pair<String, String>> inviteCodeGrid = new Grid<>();
        inviteCodeGrid.setItems(database.getInviteCodes());
        inviteCodeGrid.addColumn(Pair::getFirst);
        inviteCodeGrid.addColumn(Pair::getSecond);
        dialogLayout.add(inviteCodeGrid);


        HorizontalLayout inviteCodeActionsLayout = new HorizontalLayout();
        dialogLayout.add(inviteCodeActionsLayout);

        Button addUserButton = new Button("Generate Code");
        addUserButton.addClickListener(event ->
        {
            Utils.generateInviteCode();
            inviteCodeGrid.setItems(database.getInviteCodes());
        });
        inviteCodeActionsLayout.add(addUserButton);


        this.setWidth("80vw");
        this.add(dialogLayout);


    }
}
