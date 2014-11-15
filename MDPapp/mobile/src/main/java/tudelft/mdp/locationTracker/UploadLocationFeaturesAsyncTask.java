package tudelft.mdp.locationTracker;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;

import tudelft.mdp.backend.endpoints.locationFeaturesRecordApi.LocationFeaturesRecordApi;
import tudelft.mdp.backend.endpoints.locationFeaturesRecordApi.model.LocationFeaturesRecord;
import tudelft.mdp.backend.endpoints.locationFeaturesRecordApi.model.LocationFeaturesRecordWrapper;

/**
 * Created by t7 on 15-11-14.
 */
public class UploadLocationFeaturesAsyncTask extends AsyncTask<Object, Void, Boolean> {

    private static LocationFeaturesRecordApi mLocationFeaturesEndpointService = null;

    private static final String TAG = "MDP-UploadLocationFeaturesAsyncTask";
    private Context context;

    @Override
    protected Boolean doInBackground(Object... params) {

        context = (Context) params[0];

        @SuppressWarnings("unchecked")
        ArrayList<LocationFeaturesRecord> mLocationFeaturesRecords = (ArrayList<LocationFeaturesRecord>) params[1];

        if (mLocationFeaturesEndpointService == null) {
            /* For testing against a deployed backend */
            LocationFeaturesRecordApi.Builder builder = new LocationFeaturesRecordApi.Builder(
                    AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null);
            mLocationFeaturesEndpointService = builder.build();
        }

        return sendByChunks(mLocationFeaturesRecords);

    }

    private boolean sendByChunks(ArrayList<LocationFeaturesRecord> rawScans){
        int chunkSize = 50;
        Log.e(TAG, "Uploading location features records " + rawScans.size());

        ArrayList<ArrayList<LocationFeaturesRecord>> wrapperChunks = new ArrayList<ArrayList<LocationFeaturesRecord>>();

        for (int i = 0; i < rawScans.size(); i += chunkSize){
            wrapperChunks.add(new ArrayList<LocationFeaturesRecord>(
                            rawScans.subList(i,  i + (Math.min(chunkSize, rawScans.size() - i)) ))
            );
        }

        Log.i(TAG, "Dividing into chunks:" + wrapperChunks.size());


        int chunkNum = 0;
        try {
            for (ArrayList<LocationFeaturesRecord> chunk : wrapperChunks) {
                LocationFeaturesRecordWrapper recordWrapper = new LocationFeaturesRecordWrapper();
                recordWrapper.setLocationFeaturesRecords(chunk);

                mLocationFeaturesEndpointService.insertBulk(recordWrapper).execute();
                chunkNum++;
            }
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Some error while uploading chunk " + chunkNum + " : " + e.getMessage());
            return false;
        }


    }

    @Override
    protected void onPostExecute(Boolean tagRegistered) {
        if (tagRegistered) {
            Log.e(TAG, "Successful logging weka location features.");
        } else {
            Log.e(TAG, "Error while inserting in DB.");
        }
    }
}
