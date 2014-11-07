package tudelft.mdp.deviceManager;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import tudelft.mdp.backend.endpoints.energyConsumptionRecordEndpoint.EnergyConsumptionRecordEndpoint;
import tudelft.mdp.backend.endpoints.energyConsumptionRecordEndpoint.model.EnergyConsumptionRecord;

public class RequestUserEnergyConsumptionHistoryAsyncTask extends AsyncTask<Object, Void, Boolean> {

    private static EnergyConsumptionRecordEndpoint mEnergyConsumptionEndpoint = null;
    private static final String TAG = "MDP-RequestUserEnergyConsumptionHistoryAsyncTask";
    public RequestUserEnergyConsumptionHistoryAsyncResponse delegate = null;


    private List<EnergyConsumptionRecord> mUserStats = new ArrayList<EnergyConsumptionRecord>();

    protected Boolean doInBackground(Object... params) {

        String username = (String) params[0];
        String minDate = (String) params[1];
        String maxDate = (String) params[2];


        if (mEnergyConsumptionEndpoint == null) {
            /* For testing against a deployed backend */
            EnergyConsumptionRecordEndpoint.Builder builder = new EnergyConsumptionRecordEndpoint.Builder(
                    AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null);
            mEnergyConsumptionEndpoint = builder.build();
        }


        try {
            Log.e(TAG, "Requesting user energy consumption stats.");
            mUserStats  = mEnergyConsumptionEndpoint.listEnergyRecordsByUserDate(maxDate,minDate,username).execute().getItems();
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Some error while requesting user energy consumption stats");
            return false;
        }

    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            delegate.processFinishRequestEnergyConsumptionHistory(mUserStats);
        }
    }

    public interface RequestUserEnergyConsumptionHistoryAsyncResponse {
        void processFinishRequestEnergyConsumptionHistory(List<EnergyConsumptionRecord> outputList);
    }

}
