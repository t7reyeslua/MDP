package tudelft.mdp.backend.records;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.util.ArrayList;

@Entity
public class SensorFingerprintRecord {

    @Id
    Long id;

    @Index
    Integer sensorType;

    @Index
    String sensorName;

    ArrayList<Float> values = new ArrayList<Float>();

    public SensorFingerprintRecord() {
    }

    public Integer getSensorType() {
        return sensorType;
    }

    public void setSensorType(Integer sensorType) {
        this.sensorType = sensorType;
    }

    public String getSensorName() {
        return sensorName;
    }

    public void setSensorName(String sensorName) {
        this.sensorName = sensorName;
    }

    public ArrayList<Float> getValues() {
        return values;
    }

    public void setValues(ArrayList<Float> values) {
        this.values = values;
    }
}
