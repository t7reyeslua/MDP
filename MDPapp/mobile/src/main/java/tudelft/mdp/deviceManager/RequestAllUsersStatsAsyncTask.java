package tudelft.mdp.deviceManager;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import tudelft.mdp.backend.endpoints.deviceLogEndpoint.DeviceLogEndpoint;
import tudelft.mdp.backend.endpoints.deviceLogEndpoint.model.DeviceUsageRecord;

public class RequestAllUsersStatsAsyncTask extends AsyncTask<Void, Void, Boolean> {

    private static DeviceLogEndpoint mDeviceLogEndpointService = null;
    private static final String TAG = "MDP-RequestDeviceUsageByUserAsyncTask";
    public RequestAllUsersStatsAsyncResponse delegate = null;

    private List<DeviceUsageRecord> mUserStats = new ArrayList<DeviceUsageRecord>();

    protected Boolean doInBackground(Void... params) {

        if (mDeviceLogEndpointService == null) {
            /* For testing against a deployed backend */
            DeviceLogEndpoint.Builder builder = new DeviceLogEndpoint.Builder(
                    AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null);
            mDeviceLogEndpointService = builder.build();
        }


        try {
            Log.e(TAG, "Requesting all users stats.");
            mUserStats  = mDeviceLogEndpointService.getUsersStatsOfAllDevices().execute().getItems();

            return true;
        } catch (IOException e) {
            Log.e(TAG, "Some error while requesting all users stats");
            return false;
        }

    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            delegate.processFinishRequestAllUsers(mUserStats);
        }
    }

    public interface RequestAllUsersStatsAsyncResponse {
        void processFinishRequestAllUsers(List<DeviceUsageRecord> outputList);
    }

}
