package tudelft.mdp.deviceManager;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import tudelft.mdp.utils.Utils;
import tudelft.mdp.backend.endpoints.deviceLogEndpoint.DeviceLogEndpoint;
import tudelft.mdp.enums.UserPreferences;

/**
 * AsyncTask that handles the request for the device usage from a specific user
 */
public class DeviceUsageByUserRequestAsyncTask extends AsyncTask<Object, Void, Boolean> {

    private static DeviceLogEndpoint mDeviceLogEndpointService = null;
    private static final String TAG = "MDP-DeviceUsageByUserRequestAsyncTask";
    public RequestDeviceUsageByUserAsyncResponse delegate = null;

    private String nfcTag;
    private String username;
    private List<Object> mUserDeviceInfo = new ArrayList<Object>();

    protected Boolean doInBackground(Object... params) {
        nfcTag  = (String)  params[0];
        username = (String) params[1];

        Double d1 = 0.0;
        Double d2 = 0.0;
        Double d3 = 0.0;
        Double d4 = 0.0;
        Double d5 = 0.0;
        Double d6 = 0.0;

        List<Double> result = new ArrayList<Double>();

        if (mDeviceLogEndpointService == null) {
            /* For testing against a deployed backend */
            DeviceLogEndpoint.Builder builder = new DeviceLogEndpoint.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null);
            mDeviceLogEndpointService = builder.build();
        }

        Double maxDate = Double.valueOf(Utils.getCurrentTimestamp());
        Double minDate = Utils.getMinTimestamp(UserPreferences.ALLTIME);


        try {
            Log.e(TAG, "Requesting device usage (" + nfcTag +") by user " + username);
            result  = mDeviceLogEndpointService.getUserStatsOfDevice(maxDate, minDate, nfcTag, username).execute().getItems();

            if (result.size() >= 0) {
                d1 = result.get(0);
                d2 = result.get(1);
                d3 = result.get(2);
                d4 = result.get(3);
                d5 = result.get(4) * 100;
                d6 = result.get(5);
            }

            mUserDeviceInfo.add(nfcTag);
            mUserDeviceInfo.add(d1);
            mUserDeviceInfo.add(d2);
            mUserDeviceInfo.add(d3);
            mUserDeviceInfo.add(d4);
            mUserDeviceInfo.add(d5);
            mUserDeviceInfo.add(d6);

            return true;
        } catch (IOException e) {
            Log.e(TAG, "Some error while requesting list of registered devices");
            return false;
        }

    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            delegate.processFinishRequestDeviceUsageByUser(mUserDeviceInfo);
        }
    }

    public interface RequestDeviceUsageByUserAsyncResponse {
        void processFinishRequestDeviceUsageByUser(List<Object> outputList);
    }



}
