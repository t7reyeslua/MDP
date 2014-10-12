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
import android.hardware.TriggerEventListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
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
    private Timer mTimer;
    private int counter = 0;
    private int snapshotCounter = 0;
    private boolean saveFileRequired = false;

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

    private float [] mAccelerometer = {0f,0f,0f};
    private float [] mGyroscope = {0f,0f,0f};
    private float [] mGravity ={0f,0f,0f};
    private float [] mMagneticField = {0f,0f,0f};
    private float [] mHeartRate = {0f,0f,0f};
    private float [] mTilt = {0f,0f,0f};
    private float [] mRotatioVector = {0f,0f,0f,0f,0f};
    private float [] mLinearAccelerometer = {0f,0f,0f};

    private ArrayList<String> snapshotArray =  new ArrayList<String>();

    private String filename;

    public SensorReaderService() {
    }


    @Override
    public void onCreate() {

        Log.i(LOGTAG, "Service Created.");
        configureSensing();
        buildGoogleClient();

        // Register the local broadcast receiver, defined in step 3.
        IntentFilter bundleFilter = new IntentFilter(MessagesProtocol.WEARSENSORSBUNDLE);
        DataBundleReceiver dataBundleReceiver = new DataBundleReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(dataBundleReceiver, bundleFilter);


        IntentFilter messageFilter = new IntentFilter(MessagesProtocol.WEARSENSORSMSG);
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
        }
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
       /* if (mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null){
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
        } */
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
        }

        pause = true;
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

        if (!pause) {
            String message = event.sensor.getType() + "|";

            if ((event.sensor.getType() == Constants.SAMSUNG_TILT) || (event.sensor.getType()
                    == Constants.SAMSUNG_HEART_RATE)) {
                for (int i = 0; i < 3; i++) {
                    message += String.format("%.2f", event.values[i]) + " ";
                }
            } else {
                for (int i = 0; i < event.values.length; i++) {
                    message += String.format("%.2f", event.values[i]) + " ";
                }
            }

            new SendMessageThread(mGoogleApiClient, MessagesProtocol.MSGPATH, message).start();

            if (saveFileRequired) {
                copySensorValues(event);
            }
            sendMessageSensorEventToUI(event.sensor.getType(), event.values);
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
        record += new SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(System.currentTimeMillis()) + "\t";

        for(float value : mAccelerometer){
            record += String.format("%.2f", value) + "\t";
        }
        for(float value : mGyroscope){
            record += String.format("%.2f", value) + "\t";
        }
        for(float value : mGravity){
            record += String.format("%.2f", value) + "\t";
        }
        for(float value : mMagneticField){
            record += String.format("%.2f", value) + "\t";
        }
        for(float value : mLinearAccelerometer){
            record += String.format("%.2f", value) + "\t";
        }
        for(float value : mRotatioVector){
            record += String.format("%.2f", value) + "\t";
        }
        for(int i = 0; i < 3; i++){
            float value = mTilt[i];
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

    private class SnapshotTick extends TimerTask {
        @Override
        public void run() {
            //Log.i(LOGTAG, "Taking Snapshot " + counter);
            try {
                //sendMessageIntToUI(counter);
                if (saveFileRequired) {
                    saveSnapshot();
                }

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

    private void executeCommand(int command){
        switch (command){
            case MessagesProtocol.STARTSENSING:

                Log.e(LOGTAG,"Start sensing");
                mTimer  = new Timer();
                mTimer.scheduleAtFixedRate(new SnapshotTick(), 0, 50L);
                pause = false;
                break;
            case MessagesProtocol.STOPSENSING:

                Log.e(LOGTAG,"Stop sensing");
                if (mTimer != null) {

                    Log.e(LOGTAG,"Stop Timer");
                    mTimer.cancel();
                }
                if (saveFileRequired){

                    Log.e(LOGTAG,"Call sendRecordsToMobile()");
                    sendRecordsToMobile();
                }
                saveFileRequired = false;
                pause = true;
                //stopSelf();
                break;
            default:
                break;
        }
    }

    private void sendRecordsToMobile(){

        Log.e(LOGTAG,"Start sending file");

        int sendType = 0;

        if (sendType == 0){
            new SendFileByMessagesThread(mGoogleApiClient, MessagesProtocol.MSGPATH, snapshotArray).start();
            /*
            String message = MessagesProtocol.SENDSENSEORSNAPSHOTREC_START + "| Start saving file";
            new SendMessageThread(mGoogleApiClient, MessagesProtocol.MSGPATH, message).start();

            List<String> syncSnapshot = Collections.synchronizedList(snapshotArray);
            for(String record : syncSnapshot){
                Log.e(LOGTAG,"Send record: " + record);
                message = MessagesProtocol.SENDSENSEORSNAPSHOTREC + "|" + record;
                new SendMessageThread(mGoogleApiClient, MessagesProtocol.MSGPATH, message).start();
            }


            Log.e(LOGTAG,"Stop sending file");
            message = MessagesProtocol.SENDSENSEORSNAPSHOTREC_FINISH + "| Finish saving file";

            new SendMessageThread(mGoogleApiClient, MessagesProtocol.MSGPATH, message).start();
            */
        } else if (sendType == 1) {

            DataMap dataMap = new DataMap();

            dataMap.putDouble(MessagesProtocol.NOTIFICATIONTIMESTAMP, System.currentTimeMillis());
            dataMap.putInt(MessagesProtocol.SENDER, MessagesProtocol.ID_WEAR);
            dataMap.putInt(MessagesProtocol.MSGTYPE, MessagesProtocol.SENDSENSEORSNAPSHOTREC);
            dataMap.putStringArrayList(MessagesProtocol.RECORDEDSENSORS, snapshotArray);


            new SendDataSyncThread(mGoogleApiClient, MessagesProtocol.DATAPATH, dataMap).start();
        } else if (sendType == 2) {
            // write to byte array

            int length = 0;
            for (String record : snapshotArray){
                length += record.getBytes(Charset.forName("UTF-8")).length;
            }

            byte[] byteArray = new byte[length];
            int i = 0;
            for (String record : snapshotArray){
                int recordLength = record.getBytes(Charset.forName("UTF-8")).length;
                System.arraycopy(
                        record.getBytes(Charset.forName("UTF-8")), 0,
                        byteArray, i,
                        recordLength);
                i += recordLength;
            }



            Asset asset = Asset.createFromBytes(byteArray);
            PutDataMapRequest dataMap = PutDataMapRequest.create(MessagesProtocol.FILEPATH);
            dataMap.getDataMap().putAsset(MessagesProtocol.RECORDEDSENSORS, asset);
            dataMap.getDataMap().putString(MessagesProtocol.MESSAGE, filename);
            PutDataRequest request = dataMap.asPutDataRequest();
            PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi
                    .putDataItem(mGoogleApiClient, request);
        }



    }


    private void handleMessage(Bundle bundle){
        Integer sender =bundle.getInt(MessagesProtocol.SENDER, 0);
        if (sender == MessagesProtocol.ID_MOBILE){
            filename = bundle.getString(MessagesProtocol.MESSAGE, "");
            if (filename.length() > 0){
                saveFileRequired = true;
            }
            int command = bundle.getInt(MessagesProtocol.MSGTYPE, -1);
            executeCommand(command);
        }

    }

    private void handleMessage(String msg){
        String[] parts = msg.split("\\|");
        String commandStr = parts[0];
        String message = parts[1];

        Integer command = Integer.valueOf(commandStr);
        executeCommand(command);
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


    private class DataBundleReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle message = intent.getExtras();
            handleMessage(message);
            sendBundleToUI(message);
            // Display message in UI
        }
    }

    private class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            String message = intent.getStringExtra(MessagesProtocol.MESSAGE);
            handleMessage(message);
            // Display message in UI
        }
    }

}
