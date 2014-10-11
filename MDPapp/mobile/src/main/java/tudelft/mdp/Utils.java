package tudelft.mdp;


import android.hardware.Sensor;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import tudelft.mdp.enums.Constants;
import tudelft.mdp.enums.UserPreferences;

public class Utils {

    public static String getCurrentTimestamp(){
        // 1) create a java calendar instance
        Calendar calendar = Calendar.getInstance();
        // 2) get a java.util.Date from the calendar instance.
        //    this date will represent the current instant, or "now".
        java.util.Date now = calendar.getTime();
        // 3) a java current time (now) instance
        java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(now.getTime());
        return new SimpleDateFormat("yyyyMMddHHmmss").format(currentTimestamp);
    }

    public static String getSensorName(int sensorType){
        String name = "Other";

        switch (sensorType){
            case Sensor.TYPE_ACCELEROMETER:
                name = "Accelerometer";
                break;
            case Sensor.TYPE_GYROSCOPE:
                name = "Gyroscope";
                break;
            case Sensor.TYPE_GRAVITY:
                name = "Gravity";
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                name = "Magnetic Field";
                break;
            case Sensor.TYPE_STEP_COUNTER:
                name = "Step Counter";
                break;
            case Sensor.TYPE_HEART_RATE:
                name = "Heart Rate";
                break;
            case Constants.SAMSUNG_HEART_RATE:
                name = "Heart Rate";
                break;
            case Constants.SAMSUNG_TILT:
                name = "Tilt";
                break;
            case Sensor.TYPE_LINEAR_ACCELERATION:
                name = "Linear Acceleration";
                break;
            case Sensor.TYPE_STEP_DETECTOR:
                name = "Step Detector";
                break;
            case Sensor.TYPE_SIGNIFICANT_MOTION:
                name = "Significant Motion";
                break;
            case Sensor.TYPE_ROTATION_VECTOR:
                name = "Rotation Vector";
                break;
            default:
                break;
        }

        return name;
    }
    public static Double getMinTimestamp(int mode) {
        Double minDate = 0.0;

        if (mode == UserPreferences.ALLTIME){
            minDate = 0.0;
        }

        if (mode == UserPreferences.YEAR){
            minDate = 0.0;
        }

        if (mode == UserPreferences.MONTH){
            minDate = 0.0;
        }

        if (mode == UserPreferences.WEEK){
            minDate = 0.0;
        }

        if (mode == UserPreferences.TODAY){
            minDate = 0.0;
        }

        return minDate;
    }

}
