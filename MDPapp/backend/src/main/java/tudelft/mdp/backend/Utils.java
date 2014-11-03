package tudelft.mdp.backend;

import com.google.appengine.repackaged.org.joda.time.DateTime;
import com.google.appengine.repackaged.org.joda.time.format.DateTimeFormat;
import com.google.appengine.repackaged.org.joda.time.format.DateTimeFormatter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import tudelft.mdp.backend.records.ApHistogramRecord;


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
