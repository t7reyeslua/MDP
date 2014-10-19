package tudelft.mdp.deviceManager;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;

import android.app.FragmentManager;
import android.content.Context;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import tudelft.mdp.backend.endpoints.deviceEndpoint.DeviceEndpoint;
import tudelft.mdp.backend.endpoints.deviceEndpoint.model.NfcRecord;
import tudelft.mdp.backend.endpoints.deviceLogEndpoint.DeviceLogEndpoint;
import tudelft.mdp.backend.endpoints.deviceLogEndpoint.model.NfcLogRecord;
import tudelft.mdp.enums.Constants;
import tudelft.mdp.enums.MessagesProtocol;
import tudelft.mdp.enums.UserPreferences;
import tudelft.mdp.gcm.GcmMessagingAsyncTask;

/**
 * AsyncTask that is called after a NFC tag is detected
 */
public class DeviceDetectionAsyncTask extends AsyncTask<Object, Void, Boolean> {

    private Context context;
    private String nfcTag;
    private String user;
    private FragmentManager mFragmentManager;
    private static DeviceEndpoint mDeviceEndpointService = null;
    private static DeviceLogEndpoint mDeviceLogEndpointService = null;
    private static final String TAG = "MDP-DeviceDetectionAsyncTask";

    @Override
    protected Boolean doInBackground(Object... params) {
        context = (Context) params[0];
        nfcTag  = (String)  params[1];
        user    = (String)  params[2];
        mFragmentManager   = (FragmentManager)  params[3];


        if (mDeviceEndpointService == null) {
            /* For testing against a deployed backend */
            DeviceEndpoint.Builder builder = new DeviceEndpoint.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null);
            mDeviceEndpointService = builder.build();
        }

        if (mDeviceLogEndpointService == null) {
            /* For testing against a deployed backend */
            DeviceLogEndpoint.Builder builder = new DeviceLogEndpoint.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null);
            mDeviceLogEndpointService = builder.build();
        }

        try {
            NfcRecord mDeviceInfo = mDeviceEndpointService.getDevice(nfcTag).execute();
            Log.e(TAG, "Previously registered device: TRUE");

            boolean mTraining = PreferenceManager.getDefaultSharedPreferences(context)
                    .getBoolean(UserPreferences.TRAINING_PHASE, false);

            if (!mTraining) {
                // Broadcast the ON/OFF event to all users to identify what they are doing at the moment.
                new GcmMessagingAsyncTask().execute(MessagesProtocol.SENDGCM_CMD_MOTIONLOCATION,
                        nfcTag,
                        context);
            }

            /* Tag has been previously registered. Now verify the last thing the user did with the device*/
            NfcLogRecord mUserDeviceStatus = mDeviceLogEndpointService.getLastUserLogOfDevice(nfcTag, user).execute();
            NfcLogRecord newLogRecord = new NfcLogRecord();
            if (mUserDeviceStatus == null){
                /* Has never interacted with this device before. */
                Log.e(TAG, "Previous user interaction with device: FALSE");

                newLogRecord.setNfcId(nfcTag);
                newLogRecord.setUser(user);
                newLogRecord.setState(true);

            } else {
                /* Has previously used this device. Store a new log with a toggled status. */

                Log.e(TAG, "Previous user interaction with device: TRUE. User: " +
                        mUserDeviceStatus.getUser() + " / Status: " +
                        mUserDeviceStatus.getState().toString());
                newLogRecord.setNfcId(nfcTag);
                newLogRecord.setUser(user);
                newLogRecord.setState(!mUserDeviceStatus.getState());
            }

            Log.d(TAG, "Inserting new nfc log record.");
            NfcLogRecord insertedDeviceLog = mDeviceLogEndpointService.insertDeviceLog(newLogRecord).execute();


            Log.d(TAG, "Updating device info.");
            NfcRecord deviceInfoUpdated;
            if (newLogRecord.getState()){
                deviceInfoUpdated = mDeviceEndpointService.increaseDeviceUsers(nfcTag).execute();
            } else {
                deviceInfoUpdated = mDeviceEndpointService.decreaseDeviceUsers(nfcTag).execute();
            }

            /* Check if there was a OFF/ON-ON/OFF transition of the device */

            if (deviceInfoUpdated.getState() <= 1){
                insertedDeviceLog.setId(null);
                insertedDeviceLog.setUser(Constants.ANYUSER);
                Log.e(TAG, "Inserting ANYUSER log record. " + insertedDeviceLog.getState().toString() );
                mDeviceLogEndpointService.insertDeviceLog(insertedDeviceLog).execute();
            }



            return true;
        } catch (IOException e) {
            /* Tag is not yet registered*/

            Log.e(TAG, "Previously registered device: FALSE");
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean tagExists) {
        if (tagExists) {
            //Toast.makeText(context, "Tag exists", Toast.LENGTH_LONG).show();
            Logger.getLogger(TAG).log(Level.INFO, "Tag exists");
        } else {
            //Toast.makeText(context, "Tag does not exists", Toast.LENGTH_LONG).show();
            Logger.getLogger(TAG).log(Level.INFO, "Tag does not exists");
            DeviceRegistrationAlertDialog dialog = new DeviceRegistrationAlertDialog();
            dialog.setNfcTag(nfcTag);
            dialog.show(mFragmentManager, "newDeviceDialog");
        }
    }


}
