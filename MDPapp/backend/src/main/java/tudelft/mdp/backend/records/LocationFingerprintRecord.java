package tudelft.mdp.backend.records;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;


@Entity
public class LocationFingerprintRecord {
    @Id
    private Long id;

    @Index
    private String ssid;

    @Index
    private String bssid;

    @Index
    private String place;

    @Index
    private String zone;

    @Index
    private Double rssi;

    @Index
    private String timeOfDay;

    public LocationFingerprintRecord() {
    }

    public void buildLocationFingerprintRecord(String ssid, String bssid, String place, String zone,
            Double rssi, String timeOfDay) {
        this.ssid = ssid;
        this.bssid = bssid;
        this.place = place;
        this.zone = zone;
        this.rssi = rssi;
        this.timeOfDay = timeOfDay;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getBssid() {
        return bssid;
    }

    public void setBssid(String bssid) {
        this.bssid = bssid;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public Double getRssi() {
        return rssi;
    }

    public void setRssi(Double rssi) {
        this.rssi = rssi;
    }

    public String getTimeOfDay() {
        return timeOfDay;
    }

    public void setTimeOfDay(String timeOfDay) {
        this.timeOfDay = timeOfDay;
    }
}
