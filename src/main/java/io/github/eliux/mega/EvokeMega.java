package io.github.eliux.mega;

import io.github.eliux.mega.auth.EvokeMegaAuth;
import io.github.eliux.mega.auth.EvokeMegaAuthSessionID;
import io.github.eliux.mega.cmd.MegaCmdSession;

public class EvokeMega {

    public static EvokeMegaSession currentSession() {
        final String sessionID = new MegaCmdSession().call();
        return login(new EvokeMegaAuthSessionID(sessionID));
    }

    public static EvokeMegaSession login(EvokeMegaAuth credentials) {
        return credentials.login();
    }

}
