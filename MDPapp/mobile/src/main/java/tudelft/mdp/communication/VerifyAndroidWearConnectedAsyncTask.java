package tudelft.mdp.communication;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import android.content.Context;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import tudelft.mdp.enums.UserPreferences;


public class VerifyAndroidWearConnectedAsyncTask  extends AsyncTask<Object, Void, Boolean>{

    private static final String TAG = "MDP-VerifyAndroidWearConnectedAsyncTask";
    Context context;

    protected Boolean doInBackground(Object... params) {
        context = (Context) params[0];
        GoogleApiClient mGoogleApiClient = (GoogleApiClient) params[1];

        NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
        return (nodes.getNodes().size() > 0);

    }

    @Override
    protected void onPostExecute(Boolean connected) {
        Log.w(TAG, "Android Wear: " + connected);
        if (connected) {
            PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(
                    UserPreferences.WEARCONNECTED, true).commit();
        } else {
            PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(
                    UserPreferences.WEARCONNECTED, false).commit();
        }
    }

}
