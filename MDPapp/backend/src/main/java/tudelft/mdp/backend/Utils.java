package tudelft.mdp.backend;

import java.text.SimpleDateFormat;
import java.util.Calendar;


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

}
