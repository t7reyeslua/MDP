package tudelft.mdp;

import java.util.ArrayList;

/**
 * Created by t7 on 31-10-14.
 */
public class MySensorEventObject {

    private Long timestamp;
    private Integer sensorType;
    private ArrayList<Float> values = new ArrayList<Float>();

    public MySensorEventObject() {

    }

    public MySensorEventObject(Long timestamp, Integer sensorType,
            float[] values) {
        this.timestamp = timestamp;
        this.sensorType = sensorType;
        for (float value : values) {
            this.values.add(value);
        }
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getSensorType() {
        return sensorType;
    }

    public void setSensorType(Integer sensorType) {
        this.sensorType = sensorType;
    }

    public ArrayList<Float> getValues() {
        return values;
    }

    public float[] getValuesArray() {
        float[] floatArray = new float[values.size()];
        int i = 0;

        for (Float f : values) {
            floatArray[i++] = (f != null ? f : Float.NaN); // Or whatever default you want.
        }

        return floatArray;
    }

    public void setValues(ArrayList<Float> values) {
        this.values = values;
    }
}
