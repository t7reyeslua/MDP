package tudelft.mdp.locationTracker;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import tudelft.mdp.backend.endpoints.radioMapFingerprintEndpoint.RadioMapFingerprintEndpoint;
import tudelft.mdp.backend.endpoints.radioMapFingerprintEndpoint.model.ApGaussianRecord;

public class RequestGaussiansAsyncTask extends AsyncTask<Object, Void, Boolean> {

    private static RadioMapFingerprintEndpoint mRadioMapFingerprintEndpointService = null;
    private static final String TAG = "MDP-RequestGaussiansAsyncTask";
    public RequestGaussiansAsyncResponse delegate = null;

    private List<ApGaussianRecord> mGaussianRecords = new ArrayList<ApGaussianRecord>();

    protected Boolean doInBackground(Object... params) {
        if (mRadioMapFingerprintEndpointService == null) {
            /* For testing against a deployed backend */
            RadioMapFingerprintEndpoint.Builder builder = new RadioMapFingerprintEndpoint.Builder(
                    AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null);
            mRadioMapFingerprintEndpointService = builder.build();
        }

        try {
            Log.e(TAG, "Requesting gaussians");
            mGaussianRecords  = mRadioMapFingerprintEndpointService.listGaussiansAll().execute().getItems();

            if (mGaussianRecords != null) {
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            Log.e(TAG, "Some error while requesting gaussians");
            return false;
        }

    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            delegate.processFinishRequestGaussians(mGaussianRecords);
        }
    }

    public interface RequestGaussiansAsyncResponse {
        void processFinishRequestGaussians(List<ApGaussianRecord> outputList);
    }

}
