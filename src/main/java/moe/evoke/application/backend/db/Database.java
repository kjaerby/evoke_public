package moe.evoke.application.backend.db;

import com.apptastic.rssreader.Item;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.vaadin.flow.internal.Pair;
import moe.evoke.application.backend.anilist.Anilist;
import moe.evoke.application.backend.anilist.data.AnimeData;
import moe.evoke.application.backend.anilist.data.TagsItem;
import moe.evoke.application.backend.hoster.ipfs.IPFSGateway;
import moe.evoke.application.backend.mirror.distribution.DistributionJob;
import moe.evoke.application.backend.mirror.distribution.DistributionJobStatus;
import moe.evoke.application.backend.mirror.distribution.DistributionSource;
import moe.evoke.application.backend.mirror.distribution.DistributionTarget;
import moe.evoke.application.backend.monthly.moe.animes.Response;
import moe.evoke.application.backend.util.Utils;
import moe.evoke.application.backend.waifulabs.Waifulabs;
import moe.evoke.application.security.DataSourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

@Service
public class Database {

    private static final Logger logger = LoggerFactory.getLogger(Database.class);

    private static final Set<String> animeGenres = new HashSet<>();
    private static final Set<String> animeTags = new HashSet<>();
    private static final Set<String> animeYear = new HashSet<>();
    private static final Set<String> animeFormats = new HashSet<>();

    private static final Database database;

    static {
        database = new Database();
    }

    @Autowired
    private final JdbcTemplate jdbcTemplate;

    private Database() {
        this.jdbcTemplate = new JdbcTemplate(new DataSourceConfig().getDataSource());
    }

    public static Database instance() {
        return database;
    }

    public List<Anime> getAnime() {
        return jdbcTemplate.query("select a.ID, a.AnilistID, ac.Title, ac.Cover, ac.EpisodeCount from Anime a, AnilistCache ac " +
                "WHERE a.AnilistID = ac.AnilistID", result -> {

            List<Anime> animes = new ArrayList<>();
            while (result.next()) {
                Anime anime = new Anime();
                anime.setID(result.getLong(1));
                anime.setAnilistID(result.getLong(2));
                anime.setName(result.getString(3));
                anime.setCover(result.getString(4));
                anime.setEpisodeCount(result.getInt(5));

                animes.add(anime);
            }

            return animes;
        });
    }

    public List<Hoster> getHoster() {
        return jdbcTemplate.query("SELECT ID, Name FROM Hoster WHERE IsActive = 1;",
                resultSet -> {
                    List<Hoster> hostList = new ArrayList<>();
                    while (resultSet.next()) {
                        Hoster hoster = new Hoster();
                        hoster.setID(resultSet.getLong(1));
                        hoster.setName(resultSet.getString(2));

                        hostList.add(hoster);
                    }
                    return hostList;
                });
    }

    public Anime getAnimeByID(long id) {
        return jdbcTemplate.query("select a.ID, a.AnilistID, ac.Title, ac.Cover, ac.EpisodeCount from Anime a, AnilistCache ac " +
                "WHERE a.AnilistID = ac.AnilistID AND a.ID = ? ", statement -> {
            statement.setLong(1, id);
        }, resultSet -> {
            if (resultSet.next()) {
                Anime anime = new Anime();
                anime.setID(resultSet.getLong(1));
                anime.setAnilistID(resultSet.getLong(2));
                anime.setName(resultSet.getString(3));
                anime.setCover(resultSet.getString(4));
                anime.setEpisodeCount(resultSet.getInt(5));

                return anime;
            }
            return null;
        });
    }

    public Hoster getHosterByID(long ID) {
        return jdbcTemplate.query("SELECT ID, Name FROM Hoster WHERE ID = ? AND IsActive = 1;", statement -> {
            statement.setLong(1, ID);
        }, resultSet -> {
            if (resultSet.next()) {
                Hoster hoster = new Hoster();
                hoster.setID(resultSet.getLong(1));
                hoster.setName(resultSet.getString(2));
                return hoster;
            }
            return null;
        });
    }

    public Anime getAnimeByAnilistID(long id) {

        Anime anime = jdbcTemplate.query("select a.ID, a.AnilistID, ac.Title, ac.Cover, ac.EpisodeCount from Anime a, AnilistCache ac " +
                "WHERE a.AnilistID = ac.AnilistID AND a.AnilistID = ? ", statement -> {
            statement.setLong(1, id);
        }, resultSet -> {
            if (resultSet.next()) {
                Anime tmp = new Anime();
                tmp.setID(resultSet.getLong(1));
                tmp.setAnilistID(resultSet.getLong(2));
                tmp.setName(resultSet.getString(3));
                tmp.setCover(resultSet.getString(4));
                tmp.setEpisodeCount(resultSet.getInt(5));
                return tmp;
            }
            return null;
        });

        if (anime != null) {
            return anime;
        }

        try {
            logger.warn("Could not find anime with anilist id: " + id);
            try {
                Anilist.getInfoForAnime(id);
                createAnime(id);
            } catch (Exception ex) {
                logger.error("Error during anime creation!", ex);
                return null;
            }

            return getAnimeByAnilistID(id);
        } catch (Exception ex) {
            logger.error("Error during anime creation!", ex);
        }

        return null;
    }

    public List<Episode> getEpisodesForAnime(Anime anime) {
        return jdbcTemplate.query("SELECT ID, AnimeID, Number FROM Episode where AnimeID = ? ORDER BY Number ASC;", statement -> {
            statement.setLong(1, anime.getID());
        }, resultSet -> {
            List<Episode> episodes = new ArrayList<>();
            while (resultSet.next()) {
                Episode episode = new Episode();
                episode.setID(resultSet.getLong(1));
                episode.setAnimeID(resultSet.getLong(2));
                episode.setNumber(resultSet.getLong(3));
                episodes.add(episode);
            }
            return episodes;
        });
    }

    public List<HostedEpisode> getHostedEpisodesForEpisode(Episode episode) {
        return jdbcTemplate.query(
                "SELECT HostedEpisode.ID, HostedEpisode.HostID, HostedEpisode.EpisodeID, HostedEpisode.StreamURL " +
                        "FROM HostedEpisode, Hoster " +
                        "WHERE HostedEpisode.EpisodeID = ? AND HostedEpisode.HostID = Hoster.ID AND Hoster.IsActive = 1 " +
                        "ORDER BY Hoster.Priority DESC;", statement -> {
                    statement.setLong(1, episode.getID());
                }, resultSet -> {

                    List<HostedEpisode> hostedEpisodes = new ArrayList<>();
                    while (resultSet.next()) {
                        HostedEpisode hostedEpisode = new HostedEpisode();
                        hostedEpisode.setID(resultSet.getLong(1));
                        hostedEpisode.setHostID(resultSet.getLong(2));
                        hostedEpisode.setEpisodeID(resultSet.getLong(3));
                        hostedEpisode.setStreamURL(resultSet.getString(4));

                        hostedEpisodes.add(hostedEpisode);
                    }

                    return hostedEpisodes;
                });
    }

