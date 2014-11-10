package tudelft.mdp.deviceManager;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import tudelft.mdp.backend.endpoints.deviceLogEndpoint.DeviceLogEndpoint;
import tudelft.mdp.backend.endpoints.deviceLogEndpoint.model.NfcRecord;


public class RequestUserActiveDevicesAsyncTask extends AsyncTask<Object, Void, Boolean> {

    private static DeviceLogEndpoint mDeviceLogEndpointService = null;
    private static final String TAG = "MDP-RequestUserActiveDevicesAsyncTask";
    public RequestUserActiveDevicesAsyncResponse delegate = null;

    private List<NfcRecord> mUserStats = new ArrayList<NfcRecord>();

    protected Boolean doInBackground(Object... params) {
        String user = (String) params[0];

        if (mDeviceLogEndpointService == null) {
            /* For testing against a deployed backend */
            DeviceLogEndpoint.Builder builder = new DeviceLogEndpoint.Builder(
                    AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null);
            mDeviceLogEndpointService = builder.build();
        }


        try {
            Log.e(TAG, "Requesting user stats of active devices.");
            mUserStats  = mDeviceLogEndpointService.getUserActiveDevices(user).execute().getItems();


            return true;
        } catch (IOException e) {
            Log.e(TAG, "Some error while requesting all users stats");
            return false;
        }

    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            if (mUserStats != null) {
                Log.i(TAG, "Active devices: "  + mUserStats.size());
                delegate.processFinishRequestUserActiveDevices(mUserStats);
            } else {
                Log.i(TAG, "Active devices: 0");
            }
        }
    }

    public interface RequestUserActiveDevicesAsyncResponse {
        void processFinishRequestUserActiveDevices(List<NfcRecord> outputList);
    }

}

