package ft_test;


import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by t7 on 20-10-14.
 */
public class WekaSensorsRawDataObject {


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
                sensorName = Constants.SENSOR_LINE;
                break;
            case 1:
                sensorName = Constants.SENSOR_TIME;
                break;
            case 2:
                sensorName = Constants.SENSOR_ACCX;
                break;
            case 3:
                sensorName = Constants.SENSOR_ACCY;
                break;
            case 4:
                sensorName = Constants.SENSOR_ACCZ;
                break;
            case 5:
                sensorName = Constants.SENSOR_GYROX;
                break;
            case 6:
                sensorName = Constants.SENSOR_GYROX;
                break;
            case 7:
                sensorName = Constants.SENSOR_GYROX;
                break;
            case 8:
                sensorName = Constants.SENSOR_MAGX;
                break;
            case 9:
                sensorName = Constants.SENSOR_MAGY;
                break;
            case 10:
                sensorName = Constants.SENSOR_MAGZ;
                break;
            case 11:
                sensorName = Constants.SENSOR_LACCX;
                break;
            case 12:
                sensorName = Constants.SENSOR_LACCY;
                break;
            case 13:
                sensorName = Constants.SENSOR_LACCZ;
                break;
            case 14:
                sensorName = Constants.SENSOR_TILTX;
                break;
            case 15:
                sensorName = Constants.SENSOR_TILTY;
                break;
            case 16:
                sensorName = Constants.SENSOR_TILTZ;
                break;
            case 17:
                sensorName = Constants.SENSOR_ROTX;
                break;
            case 18:
                sensorName = Constants.SENSOR_ROTY;
                break;
            case 19:
                sensorName = Constants.SENSOR_ROTZ;
                break;     
                
            default:
                break;
        }


        return sensorName;
    }


}
