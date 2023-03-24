package moe.evoke.application.views.main;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.server.VaadinService;

import javax.servlet.http.Cookie;

public class IPFSModeSelectorDialog extends Dialog {

    public IPFSModeSelectorDialog() {
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setSizeFull();
        add(dialogLayout);

        RadioButtonGroup<String> radioGroup = new RadioButtonGroup<>();
        radioGroup.setLabel("IPFS Operation Mode");
        radioGroup.setItems("Support Us", "Just Stream");
        radioGroup.setValue("Support Us");
        dialogLayout.add(radioGroup);

        Details details = new Details("Mode Explanation",
                new Text("This mode will run an IPFS node inside your browser. The watched content will be shared with other users on the IPFS network!"));
        details.setOpened(true);
        dialogLayout.add(details);

        radioGroup.addValueChangeListener(event -> {
            if (event.getValue().equals("Support Us")) {
                Text content = new Text("This mode will run an IPFS node inside your browser. The watched content will be shared with other users on the IPFS network!");
                details.setContent(content);
            } else if (event.getValue().equals("Just Stream")) {
                Text content = new Text("In this mode you will just watch the stream via an IPFS gateway (either hosted by us or a public one)");
                details.setContent(content);
            }
        });

        Button saveButton = new Button("Save Selection");
        saveButton.setWidthFull();
        saveButton.addClickListener(event -> {
            Cookie cookie = new Cookie(IPFSMode.class.getSimpleName(), IPFSMode.NODE.name());
            cookie.setPath(VaadinService.getCurrentRequest().getContextPath());
            if (radioGroup.getValue().equals("Support Us")) {
                cookie.setValue(IPFSMode.NODE.name());
            } else if (radioGroup.getValue().equals("Just Stream")) {
                cookie.setValue(IPFSMode.GATEWAY.name());
            }
            VaadinService.getCurrentResponse().addCookie(cookie);
            this.close();
        });
        dialogLayout.add(saveButton);
    }
}
