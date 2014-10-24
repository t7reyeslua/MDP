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


        place = (String)  params[2];
        zone  = (String)  params[3];



        if (mRadioMapFingerprintEndpointService == null) {
            /* For testing against a deployed backend */
            RadioMapFingerprintEndpoint.Builder builder = new RadioMapFingerprintEndpoint.Builder(
                    AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null);
            mRadioMapFingerprintEndpointService = builder.build();
        }


        return sendByChunks(rawScans);

    }

    private boolean sendByChunks(ArrayList<ApHistogramRecord> rawScans){
        int chunkSize = 50;
        Log.e(TAG, "Uploading histogram scans " + rawScans.size());

        ArrayList<ArrayList<ApHistogramRecord>> wrapperChunks = new ArrayList<ArrayList<ApHistogramRecord>>();

        for (int i = 0; i < rawScans.size(); i += chunkSize){
            wrapperChunks.add(new ArrayList<ApHistogramRecord>(
                            rawScans.subList(i,  i + (Math.min(chunkSize, rawScans.size() - i)) ))
            );
        }

        Log.i(TAG, "Dividing into chunks:" + wrapperChunks.size());


        int chunkNum = 0;
        try {
            for (ArrayList<ApHistogramRecord> chunk : wrapperChunks) {
                ApHistogramRecordWrapper recordWrapper = new ApHistogramRecordWrapper();
                recordWrapper.setLocalHistogram(chunk);


                mRadioMapFingerprintEndpointService.increaseApHistogramRssiCountInZoneBulk(recordWrapper).execute();
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
            Log.w(TAG, "Histograms uploaded successfully, Requesting Gaussians");
            Toast.makeText(context, "Histograms uploaded successfully", Toast.LENGTH_SHORT).show();
            new RequestCalculateGaussiansAsyncTask().execute(context, place, zone);
        } else {
            Log.e(TAG, "Ooops! Some problem occurred while uploading the histograms.");
            Toast.makeText(context, "Ooops! Some problem occurred while uploading the histograms.", Toast.LENGTH_SHORT).show();
        }
    }

}
