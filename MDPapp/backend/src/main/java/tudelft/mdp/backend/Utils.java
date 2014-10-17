package tudelft.mdp.backend;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

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
