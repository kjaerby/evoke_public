package moe.evoke.application.backend.hoster.ipfs;

import com.google.gson.Gson;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

import java.io.Serializable;

public class IPFSGateway implements Serializable {

    private long ID;
    private String address;
    private String apiAddress;
    private boolean isPublic;
    private String ipfsURL;
    private StatsRepoResponse statsRepoResponse;

    public IPFSGateway(long ID, String address, String apiAddress, boolean isPublic, String ipfsURL) {
        this.ID = ID;
        this.address = address;
        this.apiAddress = apiAddress;
        this.isPublic = isPublic;
        this.ipfsURL = ipfsURL;

        if (apiAddress != null && !apiAddress.isEmpty()) {
            new Thread(() -> {
                try {
                    String apiEndpoint = apiAddress + "stats/repo";
                    HttpResponse<String> response = Unirest.post(apiEndpoint).socketTimeout(30 * 60 * 1000).connectTimeout(30 * 60 * 1000).asString();

                    if (response.isSuccess()) {
                        Gson gson = new Gson();
                        statsRepoResponse = gson.fromJson(response.getBody(), StatsRepoResponse.class);
                    }
                } catch (Exception ex) {
                    this.statsRepoResponse = null;
                    ex.printStackTrace();
                }
            }).start();
        }
    }

    public long getID() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getApiAddress() {
        return apiAddress;
    }

    public void setApiAddress(String apiAddress) {
        this.apiAddress = apiAddress;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public String getIpfsURL() {
        return ipfsURL;
    }

    public void setIpfsURL(String ipfsURL) {
        this.ipfsURL = ipfsURL;
    }

    public StatsRepoResponse getStatsRepoResponse() {
        return statsRepoResponse;
    }

    public void setStatsRepoResponse(StatsRepoResponse statsRepoResponse) {
        this.statsRepoResponse = statsRepoResponse;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IPFSGateway gateway = (IPFSGateway) o;

        if (ID != gateway.ID) return false;
        if (isPublic != gateway.isPublic) return false;
        if (address != null ? !address.equals(gateway.address) : gateway.address != null) return false;
        if (apiAddress != null ? !apiAddress.equals(gateway.apiAddress) : gateway.apiAddress != null) return false;
        if (ipfsURL != null ? !ipfsURL.equals(gateway.ipfsURL) : gateway.ipfsURL != null) return false;
        return statsRepoResponse != null ? statsRepoResponse.equals(gateway.statsRepoResponse) : gateway.statsRepoResponse == null;
    }

    @Override
    public int hashCode() {
        int result = (int) (ID ^ (ID >>> 32));
        result = 31 * result + (address != null ? address.hashCode() : 0);
        result = 31 * result + (apiAddress != null ? apiAddress.hashCode() : 0);
        result = 31 * result + (isPublic ? 1 : 0);
        result = 31 * result + (ipfsURL != null ? ipfsURL.hashCode() : 0);
        result = 31 * result + (statsRepoResponse != null ? statsRepoResponse.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "IPFSGateway{" +
                "ID=" + ID +
                ", address='" + address + '\'' +
                ", apiAddress='" + apiAddress + '\'' +
                ", isPublic=" + isPublic +
                ", ipfsURL='" + ipfsURL + '\'' +
                ", statsRepoResponse=" + statsRepoResponse +
                '}';
    }
}
