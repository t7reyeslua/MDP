package tudelft.mdp.backend;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import tudelft.mdp.backend.enums.Constants;
import tudelft.mdp.backend.enums.Devices;
import tudelft.mdp.backend.enums.Energy;
import tudelft.mdp.backend.records.ApHistogramRecord;


public class Utils {

    public static String getCurrentTimestamp(){
        // 1) create a java calendar instance
        Calendar calendar = Calendar.getInstance();
        // 2) get a java.util.Date from the calendar instance.
        //    this date will represent the current instant, or "now".
        calendar.setTimeZone(TimeZone.getTimeZone("CET"));
        java.util.Date now = calendar.getTime();
        // 3) a java current time (now) instance
        java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(now.getTime());

        return new SimpleDateFormat("yyyyMMddHHmmss").format(currentTimestamp);
    }

    public static String getMinTimestamp(int mode) {
        Calendar calendar = Calendar.getInstance();
        TimeZone timeZone = TimeZone.getTimeZone("CET");
        calendar.setTimeZone(timeZone);

        String minDate = "0";

        // TODO
        if (mode == Constants.ALLTIME){
            minDate = "19880106000000";
        }

        if (mode == Constants.YEAR){
            //Current year + 010101000000
            minDate = String.valueOf(calendar.get(Calendar.YEAR))
                    + "0101000000";

        }

        if (mode == Constants.MONTH){
            //Current year + Current month + 01000000
            minDate = String.valueOf(calendar.get(Calendar.YEAR))
                    + String.format("%02d",calendar.get(Calendar.MONTH) + 1)
                    + "01000000";
        }

        if (mode == Constants.WEEK){
            int year = calendar.get(Calendar.YEAR);
            int week = calendar.get(Calendar.WEEK_OF_YEAR);

            calendar.clear();
            calendar.set(Calendar.WEEK_OF_YEAR, week);
            calendar.set(Calendar.YEAR, year);

            minDate = String.valueOf(calendar.get(Calendar.YEAR))
                    + String.format("%02d",calendar.get(Calendar.MONTH ) + 1)
                    + String.format("%02d",calendar.get(Calendar.DAY_OF_MONTH))
                    + "115959";

        }

        if (mode == Constants.TODAY){
            //Current year + Current month + Current Day + 000000
            minDate = String.valueOf(calendar.get(Calendar.YEAR))
                    + String.format("%02d",calendar.get(Calendar.MONTH ) + 1)
                    + String.format("%02d",calendar.get(Calendar.DAY_OF_MONTH))
                    + "000000";
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

    public static long convertTimestampToSeconds(String timestamp){
        // Timestamp format: 20141102190401  -YYYY MM DD hh mm ss
                                            //0123 45 67 89 01 23
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        long dateTime = -1;
        try {
            Date date = simpleDateFormat.parse(timestamp);
            dateTime = date.getTime();
        } catch (ParseException e) {
            System.out.println(e.getMessage());
        }

        return dateTime;
    }

    public static double differenceBetweenDates(String dateNewest, String dateOldest){
        long timeNewest = convertTimestampToSeconds(dateNewest);
        long timeOldest = convertTimestampToSeconds(dateOldest);

        double diff = (double) (timeNewest - timeOldest);
        return diff / 1000;
    }



    public static Double getStd(ArrayList<ApHistogramRecord> list){
        Double std = 0.0;
        if(!list.isEmpty()) {
            int count = 0;
            Double mean = getMean(list);
            for (ApHistogramRecord n : list) {
                count += n.getCount();
                Double delta = n.getRssi()-mean;
                std += (delta * delta) * n.getCount();
            }
            std = Math.sqrt(std/count);
        }
        return std;
    }

    public static Double getMean(ArrayList<ApHistogramRecord> list){
        Double sum = 0.0;
        if(!list.isEmpty()) {
            int count = 0;
            for (ApHistogramRecord n : list) {
                count += n.getCount();
                sum += (n.getRssi() * n.getCount());
            }
            return sum / count;
        }
        return sum;
    }


}
