package tudelft.mdp.services;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
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
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.Vibrator;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import tudelft.mdp.communication.SendDataSyncThread;
import tudelft.mdp.communication.SendFileByMessagesThread;
import tudelft.mdp.communication.SendMessageThread;
import tudelft.mdp.enums.Constants;
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
    private static boolean isStarted = false;
    private static boolean isConnected = false;
    private Timer mTimerSendSnapshot;
    private Timer mTimerDoSnapshot;
    private int snapshotCounter = 0;
    private int counter = 0;
    private boolean saveFileRequired = false;

    private static final String LOGTAG = "MDP-Wear SensorReaderService";

    GoogleApiClient mGoogleApiClient;

    public static final int MSG_REGISTER_CLIENT = 1;
    public static final int MSG_UNREGISTER_CLIENT = 2;
    public static final int MSG_SET_SENSOR_EVENT_VALUE = 5;
    public static final int MSG_SET_BUNDLE_VALUE = 6;

    private float [] mAccelerometer = {0f,0f,0f};
    private float [] mGyroscope = {0f,0f,0f};
    private float [] mGravity ={0f,0f,0f};
    private float [] mMagneticField = {0f,0f,0f};
    private float [] mHeartRate = {0f,0f,0f};
    private float [] mTilt = {0f,0f,0f};
    private float [] mRotatioVector = {0f,0f,0f,0f,0f};
    private float [] mLinearAccelerometer = {0f,0f,0f};

    private ArrayList<String> snapshotArray = new ArrayList<String>();

    private String filename;



    public SensorReaderService() {
    }


    @Override
    public void onCreate() {
        Log.i(LOGTAG, "Service Created.");
        //configureSensing();
        configureBroadcastReceivers();
        buildGoogleClient();

        isRunning = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mTimerDoSnapshot != null) {
            mTimerDoSnapshot.cancel();
        }

        if (mTimerSendSnapshot != null) {
            mTimerSendSnapshot.cancel();
        }

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }

        isRunning = false;
        isStarted = false;
        isConnected = false;

        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
        }

        Log.i(LOGTAG, "Service Stopped.");
        Log.i(LOGTAG, "Unregistered Sensor Listener.");
    }


    public static boolean isRunning()
    {
        return isRunning;
    }
    public static boolean isStarted()
    {
        return isStarted;
    }
    public static boolean isConnected()
    {
        return isConnected;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(LOGTAG, "onBind");
        return mMessenger.getBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(LOGTAG, "Received start id " + startId + ": " + intent);
        isStarted = true;
        return START_NOT_STICKY;
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

    @Override
    public void onConnected(Bundle connectionHint) {
        isConnected = true;
    }

    @Override
    public void onConnectionSuspended(int cause) { isConnected = false; }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) { isConnected = false; }



    private void configureBroadcastReceivers(){
        // Register the local broadcast receiver, defined in step 3.
        IntentFilter bundleFilter = new IntentFilter(MessagesProtocol.WEARSENSORSBUNDLE);
        DataBundleReceiver dataBundleReceiver = new DataBundleReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(dataBundleReceiver, bundleFilter);
    }




    public void configureSensing() {
        // Get the SensorManager
        mSensorManager= (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);


        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){
            Log.e(LOGTAG, "Sensor registered: Accelerometer");
            mSensorManager.registerListener(this,
                    mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                    Constants.SAMPLING_RATE);
                    //SensorManager.SENSOR_DELAY_GAME);
        }
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null){
            Log.i(LOGTAG, "Sensor registered: Gyroscope");
            mSensorManager.registerListener(this,
                    mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                    Constants.SAMPLING_RATE);
                    //SensorManager.SENSOR_DELAY_GAME);
        }
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null){
            Log.i(LOGTAG, "Sensor registered: Magnetic Field");
            mSensorManager.registerListener(this,
                    mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                    Constants.SAMPLING_RATE);
                    //SensorManager.SENSOR_DELAY_GAME);
        }
        if (mSensorManager.getDefaultSensor(Constants.SAMSUNG_TILT) != null){
            Log.i(LOGTAG, "Sensor registered: Tilt sensor");
            mSensorManager.registerListener(this,
                    mSensorManager.getDefaultSensor(Constants.SAMSUNG_TILT),
                    Constants.SAMPLING_RATE);
                    //SensorManager.SENSOR_DELAY_GAME);
        }
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) != null){
            Log.i(LOGTAG, "Sensor registered: Rotation Vector");
            mSensorManager.registerListener(this,
                    mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
                    Constants.SAMPLING_RATE);
                    //SensorManager.SENSOR_DELAY_GAME);
        }
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null){
            Log.i(LOGTAG, "Sensor registered: Linear Acceleration");
            mSensorManager.registerListener(this,
                    mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
                    Constants.SAMPLING_RATE);
                    //SensorManager.SENSOR_DELAY_GAME);
        }
       /* if (mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null){
            Log.i(LOGTAG, "Sensor registered: Step Counter");
            mSensorManager.registerListener(this,
                    mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER),
                    SensorManager.SENSOR_DELAY_FASTEST);
        }

        if (mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY) != null){
            Log.i(LOGTAG, "Sensor registered: Gravity");
            mSensorManager.registerListener(this,
                    mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY),
                    Constants.SAMPLING_RATE);
                    //SensorManager.SENSOR_DELAY_GAME);
        }
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE) != null){
            Log.i(LOGTAG, "Sensor registered: Heart Rate");
            mSensorManager.registerListener(this,
                    mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE),
                    SensorManager.SENSOR_DELAY_FASTEST);
        }
        if (mSensorManager.getDefaultSensor(Constants.SAMSUNG_HEART_RATE) != null){
           *Log.i(LOGTAG, "Sensor registered: Heart Rate");
            mSensorManager.registerListener(this,
                    mSensorManager.getDefaultSensor(Constants.SAMSUNG_HEART_RATE),
                    SensorManager.SENSOR_DELAY_FASTEST);
        }
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null){
            Log.i(LOGTAG, "Sensor registered: Light");
            mSensorManager.registerListener(this,
                    mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT),
                    SensorManager.SENSOR_DELAY_FASTEST);
        }
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR) != null){
            Log.i(LOGTAG, "Sensor registered: Step Detector");
            mSensorManager.registerListener(this,
                    mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR),
                    SensorManager.SENSOR_DELAY_FASTEST);
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
        } */

        pause = true;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.i(LOGTAG, "New sensor event " + counter);
        if (!pause) {
            String message = event.sensor.getType() + "|";

            if ((event.sensor.getType() == Constants.SAMSUNG_TILT) ||
                (event.sensor.getType() == Constants.SAMSUNG_HEART_RATE)) {
                for (int i = 0; i < 3; i++) {
                    message += String.format("%.2f", event.values[i]) + " ";
                }
            } else {
                for (int i = 0; i < event.values.length; i++) {
                    message += String.format("%.2f", event.values[i]) + " ";
                }
            }

            copySensorValues(event);
            counter++;
        }

    }

    private void copySensorValues(SensorEvent event){
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                System.arraycopy(event.values, 0, mAccelerometer, 0, event.values.length);
                break;
            case Sensor.TYPE_GYROSCOPE:
                System.arraycopy(event.values, 0, mGyroscope, 0, event.values.length);
                break;
            case Sensor.TYPE_GRAVITY:
                System.arraycopy(event.values, 0, mGravity, 0, event.values.length);
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                System.arraycopy(event.values, 0, mMagneticField, 0, event.values.length);
                break;
            case Constants.SAMSUNG_HEART_RATE:
                System.arraycopy(event.values, 0, mHeartRate, 0, 3);
                break;
            case Constants.SAMSUNG_TILT:
                System.arraycopy(event.values, 0, mTilt, 0, 3);
                break;
            case Sensor.TYPE_LINEAR_ACCELERATION:
                System.arraycopy(event.values, 0, mLinearAccelerometer, 0, event.values.length);
                break;
            case Sensor.TYPE_ROTATION_VECTOR:
                System.arraycopy(event.values, 0, mRotatioVector, 0, event.values.length);
                break;
            default:
                break;
        }
    }

    private void saveSnapshot(){
        String record = String.valueOf(++snapshotCounter) + "\t";
        record += new SimpleDateFormat("HHmmssSSS").format(System.currentTimeMillis()) + "\t";

        for(float value : mAccelerometer){
            record += String.format("%.2f", value) + "\t";
        }
        for(float value : mGyroscope){
            record += String.format("%.2f", value) + "\t";
        }/*
        for(float value : mGravity){
            record += String.format("%.2f", value) + "\t";
        }*/
        for(float value : mMagneticField){
            record += String.format("%.2f", value) + "\t";
        }
        for(float value : mLinearAccelerometer){
            record += String.format("%.2f", value) + "\t";
        }

        for(int i = 0; i < 3; i++){
            float value = mTilt[i];
            record += String.format("%.2f", value) + "\t";
        }
        //5th is always 0
        for(int i = 0; i < 4; i++){
            float value = mRotatioVector[i];
            record += String.format("%.2f", value) + "\t";
        }
        /*
        for(int i = 0; i < 3; i++){
            float value = mHeartRate[i];
            record += String.format("%.2f", value) + "\t";
        }
        */
        snapshotArray.add(record);
    }

    private void sendUpdate(){
        Log.e(LOGTAG, "SEND UPDATE");
        if (snapshotArray.size() > 0) {
            //new SendMessageThread(mGoogleApiClient, MessagesProtocol.MSGPATH, MessagesProtocol.SENDSENSEORSNAPSHOTUPDATE + "|" + snapshotArray.get(snapshotArray.size() - 1)).start();
            sendMessageToUI(Constants.SNAPSHOT_SENSORS, snapshotArray.get(snapshotArray.size() - 1));
        }
    }

    private void sendMessageSensorEventToUI(Integer sensorType, float[] sensorEvent) {
        for (Messenger messenger : mClients){
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

    private void sendMessageToUI(Integer sensorType, String sensorEvent) {
        for (Messenger messenger : mClients){
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
        for (Messenger messenger : mClients){
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

    private void sendRecordsToMobile(){
        ArrayList<String> records = new ArrayList<String>(snapshotArray);
        Log.e(LOGTAG,"Start sending file. Number of records: " + snapshotArray.size() + "-" + records.size());

        new SendFileByMessagesThread(mGoogleApiClient, MessagesProtocol.MSGPATH, records).start();
        snapshotArray.clear();
    }

    private void sensingInit(){

        Log.e(LOGTAG,"Start sensing");

        configureSensing();
        Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(500);

        counter = 0;
        snapshotCounter = 0;
        snapshotArray.clear();

        /*mTimerSendSnapshot  = new Timer();
        mTimerSendSnapshot.scheduleAtFixedRate(new SnapshotTick(), 0, Constants.TIMER_UPDATE_RATE);*/


        mTimerDoSnapshot  = new Timer();
        mTimerDoSnapshot.scheduleAtFixedRate(new UpdateSnapshotTick(), 0, Constants.TIMER_SAMPLING_RATE);


        pause = false;
    }

    private void sensingFinish(){

        Log.e(LOGTAG,"Stop sensing");
        if (mSensorManager != null){ mSensorManager.unregisterListener(this); }

        Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(500);

        if (mTimerSendSnapshot != null) {
            Log.e(LOGTAG,"Stop Timer mTimerSendSnapshot");
            mTimerSendSnapshot.cancel();
        }

        if (mTimerDoSnapshot != null) {
            Log.e(LOGTAG,"Stop Timer mTimerDoSnapshot");
            mTimerDoSnapshot.cancel();
        }


        if (saveFileRequired){
            Log.e(LOGTAG,"Call sendRecordsToMobile()");
            sendRecordsToMobile();
        }
        saveFileRequired = false;
        pause = true;
    }


    private void executeCommand(int command){
        switch (command){
            case MessagesProtocol.STARTSENSING:
                sensingInit();
                break;
            case MessagesProtocol.STOPSENSING:
                sensingFinish();
                break;
            case MessagesProtocol.KILLSERVICE:
                stopSelf();
            default:
                break;
        }
    }

    private void handleMessage(Bundle bundle){
        Integer sender =bundle.getInt(MessagesProtocol.SENDER, 0);
        if (sender == MessagesProtocol.ID_MOBILE){
            filename = bundle.getString(MessagesProtocol.MESSAGE, "");
            if (filename.length() > 0){
                Log.e(LOGTAG, "Message received from mobile:" + filename);
                saveFileRequired = true;
            }
            int command = bundle.getInt(MessagesProtocol.MSGTYPE, -1);
            executeCommand(command);
        }

    }


    private class DataBundleReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle message = intent.getExtras();
            handleMessage(message);
            sendBundleToUI(message);
        }
    }

    private class IncomingMessageHandler extends Handler {
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

    private class SnapshotTick extends TimerTask {
        @Override
        public void run() {
            Log.w(LOGTAG, "Taking SnapshotTick " + counter);
            try {
                sendUpdate();
            } catch (Throwable t) {
                Log.e("SnapshotTick", "SnapshotTick Failed.", t);
            }
        }
    }

    private class UpdateSnapshotTick extends TimerTask {
        @Override
        public void run() {
            Log.w(LOGTAG, "Taking UpdateSnapshotTick " + counter);
            try {
                saveSnapshot();
            } catch (Throwable t) {
                Log.e("SnapshotTick", "SnapshotTick Failed.", t);
            }
        }
    }


}
