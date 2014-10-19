package tudelft.mdp.backend.records;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Entity
public class DeviceMotionLocationRecord {

    @Id
    private Long id;

    @Index
    private String deviceId;

    @Index
    private String deviceName;

    ArrayList<String> sensorValues = new ArrayList<String>();
    ArrayList<String> networkValues = new ArrayList<String>();

    public DeviceMotionLocationRecord() {
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public ArrayList<String> getSensorValues() {
        return sensorValues;
    }

    public void setSensorValues(ArrayList<String> sensorValues) {
        this.sensorValues = sensorValues;
    }

    public ArrayList<String> getNetworkValues() {
        return networkValues;
    }

    public void setNetworkValues(ArrayList<String> networkValues) {
        this.networkValues = networkValues;
    }
}
