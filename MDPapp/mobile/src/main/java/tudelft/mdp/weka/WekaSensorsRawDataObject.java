package tudelft.mdp.weka;

import android.hardware.Sensor;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import tudelft.mdp.enums.Constants;
import tudelft.mdp.fileManagement.FileCreator;
import tudelft.mdp.utils.Utils;

/**
 * Created by t7 on 20-10-14.
 */
public class WekaSensorsRawDataObject {


    private static final String LOGTAG = "WekaSensorsRawDataObject";
    private ArrayList<String> mSensorReadings = new ArrayList<String>();
    private HashMap<Integer, ArrayList<String>> mRecordedSensorsHM = new HashMap<Integer, ArrayList<String>>();
    private HashMap<String, ArrayList<Double>> mSensorsArrays = new HashMap<String, ArrayList<Double>>();
    private boolean timestampRecorded = false;

    public WekaSensorsRawDataObject() {
    }

    public WekaSensorsRawDataObject(ArrayList<String> sensorReadings) {
        mSensorReadings = sensorReadings;
        mRecordedSensorsHM.clear();
        timestampRecorded = false;
    }

    public WekaSensorsRawDataObject(
            HashMap<Integer, ArrayList<String>> recordedSensorsHM) {
        mRecordedSensorsHM = recordedSensorsHM;
        mSensorReadings.clear();
        timestampRecorded = false;
    }

    public HashMap<Integer, ArrayList<String>> getRecordedSensorsHM() {
        return mRecordedSensorsHM;
    }

