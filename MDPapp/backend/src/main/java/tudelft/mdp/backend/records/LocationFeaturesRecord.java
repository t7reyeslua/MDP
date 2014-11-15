package tudelft.mdp.backend.records;

import com.google.appengine.api.datastore.Text;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

@Entity
public class LocationFeaturesRecord {

    @Id
    private Long id;

    @Index
    private String username;

    @Index
    private String place;

    @Index
    private String zone;

    @Index
    private String timestamp;

    private Text locationFeatures;

    public LocationFeaturesRecord() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Text getLocationFeatures() {
        return locationFeatures;
    }

    public void setLocationFeatures(Text locationFeatures) {
        this.locationFeatures = locationFeatures;
    }
}
