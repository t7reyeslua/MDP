package tudelft.mdp.deviceManager;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;

import android.app.FragmentManager;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.util.List;

import tudelft.mdp.backend.endpoints.deviceEndpoint.DeviceEndpoint;
import tudelft.mdp.backend.endpoints.deviceEndpoint.model.NfcRecord;

/**
 * Async task that request the list of existing devices and their general info.
 */
public class DeviceListRequestAsyncTask extends AsyncTask<Void, Void, Boolean> {

    private static DeviceEndpoint mDeviceEndpointService = null;
    private static final String TAG = "MDP-DeviceListRequestAsyncTask";
    public RequestDeviceListAsyncResponse delegate=null;

    private List<NfcRecord> mDevices;

    protected Boolean doInBackground(Void... params) {
        if (mDeviceEndpointService == null) {
            /* For testing against a deployed backend */
            DeviceEndpoint.Builder builder = new DeviceEndpoint.Builder(
                    AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null);
            mDeviceEndpointService = builder.build();
        }


        try {
            Log.e(TAG, "Requesting list of registered devices");
            mDevices = mDeviceEndpointService.listDevices(100).execute().getItems();
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Some error while requesting list of registered devices");
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            delegate.processFinishRequestDeviceList(mDevices);
        }
    }

    public interface RequestDeviceListAsyncResponse {
        void processFinishRequestDeviceList(List<NfcRecord> outputList);
    }



}
