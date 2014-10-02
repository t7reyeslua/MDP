package tudelft.mdp;


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
