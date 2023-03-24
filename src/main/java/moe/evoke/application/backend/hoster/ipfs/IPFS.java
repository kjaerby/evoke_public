package moe.evoke.application.backend.hoster.ipfs;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.vaadin.flow.internal.Pair;
import io.ipfs.api.MerkleNode;
import io.ipfs.api.NamedStreamable;
import moe.evoke.application.backend.db.Database;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class IPFS {
    private static final Logger logger = LoggerFactory.getLogger(IPFS.class);

    private static final int TOKEN_KEY = 1337;
    private static final LoadingCache<Integer, List<IPFSGateway>> gateways;

    static {
        gateways = Caffeine.newBuilder()
                .maximumSize(1)
                .expireAfterWrite(4, TimeUnit.HOURS)
                .refreshAfterWrite(2, TimeUnit.HOURS)
                .build(key -> collectGateways());

        gateways.get(TOKEN_KEY);
    }

    public static String getRandomGateway() {
        List<String> gateways = Database.instance().getIPFSGateways();

        Random random = new Random();
        return gateways.get(random.nextInt(gateways.size()));
    }

    public static List<String> getGateways() {
        return new ArrayList<>(Database.instance().getIPFSGateways());
    }

    private static List<IPFSGateway> findUploadGateways() {
        List<IPFSGateway> gateways = new ArrayList<>();
        boolean publicGateway = false, privateGateway = false;
        for (IPFSGateway gateway : getAllGateways()) {
            if (gateway.getStatsRepoResponse() == null) {
                continue;
            }

            long storageAvailable = gateway.getStatsRepoResponse().getStorageMax() - gateway.getStatsRepoResponse().getRepoSize();
            if (!publicGateway && gateway.isPublic() && storageAvailable > 0) {
                gateways.add(gateway);
                publicGateway = true;
            }

            if (!privateGateway && !gateway.isPublic() && storageAvailable > 0) {
                gateways.add(gateway);
                privateGateway = true;
            }
        }

        return gateways;
    }

    public static String uploadFile(File fileToUpload, boolean replicate) throws IOException {
        // Get 2 IPFS nodes...
        // 1. is frontend --> public = 1
        // 2. is backend --> public = 0
        List<IPFSGateway> gateways = findUploadGateways();
        if (gateways.size() < 2) {
            logger.error("Could not find any gateway!");
            logger.info("Waiting 10min, then retry!");

            try {
                Thread.sleep(10 * 60 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            gateways = findUploadGateways();
            if (gateways.size() < 2) {
                logger.error("Still no gateways.. giving up!");
                return null;
            }
        }


        List<Pair<IPFSGateway, String>> pins = new ArrayList<>();
        gateways.parallelStream().forEach(gateway -> {
            logger.info("Uploading to: " + gateway.getIpfsURL());
            var ipfs = new io.ipfs.api.IPFS(gateway.getIpfsURL());
            logger.debug("Uploading " + fileToUpload.getName() + " ...");
            NamedStreamable.FileWrapper file = new NamedStreamable.FileWrapper(fileToUpload);

            try {
                MerkleNode addResult = ipfs.add(file).get(0);
                pins.add(new Pair<>(gateway, addResult.hash.toBase58()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        logger.debug("Upload complete!");

        // Keep track of files...
        // Add to IPFSIndex
        for (Pair<IPFSGateway, String> pin : pins) {
            logger.debug("Create index for '" + pin.getSecond() + "' on '" + pin.getFirst().getIpfsURL() + "'");
            try {
                Database.instance().createIPFSIndex(pin.getFirst(), pin.getSecond());
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }

        if (pins.size() == 2) {
            return pins.get(0).getSecond();
        }

        return null;
    }

    public static List<IPFSGateway> getAllGateways() {
        return gateways.get(TOKEN_KEY);
    }

    private static List<IPFSGateway> collectGateways() {
        return Database.instance().getAllIPFSGateways();
    }
}
