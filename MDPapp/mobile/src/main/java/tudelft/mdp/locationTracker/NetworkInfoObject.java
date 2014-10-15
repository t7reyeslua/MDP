package tudelft.mdp.locationTracker;


import java.io.Serializable;
import java.util.ArrayList;

public class NetworkInfoObject implements Serializable {


    public static final long serialVersionUID = 43L;

    public String SSID;
    public String BSSID;

    public Integer RSSI;
    public Integer count;

    public Double mean;
    public Double std;

    public ArrayList<Integer> RSSIarray = new ArrayList<Integer>();

    public NetworkInfoObject(){
    }

    public NetworkInfoObject(String SSID, String BSSID,
            Integer RSSI) {
        this.SSID = SSID;
        this.BSSID = BSSID;
        this.RSSI = RSSI;
        this.count = 1;
        this.std = 0.0;
    }

    public NetworkInfoObject(String SSID, String BSSID, Double mean, Double std,
            Integer count) {
        this.SSID = SSID;
        this.BSSID = BSSID;
        this.mean = mean;
        this.std = std;
        this.count = count;
    }

    public void addRSSI(Integer level){
        RSSIarray.add(level);
    }

    public ArrayList<Integer> getRSSIarray() {
        return RSSIarray;
    }


    public void setRSSIarray(ArrayList<Integer> RSSIarray) {
        this.RSSIarray = RSSIarray;
    }

    public Integer getRSSI() {
        return RSSI;
    }

    public void setRSSI(Integer RSSI) {
        this.RSSI = RSSI;
    }

    public Double getStd() {
        return std;
    }

    public void setStd(Double std) {
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

    public Double getMean() {
        return mean;
    }

    public void setMean(Double mean) {
        this.mean = mean;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

}
