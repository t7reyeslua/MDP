package tudelft.mdp.locationTracker;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;

import tudelft.mdp.backend.endpoints.locationLogEndpoint.LocationLogEndpoint;
import tudelft.mdp.backend.endpoints.locationLogEndpoint.model.LocationLogRecord;

public class UploadCurrentLocationAsyncTask extends AsyncTask<Object, Void, Boolean> {

    private static LocationLogEndpoint mLocationLogEndpointService = null;

    private static final String TAG = "MDP-UploadCurrentLocationAsyncTask";

    @Override
    protected Boolean doInBackground(Object... params) {

        LocationLogRecord locationLogRecord = (LocationLogRecord) params[0];

        if (mLocationLogEndpointService == null) {
            /* For testing against a deployed backend */
            LocationLogEndpoint.Builder builder = new LocationLogEndpoint.Builder(
                    AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null);
            mLocationLogEndpointService = builder.build();
        }

        try {
            Log.e(TAG, "Calling mLocationLogEndpointService.insertLocationLogRecord");
            mLocationLogEndpointService.insertLocationLogRecord(locationLogRecord).execute();

            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean tagRegistered) {
        if (tagRegistered) {
            Log.e(TAG, "Successful logging.");
        } else {
            Log.e(TAG, "Error while inserting in DB.");
        }
    }
}
