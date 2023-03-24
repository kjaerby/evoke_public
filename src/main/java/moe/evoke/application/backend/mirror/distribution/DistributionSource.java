package moe.evoke.application.backend.mirror.distribution;

import moe.evoke.application.backend.db.Anime;
import moe.evoke.application.backend.malsync.MALSync;
import moe.evoke.application.backend.malsync.MALSyncProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public enum DistributionSource {
    MANUAL,
    GOGO,
    MEGA,
    TWIST,
    CRUNCHYROLL,
    TORRENT;


    public static final String MAGNET_LINK = "magnet.link";

    public static List<DistributionSource> availableModes(Anime anime) {
        List<DistributionSource> availableModes = new ArrayList<>();
        for (DistributionSource mode : DistributionSource.values()) {
            if (mode.isAvailable(anime)) {
                availableModes.add(mode);
            }
        }

        return availableModes;
    }

    public boolean isAvailable(Anime anime) {
        if (this == DistributionSource.MANUAL || this == TORRENT || this == MEGA)
            return true;

        List<MALSyncProvider> providerList = MALSync.getProviderForAnime(anime);
        if (providerList == null)
            return false;

        Optional<MALSyncProvider> result = providerList.stream().filter(malSyncProvider -> malSyncProvider.getProvider().toLowerCase().contains(this.toString().toLowerCase())).findFirst();
        return result.isPresent();
    }

}
