package tudelft.mdp.locationTracker;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;

import android.content.Context;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import tudelft.mdp.Utils;
import tudelft.mdp.backend.endpoints.locationLogEndpoint.LocationLogEndpoint;
import tudelft.mdp.backend.endpoints.locationLogEndpoint.model.LocationLogRecord;
import tudelft.mdp.enums.Constants;
import tudelft.mdp.enums.UserPreferences;

public class UserLocationHistoryRequestAsyncTask extends AsyncTask<Object, Void, Boolean> {

    private static LocationLogEndpoint mLocationLogEndpointService = null;
    private static final String TAG = "MDP-DeviceUsageByUserRequestAsyncTask";
    public RequestUserLocationHistoryAsyncResponse delegate = null;

    private String mode;
    private String username;
    private Context mContext;
    private List<LocationLogRecord> mUserLocationHistory = new ArrayList<LocationLogRecord>();

    protected Boolean doInBackground(Object... params) {
        mContext = (Context) params[0];

        mode  = PreferenceManager.getDefaultSharedPreferences(mContext)
                .getString(UserPreferences.LOCATIONMODE, Constants.LOC_BAYESSIAN);
        username = PreferenceManager.getDefaultSharedPreferences(mContext)
                .getString(UserPreferences.USERNAME, null);



        if (mLocationLogEndpointService == null) {
            /* For testing against a deployed backend */
            LocationLogEndpoint.Builder builder = new LocationLogEndpoint.Builder(
                    AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null);
            mLocationLogEndpointService = builder.build();
        }

        Double maxDate = Double.valueOf(Utils.getCurrentTimestamp());
        Double minDate = Utils.getMinTimestamp(UserPreferences.ALLTIME);


        try {
            Log.e(TAG, "Requesting location history of user " + username);
            mUserLocationHistory  = mLocationLogEndpointService.listLocationLogByUserDate(maxDate, minDate, username).execute().getItems();


            return true;
        } catch (IOException e) {
            Log.e(TAG, "Some error while requesting user location history");
            return false;
        }

    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            delegate.processFinishRequestUserLocationHistory(mUserLocationHistory);
        }
    }

    public interface RequestUserLocationHistoryAsyncResponse {
        void processFinishRequestUserLocationHistory(List<LocationLogRecord> outputList);
    }

}
