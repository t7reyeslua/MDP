package tudelft.mdp.backend.records;

import com.google.appengine.api.datastore.Text;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.util.ArrayList;

@Entity
public class DeviceMotionLocationRecord {

    @Id
    private Long id;

    @Index
    private String username;

    @Index
    private String event;

    @Index
    private String deviceType;

    @Index
    private String deviceId;

    @Index
    private String timestamp;

    private Text motionFeatures;
    private Text locationFeatures;


    public DeviceMotionLocationRecord() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Text getMotionFeatures() {
        return motionFeatures;
    }

    public void setMotionFeatures(Text motionFeatures) {
        this.motionFeatures = motionFeatures;
    }

    public Text getLocationFeatures() {
        return locationFeatures;
    }

    public void setLocationFeatures(Text locationFeatures) {
        this.locationFeatures = locationFeatures;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
