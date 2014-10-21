package tudelft.mdp.utils;


import android.hardware.Sensor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

    public static Double getStdInt(ArrayList<Integer> list){
        Double std = 0.0;
        if(!list.isEmpty()) {
            Double mean = getMeanInt(list);
            for (Integer n : list) {
                Double delta = n - mean;
                std += (delta * delta);
            }
            std = Math.sqrt(std/list.size());
        }
        return std;
    }

    public static Double getMeanInt(ArrayList<Integer> list){
        Double sum = 0.0;
        if(!list.isEmpty()) {
            for (Integer n : list) {
                sum += n;
            }
            return sum / list.size();
        }
        return sum;
    }

    public static Double getStd(ArrayList<Double> list){
        Double std = 0.0;
        if(!list.isEmpty()) {
            Double mean = getMean(list);
            for (Double n : list) {
                Double delta = n - mean;
                std += (delta * delta);
            }
            std = Math.sqrt(std/list.size());
        }
        return std;
    }

    public static Double getMean(ArrayList<Double> list){
        Double sum = 0.0;
        if(!list.isEmpty()) {
            for (Double n : list) {
                sum += n;
            }
            return sum / list.size();
        }
        return sum;
    }

}
