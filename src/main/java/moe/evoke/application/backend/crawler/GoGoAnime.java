package moe.evoke.application.backend.crawler;

import moe.evoke.application.backend.Config;
import moe.evoke.application.backend.db.Anime;
import moe.evoke.application.backend.db.Database;
import moe.evoke.application.backend.db.Episode;
import moe.evoke.application.backend.db.Hoster;
import moe.evoke.application.backend.malsync.MALSync;
import moe.evoke.application.backend.malsync.MALSyncProvider;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GoGoAnime {

    private static final Logger logger = LoggerFactory.getLogger(GoGoAnime.class);

    public static void crawl(Anime anime, String baseURL) {

        HashMap<Integer, String> urls = new HashMap<Integer, String>();

        System.setProperty("webdriver.chrome.driver", "./chromedriver");

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--mute-audio");
        options.addArguments("--headless");

        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, 10);

        logger.info("Started Crawling for '" + anime.getName() + "' ...");

        try {
            driver.get("https://gogoanime.ai/");

            int counter = 1;
            while (true) {
                logger.info("Crawling Episode: " + counter);

                try {
                    driver.get(baseURL + counter);

                    WebElement iframe = wait.until(webDriver -> webDriver.findElement(By.xpath("//*[@id=\"load_anime\"]/div/div/iframe")));
                    String url = iframe.getAttribute("src");
                    urls.put(counter, url);

                    counter++;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    break;
                }
            }

            logger.info("Create Database Entries ");

            Hoster gogoHoster = Database.instance().getHoster().stream().filter(hoster -> hoster.getName().equalsIgnoreCase("gogo")).findFirst().get();

            for (Map.Entry<Integer, String> entry : urls.entrySet()) {
                Optional<Episode> episode = anime.getEpisodes().stream().filter(episode1 -> episode1.getNumber() == entry.getKey()).findAny();
                if (episode.isEmpty()) {
                    Database.instance().createEpisode(anime, entry.getKey());
                    episode = anime.getEpisodes().stream().filter(episode1 -> episode1.getNumber() == entry.getKey()).findAny();
                }
                episode.ifPresent(eps -> Database.instance().createHostedEpisode(gogoHoster, eps, entry.getValue()));
            }

        } finally {
            driver.quit();
        }

        logger.info("Crawling finished!");
    }

    public static String crawl(String url) {
        System.setProperty("webdriver.chrome.driver", "./chromedriver");

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--mute-audio");
        options.addArguments("--headless");
        //options.addExtensions(new File("misc/ublock.crx"));

        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, 10);

        try {
            driver.get(url);

            final String mp4uploadButton = "#wrapper_bg > section > section.content_left > div:nth-child(1) > div.anime_video_body > div.anime_muti_link > ul > li.mp4upload > a";
            WebElement button = wait.until(webDriver -> webDriver.findElement(By.cssSelector(mp4uploadButton)));
            //button.click();
            JavascriptExecutor jse = (JavascriptExecutor)driver;
            jse.executeScript("arguments[0].click()", button);

            WebElement iframe = wait.until(webDriver -> webDriver.findElement(By.xpath("//*[@id=\"load_anime\"]/div/div/iframe")));
            String iFrameUrl = iframe.getAttribute("src");

            iFrameUrl = iFrameUrl.replace("\"https://www.mp4upload.com/embed-", "");

            return iFrameUrl;
        } catch (Exception ex) {
            ex.printStackTrace();

        } finally {
            driver.quit();
        }

        return "n/a";
    }

    public static String getDownloadURL(Anime anime, Episode episode) {
        Anime animeToProcess = Database.instance().getAnimeByAnilistID(anime.getAnilistID());
        List<MALSyncProvider> providers = MALSync.getProviderForAnime(animeToProcess);
        if (providers != null) {
            Optional<MALSyncProvider> provider = providers.stream().filter(malSyncProvider -> malSyncProvider.provider.equalsIgnoreCase("gogoanime")).findFirst();

            if (provider.isPresent()) {
                String baseURL = "https://gogoanime.ai/" + provider.get().identifier + "-episode-";

                return crawl(baseURL + episode.getNumber())
                        .replace("https://www.mp4upload.com/embed-", "")
                        .replace(".html", "");
            }
        }
        return "n/a";
    }

    private static String getFileDownloadURL(String downloadPage) throws IOException {
        Document doc = Jsoup.connect(downloadPage).get();
        Elements downloadUrls = doc.select("a");

        Map<String, String> linkMap = new HashMap<>();
        for (Element links : downloadUrls) {
            if (links.text().contains("mp4)")) {
                String key = links.text().replace("Download (", "").replace("- mp4)", "").trim();

                logger.debug("Download URL: " + links.attr("href") + " - '" + key + "'");
                linkMap.put(key, links.attr("href"));
            }
        }

        String keyToDownload = "";
        if (linkMap.containsKey("HDP"))
            keyToDownload = "HDP";
        else if (linkMap.containsKey("1080P"))
            keyToDownload = "1080P";
        else if (linkMap.containsKey("720P"))
            keyToDownload = "720P";
        else if (linkMap.containsKey("480P"))
            keyToDownload = "480P";
        else if (linkMap.containsKey("360P"))
            keyToDownload = "360P";

        logger.debug("To Download: " + keyToDownload);
        return linkMap.get(keyToDownload);
    }

    public static boolean downloadFileFromMP4Upload(String downloadURL, String filename) {
        logger.debug("Download '" + filename + "' from '" + downloadURL + "'");

        try {
            String[] commandNormal = {"curl", "-k", "-L", "-o", filename, "-C", "-", "https://www.mp4upload.com/" + downloadURL,
                    "-H", "user-agent: " + Config.getRequestUserAgent(),
                    "-H", "content-type: application/x-www-form-urlencoded",
                    "-H", "referer: https://www.mp4upload.com/" + downloadURL,
                    "--data-raw", "op=download2&id=" + downloadURL + "&rand=&referer=https%3A%2F%2Fwww.mp4upload.com%2Fembed-" + downloadURL + ".html&method_free=+&method_premium="
            };
            var processBuilder = new ProcessBuilder();
            int returnCode = processBuilder.command(commandNormal).inheritIO().start().waitFor();

            if (returnCode != 0) {
                logger.error("curl returned: " + returnCode);
                return false;
            }

            File downloadFile = new File(filename);
            long fileSizeInBytes = downloadFile.length();
            long fileSizeInKB = fileSizeInBytes / 1024;
            long fileSizeInMB = fileSizeInKB / 1024;
            if (fileSizeInMB < 25) {
                downloadFile.delete();
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return new File(filename).exists();
    }
}
