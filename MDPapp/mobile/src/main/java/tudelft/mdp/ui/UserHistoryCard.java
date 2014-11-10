package tudelft.mdp.ui;

import com.devspark.robototextview.widget.RobotoTextView;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import it.gmariotti.cardslib.library.internal.Card;
import tudelft.mdp.R;
import tudelft.mdp.backend.endpoints.locationLogEndpoint.model.LocationLogRecord;

public class UserHistoryCard extends Card {

    private Context context;
    private LocationLogRecord mLocationLogRecord;

    protected TextView mDay;
    protected TextView mMonthYear;
    protected TextView mTimestamp;
    protected TextView mZone;
    protected TextView mPlace;


    public UserHistoryCard(Context context,
            LocationLogRecord locationLogRecord) {
        super(context, R.layout.card_location_history);
        mLocationLogRecord = locationLogRecord;
    }

    public UserHistoryCard(Context context, int innerLayout,
            LocationLogRecord locationLogRecord) {
        super(context, innerLayout);
        mLocationLogRecord = locationLogRecord;
    }

    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public void setContext(Context context) {
        this.context = context;
    }

    public LocationLogRecord getLocationLogRecord() {
        return mLocationLogRecord;
    }

    public void setLocationLogRecord(LocationLogRecord locationLogRecord) {
        mLocationLogRecord = locationLogRecord;
    }

    private void init(){

    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {

        //Retrieve elements
        mDay        = (RobotoTextView) parent.findViewById(R.id.twDay);
        mMonthYear  = (RobotoTextView) parent.findViewById(R.id.twMonthYear);
        mTimestamp  = (RobotoTextView) parent.findViewById(R.id.twTimestamp);
        mPlace      = (RobotoTextView) parent.findViewById(R.id.twPlace);
        mZone       = (RobotoTextView) parent.findViewById(R.id.twZone);

        String fullTimestamp = String.valueOf(mLocationLogRecord.getTimestamp());
        Log.i(TAG, fullTimestamp);
        String YYYY = fullTimestamp.substring(0,4).replace(".","");
        Log.i(TAG, YYYY);
        String MM   = fullTimestamp.substring(4,6);
        Log.i(TAG, MM);
        String dd   = fullTimestamp.substring(6,8);
        Log.i(TAG, dd);
        String HH   = fullTimestamp.substring(8,10);
        Log.i(TAG, HH);
        String mm   = fullTimestamp.substring(10,12);
        Log.i(TAG, mm);
        String ss   = fullTimestamp.substring(12,14);
        Log.i(TAG, ss);

        String month = calculateMonth(MM);

        if (mDay!=null)
            mDay.setText(dd);

        if (mMonthYear!=null)
            mMonthYear.setText(month.toUpperCase() + " " + YYYY);

        if (mTimestamp!=null) {
            mTimestamp.setText(HH +":" + mm  +":" + ss);
        }

        if (mPlace!=null) {
            mPlace.setText(mLocationLogRecord.getPlace());
        }

        if (mZone!=null) {
            mZone.setText(mLocationLogRecord.getZone());
        }

    }

    private String calculateMonth(String MM){
        String month = "";

        if (MM.equals("01")){
            return "Jan";
        }
        if (MM.equals("02")){
            return "Feb";
        }
        if (MM.equals("03")){
            return "Mar";
        }
        if (MM.equals("04")){
            return "Apr";
        }
        if (MM.equals("05")){
            return "May";
        }
        if (MM.equals("06")){
            return "Jun";
        }
        if (MM.equals("07")){
            return "Jul";
        }
        if (MM.equals("08")){
            return "Ago";
        }
        if (MM.equals("09")){
            return "Sep";
        }
        if (MM.equals("10")){
            return "Oct";
        }
        if (MM.equals("11")){
            return "Nov";
        }
        if (MM.equals("12")){
            return "Dec";
        }

        return month;
    }




}
