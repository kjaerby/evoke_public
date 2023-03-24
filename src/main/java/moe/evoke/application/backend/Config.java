package moe.evoke.application.backend;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {

    private static Properties prop = null;

    static {
        initConfig();
    }

    private synchronized static void initConfig() {
        if (prop != null) {
            return;
        }

        try (InputStream input = new FileInputStream("config.properties")) {
            prop = new Properties();
            prop.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
            prop = new Properties();
        }
    }

    private static String getConfigValue(String key, String defaultValue) {
        // . is not allowed in bash
        String value = System.getenv(key.replace(
                ".", "_"));
        if (value == null || value.isEmpty()) {
            value = prop.getProperty(key, defaultValue);
        }

        return value;
    }

    private static int getConfigValue(String key, int defaultValue) {
        return Integer.parseInt(getConfigValue(key, Integer.toString(defaultValue)));
    }

    public static String getSQLDatabse() {
        return getConfigValue("db.database", "");
    }

    public static String getSQLServer() {
        return getConfigValue("db.server", "");
    }

    public static String getSQLPort() {
        return getConfigValue("db.port", "");
    }

    public static String getSQLUser() {
        return getConfigValue("db.user", "");
    }

    public static String getSQLPassword() {
        return getConfigValue("db.password", "");
    }

    public static String getVivoAPIKey() {
        return getConfigValue("mirror.vivo.api.key", "");
    }

    public static String getStreamZAPIKey() {
        return getConfigValue("mirror.streamz.api.key", "");
    }

    public static String getStreamtapeAPIUser() {
        return getConfigValue("mirror.streamtape.api.user", "");
    }

    public static String getStreamtapeAPIPassword() {
        return getConfigValue("mirror.streamtape.api.password", "");
    }

    public static String getMP4UploadUsername() {
        return getConfigValue("mirror.mp4upload.api.user", "");
    }

    public static String getMP4UploadPassword() {
        return getConfigValue("mirror.mp4upload.api.password", "");
    }

    public static String getMonthlyMoeAPIKey() {
        return getConfigValue("api.monthly.moe.key", "");
    }

    public static String getRequestUserAgent() {
        return getConfigValue("mirror.useragent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 11_2_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.114 Safari/537.36");
    }

    public static boolean isAiringImportActive() {
        return Boolean.parseBoolean(getConfigValue("airing.autoimport.enable", "false"));
    }

    public static int getAiringImportInterval() {
        return Integer.parseInt(getConfigValue("airing.autoimport.interval", "60"));
    }

    public static String getGitLabServer() {
        return getConfigValue("gitlab.server", "");
    }

    public static String getGitLabToken() {
        return getConfigValue("gitlab.token", "");
    }

    public static String getGitLabProjectID() {
        return getConfigValue("gitlab.project.id", "");
    }


    public static String getIPFSUrl() {
        return getConfigValue("mirror.ipfs.url", "/ip4/127.0.0.1/tcp/5001");
    }

    public static int getMegaFtpPort() {
        return getConfigValue("mirror.mega.ftp.port", 4990);
    }

    public static String getMegaFtpAddress() {
        return getConfigValue("mirror.mega.ftp.address", "localhost");
    }

    public static String getRedisAddress() {
        return getConfigValue("redis.address", "localhost");
    }

    public static int getRedisPort() {
        return getConfigValue("redis.port", 6379);
    }

    public static String getIPFSCluster() {
        return getConfigValue("mirror.ipfs.cluster", "/ip4/127.0.0.1/tcp/9094");
    }

    public static String getIPFSClusterAllocations() {
        return getConfigValue("mirror.ipfs.cluster.allocations", "");
    }

    public static boolean isTwistEnabled() {
        return Boolean.parseBoolean(getConfigValue("mirror.source.twist", "true"));
    }

    public static boolean isGogoEnabled() {
        return Boolean.parseBoolean(getConfigValue("mirror.source.gogo", "true"));
    }
}