    public void setRecordedSensorsHM(HashMap<Integer, ArrayList<String>> recordedSensorsHM) {
        mRecordedSensorsHM = recordedSensorsHM;
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

    public void saveToFile(String event, boolean consolidated){

        if (consolidated) {
            if (mSensorReadings.size() == 0) {
                return;
            }
        } else {
            if (mRecordedSensorsHM.size() == 0){
                return;
            }
        }
        Log.i(LOGTAG, "saveToFile");

        FileCreator mFileCreator;
        String motionFeatures = getFeatures(10000, consolidated);
        if (motionFeatures.length() > 0) {
            mFileCreator = new FileCreator("MOTION_" + event + "_", Constants.DIRECTORY_TRAINING);
            mFileCreator.openFileWriter();
            mFileCreator.saveData(motionFeatures);
            mFileCreator.closeFileWriter();
        }

        mFileCreator = new FileCreator("RAW_" + event + "_", Constants.DIRECTORY_TRAINING);
        mFileCreator.openFileWriter();
        mFileCreator.saveData("=======================" + "RAW DATA" + "=======================\n");

        if (consolidated) {
            for (String record : mSensorReadings) {
                mFileCreator.saveData(record + "\n");
            }
        } else {
            for (Integer sensorType : mRecordedSensorsHM.keySet()){
                Log.i(LOGTAG, "saving To File:" + Utils.getSensorName(sensorType) + " Records: " + mRecordedSensorsHM.get(sensorType).size());
                mFileCreator.saveData("#####################" + Utils.getSensorName(sensorType) + "#####################\n");
                for (String record : mRecordedSensorsHM.get(sensorType)){
                    mFileCreator.saveData(record + "\n");
                }
            }
        }
        mFileCreator.closeFileWriter();



    }

    /**
     * Separates the string sensor readings into each axis values
     * @param consolidated True if all sensors readings are in a single line.
     *                     False if there is a different string array for each sensor type.
     */
    public void buildSensorArrays(boolean consolidated){
        mSensorsArrays.clear();
        if (consolidated) {
            Log.i(LOGTAG, "buildSensorArrays: Consolidated");
            for (String record : mSensorReadings) {
                separateBySensors(record, true, 0);
            }
        } else {
            Log.i(LOGTAG, "buildSensorArrays: Not consolidated");
            for (Integer sensorType : mRecordedSensorsHM.keySet()){
                Log.i(LOGTAG, "buildSensorArrays:" + Utils.getSensorName(sensorType) + " Records:" + mRecordedSensorsHM.get(sensorType).size());
                for (String record : mRecordedSensorsHM.get(sensorType)){
                    separateBySensors(record, false, sensorType);
                }
                if (sensorType.equals(Sensor.TYPE_ACCELEROMETER)){
                    timestampRecorded = true;
                    Log.i(LOGTAG, "buildSensorArrays: timestampRecorded");
                }
            }
            timestampRecorded = false;
            verifyTiltAxisExist();

        }

    }

    public void verifyTiltAxisExist(){
        if (!mSensorsArrays.containsKey(Constants.SENSOR_TILTX)){
            Log.i(LOGTAG, "Adding " + Constants.SENSOR_TILTX );
            ArrayList<Double> array = new ArrayList<Double>();
            array.add(0.0);
            mSensorsArrays.put(Constants.SENSOR_TILTX, array);
        }
        if (!mSensorsArrays.containsKey(Constants.SENSOR_TILTY)){
            Log.i(LOGTAG, "Adding " + Constants.SENSOR_TILTY );
            ArrayList<Double> array = new ArrayList<Double>();
            array.add(0.0);
            mSensorsArrays.put(Constants.SENSOR_TILTY, array);
        }
        if (!mSensorsArrays.containsKey(Constants.SENSOR_TILTZ)){
            Log.i(LOGTAG, "Adding " + Constants.SENSOR_TILTZ );
            ArrayList<Double> array = new ArrayList<Double>();
            array.add(0.0);
            mSensorsArrays.put(Constants.SENSOR_TILTZ, array);
        }
    }



    public void addValueToSensorArray(String sensorName, Double value){
        if(!mSensorsArrays.containsKey(sensorName)){
            ArrayList<Double> array = new ArrayList<Double>();
            mSensorsArrays.put(sensorName, array);
        }
        ArrayList<Double> current = mSensorsArrays.get(sensorName);
        if (!sensorName.equals(Constants.SENSOR_TIME)) {
            current.add(value);
            mSensorsArrays.put(sensorName, current);
        } else {
            if (!timestampRecorded){
                current.add(value);
                mSensorsArrays.put(sensorName, current);
            }
        }
    }


    public void separateBySensors(String record, boolean consolidated, int sensorType){
        String[] parts = record.split("\t");
        for ( int i = 0; i < parts.length; i++){
            String sensorName = getSensorName(i, consolidated, sensorType);
            addValueToSensorArray(sensorName, Double.valueOf(parts[i]));
        }
    }

    public String getSensorName(int position, boolean consolidated, int sensorType){
        String sensorName = "";

        if (consolidated) {
            switch (position) {
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
                    sensorName = Constants.SENSOR_GYROY;
                    break;
                case 7:
                    sensorName = Constants.SENSOR_GYROZ;
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
        } else {
            switch (sensorType){
                case Sensor.TYPE_ACCELEROMETER:
                    switch (position) {
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
                        default:
                            break;
                    }
                    break;
                case Sensor.TYPE_GYROSCOPE:
                    switch (position) {
                        case 0:
                            sensorName = Constants.SENSOR_LINE;
                            break;
                        case 1:
                            sensorName = Constants.SENSOR_TIME;
                            break;
                        case 2:
                            sensorName = Constants.SENSOR_GYROX;
                            break;
                        case 3:
                            sensorName = Constants.SENSOR_GYROY;
                            break;
                        case 4:
                            sensorName = Constants.SENSOR_GYROZ;
                            break;
                        default:
                            break;
                    }
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    switch (position) {
                        case 0:
                            sensorName = Constants.SENSOR_LINE;
                            break;
                        case 1:
                            sensorName = Constants.SENSOR_TIME;
                            break;
                        case 2:
                            sensorName = Constants.SENSOR_MAGX;
                            break;
                        case 3:
                            sensorName = Constants.SENSOR_MAGY;
                            break;
                        case 4:
                            sensorName = Constants.SENSOR_MAGZ;
                            break;
                        default:
                            break;
                    }
                    break;
                case Constants.SAMSUNG_TILT:
                    switch (position) {
                        case 0:
                            sensorName = Constants.SENSOR_LINE;
                            break;
                        case 1:
                            sensorName = Constants.SENSOR_TIME;
                            break;
                        case 2:
                            sensorName = Constants.SENSOR_TILTX;
                            break;
                        case 3:
                            sensorName = Constants.SENSOR_TILTY;
                            break;
                        case 4:
                            sensorName = Constants.SENSOR_TILTZ;
                            break;
                        default:
                            break;
                    }
                    break;
                case Sensor.TYPE_LINEAR_ACCELERATION:
                    switch (position) {
                        case 0:
                            sensorName = Constants.SENSOR_LINE;
                            break;
                        case 1:
                            sensorName = Constants.SENSOR_TIME;
                            break;
                        case 2:
                            sensorName = Constants.SENSOR_LACCX;
                            break;
                        case 3:
                            sensorName = Constants.SENSOR_LACCY;
                            break;
                        case 4:
                            sensorName = Constants.SENSOR_LACCZ;
                            break;
                        default:
                            break;
                    }
                    break;
                case Sensor.TYPE_ROTATION_VECTOR:
                    switch (position) {
                        case 0:
                            sensorName = Constants.SENSOR_LINE;
                            break;
                        case 1:
                            sensorName = Constants.SENSOR_TIME;
                            break;
                        case 2:
                            sensorName = Constants.SENSOR_ROTX;
                            break;
                        case 3:
                            sensorName = Constants.SENSOR_ROTY;
                            break;
                        case 4:
                            sensorName = Constants.SENSOR_ROTZ;
                            break;
                        default:
                            break;
                    }
                    break;
                default:
                    break;
            }


        }

        return sensorName;
    }

    public String getAxisName(int position){
        String sensorName = "";
        switch (position) {
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
                sensorName = Constants.SENSOR_GYROY;
                break;
            case 7:
                sensorName = Constants.SENSOR_GYROZ;
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


    /**
     * @author Luis Gonzalez
     * @version 1, 29/10/14
     *
     * @brief it returns a string (line) with all the features for motion sensors
     */
    public String getFeatures(int nSamples, boolean consolidated){
        Log.i(LOGTAG, "Getting Features");
        if (mRecordedSensorsHM.size() == 0){
            return "";
        }

        buildSensorArrays(consolidated);
        for (String t : mSensorsArrays.keySet()){
            Log.i(LOGTAG, "Array: " + t + " size: " + mSensorsArrays.get(t).size());
        }


        String features="";
        String Precision=Constants.FT_PRESICION;
        ArrayList<Double> tArray = new ArrayList<Double>();
        ArrayList<Double> xArray = new ArrayList<Double>();
        ArrayList<Double> yArray = new ArrayList<Double>();
        ArrayList<Double> zArray = new ArrayList<Double>();
        ArrayList<Double> xFilArray = new ArrayList<Double>();
        ArrayList<Double> yFilArray = new ArrayList<Double>();
        ArrayList<Double> zFilArray = new ArrayList<Double>();
        double UL=Constants.STEP_UP_THRESHOLD;
        double LL=Constants.STEP_DOWN_THRESHOLD;
        double SampleFreq=0;
        double x,y,z;

        tArray=mSensorsArrays.get(Constants.SENSOR_TIME);
        SampleFreq=SensorFeatures.GetSampleFreq(tArray);



        for(int i=2;i<mSensorsArrays.size()-2;i=i+3){

            //features += getAxisName(i) + "\t";
            Log.i(LOGTAG, "i:"+ i + " AxisName:" + getAxisName(i)
                    + " AxisName:" + getAxisName(i + 1)
                    + " AxisName:" + getAxisName(i + 2));


            xArray=mSensorsArrays.get(getAxisName(i));
            yArray=mSensorsArrays.get(getAxisName(i + 1));
            zArray=mSensorsArrays.get(getAxisName(i + 2));

            if (xArray == null){
                xArray = new ArrayList<Double>();
                xArray.add(0.0);
            }
            if (yArray == null){
                yArray = new ArrayList<Double>();
                yArray.add(0.0);
            }
            if (zArray == null){
                zArray = new ArrayList<Double>();
                zArray.add(0.0);
            }

            int tmpsize=xArray.size()-nSamples;

            //TODO make possible to change the sample number nSamples,

            for(int r=1;r<=tmpsize;r++){
                xArray.remove(xArray.size()-r);
                yArray.remove(yArray.size()-r);
                zArray.remove(zArray.size()-r);
            }

            //Time domain fts
            x = SensorFeatures.Mean(xArray);
            y = SensorFeatures.Mean(yArray);
            z = SensorFeatures.Mean(zArray);


            features = features+String.format(Precision,x)+",";
            features = features+String.format(Precision,y)+",";
            features = features+String.format(Precision,z)+",";
            features = features+String.format(Precision,SensorFeatures.Magnitud(x,y,z))+",";

            x = SensorFeatures.StandardDeviation(xArray);
            y = SensorFeatures.StandardDeviation(yArray);
            z = SensorFeatures.StandardDeviation(zArray);

            features = features+String.format(Precision,x)+",";
            features = features+String.format(Precision,y)+",";
            features = features+String.format(Precision,z)+",";
            features = features+String.format(Precision,SensorFeatures.Magnitud(x,y,z))+",";

            x = SensorFeatures.Variance(xArray);
            y = SensorFeatures.Variance(yArray);
            z = SensorFeatures.Variance(zArray);

            features = features+String.format(Precision,x)+",";
            features = features+String.format(Precision,y)+",";
            features = features+String.format(Precision,z)+",";
            features = features+String.format(Precision,SensorFeatures.Magnitud(x,y,z))+",";

            //freq. domain fts.
            features = features+String.format(Precision,SensorFeatures.FirstComponentFFT(xArray, SampleFreq))+",";
            features = features+String.format(Precision,SensorFeatures.FirstComponentFFT(yArray, SampleFreq))+",";
            features = features+String.format(Precision,SensorFeatures.FirstComponentFFT(zArray, SampleFreq))+",";

            features = features+String.valueOf(SensorFeatures.ZeroXing(SensorFeatures.ZeroNormal(xArray)))+",";
            features = features+String.valueOf(SensorFeatures.ZeroXing(SensorFeatures.ZeroNormal(yArray)))+",";
            features = features+String.valueOf(SensorFeatures.ZeroXing(SensorFeatures.ZeroNormal(zArray)))+",";

            /**
             *
             * Maybe, maybe is computationally too much
             *
             * **/
            xFilArray =XCounter.ButtFilterArray(SensorFeatures.ZeroNormal(xArray),2,Constants.CUT_OFF_FREQ,SampleFreq);
            yFilArray =XCounter.ButtFilterArray(SensorFeatures.ZeroNormal(yArray),2,Constants.CUT_OFF_FREQ,SampleFreq);
            zFilArray =XCounter.ButtFilterArray(SensorFeatures.ZeroNormal(zArray),2,Constants.CUT_OFF_FREQ,SampleFreq);

            //**This might need to be filtered!!!**/
            features = features+String.valueOf(XCounter.stepCounter(xFilArray,UL,LL))+",";
            features = features+String.valueOf(XCounter.stepCounter(yFilArray,UL,LL))+",";
            features = features+String.valueOf(XCounter.stepCounter(zFilArray,UL,LL))+",";


            //features += "\n";


        }


        return deletelastchar(features,',');
    }

    /**
     * @author Luis Gonzalez
     * @version 1, 30/10/14
     *
     * @brief it returns a Arraylist of strings (line) with all attributes for printing
     */
    public ArrayList<String> getAttributes(){
        ArrayList<String> AttributesList = new ArrayList<String>();



        for(int i=2;i<(18)+2;i=i+3){
//    		for(int i=2;i<18;i++){

            String[] sensornameX = getAxisName(i).split("_");
            String[] sensornameY = getAxisName(i + 1).split("_");
            String[] sensornameZ = getAxisName(i + 2).split("_");

            AttributesList.add("@attribute mean_"+sensornameX[1]+sensornameX[2]+" numeric");
            AttributesList.add("@attribute mean_"+sensornameY[1]+sensornameY[2]+" numeric");
            AttributesList.add("@attribute mean_"+sensornameZ[1]+sensornameZ[2]+" numeric");
            AttributesList.add("@attribute meanMag_"+sensornameX[1]+sensornameX[2]+sensornameY[2]+sensornameZ[2]+" numeric");

            AttributesList.add("@attribute std_"+sensornameX[1]+sensornameX[2]+" numeric");
            AttributesList.add("@attribute std_"+sensornameY[1]+sensornameY[2]+" numeric");
            AttributesList.add("@attribute std_"+sensornameZ[1]+sensornameZ[2]+" numeric");
            AttributesList.add("@attribute stdMag_"+sensornameX[1]+sensornameX[2]+sensornameY[2]+sensornameZ[2]+" numeric");

            AttributesList.add("@attribute var_"+sensornameX[1]+sensornameX[2]+" numeric");
            AttributesList.add("@attribute var_"+sensornameY[1]+sensornameY[2]+" numeric");
            AttributesList.add("@attribute var_"+sensornameZ[1]+sensornameZ[2]+" numeric");
            AttributesList.add("@attribute varMag_"+sensornameX[1]+sensornameX[2]+sensornameY[2]+sensornameZ[2]+" numeric");

            AttributesList.add("@attribute FundF_"+sensornameX[1]+sensornameX[2]+" numeric");
            AttributesList.add("@attribute FundF_"+sensornameY[1]+sensornameY[2]+" numeric");
            AttributesList.add("@attribute FundF_"+sensornameZ[1]+sensornameZ[2]+" numeric");

            AttributesList.add("@attribute ZXing_"+sensornameX[1]+sensornameX[2]+" numeric");
            AttributesList.add("@attribute ZXing_"+sensornameY[1]+sensornameY[2]+" numeric");
            AttributesList.add("@attribute ZXing_"+sensornameZ[1]+sensornameZ[2]+" numeric");

            AttributesList.add("@attribute SCount_"+sensornameX[1]+sensornameX[2]+" numeric");
            AttributesList.add("@attribute SCount_"+sensornameY[1]+sensornameY[2]+" numeric");
            AttributesList.add("@attribute SCount_"+sensornameZ[1]+sensornameZ[2]+" numeric");

        }


        return AttributesList;
    }

    /**
     * @author Luis Gonzalez
     * @brief Based on the code of:
     * http://stackoverflow.com/questions/7438612/how-to-remove-the-last-character-from-a-string
     * @return deletes the last char character of a string, to take away the last coma of the ft string if needed
     */
    public static String deletelastchar(String str,char x) {
        if (str.length() > 0 && str.charAt(str.length()-1)==x) {
            str = str.substring(0, str.length()-1);
        }
        return str;
    }


}
