package moe.evoke.application.backend.mirror;

import moe.evoke.application.backend.Config;
import moe.evoke.application.backend.mirror.distribution.DistributionHelper;
import moe.evoke.application.backend.mirror.distribution.DistributionJob;
import moe.evoke.application.backend.mirror.distribution.DistributionJobStatus;
import moe.evoke.application.backend.mirror.distribution.DistributionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class DistributionWorker {

    private static final Logger logger = LoggerFactory.getLogger(DistributionWorker.class);

    public static void main(String[] args) {
        logger.info("Starting distribution worker....");

        if (Config.isAiringImportActive()) {
            logger.info("Starting airing distribution...");
            DistributionHelper.watchAiringDistribution();
        }

        logger.info("Starting main loop...");
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future future = null;
        var ref = new Object() {
            Optional<DistributionJob> job = Optional.empty();
        };

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (ref.job.isPresent()) {
                DistributionManager.updateJobStatus(ref.job.get(), DistributionJobStatus.ERROR);
            }
        }));

        while (true) {
            // catch everything to prevent crash of app
            try {
                if (future != null) {
                    if (future.isDone() || future.isCancelled()) {
                        future = null;
                    }
                } else {
                    ref.job = DistributionManager.getNextJob();
                    if (ref.job != null && ref.job.isPresent()) {
                        try {
                            logger.info("Submitted job.");
                            future = executor.submit(() -> DistributionHelper.distributeEpisode(ref.job.get()));

                        } catch (Throwable ex) {
                            logger.error("Exception thrown:", ex);
                            DistributionManager.updateJobStatus(ref.job.get(), DistributionJobStatus.ERROR);
                        }
                    }
                }
            } catch (Throwable ex) {
                logger.error("Exception thrown:", ex);
            }

            try {
                long random = Math.round(Math.random() * 25000.0) + 10000;
                Thread.sleep(random);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


    }


}
