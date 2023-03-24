package moe.evoke.application.backend.discord;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.NewsChannel;
import moe.evoke.application.backend.db.Anime;
import moe.evoke.application.backend.db.Episode;

public class Discord {

    private static final GatewayDiscordClient client;

    static {
        final String token = "";
        client = DiscordClientBuilder.create(token)
                .build()
                .login()
                .block();
    }

    public static void announceEpisode(Anime anime, Episode episode) {
        NewsChannel channel = client.getChannelById(Snowflake.of("<>")).cast(NewsChannel.class).block();

        channel.createEmbed(embedCreateSpec -> {
            embedCreateSpec.setImage(anime.getCover());
            embedCreateSpec.setTitle(anime.getName());
            embedCreateSpec.setDescription("Added Episode " + episode.getNumber());
            embedCreateSpec.setUrl("https://<>/watch/" + anime.getAnilistID() + "/" + episode.getNumber());
        }).block().publish();

    }
}
