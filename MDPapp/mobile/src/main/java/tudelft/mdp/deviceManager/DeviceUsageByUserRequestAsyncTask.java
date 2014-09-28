package tudelft.mdp.deviceManager;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import tudelft.mdp.backend.endpoints.deviceLogEndpoint.DeviceLogEndpoint;

/**
 * AsyncTask that handles the request for the device usage from a specific user
 */
public class DeviceUsageByUserRequestAsyncTask extends AsyncTask<Object, Void, Boolean> {

    private static DeviceLogEndpoint mDeviceLogEndpointService = null;
    private static final String TAG = "MDP-DeviceUsageByUserRequestAsyncTask";
    public RequestDeviceUsageByUserAsyncResponse delegate = null;

    private String nfcTag;
    private String username;
    private List<Object> mUserDeviceInfo;

    protected Boolean doInBackground(Object... params) {
        nfcTag  = (String)  params[0];
        username = (String) params[1];

        mUserDeviceInfo = new ArrayList<Object>();

        if (mDeviceLogEndpointService == null) {
            /* For testing against a deployed backend */
            DeviceLogEndpoint.Builder builder = new DeviceLogEndpoint.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null);
            mDeviceLogEndpointService = builder.build();
        }



        /*
        try {
            Log.e(TAG, "Requesting list of registered devices");
            mDevices = mDeviceEndpointService.listindexFoundDevices(100).execute().getItems();
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Some error while requesting list of registered devices");
            return false;
        }
        */

        Double d1 = 3601.00;
        Double d2 = 3599.00;
        Double d3 = 21.42;
        Double d4 = 19.88;
        Double d5 = 76.89;


        mUserDeviceInfo.add(nfcTag);
        mUserDeviceInfo.add(d1);
        mUserDeviceInfo.add(d2);
        mUserDeviceInfo.add(d3);
        mUserDeviceInfo.add(d4);
        mUserDeviceInfo.add(d5);

        return true;
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
