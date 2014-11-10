package tudelft.mdp.deviceManager;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

import tudelft.mdp.backend.endpoints.deviceEndpoint.DeviceEndpoint;
import tudelft.mdp.backend.endpoints.deviceEndpoint.model.NfcRecord;

/**
 * AsyncTask for registering a new device in the database
 */
public class DeviceRegistrationAsyncTask extends AsyncTask<Object, Void, Boolean> {

    private static DeviceEndpoint mDeviceEndpointService = null;
    private static final String TAG = "MDP-NfcDetectionAsyncTask";

    private Context context;

    @Override
    protected Boolean doInBackground(Object... params) {

        String nfcTag              = (String)  params[0];
        String deviceType          = (String)  params[1];
        String deviceDescription   = (String)  params[2];
        String deviceLocation      = (String)  params[3];
        String devicePlace         = (String)  params[4];
        context                    = (Context) params[5];


        if (mDeviceEndpointService == null) {
            /* For testing against a deployed backend */
            DeviceEndpoint.Builder builder = new DeviceEndpoint.Builder(
                    AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null);
            mDeviceEndpointService = builder.build();
        }

        try {
            NfcRecord mDeviceInfo = new NfcRecord();
            mDeviceInfo.setNfcId(nfcTag);
            mDeviceInfo.setType(deviceType);
            mDeviceInfo.setDescription(deviceDescription);
            mDeviceInfo.setLocation(deviceLocation);
            mDeviceInfo.setPlace(devicePlace);

            Log.e(TAG, "Calling mDeviceEndpointService.insertDevice");
            mDeviceEndpointService.insertDevice(mDeviceInfo).execute();

            return true;
        } catch (IOException e) {
            /* Tag was not yet registered*/
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean tagRegistered) {
        if (tagRegistered) {
            Toast.makeText(context, "Successful registration.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Successful registration.");
        } else {
            Toast.makeText(context, "Oops! Something happened. Try again later.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Error while inserting in DB.");
        }
    }


}
