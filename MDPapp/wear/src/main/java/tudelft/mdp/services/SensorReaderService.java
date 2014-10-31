package tudelft.mdp.services;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataMap;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import tudelft.mdp.Utils;
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
    private Timer mTimerRecordingSessionSnapshot;
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
    private float [] mMagneticFieldUncalibrated = {0f,0f,0f};
    private float [] mHeartRate = {0f,0f,0f};
    private float [] mTilt = {0f,0f,0f};
    private float [] mRotationVector = {0f,0f,0f,0f,0f};
    private float [] mGameRotationVector = {0f,0f,0f,0f,0f};
    private float [] mLinearAccelerometer = {0f,0f,0f};
    private float [] mStepCounter = {0f};
    private float [] mStepDetector = {0f};

    private ArrayList<String> snapshotArray = new ArrayList<String>();
    private ArrayList<Integer> sensorsToRecord = new ArrayList<Integer>();

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









    public void configureSensing(ArrayList<Integer> sensorsToRecord, Double Hz) {
        Integer samplingRate = 1000000/Hz.intValue();
        mSensorManager= (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        for (Integer sensorType : sensorsToRecord){
            Log.e(LOGTAG, "Sensor registered: " + Utils.getSensorName(sensorType));
            mSensorManager.registerListener(this,
                    mSensorManager.getDefaultSensor(sensorType),
                    samplingRate);
        }
        pause = true;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.i(LOGTAG, "New sensor event " + counter++);
        if (!pause) {
            copySensorValues(event);
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
                System.arraycopy(event.values, 0, mRotationVector, 0, event.values.length);
                break;
            case Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED:
                System.arraycopy(event.values, 0, mMagneticFieldUncalibrated, 0,  event.values.length);
                break;
            case Sensor.TYPE_GAME_ROTATION_VECTOR:
                System.arraycopy(event.values, 0, mGameRotationVector, 0, event.values.length);
                break;
            case Sensor.TYPE_STEP_COUNTER:
                System.arraycopy(event.values, 0, mStepCounter, 0, event.values.length);
                break;
            case Sensor.TYPE_STEP_DETECTOR:
                System.arraycopy(event.values, 0, mStepDetector, 0, event.values.length);
                break;
            default:
                break;
        }
    }

    private void saveSnapshot(){
        String record = String.valueOf(++snapshotCounter) + "\t";
        record += new SimpleDateFormat("HHmmssSSS").format(System.currentTimeMillis()) + "\t";

        for (Integer sensorType : sensorsToRecord){
            switch (sensorType) {
                case Sensor.TYPE_ACCELEROMETER:
                    for(float value : mAccelerometer){
                        record += String.format("%.4f", value) + "\t";
                    }
                    break;
                case Sensor.TYPE_GYROSCOPE:
                    for(float value : mGyroscope){
                        record += String.format("%.4f", value) + "\t";
                    }
                    break;
                case Sensor.TYPE_GRAVITY:
                    for(float value : mGravity){
                        record += String.format("%.4f", value) + "\t";
                    }
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    for(float value : mMagneticField){
                        record += String.format("%.4f", value) + "\t";
                    }
                    break;
                case Constants.SAMSUNG_HEART_RATE:
                    for(int i = 0; i < 3; i++){
                        float value = mHeartRate[i];
                        record += String.format("%.2f", value) + "\t";
                    }
                    break;
                case Constants.SAMSUNG_TILT:
                    for(int i = 0; i < 3; i++){
                        float value = mTilt[i];
                        record += String.format("%.4f", value) + "\t";
                    }
                    break;
                case Sensor.TYPE_LINEAR_ACCELERATION:
                    for(float value : mLinearAccelerometer){
                        record += String.format("%.4f", value) + "\t";
                    }
                    break;
                case Sensor.TYPE_ROTATION_VECTOR:
                    for(int i = 0; i <3; i++){
                        //TODO: change to 5
                        float value = mRotationVector[i];
                        record += String.format("%.4f", value) + "\t";
                    }
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED:
                    for(float value : mMagneticFieldUncalibrated){
                        record += String.format("%.4f", value) + "\t";
                    }
                    break;
                case Sensor.TYPE_GAME_ROTATION_VECTOR:
                    for(int i = 0; i <3; i++){
                        //TODO: change to 5
                        float value = mGameRotationVector[i];
                        record += String.format("%.4f", value) + "\t";
                    }
                    break;
                case Sensor.TYPE_STEP_COUNTER:
                    for(float value : mStepCounter){
                        record += String.format("%.2f", value) + "\t";
                    }
                    break;
                case Sensor.TYPE_STEP_DETECTOR:
                    for(float value : mStepDetector){
                        record += String.format("%.2f", value) + "\t";
                    }
                    break;
                default:
                    break;
            }
        }

        snapshotArray.add(record);
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






    private void sendUpdate(){
        Log.e(LOGTAG, "SEND UPDATE");
        if (snapshotArray.size() > 0) {
            new SendMessageThread(mGoogleApiClient, MessagesProtocol.MSGPATH, MessagesProtocol.SENDSENSEORSNAPSHOTUPDATE + "|" + snapshotArray.size()).start();
        }
    }

    private void sendRecordsToMobile(){
        ArrayList<String> records = new ArrayList<String>(snapshotArray);
        Log.e(LOGTAG,"Start sending file. Number of records: " + snapshotArray.size() + "-" + records.size());

        new SendFileByMessagesThread(mGoogleApiClient, MessagesProtocol.MSGPATH, records, sensorsToRecord).start();
        snapshotArray.clear();
    }


    private void sendDataMapToMobile(Integer msgType, String message){
        DataMap dataMap = new DataMap();
        dataMap.putInt(MessagesProtocol.SENDER, MessagesProtocol.ID_WEAR);
        dataMap.putInt(MessagesProtocol.MSGTYPE, msgType);
        dataMap.putString(MessagesProtocol.MESSAGE, message);
        dataMap.putDouble(MessagesProtocol.TIMESTAMP, System.currentTimeMillis());

        if (msgType.equals(MessagesProtocol.QUERYSENSORLISTRESPONSE)){
            ArrayList<Integer> mSensorList = retrieveSensorList();
            dataMap.putIntegerArrayList(MessagesProtocol.SENSORLISTRESULT, mSensorList);
        }

        new SendDataSyncThread(mGoogleApiClient, MessagesProtocol.DATAPATH, dataMap).start();
    }










    private void sensingInit(ArrayList<Integer> sensorsToRecord, Double hz, Integer duration){

        Log.e(LOGTAG,"Start sensing " + sensorsToRecord.size() + " sensors. Hz=" + hz + "Time=" + duration);

        configureSensing(sensorsToRecord, hz);
        Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(500);

        counter = 0;
        snapshotCounter = 0;
        snapshotArray.clear();


        Long mTimerSamplingRate = (long) 1000/hz.intValue();
        Log.i(LOGTAG, "Sampling Rate Tick:" + mTimerSamplingRate.toString());
        mTimerDoSnapshot  = new Timer();
        mTimerDoSnapshot.scheduleAtFixedRate(new SaveCurrentValuesSnapshotTick(), 0, mTimerSamplingRate);


        if (saveFileRequired) {
            mTimerSendSnapshot = new Timer();
            mTimerSendSnapshot
                    .scheduleAtFixedRate(new SendUpdateSnapshotTick(), 0, Constants.TIMER_UPDATE_RATE);
        }


        if (duration > 0) {
            Long mRecodingSessionSamplingRate = (long) 1000 * duration;
            Log.i(LOGTAG, "Recording Session Tick:" + mRecodingSessionSamplingRate.toString());
            mTimerRecordingSessionSnapshot = new Timer();
            mTimerRecordingSessionSnapshot
                    .scheduleAtFixedRate(new RecordingSessionSnapshotTick(), 0, mRecodingSessionSamplingRate);
        }

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

        if (mTimerRecordingSessionSnapshot != null) {
            Log.e(LOGTAG,"Stop Timer mTimerRecordingSessionSnapshot");
            mTimerRecordingSessionSnapshot.cancel();
        }

        if (saveFileRequired){
            Log.e(LOGTAG,"Call sendRecordsToMobile()");
            sendRecordsToMobile();
        }


        saveFileRequired = false;
        pause = true;


    }



    private ArrayList<Integer> retrieveSensorList(){
        mSensorManager= (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> mSensorList =  mSensorManager.getSensorList(Sensor.TYPE_ALL);
        ArrayList<Integer> mSesorListTypes = new ArrayList<Integer>();
        for (Sensor sensor : mSensorList){
            mSesorListTypes.add(sensor.getType());
        }

        Log.d(LOGTAG, "No. of sensors: " + mSesorListTypes.size());
        return  mSesorListTypes;
    }


    private void executeCommand(int command, Bundle bundle){
        switch (command){
            case MessagesProtocol.STARTSENSING:
                Log.d(LOGTAG, "Execute Command: START SENSING");
                sensorsToRecord = bundle.getIntegerArrayList(MessagesProtocol.SENSORSTORECORD);
                Double hz = bundle.getDouble(MessagesProtocol.SENSORHZ, 50);
                Integer duration = bundle.getInt(MessagesProtocol.SENSOR_RECORDING_SECONDS, 6);
                sensingInit(sensorsToRecord, hz, duration);
                break;
            case MessagesProtocol.STOPSENSING:
                Log.d(LOGTAG, "Execute Command: STOP SENSING");
                sensingFinish();
                break;
            case MessagesProtocol.QUERYSENSORLIST:
                Log.d(LOGTAG, "Execute Command: QUERY SENSOR LIST");
                sendDataMapToMobile(MessagesProtocol.QUERYSENSORLISTRESPONSE, "Sensor List Response");
                break;
            case MessagesProtocol.KILLSERVICE:
                stopSelf();
            default:
                break;
        }
    }

    private void handleMessage(Bundle bundle){

        Log.d(LOGTAG, "HandleMessage: " + bundle.toString());
        Integer sender =bundle.getInt(MessagesProtocol.SENDER, 0);
        if (sender == MessagesProtocol.ID_MOBILE){
            filename = bundle.getString(MessagesProtocol.MESSAGE, "");
            if (filename.length() > 0){
                Log.e(LOGTAG, "Message received from mobile:" + filename);
                saveFileRequired = true;
            }
            int command = bundle.getInt(MessagesProtocol.MSGTYPE, -1);
            executeCommand(command, bundle);
        }

    }


    private class DataBundleReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOGTAG, "DataBundleReceiver");
            Bundle message = intent.getExtras();
            handleMessage(message);
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


    private class SendUpdateSnapshotTick extends TimerTask {
        @Override
        public void run() {
            Log.w(LOGTAG, "Taking SendUpdateSnapshotTick " + counter);
            try {
                sendUpdate();
            } catch (Throwable t) {
                Log.e("SendUpdateSnapshotTick", "SendUpdateSnapshotTick Failed.", t);
            }
        }
    }

    private class SaveCurrentValuesSnapshotTick extends TimerTask {
        @Override
        public void run() {
            Log.w(LOGTAG, "Taking SaveCurrentValuesSnapshotTick " + counter);
            try {
                saveSnapshot();
            } catch (Throwable t) {
                Log.e("SaveCurrentValuesSnapshotTick", "SaveCurrentValuesSnapshotTick Failed.", t);
            }
        }
    }

    private class RecordingSessionSnapshotTick extends TimerTask {
        boolean firstTick = false;
        @Override
        public void run() {
            Log.w(LOGTAG, "Taking RecordingSessionSnapshotTick ");
            try {
                if (!firstTick){
                    firstTick = true;
                    return;
                }
                sensingFinish();
            } catch (Throwable t) {
                Log.e("RecordingSessionSnapshotTick", "RecordingSessionSnapshotTick Failed.", t);
            }
        }
    }


}
