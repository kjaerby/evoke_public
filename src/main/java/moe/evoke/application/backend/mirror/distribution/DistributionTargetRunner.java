package moe.evoke.application.backend.mirror.distribution;

import moe.evoke.application.backend.db.Anime;
import moe.evoke.application.backend.db.Episode;

import java.io.File;

public interface DistributionTargetRunner {

    void execute(File fileToUpload, Anime anime, Episode episode);

}
