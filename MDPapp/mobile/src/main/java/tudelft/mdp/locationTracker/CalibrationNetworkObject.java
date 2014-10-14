package tudelft.mdp.locationTracker;


public class CalibrationNetworkObject {
    public String SSID;
    public String BSSID;
    public Float mean;
    public Integer count;

    public CalibrationNetworkObject(){
    }

    public CalibrationNetworkObject(String SSID, String BSSID, Float mean,
            Integer count) {
        this.SSID = SSID;
        this.BSSID = BSSID;
        this.mean = mean;
        this.count = count;
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
