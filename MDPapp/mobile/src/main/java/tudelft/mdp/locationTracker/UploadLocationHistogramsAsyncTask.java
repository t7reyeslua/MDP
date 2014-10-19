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
import tudelft.mdp.backend.endpoints.radioMapFingerprintEndpoint.model.ApHistogramRecord;
import tudelft.mdp.backend.endpoints.radioMapFingerprintEndpoint.model.ApHistogramRecordWrapper;

public class UploadLocationHistogramsAsyncTask extends AsyncTask<Object, Void, Boolean> {

    private static RadioMapFingerprintEndpoint mRadioMapFingerprintEndpointService = null;
    private static final String TAG = "MDP-UploadLocationHistogramsAsyncTask";
    private Context context;
    private String place;
    private String zone;

    protected Boolean doInBackground(Object... params) {
        context = (Context) params[0];

        @SuppressWarnings("unchecked")
        ArrayList<ApHistogramRecord> rawScans  = (ArrayList<ApHistogramRecord>) params[1];
        ApHistogramRecordWrapper recordWrapper = new ApHistogramRecordWrapper();
        recordWrapper.setLocalHistogram(rawScans);


        place = (String)  params[1];
        zone  = (String)  params[2];



        if (mRadioMapFingerprintEndpointService == null) {
            /* For testing against a deployed backend */
            RadioMapFingerprintEndpoint.Builder builder = new RadioMapFingerprintEndpoint.Builder(
                    AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null);
            mRadioMapFingerprintEndpointService = builder.build();
        }

        try {
            Log.e(TAG, "Uploading histograms scans " + rawScans.size());
            mRadioMapFingerprintEndpointService.increaseApHistogramRssiCountInZoneBulk(recordWrapper).execute();
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Some error while uploading");
            return false;
        }

    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            Toast.makeText(context, "Histograms uploaded successfully", Toast.LENGTH_SHORT).show();
            new CalculateGaussiansRequestAsyncTask().execute(context, place, zone);
        } else {
            Toast.makeText(context, "Ooops! Some problem occurred while uploading the histograms.", Toast.LENGTH_SHORT).show();
        }
    }

}
