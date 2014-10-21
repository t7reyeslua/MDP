package tudelft.mdp.locationTracker;


import java.io.Serializable;
import java.util.ArrayList;

public class NetworkInfoObject implements Serializable {


    public static final long serialVersionUID = 43L;

    public String SSID;
    public String BSSID;

    public Double RSSI;
    public Integer count;

    public Double mean;
    public Double std;

    public ArrayList<Double> RSSIarray = new ArrayList<Double>();

    public NetworkInfoObject(){
    }

    public NetworkInfoObject(String SSID, String BSSID,
            Double RSSI) {
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

    public void addRSSI(Double level){
        RSSIarray.add(level);
    }

    public ArrayList<Double> getRSSIarray() {
        return RSSIarray;
    }


    public void setRSSIarray(ArrayList<Double> RSSIarray) {
        this.RSSIarray = RSSIarray;
    }

    public Double getRSSI() {
        return RSSI;
    }

    public void setRSSI(Double RSSI) {
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
