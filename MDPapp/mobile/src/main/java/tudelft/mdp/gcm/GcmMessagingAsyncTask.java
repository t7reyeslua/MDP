package tudelft.mdp.gcm;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

import tudelft.mdp.backend.endpoints.messaging.Messaging;

public class GcmMessagingAsyncTask extends AsyncTask<Object, Void, Boolean> {

    private static final String TAG = "MDP-GcmMessagingAsyncTask";
    private static Messaging mMessagingEndpointService = null;

    private Context context;
    private String msgType;
    private String msgLoad;

    @Override
    protected Boolean doInBackground(Object... params) {

        msgType             = (String)  params[0];
        msgLoad             = (String)  params[1];
        context             = (Context) params[2];


        if (mMessagingEndpointService == null) {
            /* For testing against a deployed backend */
            Messaging.Builder builder = new Messaging.Builder(
                    AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null);
            mMessagingEndpointService = builder.build();
        }

        try {
            Log.e(TAG, "Calling mMessagingEndpointService.sendMessage");
            mMessagingEndpointService.messagingEndpoint().sendMessage(msgType + "|" + msgLoad).execute();
            return true;
        } catch (IOException e) {
            return false;
        }

    }

    @Override
    protected void onPostExecute(Boolean msgSent) {
        if (msgSent) {
            //Toast.makeText(context, "Message broadcast ["+ msgType +"] :" + msgLoad , Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Successfully sent message.");
        } else {
            Toast.makeText(context, "Oops! Something happened. Try again later.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error while sending message");
        }
    }

}
