package moe.evoke.application.security;

import moe.evoke.application.backend.db.Database;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthenticatedUserService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) {
        long id = Database.instance().getIDForUser(username);

        if (id == -1) {
            throw new UsernameNotFoundException("The user " + username + " does not exist");
        }
        return new AuthenticatedUser(username);
    }

}