package moe.evoke.application.views.main;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import moe.evoke.application.backend.util.GitLab;
import moe.evoke.application.backend.util.Utils;
import moe.evoke.application.security.SecurityUtils;

public class FeedbackDialog extends Dialog {

    public FeedbackDialog() {
        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setWidth("40vw");
        dialogLayout.setHeight("60vh");

        TextField subjectField = new TextField();
        subjectField.setLabel("Subject");
        subjectField.setWidthFull();
        dialogLayout.add(subjectField);

        TextArea feedbackDescription = new TextArea();
        feedbackDescription.setLabel("Description");
        feedbackDescription.setSizeFull();
        dialogLayout.add(feedbackDescription);

        String username = SecurityUtils.getUsername();
        String page = Utils.getLocation();
        Button confirmButton = new Button("Send", confirmEvent -> {
            GitLab.createIssue(subjectField.getValue(), feedbackDescription.getValue(), username, page);
            Notification.show("Feedback was send!", 5000, Notification.Position.TOP_END);
            this.close();
        });
        dialogLayout.add(confirmButton);

        this.add(dialogLayout);
        this.open();
    }
}
