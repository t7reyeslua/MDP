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

        if (mRadioMapFingerprintEndpointService == null) {
            /* For testing against a deployed backend */
            RadioMapFingerprintEndpoint.Builder builder = new RadioMapFingerprintEndpoint.Builder(
                    AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null);
            mRadioMapFingerprintEndpointService = builder.build();
        }

        return sendByChunks(rawScans);

    }

    private boolean sendByChunks(ArrayList<LocationFingerprintRecord> rawScans){
        int chunkSize = 100;
        Log.e(TAG, "Uploading raw scans " + rawScans.size());

        ArrayList<ArrayList<LocationFingerprintRecord>> wrapperChunks = new ArrayList<ArrayList<LocationFingerprintRecord>>();

        for (int i = 0; i < rawScans.size(); i += chunkSize){
            wrapperChunks.add(new ArrayList<LocationFingerprintRecord>(
                    rawScans.subList(i,  i + (Math.min(chunkSize, rawScans.size() - i)) ))
            );
        }

        Log.i(TAG, "Dividing into chunks:" + wrapperChunks.size());


        int chunkNum = 0;
        try {
            for (ArrayList<LocationFingerprintRecord> chunk : wrapperChunks) {
                LocationFingerprintRecordWrapper recordWrapper = new LocationFingerprintRecordWrapper();
                recordWrapper.setLocationFingerprintRecordWrapperArrayList(chunk);

                mRadioMapFingerprintEndpointService.insertRawLocationFingerprintForZoneBulk(recordWrapper).execute();
                chunkNum++;
            }
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Some error while uploading chunk " + chunkNum + " : " + e.getMessage());
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
