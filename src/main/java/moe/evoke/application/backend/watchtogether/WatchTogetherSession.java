package moe.evoke.application.backend.watchtogether;

import com.google.gson.Gson;
import moe.evoke.application.backend.Config;
import moe.evoke.application.backend.db.Database;
import moe.evoke.application.backend.db.HostedEpisode;
import moe.evoke.application.components.peertube.PlayerStatus;
import moe.evoke.application.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

public class WatchTogetherSession {

    private static final Logger logger = LoggerFactory.getLogger(WatchTogetherSession.class);
    private static final String PREFIX = "w2g";

    private final Jedis jedis;
    private final String sessionChannel;
    private final Gson gson = new Gson();

    private final String userChannel;
    private final String currentEpisodeChannel;
    private final String playerStatusChannel;

    public WatchTogetherSession(String username) {
        this.jedis = new Jedis(Config.getRedisAddress(), Config.getRedisPort());
        this.sessionChannel = PREFIX + "." + username + ".";

        this.userChannel = sessionChannel + "user";
        this.currentEpisodeChannel = sessionChannel + "currentEpisode";
        this.playerStatusChannel = sessionChannel + "playerStatus";

        String storedUser = jedis.get(userChannel);
        if (storedUser == null || storedUser.isEmpty()) {
            this.setUser(SecurityUtils.getUsername());
        } else {
            this.setUser(storedUser);
        }
    }

    public String getUser() {
        return jedis.get(userChannel);
    }

    public void setUser(String user) {
        jedis.set(userChannel, user);
    }

    public HostedEpisode getCurrentEpisode() {
        String hEpId = jedis.get(currentEpisodeChannel);
        try {
            return Database.instance().getHostedEpisodeByID(Long.parseLong(hEpId));
        } catch (Exception ex) {
            return null;
        }
    }

    public void setCurrentEpisode(HostedEpisode currentEpisode) {
        jedis.set(currentEpisodeChannel, String.valueOf(currentEpisode.getID()));
    }

    public PlayerStatus getPlayerStatus() {
        return gson.fromJson(jedis.get(playerStatusChannel), PlayerStatus.class);
    }

    public void setPlayerStatus(PlayerStatus playerStatus) {
        jedis.set(playerStatusChannel, gson.toJson(playerStatus));
    }
}
