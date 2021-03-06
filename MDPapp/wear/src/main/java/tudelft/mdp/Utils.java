package tudelft.mdp;


import android.hardware.Sensor;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import tudelft.mdp.enums.Constants;

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

    public static String getCurrentTimeOfDay(){
        Calendar calendar = Calendar.getInstance();
        java.util.Date now = calendar.getTime();
        java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(now.getTime());

        Integer hourOfDay =  Integer.valueOf(new SimpleDateFormat("HH").format(currentTimestamp));

        String timeOfDay;
        if (hourOfDay < 6){
            timeOfDay = "A";
        } else if (hourOfDay < 12){
            timeOfDay = "B";
        } else if (hourOfDay < 18){
            timeOfDay = "C";
        } else {
            timeOfDay = "D";
        }

        return timeOfDay;
    }
    public static int getSensorLength(int sensorType){
        int length = 0;

        switch (sensorType){
            case Sensor.TYPE_ACCELEROMETER:
                length = 3;
                break;
            case Sensor.TYPE_GYROSCOPE:
                length = 3;
                break;
            case Sensor.TYPE_ORIENTATION:
                length = 3;
                break;
            case Sensor.TYPE_GAME_ROTATION_VECTOR:
                //TODO: change to 5
                length = 3;
                break;
            case Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED:
                length = 3;
                break;
            case Sensor.TYPE_GRAVITY:
                length = 3;
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                length = 3;
                break;
            case Sensor.TYPE_STEP_COUNTER:
                length = 1;
                break;
            case Sensor.TYPE_HEART_RATE:
                length = 3;
                break;
            case Constants.SAMSUNG_HEART_RATE:
                length = 3;
                break;
            case Constants.SAMSUNG_TILT:
                length = 3;
                break;
            case Sensor.TYPE_LINEAR_ACCELERATION:
                length = 3;
                break;
            case Sensor.TYPE_STEP_DETECTOR:
                length = 1;
                break;
            case Sensor.TYPE_SIGNIFICANT_MOTION:
                length = 1;
                break;
            case Sensor.TYPE_ROTATION_VECTOR:
                //TODO: change to 5
                length = 3;
                break;
            default:
                break;
        }

        return length;
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
            case Sensor.TYPE_ORIENTATION:
                name = "Orientation";
                break;
            case Sensor.TYPE_GAME_ROTATION_VECTOR:
                name = "Game Rotation";
                break;
            case Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED:
                name = "Magnetic Field Uncalibrated";
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



}
