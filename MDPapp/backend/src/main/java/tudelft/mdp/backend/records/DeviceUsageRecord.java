package tudelft.mdp.backend.records;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

@Entity
public class DeviceUsageRecord {

    @Id
    private Long id;
    @Index
    private String username;
    @Index
    private String deviceId;
    @Index
    private String deviceType;
    @Index
    private Double userTime;
    @Index
    private Integer timespan;

    public DeviceUsageRecord() {
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

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public Double getUserTime() {
        return userTime;
    }

    public void setUserTime(Double userTime) {
        this.userTime = userTime;
    }

    public Integer getTimespan() {
        return timespan;
    }

    public void setTimespan(Integer timespan) {
        this.timespan = timespan;
    }
}
