package moe.evoke.application.components;

import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Tag("ipfs")
@NpmPackage(value = "jquery", version = "^3.6.0")
@NpmPackage(value = "videostream", version = "^3.2.2")
@NpmPackage(value = "it-to-stream", version = "^1.0.0")
@NpmPackage(value = "electron-webrtc", version = "^0.3.0")
@JsModule("./components/ipfs-js.js")
public class IPFSJs extends HtmlComponent {

    private static final Logger logger = LoggerFactory.getLogger(IPFSJs.class);

    public IPFSJs() {

    }

    public void loadVideoStreamForElement(String elementId, String cid, String fallback) {
        UI.getCurrent().getPage().executeJs("window.loadVideo(\"" + elementId + "\", \"" + cid + "\", \"" + fallback + "\");");
    }


}
