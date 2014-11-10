package tudelft.mdp.weka;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

import tudelft.mdp.backend.endpoints.deviceMotionLocationRecordEndpoint.DeviceMotionLocationRecordEndpoint;
import tudelft.mdp.backend.endpoints.deviceMotionLocationRecordEndpoint.model.DeviceMotionLocationRecord;

public class UploadMotionLocationFeaturesAsyncTask extends AsyncTask<Object, Void, Boolean> {

    private static DeviceMotionLocationRecordEndpoint mDeviceMotionLocationRecordEndpoint = null;
    private static final String TAG = "MDP-UploadMotionLocationFeaturesAsyncTask";
    private Context context;

    protected Boolean doInBackground(Object... params) {
        context = (Context) params[0];

        @SuppressWarnings("unchecked")
        DeviceMotionLocationRecord deviceMotionLocationRecord  = (DeviceMotionLocationRecord) params[1];

        if (mDeviceMotionLocationRecordEndpoint == null) {
            /* For testing against a deployed backend */
            DeviceMotionLocationRecordEndpoint.Builder builder = new DeviceMotionLocationRecordEndpoint.Builder(
                    AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null);
            mDeviceMotionLocationRecordEndpoint = builder.build();
        }

        try {
            mDeviceMotionLocationRecordEndpoint.insertDeviceMotionLocationRecord(deviceMotionLocationRecord).execute();
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Some error while uploading features: " + e.getMessage());
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            Toast.makeText(context, "Features uploaded successfully", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "Features uploaded successfully");
        } else {
            Toast.makeText(context, "Ooops! Some problem occurred while uploading the features.", Toast.LENGTH_SHORT).show();
        }
    }

}
