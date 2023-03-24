package moe.evoke.application.views.controlpanel.dialogs;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import moe.evoke.application.backend.db.Database;
import moe.evoke.application.backend.db.User;

public class UserDialog extends Dialog {

    public UserDialog(Database database) {
        VerticalLayout dialogLayout = new VerticalLayout();

        Grid<User> userGrid = new Grid<>(User.class);
        userGrid.setItems(database.getUsers());
        dialogLayout.add(userGrid);

        HorizontalLayout userActionsLayout = new HorizontalLayout();
        dialogLayout.add(userActionsLayout);

        Button addUserButton = new Button("Add User");
        addUserButton.addClickListener(event -> new UserActionDialog(() -> userGrid.setItems(database.getUsers()), null, UserActionType.CREATE).open());
        userActionsLayout.add(addUserButton);

        Button updateUserButton = new Button("Update User");
        updateUserButton.addClickListener(event -> new UserActionDialog(() -> userGrid.setItems(database.getUsers()), userGrid.getSelectedItems(), UserActionType.UPDATE).open());
        userActionsLayout.add(updateUserButton);

        Button removeUserButton = new Button("Remove User");
        removeUserButton.addClickListener(event -> new UserActionDialog(() -> userGrid.setItems(database.getUsers()), userGrid.getSelectedItems(), UserActionType.DELETE).open());
        userActionsLayout.add(removeUserButton);

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
