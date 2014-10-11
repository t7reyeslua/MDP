package tudelft.mdp.services;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.TriggerEventListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import tudelft.mdp.communication.SendDataSyncThread;
import tudelft.mdp.communication.SendMessageThread;
import tudelft.mdp.enums.MessagesProtocol;

public class SensorReaderService extends Service implements
        SensorEventListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    // Target we publish for clients to send messages to IncomingHandler.
    private final Messenger mMessenger = new Messenger(new IncomingMessageHandler());
    private List<Messenger> mClients = new ArrayList<Messenger>();
    public static boolean pause;
    private SensorManager mSensorManager;

    private static boolean isRunning = false;
    private Timer mTimer = new Timer();
    private int counter = 0;

    private static final String LOGTAG = "MDP-Wear SensorReaderService";
    private Sensor mSigmotion;

    GoogleApiClient mGoogleApiClient;

    private TriggerEventListener mTriggerEventListener;

    public static final int MSG_REGISTER_CLIENT = 1;
    public static final int MSG_UNREGISTER_CLIENT = 2;
    public static final int MSG_SET_INT_VALUE = 3;
    public static final int MSG_SET_STRING_VALUE = 4;
    public static final int MSG_SET_SENSOR_EVENT_VALUE = 5;
    public static final int MSG_SET_BUNDLE_VALUE = 6;


    public SensorReaderService() {
    }


    @Override
    public void onCreate() {

        Log.i(LOGTAG, "Service Created.");
        configureSensing();
        buildGoogleClient();
        mTimer.scheduleAtFixedRate(new SnapshotTick(), 0, 1000L);

        // Register the local broadcast receiver, defined in step 3.
        IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
        MessageReceiver messageReceiver = new MessageReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, messageFilter);

        isRunning = true;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(LOGTAG, "onBind");
        return mMessenger.getBinder();
    }

    private void buildGoogleClient(){
        // Build a new GoogleApiClient
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mGoogleApiClient.connect();

    }

    // Send a data object when the data layer connection is successful.
    @Override
    public void onConnected(Bundle connectionHint) {


        // Create a DataMap object and send it to the data layer
        DataMap dataMap = new DataMap();
        dataMap.putLong(MessagesProtocol.TIMESTAMP, new Date().getTime());
        dataMap.putInt(MessagesProtocol.SENDER, MessagesProtocol.ID_WEAR);
        dataMap.putInt(MessagesProtocol.MSGTYPE, MessagesProtocol.SNDMESSAGE);
        dataMap.putString(MessagesProtocol.MESSAGE, "Hello from Wear");

        new SendDataSyncThread(mGoogleApiClient, MessagesProtocol.DATAPATH, dataMap).start();


        /*
        String msg = "0|Hello from Wear";
        new SendMessageThread(mGoogleApiClient, MessagesProtocol.MSGPATH, msg).start();
        */


    }

    // Placeholders for required connection callbacks
    @Override
    public void onConnectionSuspended(int cause) { }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) { }



    public void configureSensing() {
        // Get the SensorManager
        mSensorManager= (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);


        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){
            Log.e(LOGTAG, "Sensor registered: Accelerometer");
            mSensorManager.registerListener(this,
                    mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                    //3333);
                    SensorManager.SENSOR_DELAY_NORMAL);
        }/*
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null){
            Log.i(LOGTAG, "Sensor registered: Gyroscope");
            mSensorManager.registerListener(this,
                    mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY) != null){
            Log.i(LOGTAG, "Sensor registered: Gravity");
            mSensorManager.registerListener(this,
                    mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY),
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null){
            Log.i(LOGTAG, "Sensor registered: Magnetic Field");
            mSensorManager.registerListener(this,
                    mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null){
            Log.i(LOGTAG, "Sensor registered: Step Counter");
            mSensorManager.registerListener(this,
                    mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER),
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE) != null){
            Log.i(LOGTAG, "Sensor registered: Heart Rate");
            mSensorManager.registerListener(this,
                    mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE),
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (mSensorManager.getDefaultSensor(Constants.SAMSUNG_HEART_RATE) != null){
           *Log.i(LOGTAG, "Sensor registered: Heart Rate");
            mSensorManager.registerListener(this,
                    mSensorManager.getDefaultSensor(Constants.SAMSUNG_HEART_RATE),
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null){
            Log.i(LOGTAG, "Sensor registered: Light");
            mSensorManager.registerListener(this,
                    mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT),
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR) != null){
            Log.i(LOGTAG, "Sensor registered: Step Detector");
            mSensorManager.registerListener(this,
                    mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR),
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_SIGNIFICANT_MOTION) != null){
            Log.i(LOGTAG, "Sensor registered: Significant Motion");
            mSigmotion = mSensorManager.getDefaultSensor(Sensor.TYPE_SIGNIFICANT_MOTION);



            mTriggerEventListener = new TriggerEventListener() {
                @Override
                public void onTrigger(TriggerEvent event) {
                    sendMessageSensorEventToUI(event.sensor.getType(), event.values);
                    //mSensorManager.requestTriggerSensor(mTriggerEventListener, mSigmotion);
                }
            };
            mSensorManager.requestTriggerSensor(mTriggerEventListener, mSigmotion);
        }
        if (mSensorManager.getDefaultSensor(Constants.SAMSUNG_TILT) != null){
            Log.i(LOGTAG, "Sensor registered: Tilt sensor");
            mSensorManager.registerListener(this,
                    mSensorManager.getDefaultSensor(Constants.SAMSUNG_TILT),
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) != null){
            Log.i(LOGTAG, "Sensor registered: Rotation Vector");
            mSensorManager.registerListener(this,
                    mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null){
            Log.i(LOGTAG, "Sensor registered: Linear Acceleration");
            mSensorManager.registerListener(this,
                    mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
                    SensorManager.SENSOR_DELAY_NORMAL);
        }*/

        pause = false;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(LOGTAG, "Received start id " + startId + ": " + intent);
        return START_STICKY; // Run until explicitly stopped.
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public static boolean isRunning()
    {
        return isRunning;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mTimer != null) {
            mTimer.cancel();
        }

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }

        Log.i(LOGTAG, "Service Stopped.");
        isRunning = false;
        mSensorManager.unregisterListener(this);
        if (mTriggerEventListener != null) {
            mSensorManager.cancelTriggerSensor(mTriggerEventListener, mSigmotion);
        }
        Log.i(LOGTAG, "Unregistered Sensor Listener.");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        sendMessageSensorEventToUI(event.sensor.getType(), event.values);
        counter++;

        // Create a DataMap object and send it to the data layer
        if (mGoogleApiClient.isConnected()) {
            DataMap dataMap = new DataMap();
            dataMap.putLong(MessagesProtocol.TIMESTAMP, new Date().getTime());
            dataMap.putInt(MessagesProtocol.SENDER, MessagesProtocol.ID_WEAR);
            dataMap.putInt(MessagesProtocol.MSGTYPE, MessagesProtocol.SENSOREVENT);
            dataMap.putInt(MessagesProtocol.SENSORTYPE, event.sensor.getType());
            dataMap.putFloatArray(MessagesProtocol.SENSORVALUE, event.values);

            //Requires a new thread to avoid blocking the UI
            new SendDataSyncThread(mGoogleApiClient, MessagesProtocol.DATAPATH, dataMap).start();
        } else {
            Log.e(LOGTAG, "Google API client reconnection");
            mGoogleApiClient.connect();
        }
    }

    private class SnapshotTick extends TimerTask {
        @Override
        public void run() {
            Log.i(LOGTAG, "Taking Snapshot " + counter);
            try {
                sendMessageIntToUI(counter);

            } catch (Throwable t) { //you should always ultimately catch all exceptions in timer tasks.
                Log.e("SnapshotTick", "SnapshotTick Failed.", t);
            }
        }
    }


    private void sendMessageIntToUI(int intvaluetosend) {
        Iterator<Messenger> messengerIterator = mClients.iterator();
        while(messengerIterator.hasNext()) {
            Messenger messenger = messengerIterator.next();
            try {
                // Send data as an Integer
                messenger.send(Message.obtain(null, MSG_SET_INT_VALUE, intvaluetosend, 0));


            } catch (RemoteException e) {
                // The client is dead. Remove it from the list.
                mClients.remove(messenger);
            }
        }
    }

    private void sendMessageSensorEventToUI(Integer sensorType, float[] sensorEvent) {
        Iterator<Messenger> messengerIterator = mClients.iterator();
        while(messengerIterator.hasNext()) {
            Messenger messenger = messengerIterator.next();
            try {

                ArrayList<Object> valuesToSend = new ArrayList<Object>();
                valuesToSend.add(sensorType);
                valuesToSend.add(sensorEvent);

                Bundle bundle = new Bundle();
                bundle.putSerializable("sensor", valuesToSend);
                Message msg = Message.obtain(null, MSG_SET_SENSOR_EVENT_VALUE);
                msg.setData(bundle);
                messenger.send(msg);

            } catch (RemoteException e) {
                // The client is dead. Remove it from the list.
                mClients.remove(messenger);
            }
        }
    }

    private void sendBundleToUI(Bundle bundle) {
        Iterator<Messenger> messengerIterator = mClients.iterator();
        while(messengerIterator.hasNext()) {
            Messenger messenger = messengerIterator.next();
            try {

                Message msg = Message.obtain(null, MSG_SET_BUNDLE_VALUE);
                msg.setData(bundle);
                messenger.send(msg);

            } catch (RemoteException e) {
                // The client is dead. Remove it from the list.
                mClients.remove(messenger);
            }
        }
    }

    /**
     * Handle incoming messages from MainActivity
     */
    private class IncomingMessageHandler extends Handler { // Handler of incoming messages from clients.
        @Override
        public void handleMessage(Message msg) {
            Log.d(LOGTAG,"handleMessage: " + msg.what);
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    mClients.add(msg.replyTo);
                    break;
                case MSG_UNREGISTER_CLIENT:
                    mClients.remove(msg.replyTo);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle message = intent.getExtras();
            sendBundleToUI(message);

            // Display message in UI
        }
    }


}
