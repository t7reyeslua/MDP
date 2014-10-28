package tudelft.mdp.gcm;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.os.Vibrator;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import android.os.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import tudelft.mdp.enums.MessagesProtocol;

public class GcmIntentService extends IntentService {

    public static final String LOGTAG = "MDP GcmIntentService";

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (extras != null && !extras.isEmpty()) {  // has effect of unparcelling Bundle
            // Since we're not using two way messaging, this is all we really to check for
            if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                Logger.getLogger("GCM_RECEIVED").log(Level.INFO, extras.toString());

                String message = extras.getString("message");

                //showToast(message);
                handleMessage(message);
            }
        }
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    protected void showToast(final String message) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        });
        vibrate();
    }

    protected void vibrate() {
        Vibrator v = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(500);
    }

    protected void handleMessage(String msg){
        String[] parts = msg.split("\\|");
        Integer msgType = 0;
        String msgLoad = msg;

        if(parts.length > 1) {
            msgType = Integer.valueOf(parts[0]);
            msgLoad = parts[1];
        }

        Intent messageIntent;
        switch (msgType){
            case MessagesProtocol.SENDGCM_CMD_LOCATION:
                Log.i(LOGTAG, "CMD received from GCM: " + MessagesProtocol.COLLECTDATA_LOCATION);
                // Broadcast message to interested parties
                messageIntent = new Intent(MessagesProtocol.COLLECTDATA_LOCATION);
                messageIntent.putExtra(MessagesProtocol.MESSAGE, msgLoad);
                LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent);
                break;
            case MessagesProtocol.SENDGCM_CMD_MOTION:
                Log.i(LOGTAG, "CMD received from GCM: " + MessagesProtocol.COLLECTDATA_MOTION);
                // Broadcast message to interested parties
                messageIntent = new Intent(MessagesProtocol.COLLECTDATA_MOTION);
                messageIntent.putExtra(MessagesProtocol.MESSAGE, msgLoad);
                LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent);
                break;
            case MessagesProtocol.SENDGCM_CMD_MOTIONLOCATION:
                Log.i(LOGTAG, "CMD received from GCM: " + MessagesProtocol.COLLECTDATA_MOTIONLOCATION);
                // Broadcast message to interested parties
                messageIntent = new Intent(MessagesProtocol.COLLECTDATA_MOTIONLOCATION);
                messageIntent.putExtra(MessagesProtocol.MESSAGE, msgLoad);
                LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent);
                break;
            case MessagesProtocol.SENDGCM_CMD_UPDATEGAUSSIANS:
                Log.i(LOGTAG, "CMD received from GCM: " + MessagesProtocol.COLLECTDATA_MOTIONLOCATION);
                // Broadcast message to interested parties
                messageIntent = new Intent(MessagesProtocol.UPDATE_GAUSSIANS);
                messageIntent.putExtra(MessagesProtocol.MESSAGE, msgLoad);
                LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent);
                break;
            case MessagesProtocol.SENDGCM_MSG:
                showToast(msgLoad);
                Log.i(LOGTAG, "CMD received from GCM: " + MessagesProtocol.SENDGCM_MSG);
                // Broadcast message to interested parties
                messageIntent = new Intent(MessagesProtocol.MSG_RECEIVED);
                messageIntent.putExtra(MessagesProtocol.MESSAGE, msgLoad);
                LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent);
                break;
            default:
                showToast(msgLoad);
                break;
        }
    }



}
