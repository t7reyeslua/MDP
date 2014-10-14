package tudelft.mdp.locationTracker;


import java.io.Serializable;

public class NetworkInfoObject implements Serializable {


    public static final long serialVersionUID = 43L;

    public String SSID;
    public String BSSID;

    public Integer RSSI;
    public Integer count;

    public Float mean;
    public Float std;

    public NetworkInfoObject(){
    }

    public NetworkInfoObject(String SSID, String BSSID,
            Integer RSSI) {
        this.SSID = SSID;
        this.BSSID = BSSID;
        this.RSSI = RSSI;
        this.count = 1;
    }

    public NetworkInfoObject(String SSID, String BSSID, Float mean,
            Integer count) {
        this.SSID = SSID;
        this.BSSID = BSSID;
        this.mean = mean;
        this.count = count;
    }

    public Integer getRSSI() {
        return RSSI;
    }

    public void setRSSI(Integer RSSI) {
        this.RSSI = RSSI;
    }

    public Float getStd() {
        return std;
    }

    public void setStd(Float std) {
        this.std = std;
    }

    public String getSSID() {
        return SSID;
    }

    public void setSSID(String SSID) {
        this.SSID = SSID;
    }

    public String getBSSID() {
        return BSSID;
    }

    public void setBSSID(String BSSID) {
        this.BSSID = BSSID;
    }

    public Float getMean() {
        return mean;
    }

    public void setMean(Float mean) {
        this.mean = mean;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

}
