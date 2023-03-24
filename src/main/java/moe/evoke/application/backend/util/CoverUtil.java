package moe.evoke.application.backend.util;

import moe.evoke.application.backend.db.Anime;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;

public class CoverUtil {

    public static InputStream getCoverForAnime(Anime anime) {
        File repositoryFolder = new File("./repository/covers/");
        if (!repositoryFolder.exists()) {
            repositoryFolder.mkdirs();
        }

        File animeCover = new File(repositoryFolder.getAbsolutePath() + "/" + anime.getAnilistID());
        if (!animeCover.exists()) {
            try {
                String coverURL = anime.getCover();
                InputStream coverInputStream = new URL(coverURL).openStream();
                FileUtils.copyInputStreamToFile(coverInputStream, animeCover);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        if (!animeCover.exists()) {
            return null;
        }

        try {
            return new FileInputStream(animeCover);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void deleteCoverForAnime(Anime anime) {
        File repositoryFolder = new File("./repository/covers/");
        if (!repositoryFolder.exists()) {
            return;
        }

        File animeCover = new File(repositoryFolder.getAbsolutePath() + "/" + anime.getAnilistID());
        if (animeCover.exists()) {
            animeCover.delete();
        }
    }
}
