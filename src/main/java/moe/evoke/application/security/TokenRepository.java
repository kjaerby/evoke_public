package moe.evoke.application.security;

import moe.evoke.application.backend.db.Database;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import java.util.Date;

public class TokenRepository implements PersistentTokenRepository {

    private static final Logger logger = LoggerFactory.getLogger(TokenRepository.class);

    @Override
    public void createNewToken(PersistentRememberMeToken persistentRememberMeToken) {
        Database.instance().createUserSession(persistentRememberMeToken.getUsername(), persistentRememberMeToken.getTokenValue(), persistentRememberMeToken.getDate(), persistentRememberMeToken.getSeries());
    }

    @Override
    public void updateToken(String series, String tokenValue, Date lastUsed) {
        Database.instance().updateUserSession(series, tokenValue, lastUsed);
    }

    @Override
    public PersistentRememberMeToken getTokenForSeries(String series) {
        return Database.instance().getPersistentRememberMeToken(series);
    }

    @Override
    public void removeUserTokens(String token) {
        Database.instance().removeUserSession(token);
    }
}