    public Hoster getHosterForHostedEpisode(HostedEpisode hostedEpisode) {
        return jdbcTemplate.query("SELECT ID, Name FROM Hoster where ID = ? AND IsActive = 1;", statement -> {
            statement.setLong(1, hostedEpisode.getHostID());
        }, resultSet -> {
            if (resultSet.next()) {
                Hoster hoster = new Hoster();
                hoster.setID(resultSet.getLong(1));
                hoster.setName(resultSet.getString(2));

                return hoster;
            }

            return null;
        });
    }

    public void createAnime(long anilistID) {
        jdbcTemplate.update("INSERT INTO Anime (AnilistID) VALUES (?);", statement ->
        {
            statement.setLong(1, anilistID);
        });

        Utils.fillAnimeMetadata(anilistID);
        Utils.generateAnimeNews(getAnimeByAnilistID(anilistID));
    }

    public void removeAnime(Anime anime) {
        jdbcTemplate.update("DELETE FROM Anime WHERE ID = ?;", statement ->
        {
            statement.setLong(1, anime.getID());
        });
    }

    public void createHoster(String value) {
        jdbcTemplate.update("INSERT INTO Hoster (Name) VALUES (?);", statement ->
        {
            statement.setString(1, value);
        });
    }

    public void removeHoster(Hoster hoster) {
        jdbcTemplate.update("DELETE FROM Hoster WHERE ID = ?;", statement ->
        {
            statement.setLong(1, hoster.getID());
        });
    }

    public void removeEpisode(Episode episode) {
        jdbcTemplate.update("DELETE FROM Episode WHERE ID = ?;", statement ->
        {
            statement.setLong(1, episode.getID());
        });
    }

    public void createEpisode(Anime anime, long number) {
        jdbcTemplate.update("INSERT INTO Episode (AnimeID,`Number`) VALUES (?,?);", statement ->
        {
            statement.setLong(1, anime.getID());
            statement.setLong(2, number);
        });
    }

    public void createHostedEpisode(Hoster hoster, Episode episode, String streamURL) {
        if (episode.getHostedEpisodes() == null || episode.getHostedEpisodes().size() == 0) {
            Utils.generateEpisodeNews(episode);
        }

        jdbcTemplate.update("INSERT INTO HostedEpisode (HostID,EpisodeID,StreamURL) VALUES (?,?,?);", statement ->
        {
            statement.setLong(1, hoster.getID());
            statement.setLong(2, episode.getID());
            statement.setString(3, streamURL);
        });
    }

    public void removeHostedEpisode(HostedEpisode hostedEpisode) {
        jdbcTemplate.update("DELETE FROM HostedEpisode WHERE ID = ?;", statement ->
        {
            statement.setLong(1, hostedEpisode.getID());
        });
    }

    public void createAnilistCache(long anilistID, AnimeData data) {
        Gson gson = new Gson();

        jdbcTemplate.update("INSERT INTO AnilistCache (AnilistID,Data,LastModified) VALUES (?,?,NOW());", statement ->
        {
            statement.setLong(1, anilistID);
            statement.setString(2, gson.toJson(data));
        });
    }

    public AnimeData getAnilistCache(long anilistID) {
        Gson gson = new Gson();
        return jdbcTemplate.query("SELECT Data FROM AnilistCache where AnilistID = ?;", statement -> {
            statement.setLong(1, anilistID);
        }, resultSet -> {
            if (resultSet.next()) {
                return gson.fromJson(resultSet.getString(1), AnimeData.class);
            }
            return null;
        });
    }

    public HostedEpisode getHostedEpisodeForStreamURL(String streamURL) {
        return jdbcTemplate.query("SELECT ID, HostID, EpisodeID, StreamURL FROM HostedEpisode where StreamURL = ?;", statement -> {
            statement.setString(1, streamURL);
        }, resultSet -> {
            if (resultSet.next()) {
                HostedEpisode hostedEpisode = new HostedEpisode();
                hostedEpisode.setID(resultSet.getLong(1));
                hostedEpisode.setHostID(resultSet.getLong(2));
                hostedEpisode.setEpisodeID(resultSet.getLong(3));
                hostedEpisode.setStreamURL(resultSet.getString(4));
                return hostedEpisode;
            }

            return null;
        });
    }

    public HostedEpisode getHostedEpisodeByID(long ID) {
        return jdbcTemplate.query("SELECT ID, HostID, EpisodeID, StreamURL FROM HostedEpisode where ID = ?;", statement -> {
            statement.setLong(1, ID);
        }, resultSet -> {
            if (resultSet.next()) {
                HostedEpisode hostedEpisode = new HostedEpisode();
                hostedEpisode.setID(resultSet.getLong(1));
                hostedEpisode.setHostID(resultSet.getLong(2));
                hostedEpisode.setEpisodeID(resultSet.getLong(3));
                hostedEpisode.setStreamURL(resultSet.getString(4));
                return hostedEpisode;
            }

            return null;
        });
    }

    public Episode getEpisodeByID(long ID) {
        return jdbcTemplate.query("SELECT ID, AnimeID, Number FROM Episode where ID = ? ORDER BY Number ASC;", statement -> {
            statement.setLong(1, ID);
        }, resultSet -> {
            if (resultSet.next()) {
                Episode episode = new Episode();
                episode.setID(resultSet.getLong(1));
                episode.setAnimeID(resultSet.getLong(2));
                episode.setNumber(resultSet.getLong(3));
                return episode;
            }

            return null;
        });
    }

    public void createMALSyncCache(long anilistID, String data) {
        jdbcTemplate.update("INSERT INTO MALSyncCache (AnilistID,Data,LastModified) VALUES (?,?,NOW());", statement ->
        {
            statement.setLong(1, anilistID);
            statement.setString(2, data);
        });
    }

    public String getMALSyncCache(long anilistID) {
        return jdbcTemplate.query("SELECT Data FROM MALSyncCache where AnilistID = ?;", statement -> {
            statement.setLong(1, anilistID);
        }, resultSet -> {
            if (resultSet.next()) {
                return resultSet.getString(1);
            }

            return null;
        });

    }

