package moe.evoke.application.backend.db;

import java.util.Date;
import java.util.Objects;

public class News {

    private long ID;
    private String title;
    private Date dateOfPost;
    private NewsType newsType;

    private String content;
    private Anime anime;
    private Episode episode;

    @Override
    public String toString() {
        return "News{" +
                "ID=" + ID +
                ", title='" + title + '\'' +
                ", dateOfPost=" + dateOfPost +
                ", newsType=" + newsType +
                ", content='" + content + '\'' +
                ", anime=" + anime +
                ", episode=" + episode +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        News news = (News) o;
        return ID == news.ID && Objects.equals(title, news.title) && Objects.equals(dateOfPost, news.dateOfPost) && newsType == news.newsType && Objects.equals(content, news.content) && Objects.equals(anime, news.anime) && Objects.equals(episode, news.episode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ID, title, dateOfPost, newsType, content, anime, episode);
    }

    public NewsType getNewsType() {
        return newsType;
    }

    public void setNewsType(NewsType newsType) {
        this.newsType = newsType;
    }

    public Anime getAnime() {
        if (anime == null && episode != null)
            return episode.getAnime();

        return anime;
    }

    public void setAnime(Anime anime) {
        this.anime = anime;
    }

    public Episode getEpisode() {
        return episode;
    }

    public void setEpisode(Episode episode) {
        this.episode = episode;
    }

    public long getID() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getDateOfPost() {
        return dateOfPost;
    }

    public void setDateOfPost(Date dateOfPost) {
        this.dateOfPost = dateOfPost;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
