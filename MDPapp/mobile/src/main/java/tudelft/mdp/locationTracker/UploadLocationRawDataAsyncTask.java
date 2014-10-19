package tudelft.mdp.locationTracker;


import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import tudelft.mdp.backend.endpoints.radioMapFingerprintEndpoint.RadioMapFingerprintEndpoint;
import tudelft.mdp.backend.endpoints.radioMapFingerprintEndpoint.model.LocationFingerprintRecord;
import tudelft.mdp.backend.endpoints.radioMapFingerprintEndpoint.model.LocationFingerprintRecordWrapper;

public class UploadLocationRawDataAsyncTask extends AsyncTask<Object, Void, Boolean> {

    private static RadioMapFingerprintEndpoint mRadioMapFingerprintEndpointService = null;
    private static final String TAG = "MDP-UploadLocationRawDataAsyncTask";
    private Context context;

    protected Boolean doInBackground(Object... params) {
        context = (Context) params[0];

        @SuppressWarnings("unchecked")
        ArrayList<LocationFingerprintRecord> rawScans  = (ArrayList<LocationFingerprintRecord>) params[1];
        LocationFingerprintRecordWrapper recordWrapper = new LocationFingerprintRecordWrapper();
        recordWrapper.setLocationFingerprintRecordWrapperArrayList(rawScans);



        if (mRadioMapFingerprintEndpointService == null) {
            /* For testing against a deployed backend */
            RadioMapFingerprintEndpoint.Builder builder = new RadioMapFingerprintEndpoint.Builder(
                    AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null);
            mRadioMapFingerprintEndpointService = builder.build();
        }

        try {
            Log.e(TAG, "Uploading raw scans " + rawScans.size());
            mRadioMapFingerprintEndpointService.insertRawLocationFingerprintForZoneBulk(recordWrapper).execute();
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Some error while uploading");
            return false;
        }

    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            Toast.makeText(context, "Scans uploaded successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Ooops! Some problem occurred while uploading the scans.", Toast.LENGTH_SHORT).show();
        }
    }

}
