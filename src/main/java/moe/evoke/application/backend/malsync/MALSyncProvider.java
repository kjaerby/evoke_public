package moe.evoke.application.backend.malsync;

import java.util.Objects;

public class MALSyncProvider {

    public String provider;
    public String identifier;
    public String url;

    @Override
    public String toString() {
        return "MALSyncProvider{" +
                "provider='" + provider + '\'' +
                ", identifier='" + identifier + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MALSyncProvider that = (MALSyncProvider) o;
        return Objects.equals(provider, that.provider) && Objects.equals(identifier, that.identifier) && Objects.equals(url, that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(provider, identifier, url);
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
