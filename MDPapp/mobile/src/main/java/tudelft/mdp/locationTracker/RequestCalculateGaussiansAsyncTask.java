package tudelft.mdp.locationTracker;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

import tudelft.mdp.backend.endpoints.radioMapFingerprintEndpoint.RadioMapFingerprintEndpoint;
import tudelft.mdp.enums.MessagesProtocol;
import tudelft.mdp.gcm.GcmMessagingAsyncTask;

public class RequestCalculateGaussiansAsyncTask extends AsyncTask<Object, Void, Boolean> {

    private static RadioMapFingerprintEndpoint mRadioMapFingerprintEndpointService = null;
    private static final String TAG = "MDP-UploadLocationRawDataAsyncTask";
    private Context context;

    protected Boolean doInBackground(Object... params) {
        context      = (Context) params[0];
        String place = (String)  params[1];
        String zone  = (String)  params[2];


        if (mRadioMapFingerprintEndpointService == null) {
            /* For testing against a deployed backend */
            RadioMapFingerprintEndpoint.Builder builder = new RadioMapFingerprintEndpoint.Builder(
                    AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null);
            mRadioMapFingerprintEndpointService = builder.build();
        }

        try {
            mRadioMapFingerprintEndpointService.calculateZoneGaussians(place, zone).execute();
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Some error while uploading");
            return false;
        }

    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            Toast.makeText(context, "Gaussians calculated successfully", Toast.LENGTH_SHORT).show();
            new GcmMessagingAsyncTask().execute(MessagesProtocol.SENDGCM_CMD_UPDATEGAUSSIANS,
                                                MessagesProtocol.UPDATE_GAUSSIANS,
                                                context);
        } else {
            Toast.makeText(context, "Ooops! Some problem occurred while doing this.", Toast.LENGTH_SHORT).show();
        }
    }

}
