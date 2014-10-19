package tudelft.mdp.backend.records;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

import java.util.ArrayList;

@Entity
public class SensorFingerprintRecordWrapper {


    ArrayList<SensorFingerprintRecord> mSensorFingerprintRecordWrapperArrayList = new ArrayList<SensorFingerprintRecord>();

    public SensorFingerprintRecordWrapper() {
    }

    public ArrayList<SensorFingerprintRecord> getSensorFingerprintRecordWrapperArrayList() {
        return mSensorFingerprintRecordWrapperArrayList;
    }

    public void setSensorFingerprintRecordWrapperArrayList(
            ArrayList<SensorFingerprintRecord> sensorFingerprintRecordWrapperArrayList) {
        mSensorFingerprintRecordWrapperArrayList = sensorFingerprintRecordWrapperArrayList;
    }

    public void addSensorFingerprintRecor(SensorFingerprintRecord sensorFingerprintRecord){
        mSensorFingerprintRecordWrapperArrayList.add(sensorFingerprintRecord);
    }

    public void clearSensorFingerprintRecordWrapperArrayList(){
        mSensorFingerprintRecordWrapperArrayList.clear();
    }

}
