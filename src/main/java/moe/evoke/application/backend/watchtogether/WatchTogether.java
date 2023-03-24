package moe.evoke.application.backend.watchtogether;

import moe.evoke.application.backend.Config;
import redis.clients.jedis.Jedis;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class WatchTogether {

    private static final String SET_KEY = "w2g-sessions";
    private static final Jedis jedis;

    static {
        jedis = new Jedis(Config.getRedisAddress(), Config.getRedisPort());
    }

    public static WatchTogetherSession createNewSession(String username) {
        WatchTogetherSession session = new WatchTogetherSession(username);
        jedis.sadd(SET_KEY, username);
        return session;
    }

    public static Collection<WatchTogetherSession> getSessions() {
        Set<String> sessions = jedis.smembers(SET_KEY);

        return sessions.stream().map(WatchTogetherSession::new).collect(Collectors.toList());
    }

    public static WatchTogetherSession getSession(String username) {
        return new WatchTogetherSession(username);
    }

}