    public List<Item> getNyaaRss() {
        return jdbcTemplate.query("SELECT data FROM NyaaRss ORDER BY ModifiedOn DESC;", statement -> {
        }, resultSet -> {
            Gson gson = new Gson();
            List<Item> items = new ArrayList<>();

            while (resultSet.next()) {
                Item item = gson.fromJson(resultSet.getString(1), Item.class);
                items.add(item);
            }

            return items;
        });
    }

    public void createNyaaRss(Item item) {
        Gson gson = new Gson();
        jdbcTemplate.update("INSERT INTO NyaaRss (guid,`data`) VALUES (?,?);", statement ->
        {
            statement.setString(1, item.getGuid().get());
            statement.setString(2, gson.toJson(item));
        });
    }

    public List<Pair<Date, Integer>> getStatsAvailableAnime() {
        return getStats(0);
    }

    public List<Pair<Date, Integer>> getStatsActiveUsers() {
        return getStats(1);
    }

    public List<Pair<Date, Integer>> getStatsWatchedEpisodes() {
        return getStats(2);
    }

    public List<Pair<Date, Integer>> getStats(int type) {
        return jdbcTemplate.query("SELECT Data, FromDay FROM Stats WHERE Type = " + type + " ORDER BY FromDay ASC;", resultSet -> {
            List<Pair<Date, Integer>> items = new ArrayList<>();

            while (resultSet.next()) {
                items.add(new Pair<>(resultSet.getDate(2), resultSet.getInt(1)));
            }

            return items;
        });
    }

    public List<News> getNews(int limit, NewsType... types) {

        StringBuilder sqlStmt = new StringBuilder();
        sqlStmt.append("SELECT ID, Title, Content, Date, Type, AnimeID, EpisodeID FROM News ");

        if (types != null && types.length > 0) {
            sqlStmt.append(" WHERE Type IN (");

            for (int i = 0; i < types.length; i++) {
                sqlStmt.append(types[i].ordinal());

                if (i + 1 < types.length) {
                    sqlStmt.append(",");
                }
            }

            sqlStmt.append(") ");
        }

        sqlStmt.append(" ORDER BY ModifiedOn DESC LIMIT ").append(limit).append(";");


        return jdbcTemplate.query(sqlStmt.toString(), resultSet -> {
            List<News> news = new ArrayList<>(limit);

            while (resultSet.next()) {
                News newsItem = new News();
                newsItem.setID(resultSet.getLong(1));
                newsItem.setTitle(resultSet.getString(2));
                newsItem.setContent(resultSet.getString(3));
                newsItem.setDateOfPost(resultSet.getDate(4));
                newsItem.setNewsType(NewsType.values()[resultSet.getInt(5)]);

                if (newsItem.getNewsType() == NewsType.ANIME) {
                    newsItem.setAnime(getAnimeByID(resultSet.getLong(6)));
                }

                if (newsItem.getNewsType() == NewsType.EPISODE) {
                    newsItem.setEpisode(getEpisodeByID(resultSet.getLong(7)));
                }
                news.add(newsItem);
            }

            return news;
        });
    }

    public List<News> getNews() {
        return jdbcTemplate.query("SELECT ID, Title, Content, Date FROM News ORDER BY ModifiedOn DESC;", statement -> {
        }, resultSet -> {
            List<News> news = new ArrayList<>();

            while (resultSet.next()) {
                News newsItem = new News();
                newsItem.setID(resultSet.getLong(1));
                newsItem.setTitle(resultSet.getString(2));
                newsItem.setContent(resultSet.getString(3));
                newsItem.setDateOfPost(resultSet.getDate(4));

                news.add(newsItem);
            }

            return news;
        });
    }

