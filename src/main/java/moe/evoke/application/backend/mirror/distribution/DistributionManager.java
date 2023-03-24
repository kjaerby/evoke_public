package moe.evoke.application.backend.mirror.distribution;

import moe.evoke.application.backend.db.Database;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class DistributionManager {

    private static final Logger logger = LoggerFactory.getLogger(DistributionManager.class);

    public static List<DistributionJob> getAllJobs() {
        List<DistributionJob> jobs = Database.instance().getAllDistributionJobs();
        return jobs;
    }

    public static Optional<DistributionJob> getNextJob() {

        DistributionJob job = Database.instance().getNextDistributionJob();

        return Optional.ofNullable(job);
    }

    public static void updateJobStatus(DistributionJob job, DistributionJobStatus status) {
        Database.instance().updateDistributionJob(job, status);
    }

    public static void submitJob(DistributionJob job) {
        Database.instance().createDistributionJob(job, DistributionJobStatus.OPEN);
    }


}
