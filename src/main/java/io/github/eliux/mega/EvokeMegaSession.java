package io.github.eliux.mega;

import io.github.eliux.mega.auth.MegaAuth;
import io.github.eliux.mega.cmd.MegaCmdMediaInfo;

public class EvokeMegaSession extends MegaSession {


    public EvokeMegaSession(MegaAuth authentication) {
        super(authentication);
    }

    public MegaCmdMediaInfo mediaInfo(String remotePath) {
        return new MegaCmdMediaInfo(remotePath);
    }
}
