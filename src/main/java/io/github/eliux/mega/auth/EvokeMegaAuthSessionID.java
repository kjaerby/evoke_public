package io.github.eliux.mega.auth;

import io.github.eliux.mega.EvokeMegaSession;
import io.github.eliux.mega.Mega;
import io.github.eliux.mega.error.MegaException;
import io.github.eliux.mega.error.MegaLoginException;

import java.util.Optional;

public class EvokeMegaAuthSessionID extends EvokeMegaAuth {

    private final String sessionID;

    public EvokeMegaAuthSessionID(String sessionID) {
        this.sessionID = sessionID;
    }

    public static final MegaAuthCredentials createFromEnvVariables() {
        String username = Optional.ofNullable(System.getenv(Mega.USERNAME_ENV_VAR))
                .orElseThrow(() -> MegaException.nonExistingEnvVariable(
                        Mega.USERNAME_ENV_VAR
                ));

        String password = Optional.ofNullable(System.getenv(Mega.PASSWORD_ENV_VAR))
                .orElseThrow(() -> MegaException.nonExistingEnvVariable(
                        Mega.PASSWORD_ENV_VAR
                ));

        return new MegaAuthCredentials(username, password);
    }

    public String getSessionID() {
        return sessionID;
    }

    @Override
    public EvokeMegaSession login() throws MegaLoginException {
        try {
            return new EvokeMegaSession(this);
        } catch (Throwable err) {
            throw new MegaLoginException("There is no started session", err);
        }
    }

}
