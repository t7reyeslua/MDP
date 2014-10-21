package tudelft.mdp.weka;

import java.io.File;
import java.util.ArrayList;

import tudelft.mdp.enums.Constants;
import tudelft.mdp.fileManagement.FileCreator;

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

    public void saveToFile(String event){
        if (mSensorReadings.size() == 0){
            return;
        }

        FileCreator mFileCreator = new FileCreator("MOTION_" + event + "_", Constants.DIRECTORY_TRAINING);
        mFileCreator.openFileWriter();

        for (String record : mSensorReadings){
            mFileCreator.saveData(record + "\n");
        }

        mFileCreator.closeFileWriter();


    }


}
