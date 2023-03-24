package moe.evoke.application.backend.crawler;

import moe.evoke.application.backend.db.Anime;
import moe.evoke.application.backend.db.Episode;
import moe.evoke.application.backend.malsync.MALSync;
import moe.evoke.application.backend.malsync.MALSyncProvider;
import moe.evoke.application.backend.mirror.distribution.DistributionJob;
import moe.evoke.application.backend.util.Utils;
import org.apache.commons.io.FilenameUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Crunchyroll {

    public static final String OPTION_COOKIES_PATH = "option.cookies.path";
    public static final String OPTION_EPISODE_URL = "option.episode.url";
    public static final String OPTION_USERAGENT = "option.useragent";
    private static final Logger logger = LoggerFactory.getLogger(Crunchyroll.class);

    public static File downloadEpisode(Episode episode) {

        Anime anime = episode.getAnime();
        List<MALSyncProvider> providerList = MALSync.getProviderForAnime(anime);

        Optional<MALSyncProvider> providerOptional = providerList.stream().filter(provider -> provider.getProvider().toLowerCase().contains("crunchyroll")).findFirst();
        if (!providerOptional.isPresent()) {
            logger.error("Could not get provider for anime!");
            return null;
        }

        String url = providerOptional.get().getUrl();

        Map<Long, String> episodes = getEpisodeList(url);

        if (episodes.containsKey(episode.getNumber())) {
            logger.info("Episode to download: " + episodes.get(episode.getNumber()));
        }


        return null;
    }

    private static Map<Long, String> getEpisodeList(String url) {

        Map<Long, String> episodes = new HashMap<>();

        System.setProperty("webdriver.chrome.driver", "./chromedriver");

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--mute-audio");
        options.addArguments("--headless");

        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, 10);

        driver.get(url);

        try {
            driver.get(url);

            WebElement contentVideos = wait.until(webDriver -> webDriver.findElement(By.cssSelector("#showview_content_videos > ul")));
            List<WebElement> links = contentVideos.findElements(By.tagName("a"));
            logger.info("Links: " + links.size());
            for (WebElement link : links) {
                if (link.getText().toLowerCase().contains("episode")) {
                    String linkUrl = link.getAttribute("href");
                    String episodeStr = link.findElement(By.tagName("span")).getText().trim();
                    episodeStr = episodeStr.replace("Episode", "").trim();

                    long episode = Long.parseLong(episodeStr);
                    episodes.put(episode, linkUrl);
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();

        } finally {
            driver.quit();
        }


        return episodes;
    }

    public static File downloadWithYoutubeDL(Episode episode, String url, File cookies, String userAgent) {
        String filename = episode.getAnime().getAnilistID() + "-" + episode.getNumber() + ".mp4";
        return downloadWithYoutubeDL(filename, url, cookies, userAgent);
    }

    public static File downloadWithYoutubeDL(String filename, String url, File cookies, String userAgent) {
        try {


            String[] command = {
                    "youtube-dl",
                    "--cookies",
                    cookies.getAbsolutePath(),
                    "--write-sub",
                    "--sub-lang",
                    "enUS",
                    "--sub-format",
                    "ass",
                    url,
                    "--verbose",
                    "--user-agent",
                    userAgent,
                    "--referer",
                    "https://static.crunchyroll.com/",
                    "--output",
                    filename
            };

            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.inheritIO().command(command).start().waitFor();

            File result = new File(filename);
            if (result.exists()) {
                return result;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static File downloadEpisode(DistributionJob job) {

        String url = job.sourceOptions.get(OPTION_EPISODE_URL);
        String userAgent = job.sourceOptions.get(OPTION_USERAGENT);
        File cookies = new File(job.sourceOptions.get(OPTION_COOKIES_PATH));

        File result = downloadWithYoutubeDL(job.episode, url, cookies, userAgent);
        cookies.delete();

        if (result != null && result.exists()) {
            String filename = job.episode.getAnime().getAnilistID() + "-" + job.episode.getNumber();

            File video = result.getAbsoluteFile();
            File subtitle = new File(filename + ".enUS.ass");

            File burnedVideo = Utils.burninSubtitle(video, subtitle);

            video.delete();
            subtitle.delete();

            if (burnedVideo.renameTo(video)) {
                burnedVideo = video;
            }

            return burnedVideo;
        }

        return null;
    }

    public static void main(String[] args) {
        String url = args[0];
        String cookies = args[1];
        String userAgent = args[2];
        String output = args[3];

        logger.info("URL: " + url);
        logger.info("Cookies: " + cookies);
        logger.info("UserAgent: " + userAgent);
        logger.info("Ouput: " + output);

        File downloadedFile = downloadWithYoutubeDL(output, url, new File(cookies), userAgent);
        File video = downloadedFile.getAbsoluteFile();
        File subtitle = new File(FilenameUtils.getBaseName(output) + ".enUS.ass");

        Utils.burninSubtitle(video, subtitle);
    }
}