    public void createNews(NewsType newsType, String title, String content, Anime anime, Episode episode) {

        try {
            String sqlStmt = "INSERT INTO News (Title,Content,Date,Type) VALUES (?,?,?,?);";

            if (anime != null)
                sqlStmt = "INSERT INTO News (Title,Content,Date,Type,AnimeID) VALUES (?,?,?,?,?);";

            if (episode != null)
                sqlStmt = "INSERT INTO News (Title,Content,Date,Type,EpisodeID) VALUES (?,?,?,?,?);";

            if (episode != null && anime != null)
                sqlStmt = "INSERT INTO News (Title,Content,Date,Type,AnimeID,EpisodeID) VALUES (?,?,?,?,?,?);";

            jdbcTemplate.update(sqlStmt, statement ->
            {
                statement.setString(1, title);
                statement.setString(2, content);
                statement.setDate(3, new Date(System.currentTimeMillis()));
                statement.setInt(4, newsType.ordinal());

                if (anime != null && episode != null) {
                    statement.setLong(5, anime.getID());
                    statement.setLong(6, episode.getID());
                } else if (anime != null && episode == null) {
                    statement.setLong(5, anime.getID());
                } else if (anime == null && episode != null) {
                    statement.setLong(5, episode.getID());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createUser(String username, byte[] password, byte[] salt, String email) {
        jdbcTemplate.update("INSERT INTO User (Username,Password,Salt,Email) VALUES (?,?,?,?);", statement ->
        {
            statement.setString(1, username);
            statement.setBytes(2, password);
            statement.setBytes(3, salt);
            statement.setString(4, email);
        });
    }

    public byte[] getSaltForUser(String username) {
        return jdbcTemplate.query("SELECT Salt FROM User where lower(Username) = lower(?);", statement -> {
            statement.setString(1, username);
        }, resultSet -> {

            if (resultSet.next()) {
                return resultSet.getBytes(1);
            }

            return null;
        });
    }

    public byte[] getPasswordForUser(String username) {
        return jdbcTemplate.query("SELECT Password FROM User where lower(Username) = lower(?);", statement -> {
            statement.setString(1, username);
        }, resultSet -> {

            if (resultSet.next()) {
                return resultSet.getBytes(1);
            }

            return null;
        });
    }

    public long getIDForUser(String username) {
        return jdbcTemplate.query("SELECT ID FROM User where lower(Username) = lower(?);", statement -> {
            statement.setString(1, username);
        }, resultSet -> {
            if (resultSet.next()) {
                return resultSet.getLong(1);
            }
            return Long.valueOf(-1);
        });
    }

    public long getIDForRole(UserRoles role) {
        return jdbcTemplate.query("SELECT ID FROM Roles where Name = ?;",
                statement -> statement.setString(1, role.name().toLowerCase()),
                resultSet -> {
                    if (resultSet.next()) {
                        return resultSet.getLong(1);
                    }

                    return Long.valueOf(-1);
                });
    }

    public String getUserForID(long userID) {
        return jdbcTemplate.query("SELECT Username FROM User where ID = ?;", statement -> {
            statement.setLong(1, userID);
        }, resultSet -> {
            if (resultSet.next()) {
                return resultSet.getString(1);
            }

            return null;
        });
    }

    public void setAvatarForUser(String username, InputStream avatar) {
        jdbcTemplate.update("UPDATE User SET Avatar = ? WHERE Username = ?;", statement -> {
            statement.setBinaryStream(1, avatar);
            statement.setString(2, username);
        });
    }

    public InputStream getAvatarForUser(String username) {
        InputStream inputStream = jdbcTemplate.query("SELECT Avatar FROM User where lower(Username) = lower(?) AND Avatar is not null;",
                statement -> statement.setString(1, username),
                resultSet ->
                {
                    if (resultSet.next()) {
                        return resultSet.getBinaryStream(1);
                    }
                    return null;
                });

        if (inputStream == null) {
            inputStream = Waifulabs.generateWaifu();
        }

        return inputStream;
    }

    public List<UserRoles> getRolesForUser(String username) {
        return jdbcTemplate.query("SELECT r.Name FROM `User` u, UserRoles ur, Roles r WHERE u.ID = ur.UserID and r.ID = ur.RoleID and u.Username = ?;",
                statement -> {
                    statement.setString(1, username);
                }, resultSet -> {
                    List<UserRoles> roles = new ArrayList<>();
                    while (resultSet.next()) {
                        roles.add(UserRoles.valueOf(resultSet.getString(1)));
                    }
                    return roles;
                });
    }

    public List<Episode> getWatchProgressForUser(String username) {
        return jdbcTemplate.query("SELECT wp.EpisodeID, wp.Progress from WatchProgress wp, `User` u WHERE wp.UserID = u.ID and u.Username = ?;",
                statement -> {
                    statement.setString(1, username);
                }, resultSet -> {
                    List<Episode> episodes = new ArrayList<>();
                    while (resultSet.next()) {
                        Episode episode = getEpisodeByID(resultSet.getLong(1));
                        episode.setProgress(resultSet.getDouble(2));
                        episodes.add(episode);
                    }
                    return episodes;
                });

    }

    public List<Episode> getWatchProgressForUserForAnime(String username, long animeID) {
        return jdbcTemplate.query("SELECT wp.EpisodeID, wp.Progress, wp.Completed from WatchProgress wp, `User` u WHERE wp.UserID = u.ID and u.Username = ? AND wp.EpisodeID IN (SELECT ID FROM Episode WHERE AnimeID = ?);",
                statement -> {
                    statement.setString(1, username);
                    statement.setLong(2, animeID);
                }, resultSet -> {
                    List<Episode> episodes = new ArrayList<>();
                    while (resultSet.next()) {
                        Episode episode = getEpisodeByID(resultSet.getLong(1));
                        episode.setProgress(resultSet.getDouble(2));
                        episode.setCompleted(resultSet.getBoolean(3));
                        episodes.add(episode);
                    }
                    return episodes;
                });
    }

    public void createWatchProgress(String username, Episode episode) {
        try {
            long userID = getIDForUser(username);
            jdbcTemplate.update("INSERT INTO WatchProgress (UserID,EpisodeID) VALUES (?,?);", statement ->
            {
                statement.setLong(1, userID);
                statement.setLong(2, episode.getID());
            });
        } catch (Exception ignored) {
        }
    }

    public void updateWatchProgress(String username, Episode episode, double currentTime, boolean completed) {
        long userID = getIDForUser(username);
        jdbcTemplate.update("UPDATE WatchProgress SET Progress = ?, Completed = ? WHERE UserID = ? AND EpisodeID = ?;", statement ->
        {
            statement.setDouble(1, currentTime);
            statement.setBoolean(2, completed);
            statement.setLong(3, userID);
            statement.setLong(4, episode.getID());
        });
    }

    public void removeMALSyncCache(long anilistID) {
        jdbcTemplate.update("DELETE FROM MALSyncCache WHERE AnilistID = ?;", statement ->
        {
            statement.setLong(1, anilistID);
        });
    }

    public void removeAnilistCache(long anilistID) {
        jdbcTemplate.update("DELETE FROM AnilistCache WHERE AnilistID = ?;", statement ->
        {
            statement.setLong(1, anilistID);
        });
    }

    public Date getAnilistCacheLastModified(long anilistID) {
        return jdbcTemplate.query("SELECT LastModified FROM AnilistCache WHERE AnilistID = ?;", statement ->
        {
            statement.setLong(1, anilistID);
        }, resultSet -> {
            if (resultSet.next()) {
                return resultSet.getDate(1);
            }

            return new Date(System.currentTimeMillis());
        });
    }

    public Date getMALSyncCacheLastModified(long anilistID) {
        return jdbcTemplate.query("SELECT LastModified FROM MALSyncCache WHERE AnilistID = ?;", statement ->
        {
            statement.setLong(1, anilistID);
        }, resultSet -> {
            if (resultSet.next()) {
                return resultSet.getDate(1);
            }

            return new Date(System.currentTimeMillis());
        });
    }


    public void createAnimeReport(AnimeReportReason value, String text, String username, Anime anime, Episode episode) {
        long userID = getIDForUser(username);

        jdbcTemplate.query("INSERT INTO AnimeReport (Type,Description,UserID,AnimeID,EpisodeID) VALUES (?,?,?,?,?);", statement -> {
            statement.setInt(1, value.ordinal());
            statement.setString(2, text);
            statement.setLong(3, userID);
            statement.setLong(4, anime.getID());
            statement.setLong(5, episode.getID());
        }, resultSet -> {
        });
    }

    public List<HostedEpisode> getHostedEpisodes() {
        return jdbcTemplate.query("SELECT HostedEpisode.ID, HostedEpisode.HostID, HostedEpisode.EpisodeID, HostedEpisode.StreamURL " +
                "FROM HostedEpisode", statement -> {
        }, resultSet -> {
            List<HostedEpisode> hostedEpisodes = new ArrayList<>();
            while (resultSet.next()) {
                HostedEpisode hostedEpisode = new HostedEpisode();
                hostedEpisode.setID(resultSet.getLong(1));
                hostedEpisode.setHostID(resultSet.getLong(2));
                hostedEpisode.setEpisodeID(resultSet.getLong(3));
                hostedEpisode.setStreamURL(resultSet.getString(4));
                hostedEpisodes.add(hostedEpisode);
            }

            return hostedEpisodes;
        });
    }

    public void setMALIDForAnilistID(long anilistID, long malID) {
        jdbcTemplate.update("INSERT INTO AnilistToMAL (AnilistID,MALID) VALUES (?,?);", statement ->
        {
            statement.setLong(1, anilistID);
            statement.setLong(2, malID);
        });
    }

    public long getMALIDForAnilistID(long anilistID) {
        return jdbcTemplate.query("SELECT MALID FROM AnilistToMAL WHERE AnilistID = ?", statement -> {
            statement.setLong(1, anilistID);
        }, resultSet -> {

            if (resultSet.next()) {
                return resultSet.getLong(1);
            }
            return Long.valueOf(-1);
        });
    }

    public long getAnilistIDForMALID(long malID) {
        return jdbcTemplate.query("SELECT AnilistID FROM AnilistToMAL WHERE MALID = ?", statement -> {
            statement.setLong(1, malID);
        }, resultSet -> {

            if (resultSet.next()) {
                return Long.valueOf(resultSet.getLong(1));
            }
            return Long.valueOf(-1);
        });
    }

    public Set<String> getAnimeGenres() {
        if (animeTags.isEmpty()) {
            jdbcTemplate.query("SELECT DISTINCT ac.Genres from AnilistCache ac;", statement -> {
            }, resultSet -> {
                Gson gson = new Gson();
                Type listType = new TypeToken<List<String>>() {
                }.getType();

                while (resultSet.next()) {
                    List<String> genres = gson.fromJson(resultSet.getString(1), listType);
                    if (genres != null) {
                        animeGenres.addAll(genres);
                    }
                }
            });
        }

        return animeGenres;
    }

    public Set<String> getAnimeTags() {
        if (animeTags.isEmpty()) {
            jdbcTemplate.query("SELECT DISTINCT ac.Tags from AnilistCache ac;", statement -> {
            }, resultSet -> {
                Gson gson = new Gson();
                Type listType = new TypeToken<List<TagsItem>>() {
                }.getType();

                while (resultSet.next()) {
                    List<TagsItem> tagsItems = gson.fromJson(resultSet.getString(1), listType);
                    if (tagsItems != null) {
                        for (TagsItem tagsItem : tagsItems) {
                            animeTags.add(tagsItem.getName());
                        }
                    }
                }
            });
        }

        return animeTags;
    }

    public Set<String> getAnimeYear() {
        if (animeYear.isEmpty()) {
            jdbcTemplate.query("SELECT DISTINCT ac.SeasonYear from AnilistCache ac;", statement -> {
            }, resultSet -> {
                while (resultSet.next()) {
                    String year = resultSet.getString(1);
                    if (year != null)
                        animeYear.add(year);
                }
            });
        }

        return animeYear;
    }

    public Set<String> getAnimeFormats() {
        if (animeFormats.isEmpty()) {
            jdbcTemplate.query("SELECT DISTINCT ac.Format from AnilistCache ac;", statement -> {
            }, resultSet -> {
                while (resultSet.next()) {
                    String format = resultSet.getString(1);
                    if (format != null)
                        animeFormats.add(format);
                }
            });
        }

        return animeFormats;
    }

    public void updatePasswordForUser(String username, byte[] password, byte[] salt) {
        jdbcTemplate.update("UPDATE User SET Password = ?, Salt = ? WHERE Username = ?;", statement ->
        {
            statement.setBytes(1, password);
            statement.setBytes(2, salt);
            statement.setString(3, username);
        });
    }

    public List<Episode> findMissingEpisodesForAnimes(int maxCount) {
        return jdbcTemplate.query("SELECT e.ID FROM Episode e WHERE e.ID not in (SELECT he.EpisodeID from HostedEpisode he);", statement -> {
        }, resultSet -> {
            List<Episode> result = new ArrayList<>(maxCount);
            List<Long> episodesWithoutHost = new ArrayList<>();

            while (resultSet.next()) {
                long id = resultSet.getLong(1);
                episodesWithoutHost.add(id);
            }

            for (Long aLong : episodesWithoutHost) {
                Episode episode = getEpisodeByID(aLong);
                if (episode != null) {
                    result.add(episode);
                }

                if (maxCount > -1 && result.size() >= maxCount) {
                    break;
                }
            }

            return result;
        });
    }

    public void removeWatchProgressForAnime(String username, Anime anime) {
        long userID = getIDForUser(username);

        for (Episode episode : anime.getEpisodes()) {
            jdbcTemplate.update("DELETE FROM WatchProgress WHERE UserID = ? AND EpisodeID = ?;", statement ->
            {
                statement.setLong(1, userID);
                statement.setLong(2, episode.getID());
            });
        }
    }

    public Response getMonhtlyMoeCache(long monthlyID) {
        return jdbcTemplate.query("SELECT Data FROM MonthlyMoeAnimeCache where MonthlyAnimeID = ?;", statement -> {
            statement.setLong(1, monthlyID);
        }, resultSet -> {
            Gson gson = new Gson();

            if (resultSet.next()) {
                moe.evoke.application.backend.monthly.moe.animes.Response response = gson.fromJson(resultSet.getString(1), moe.evoke.application.backend.monthly.moe.animes.Response.class);
                return response;
            }

            return null;
        });
    }

    public void createMonhtlyMoeCache(long monthlyID, Response response) {
        Gson gson = new Gson();

        jdbcTemplate.update("INSERT INTO MonthlyMoeAnimeCache (MonthlyAnimeID,Data) VALUES (?,?);", statement ->
        {
            statement.setLong(1, monthlyID);
            statement.setString(2, gson.toJson(response));
        });
    }

    public List<AnimeReport> getAnimeReports() {
        return jdbcTemplate.query("select Id, Description, UserID, AnimeID, EpisodeID, Type from AnimeReport WHERE isDone = 0", statement -> {
        }, resultSet -> {
            List<AnimeReport> animeReports = new ArrayList<>();

            while (resultSet.next()) {
                AnimeReport animeReport = new AnimeReport();
                animeReport.setId(resultSet.getLong(1));
                animeReport.setDescription(resultSet.getString(2));
                animeReport.setUserID(resultSet.getLong(3));
                animeReport.setAnimeID(resultSet.getLong(4));
                animeReport.setEpisodeID(resultSet.getLong(5));
                animeReport.setReportReason(AnimeReportReason.values()[resultSet.getInt(6)]);

                animeReports.add(animeReport);
            }

            return animeReports;
        });
    }

    public void markAnimeReportAsDone(AnimeReport animeReport) {
        jdbcTemplate.update("UPDATE AnimeReport SET IsDone = 1 WHERE ID = ?;", statement ->
        {
            statement.setLong(1, animeReport.getId());
        });
    }

    public int getAnimeCount() {
        return jdbcTemplate.query("SELECT COUNT(1) FROM Anime", statement -> {
        }, resultSet -> {
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }

            return Integer.valueOf(0);
        });
    }

    public void createUserSession(String username, String tokenValue, java.util.Date date, String series) {
        long userID = getIDForUser(username);

        jdbcTemplate.update("INSERT INTO UserSession (UserID,Token,TokenDate,Series) VALUES (?,?,?,?);", statement ->
        {
            statement.setLong(1, userID);
            statement.setString(2, tokenValue);
            statement.setDate(3, new Date(date.getTime()));
            statement.setString(4, series);
        });
    }

    public void removeUserSession(String token) {
        jdbcTemplate.update("DELETE FROM UserSession WHERE Token = ?;", statement ->
        {
            statement.setString(1, token);
        });
    }

    public PersistentRememberMeToken getPersistentRememberMeToken(String series) {
        return jdbcTemplate.query("SELECT UserID,Token,TokenDate,Series FROM UserSession WHERE Series = ?", statement -> {
            statement.setString(1, series);
        }, resultSet -> {

            if (resultSet.next()) {
                long userID = resultSet.getLong(1);
                String username = getUserForID(userID);
                PersistentRememberMeToken token = new PersistentRememberMeToken(
                        username,
                        resultSet.getString(4),
                        resultSet.getString(2),
                        resultSet.getDate(3)
                );
                return token;
            }

            return null;
        });
    }

    public double getWatchProgressPercentageForUserAndAnime(String username, Anime anime) {
        long userID = getIDForUser(username);

        return jdbcTemplate.query("SELECT COUNT(wp.EpisodeID) / ac.EpisodeCount " +
                "FROM AnilistCache ac, Anime a, WatchProgress wp, Episode e " +
                "WHERE wp.Completed = true && a.AnilistID = ac.AnilistID AND wp.EpisodeID = e.ID AND e.AnimeID = a.ID AND wp.UserID = ? AND a.ID = ? " +
                "GROUP BY a.AnilistID;", statement -> {
            statement.setLong(1, userID);
            statement.setLong(2, anime.getID());
        }, resultSet -> {

            if (resultSet.next()) {
                return resultSet.getDouble(1);
            }

            return Double.valueOf(0);
        });
    }

    public List<Anime> getLastWatchedAnimeForUser(String username, int count, boolean onlyWatching) {

        long userID = getIDForUser(username);

        String stmt = "SELECT * FROM " +
                "(" +
                "SELECT a.ID, a.AnilistID, ac.Title, COUNT(wp.EpisodeID) / ac.EpisodeCount AS Progress, MAX(wp.ModifiedOn) AS LastWatched " +
                "FROM WatchProgress wp, Episode e, Anime a, AnilistCache ac " +
                "WHERE ac.AnilistID = a.AnilistID AND wp.EpisodeID = e.ID AND e.AnimeID = a.ID AND wp.UserID = ? " +
                "GROUP BY a.ID " +
                "ORDER BY LastWatched DESC" +
                ") ss ";

        if (onlyWatching) {
            stmt += "WHERE ss.Progress < 1 ";
        }
        stmt += "LIMIT ?;";

        return jdbcTemplate.query(stmt, statement -> {

            statement.setLong(1, userID);
            statement.setInt(2, count);
        }, resultSet -> {

            List<Anime> lastWatchedAnime = new ArrayList<>(count);
            while (resultSet.next()) {
                Anime anime = new Anime();
                anime.setID(resultSet.getLong(1));
                anime.setAnilistID(resultSet.getLong(2));
                anime.setName(resultSet.getString(3));
                lastWatchedAnime.add(anime);
            }

            return lastWatchedAnime;
        });
    }

    public List<User> getUsers() {
        return jdbcTemplate.query("SELECT ID, Username, Email FROM User", statement -> {
        }, resultSet -> {
            List<User> users = new ArrayList<>(1000);

            while (resultSet.next()) {
                User user = new User();
                user.setID(resultSet.getLong(1));
                user.setUsername(resultSet.getString(2));
                user.setEmail(resultSet.getString(3));
                user.setRoles(getRolesForUser(user.getUsername()));
                users.add(user);
            }

            return users;
        });
    }

    public void updateUserSession(String series, String tokenValue, java.util.Date lastUsed) {
        jdbcTemplate.update("UPDATE UserSession SET TokenDate = ?, Token = ? WHERE Series = ?", statement ->
        {
            statement.setDate(1, new Date(lastUsed.getTime()));
            statement.setString(2, tokenValue);
            statement.setString(3, series);
        });
    }

    public void createDistributionJob(DistributionJob job, DistributionJobStatus jobStatus) {

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection
                    .prepareStatement("INSERT INTO DistributionJob (AnimeID,EpisodeID,Source,Status) VALUES (?,?,?,?);", Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, job.anime.getID());
            statement.setLong(2, job.episode.getID());
            statement.setInt(3, job.source.ordinal());
            statement.setInt(4, jobStatus.ordinal());
            return statement;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            createDistributionJobTargets(key.longValue(), job);
            createDistributionJobOptions(key.longValue(), job);
        }
    }

    private void createDistributionJobTargets(long insertedJob, DistributionJob job) {
        for (DistributionTarget target : job.targets) {

            jdbcTemplate.update("INSERT INTO DistributionJobTargets (JobID,Target) VALUES (?,?);", statement ->
            {
                statement.setLong(1, insertedJob);
                statement.setInt(2, target.ordinal());
            });
        }
    }

    private void createDistributionJobOptions(long insertedJob, DistributionJob job) {
        for (Map.Entry<String, String> entry : job.sourceOptions.entrySet()) {
            jdbcTemplate.update("INSERT INTO DistributionJobOptions (JobID,`Option`,Value) VALUES (?,?,?);", statement ->
            {
                statement.setLong(1, insertedJob);
                statement.setString(2, entry.getKey());
                statement.setString(3, entry.getValue());
            });
        }
    }

    public List<DistributionJob> getAllDistributionJobs() {
        return jdbcTemplate.query("SELECT ID,AnimeID,EpisodeID,Source,Status FROM DistributionJob ORDER BY ModifiedOn DESC", resultSet -> {
            List<DistributionJob> jobs = new ArrayList<>();

            while (resultSet.next()) {
                DistributionJob job = new DistributionJob();
                job.ID = resultSet.getLong(1);
                job.anime = getAnimeByID(resultSet.getLong(2));
                job.episode = getEpisodeByID(resultSet.getLong(3));
                job.source = DistributionSource.values()[resultSet.getInt(4)];

                job.targets = getDistributionJobTargets(job.ID);
                job.sourceOptions = getDistributionJobOptions(job.ID);

                job.setStatus(DistributionJobStatus.values()[resultSet.getInt(5)]);

                jobs.add(job);
            }

            return jobs;
        });
    }

    public List<DistributionJob> getAllDistributionJobs(DistributionJobStatus... jobStatus) {

        StringBuilder str = new StringBuilder();
        str.append("SELECT ID,AnimeID,EpisodeID,Source,Status FROM DistributionJob WHERE ");
        str.append(" Status IN ( ");

        for (int i = 0; i < jobStatus.length; i++) {
            str.append("?");

            if (i < jobStatus.length - 1) {
                str.append(",");
            }
        }

        str.append(" ) ");
        str.append(" ORDER BY ModifiedOn DESC");


        return jdbcTemplate.query(str.toString(), statement -> {
            for (int i = 0; i < jobStatus.length; i++) {
                statement.setInt(i + 1, jobStatus[i].ordinal());
            }
        }, resultSet -> {
            List<DistributionJob> jobs = new ArrayList<>();

            while (resultSet.next()) {
                DistributionJob job = new DistributionJob();
                job.ID = resultSet.getLong(1);
                job.anime = getAnimeByID(resultSet.getLong(2));
                job.episode = getEpisodeByID(resultSet.getLong(3));
                job.source = DistributionSource.values()[resultSet.getInt(4)];

                job.targets = getDistributionJobTargets(job.ID);
                job.sourceOptions = getDistributionJobOptions(job.ID);

                job.setStatus(DistributionJobStatus.values()[resultSet.getInt(5)]);

                jobs.add(job);
            }

            return jobs;
        });
    }

    public DistributionJob getNextDistributionJob() {
        return jdbcTemplate.query(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT ID,AnimeID,EpisodeID,Source,Status FROM DistributionJob WHERE Status = ? LIMIT 1 FOR UPDATE",
                    ResultSet.TYPE_FORWARD_ONLY,
                    ResultSet.CONCUR_UPDATABLE);
            statement.setInt(1, DistributionJobStatus.OPEN.ordinal());
            return statement;
        }, resultSet -> {
            if (resultSet.next()) {
                resultSet.updateInt(5, DistributionJobStatus.RUNNING.ordinal());
                resultSet.updateRow();

                DistributionJob job1 = new DistributionJob();
                job1.ID = resultSet.getLong(1);
                job1.anime = getAnimeByID(resultSet.getLong(2));
                job1.episode = getEpisodeByID(resultSet.getLong(3));
                job1.source = DistributionSource.values()[resultSet.getInt(4)];

                job1.targets = getDistributionJobTargets(job1.ID);
                job1.sourceOptions = getDistributionJobOptions(job1.ID);

                job1.setStatus(DistributionJobStatus.values()[resultSet.getInt(5)]);

                return job1;
            }
            return null;
        });
    }

    private List<DistributionTarget> getDistributionJobTargets(long jobID) {
        return jdbcTemplate.query("SELECT Target FROM DistributionJobTargets WHERE JobID = ?", statement -> {
            statement.setLong(1, jobID);
        }, resultSet -> {
            List<DistributionTarget> targets = new ArrayList<>();
            while (resultSet.next()) {
                targets.add(DistributionTarget.values()[resultSet.getInt(1)]);
            }

            return targets;
        });
    }

    private Map<String, String> getDistributionJobOptions(long jobID) {
        return jdbcTemplate.query("SELECT `Option`, Value FROM DistributionJobOptions WHERE JobID = ?", statement -> {
            statement.setLong(1, jobID);
        }, resultSet -> {
            Map<String, String> options = new HashMap<>();

            while (resultSet.next()) {
                options.put(resultSet.getString(1), resultSet.getString(2));
            }

            return options;
        });
    }

    public void updateDistributionJob(DistributionJob job, DistributionJobStatus status) {
        jdbcTemplate.update("UPDATE DistributionJob SET Status = ? WHERE ID = ?;", statement ->
        {
            statement.setInt(1, status.ordinal());
            statement.setLong(2, job.ID);
        });
    }

    public void removeAllRolesForUser(String username) {
        long userID = getIDForUser(username);
        jdbcTemplate.update("DELETE FROM UserRoles WHERE UserID = ?;", statement ->
        {
            statement.setLong(1, userID);
        });
    }

    public void addRoleToUser(String username, UserRoles role) {

        long roleID = getIDForRole(role);
        long userID = getIDForUser(username);

        jdbcTemplate.update("INSERT INTO UserRoles (UserID,RoleID) VALUES (?,?);", statement ->
        {
            statement.setLong(1, userID);
            statement.setLong(2, roleID);
        });
    }

    public void updateEmailForUser(String username, String email) {
        jdbcTemplate.update("UPDATE User SET Email = ? WHERE Username = ?;", statement ->
        {
            statement.setString(1, email);
            statement.setString(2, username);
        });
    }

    public void createInviteCode(String code) {
        jdbcTemplate.update("INSERT INTO UserInviteCode (InviteCode) VALUES (?);", statement -> statement.setString(1, code));
    }

    public List<Pair<String, String>> getInviteCodes() {
        return jdbcTemplate.query("SELECT InviteCode, UsedBy FROM UserInviteCode;", statement -> {
        }, resultSet -> {
            List<Pair<String, String>> inviteCodes = new ArrayList<>();

            while (resultSet.next()) {
                String username = getUserForID(resultSet.getLong(2));
                inviteCodes.add(new Pair<>(resultSet.getString(1), username));
            }

            return inviteCodes;
        });
    }

    public boolean isInviteCodeValid(String code) {
        return jdbcTemplate.query("SELECT InviteCode, UsedBy FROM UserInviteCode WHERE InviteCode = ? AND UsedBy is null;",
                statement -> statement.setString(1, code),
                resultSet -> {
                    return resultSet.next();
                });
    }

    public void updateInviteCode(String code, String username) {
        long userID = getIDForUser(username);

        jdbcTemplate.update("UPDATE UserInviteCode SET UsedBy = ? WHERE InviteCode = ?;", statement -> {
            statement.setLong(1, userID);
            statement.setString(2, code);
        });
    }

    public boolean isUsernameAvailable(String username) {
        return jdbcTemplate.query("SELECT Username FROM User WHERE lower(Username) = lower(?);",
                statement -> statement.setString(1, username),
                resultSet -> {
                    return !resultSet.next();
                });
    }

    public List<Anime> searchAnime(String name, List<String> genres, List<String> tags, List<String> years, List<String> format, String orderBy, boolean ascending, int limit, int offset) {
        int counter = 1;
        Map<Integer, Object> counterValue = new HashMap<>();

        StringBuilder sqlStmt = new StringBuilder();
        sqlStmt.append("SELECT a.ID, a.AnilistID, ac.Title, ac.Cover, ac.EpisodeCount ");
        sqlStmt.append(" from Anime a, AnilistCache ac ");
        sqlStmt.append(" WHERE a.AnilistID = ac.AnilistID ");
        sqlStmt.append(" AND a.ID IN (SELECT e2.AnimeID FROM Episode e2, HostedEpisode he2 WHERE e2.ID = he2.EpisodeID) ");

        if (name != null && !name.isEmpty()) {
            sqlStmt.append(" AND (");

            sqlStmt.append(" MATCH(ac.Title,ac.TitleEnglish,ac.TitleRomaji,ac.TitleNative) AGAINST (? IN NATURAL LANGUAGE MODE) ");
            counterValue.put(counter++, name);

            sqlStmt.append(" )");
        }

        if (genres != null && genres.size() > 0) {
            sqlStmt.append(" AND (");
            for (int i = 0; i < genres.size(); i++) {
                sqlStmt.append(" JSON_CONTAINS(ac.Genres, '\"").append(genres.get(i)).append("\"')");

                if (i + 1 < genres.size())
                    sqlStmt.append(" AND ");
            }
            sqlStmt.append(") ");
        }

        if (tags != null && tags.size() > 0) {
            sqlStmt.append(" AND (");
            for (int i = 0; i < tags.size(); i++) {
                sqlStmt.append(" JSON_CONTAINS(ac.Tags, '{ \"name\" : \"").append(tags.get(i)).append("\" }') ");

                if (i + 1 < tags.size())
                    sqlStmt.append(" AND ");
            }
            sqlStmt.append(") ");
        }

        if (years != null && years.size() > 0) {
            sqlStmt.append(" AND ac.SeasonYear IN (");
            for (int i = 0; i < years.size(); i++) {
                sqlStmt.append("?");

                if (i + 1 < years.size())
                    sqlStmt.append(",");

                counterValue.put(counter++, years.get(i));
            }
            sqlStmt.append(") ");
        }

        if (format != null && format.size() > 0) {
            sqlStmt.append(" AND ac.Format IN (");
            for (int i = 0; i < format.size(); i++) {
                sqlStmt.append("?");

                if (i + 1 < format.size())
                    sqlStmt.append(",");

                counterValue.put(counter++, format.get(i));
            }
            sqlStmt.append(") ");
        }

        if (orderBy != null && !orderBy.isEmpty())
            sqlStmt.append(" ORDER BY ").append(orderBy).append(" ").append(ascending ? "ASC" : "DESC").append(" ");

        if (limit > 0)
            sqlStmt.append(" LIMIT ").append(limit).append(" ");

        if (offset > 0)
            sqlStmt.append(" OFFSET ").append(offset).append(" ");

        sqlStmt.append(";");

        return jdbcTemplate.query(sqlStmt.toString(), statement -> {
            for (Map.Entry<Integer, Object> entry : counterValue.entrySet()) {
                if (entry.getValue() instanceof Integer) {
                    Integer intVal = (Integer) entry.getValue();
                    statement.setInt(entry.getKey(), intVal);
                }

                if (entry.getValue() instanceof String) {
                    String stringVal = (String) entry.getValue();
                    statement.setString(entry.getKey(), stringVal);
                }
            }
        }, resultSet -> {

            List<Anime> animeList = new ArrayList<>(limit);
            while (resultSet.next()) {
                Anime anime = new Anime();
                anime.setID(resultSet.getLong(1));
                anime.setAnilistID(resultSet.getLong(2));
                anime.setName(resultSet.getString(3));
                anime.setCover(resultSet.getString(4));
                anime.setEpisodeCount(resultSet.getInt(5));
                animeList.add(anime);
            }

            return animeList;
        });
    }

    public List<String> getIPFSGateways() {
        return jdbcTemplate.query("SELECT Address FROM IPFSGateway where Public = 1;",
                resultSet -> {
                    List<String> hostList = new ArrayList<>();
                    while (resultSet.next()) {
                        hostList.add(resultSet.getString(1));
                    }
                    return hostList;
                });
    }

    public List<IPFSGateway> getAllIPFSGateways() {
        return jdbcTemplate.query("SELECT Address, APIAddress, Public, ID, IPFSUrl FROM IPFSGateway;",
                resultSet -> {
                    List<IPFSGateway> hostList = new ArrayList<>();
                    while (resultSet.next()) {
                        hostList.add(new IPFSGateway(resultSet.getLong(4),
                                resultSet.getString(1),
                                resultSet.getString(2),
                                resultSet.getBoolean(3),
                                resultSet.getString(5)));
                    }
                    return hostList;
                });
    }

    public void createIPFSIndex(IPFSGateway gateway, String hash) {
        jdbcTemplate.update("INSERT INTO IPFSIndex (GatewayID, Base58Hash) VALUES (?,?);", statement ->
        {
            statement.setLong(1, gateway.getID());
            statement.setString(2, hash);
        });
    }

    public IPFSGateway getGatewayByID(long ID) {
        return jdbcTemplate.query("SELECT Address, APIAddress, Public, ID, IPFSUrl FROM IPFSGateway WHERE ID = ?;",
                statement -> statement.setLong(1, ID),
                resultSet -> {
                    if (resultSet.next()) {
                        return new IPFSGateway(resultSet.getLong(4),
                                resultSet.getString(1),
                                resultSet.getString(2),
                                resultSet.getBoolean(3),
                                resultSet.getString(5));
                    }
                    return null;
                });
    }

    public List<IPFSGateway> getGatewaysForHash(String hash) {
        return jdbcTemplate.query("SELECT GatewayID, Base58Hash FROM IPFSIndex WHERE GatewayID = ?;",
                statement -> statement.setString(1, hash),
                resultSet -> {
                    List<IPFSGateway> hostList = new ArrayList<>();
                    while (resultSet.next()) {
                        IPFSGateway gateway = getGatewayByID(resultSet.getLong(1));
                        hostList.add(gateway);
                    }
                    return hostList;
                });
    }
}
