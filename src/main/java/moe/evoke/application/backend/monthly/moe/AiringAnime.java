package moe.evoke.application.backend.monthly.moe;

import moe.evoke.application.backend.db.Anime;
import moe.evoke.application.backend.db.Database;
import moe.evoke.application.backend.db.Episode;
import moe.evoke.application.backend.monthly.moe.calendar.EpisodesItem;
import moe.evoke.application.backend.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class AiringAnime {

    private static final Logger logger = LoggerFactory.getLogger(AiringAnime.class);
    private final List<AiringEpisode> airingEpisodeList;
    private Anime anime;

    public AiringAnime(Anime tmpAnime, long id, List<EpisodesItem> episodesItems) {
        this.anime = tmpAnime;
        this.airingEpisodeList = new ArrayList<>();

        final Anime oldAnime = tmpAnime;
        for (EpisodesItem airingEpisode : episodesItems) {
            if (airingEpisode.getAnimeId() == id) {
                Optional<Episode> animeEpisode = Optional.empty();
                if (oldAnime.getEpisodeCount() > 0 && airingEpisode.getNumber() > oldAnime.getEpisodeCount()) {
                    // PriChan fix: search for sequel which is airing...
                    logger.debug("Using PriChan fix for: " + oldAnime.getName());
                    Episode sequel = Utils.findSequelForEpisode(oldAnime, airingEpisode.getNumber());
                    if (sequel != null) {
                        this.anime = sequel.getAnime();
                        airingEpisode.setNumber(Math.toIntExact(sequel.getNumber()));
                        animeEpisode = Optional.of(sequel);

                        logger.info("Found sequel: " + sequel.getAnime().getName() + " - " + sequel.getNumber());
                    } else {
                        logger.warn("Could not find suitable sequel for: " + oldAnime.getName());
                    }
                } else {
                    animeEpisode = oldAnime.getEpisodes().stream().filter(episode1 -> episode1.getNumber() == airingEpisode.getNumber()).findFirst();
                    if (!animeEpisode.isPresent() && (oldAnime.getEpisodeCount() == 0 || airingEpisode.getNumber() <= oldAnime.getEpisodeCount())) {
                        Database.instance().createEpisode(oldAnime, airingEpisode.getNumber());
                        animeEpisode = oldAnime.getEpisodes().stream().filter(episode1 -> episode1.getNumber() == airingEpisode.getNumber()).findFirst();
                    }
                }


                if (animeEpisode.isPresent()) {
                    if (airingEpisode.getDatetime().contains("Z")) {
                        DateTimeFormatter timeFormatter = DateTimeFormatter.ISO_DATE_TIME;
                        OffsetDateTime offsetDateTime = OffsetDateTime.parse(airingEpisode.getDatetime(), timeFormatter);
                        Date date = Date.from(Instant.from(offsetDateTime));

                        airingEpisodeList.add(new AiringEpisode(animeEpisode.get(), date));
                    } else {
                        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        try {
                            airingEpisodeList.add(new AiringEpisode(animeEpisode.get(), dateFormat.parse(airingEpisode.getDatetime())));
                        } catch (ParseException e) {
                            logger.error("Could not parse '" + airingEpisode.getDatetime() + "' to date!");
                        }
                    }
                }
            }
        }
    }

    public Anime getAnime() {
        return anime;
    }

    public List<AiringEpisode> getAiringEpisodeList() {
        return airingEpisodeList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AiringAnime that = (AiringAnime) o;
        return Objects.equals(anime, that.anime) && Objects.equals(airingEpisodeList, that.airingEpisodeList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(anime, airingEpisodeList);
    }

    public static class AiringEpisode {

        private final Episode episode;
        private final Date airingDate;

        public AiringEpisode(Episode episode, Date airingDate) {
            this.episode = episode;
            this.airingDate = airingDate;
        }

        public Episode getEpisode() {
            return episode;
        }

        public Date getAiringDate() {
            return airingDate;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AiringEpisode that = (AiringEpisode) o;
            return Objects.equals(episode, that.episode) && Objects.equals(airingDate, that.airingDate);
        }

        @Override
        public int hashCode() {
            return Objects.hash(episode, airingDate);
        }
    }
}
