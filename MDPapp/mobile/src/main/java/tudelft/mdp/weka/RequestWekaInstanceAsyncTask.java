package tudelft.mdp.weka;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import tudelft.mdp.backend.endpoints.wekaObjectRecordEndpoint.WekaObjectRecordEndpoint;
import tudelft.mdp.backend.endpoints.wekaObjectRecordEndpoint.model.WekaObjectRecord;
import tudelft.mdp.enums.Constants;
import tudelft.mdp.utils.Utils;

public class RequestWekaInstanceAsyncTask extends AsyncTask<Object, Void, Boolean> {

    private static WekaObjectRecordEndpoint mWekaObjectRecordEndpoint = null;
    private static final String TAG = "MDP-RequestWekaInstanceAsyncTask";

    //private Instances mInstances;
    private Context context;
    private File arffFile;

    protected Boolean doInBackground(Object... params) {

        context      = (Context) params[0];
        String filename = (String) params[1];

        if (mWekaObjectRecordEndpoint == null) {
            /* For testing against a deployed backend */
            WekaObjectRecordEndpoint.Builder builder = new WekaObjectRecordEndpoint.Builder(
                    AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null);
            mWekaObjectRecordEndpoint = builder.build();
        }

        try {
            Log.e(TAG, "Requesting instances");
            WekaObjectRecord wekaObjectRecord = mWekaObjectRecordEndpoint.getLatestWekaObjectRecord("Instance", "Home").execute();

            if (wekaObjectRecord != null) {
                /*byte[] instancesByteArray = wekaObjectRecord.decodeBlob();


                try {

                    mInstances = (Instances) Utils.deserialize(instancesByteArray);
                    arffFile = new File(getLoggingDirectory(Constants.DIRECTORY_WEKA),
                            filename + "_" + Utils.getCurrentTimestamp());
                    ArffSaver saver = new ArffSaver();
                    saver.setInstances(mInstances);
                    saver.setFile(arffFile);
                    saver.writeBatch();

                } catch (ClassNotFoundException e) {
                    Log.e(TAG, e.getMessage());
                }*/

                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            Log.e(TAG, "Some error while requesting instances");
            return false;
        }

    }

    public static File getLoggingDirectory(String foldername) {
        File directory;
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            Log.e(TAG, "No external storage found.");
            return null;
        }
        directory = new File(Environment.getExternalStoragePublicDirectory(Constants.DIRECTORY_APP) , foldername);
        if (!directory.mkdirs()) {
            Log.v(TAG, "Directory already exists");
        }
        return directory;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result){
            if (arffFile != null) {
                Toast.makeText(context, "Arff saved:" + arffFile.getPath(), Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

}
