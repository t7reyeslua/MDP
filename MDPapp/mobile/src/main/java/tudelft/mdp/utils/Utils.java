package tudelft.mdp.utils;


import android.hardware.Sensor;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import tudelft.mdp.enums.Constants;
import tudelft.mdp.enums.Devices;
import tudelft.mdp.enums.Energy;
import tudelft.mdp.enums.UserPreferences;

public class Utils {
    private static final String LOGTAG = "MDP-Utils";

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
        String name = "Consolidated";

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

    public static String getMinTimestamp(int mode) {

        Calendar calendar = Calendar.getInstance();
        String minDate = "0";

        // TODO
        if (mode == UserPreferences.ALLTIME){
            minDate = "19880106000000";
            Log.i(LOGTAG, "Min Date ALLTIME " + minDate);
        }

        if (mode == UserPreferences.YEAR){
            //Current year + 010101000000
            minDate = String.valueOf(calendar.get(Calendar.YEAR))
                    + "0101000000";
            Log.i(LOGTAG, "Min Date YEAR " + minDate);

        }

        if (mode == UserPreferences.MONTH){
            //Current year + Current month + 01000000
            minDate = String.valueOf(calendar.get(Calendar.YEAR))
                    + String.format("%02d",calendar.get(Calendar.MONTH) + 1)
                    + "01000000";
            Log.i(LOGTAG, "Min Date MONTH " + minDate);
        }

        if (mode == UserPreferences.WEEK){
            int year = calendar.get(Calendar.YEAR);
            int week = calendar.get(Calendar.WEEK_OF_YEAR);

            calendar.clear();
            calendar.set(Calendar.WEEK_OF_YEAR, week);
            calendar.set(Calendar.YEAR, year);

            minDate = String.valueOf(calendar.get(Calendar.YEAR))
                    + String.format("%02d",calendar.get(Calendar.MONTH ) + 1)
                    + String.format("%02d",calendar.get(Calendar.DAY_OF_MONTH))
                    + "115959";

            Log.i(LOGTAG, "Min Date WEEK " + minDate);
        }

        if (mode == UserPreferences.TODAY){
            //Current year + Current month + Current Day + 000000
            minDate = String.valueOf(calendar.get(Calendar.YEAR))
                    + String.format("%02d", calendar.get(Calendar.MONTH) + 1)
                    + String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH))
                    + "000000";
            Log.i(LOGTAG, "Min Date TODAY " + minDate);
        }

        return minDate;
    }

    public static Double getEnergyFromTime(String deviceType, Double time){
        Double energy = 0.0;
        if (deviceType.equals(Devices.COMPUTER)){
            energy = time * Energy.KWH_COMPUTER;
        } else if (deviceType.equals(Devices.TELEVISION)){
            energy = time * Energy.KWH_TELEVISION;
        } else if (deviceType.equals(Devices.WASHING_MACHINE)){
            energy = time * Energy.KWH_WASHINGMACHINE;
        } else if (deviceType.equals(Devices.VIDEO_GAME_CONSOLE)){
            energy = time * Energy.KWH_VIDEOGAMECONSOLE;
        } else if (deviceType.equals(Devices.HOTPLATES)){
            energy = time * Energy.KWH_HOTPLATES;
        } else if (deviceType.equals(Devices.MICROWAVE)){
            energy = time * Energy.KWH_MICROWAVE;
        } else if (deviceType.equals(Devices.LIGHTS)){
            energy = time * Energy.KWH_LIGHTS;
        } else if (deviceType.equals(Devices.STEREO)){
            energy = time * Energy.KWH_STEREO;
        } else if (deviceType.equals(Devices.FRIDGE)){
            energy = time * Energy.KWH_FRIDGE;
        } else if (deviceType.equals(Devices.COOKER)){
            energy = time * Energy.KWH_COOKER;
        } else if (deviceType.equals(Devices.GRILL)){
            energy = time * Energy.KWH_GRILL;
        } else if (deviceType.equals(Devices.VACUUM_CLEANER)){
            energy = time * Energy.KWH_VACUUMCLEANER;
        }
        return energy;
    }

    public static Double getTimeFromEnergy(String deviceType, Double energy){
        Double time = 0.0;
        if (deviceType.equals(Devices.COMPUTER)){
            time = energy / Energy.KWH_COMPUTER;
        } else if (deviceType.equals(Devices.TELEVISION)){
            time = energy / Energy.KWH_TELEVISION;
        } else if (deviceType.equals(Devices.WASHING_MACHINE)){
            time = energy / Energy.KWH_WASHINGMACHINE;
        } else if (deviceType.equals(Devices.VIDEO_GAME_CONSOLE)){
            time = energy / Energy.KWH_VIDEOGAMECONSOLE;
        } else if (deviceType.equals(Devices.HOTPLATES)){
            time = energy / Energy.KWH_HOTPLATES;
        } else if (deviceType.equals(Devices.MICROWAVE)){
            time = energy / Energy.KWH_MICROWAVE;
        } else if (deviceType.equals(Devices.LIGHTS)){
            time = energy / Energy.KWH_LIGHTS;
        } else if (deviceType.equals(Devices.STEREO)){
            time = energy / Energy.KWH_STEREO;
        } else if (deviceType.equals(Devices.FRIDGE)){
            time = energy / Energy.KWH_FRIDGE;
        } else if (deviceType.equals(Devices.COOKER)){
            time = energy / Energy.KWH_COOKER;
        } else if (deviceType.equals(Devices.GRILL)){
            time = energy / Energy.KWH_GRILL;
        } else if (deviceType.equals(Devices.VACUUM_CLEANER)){
            time = energy / Energy.KWH_VACUUMCLEANER;
        }
        return time;
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
