package tudelft.mdp.weka;

import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import tudelft.mdp.enums.Constants;
import tudelft.mdp.fileManagement.FileCreator;

/**
 * Created by t7 on 20-10-14.
 */
public class WekaSensorsRawDataObject {


    private static final String LOGTAG = "WekaSensorsRawDataObject";
    private ArrayList<String> mSensorReadings = new ArrayList<String>();

    public WekaSensorsRawDataObject() {
    }

    public WekaSensorsRawDataObject(ArrayList<String> sensorReadings) {
        mSensorReadings = sensorReadings;
    }

    public ArrayList<String> getSensorReadings() {
        return mSensorReadings;
    }

    public void setSensorReadings(ArrayList<String> sensorReadings) {
        mSensorReadings = sensorReadings;
    }

    public HashMap<String, ArrayList<Double>> getSensorsArrays() {
        return mSensorsArrays;
    }

    public void setSensorsArrays(HashMap<String, ArrayList<Double>> sensorsArrays) {
        mSensorsArrays = sensorsArrays;
    }

    public void saveToFile(String event){
        if (mSensorReadings.size() == 0){
            return;
        }

        Log.i(LOGTAG, "saveToFile");

        FileCreator mFileCreator = new FileCreator("MOTION_" + event + "_", Constants.DIRECTORY_TRAINING);
        mFileCreator.openFileWriter();

        for (String record : mSensorReadings){
            mFileCreator.saveData(record + "\n");
        }

        mFileCreator.closeFileWriter();


    }

    public void buildSensorArrays(){
        for (String record : mSensorReadings){
            separateBySensors(record);
        }
    }

    private HashMap<String, ArrayList<Double>> mSensorsArrays = new HashMap<String, ArrayList<Double>>();
    public void addValueToSensorArray(String sensorName, Double value){
        if(!mSensorsArrays.containsKey(sensorName)){
            ArrayList<Double> array = new ArrayList<Double>();
            mSensorsArrays.put(sensorName, array);
        }
        ArrayList<Double> current = mSensorsArrays.get(sensorName);
        current.add(value);
        mSensorsArrays.put(sensorName, current);
    }



    public void separateBySensors(String record){
        String[] parts = record.split("\t");

        for ( int i = 0; i < parts.length; i++){
            String sensorName = getSensorName(i);
            addValueToSensorArray(sensorName, Double.valueOf(parts[i]));
        }
    }

    public String getSensorName(int i){
        String sensorName = "";

        switch (i){
            case 0:
                sensorName = Constants.SENSOR_ACCX;
                break;
            case 1:
                sensorName = Constants.SENSOR_ACCY;
                break;
            case 2:
                sensorName = Constants.SENSOR_ACCZ;
                break;
            case 3:
                sensorName = Constants.SENSOR_GYROX;
                break;
            case 4:
                sensorName = Constants.SENSOR_GYROY;
                break;
            case 5:
                sensorName = Constants.SENSOR_GYROZ;
                break;
            default:
                break;
        }


        return sensorName;
    }


}
