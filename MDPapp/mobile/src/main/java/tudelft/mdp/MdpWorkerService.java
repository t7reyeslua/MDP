package tudelft.mdp;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import tudelft.mdp.backend.endpoints.deviceLogEndpoint.model.NfcRecord;
import tudelft.mdp.backend.endpoints.deviceMotionLocationRecordEndpoint.model.DeviceMotionLocationRecord;
import tudelft.mdp.backend.endpoints.deviceMotionLocationRecordEndpoint.model.Text;
import tudelft.mdp.backend.endpoints.locationLogEndpoint.model.LocationLogRecord;
import tudelft.mdp.backend.endpoints.radioMapFingerprintEndpoint.model.ApGaussianRecord;
import tudelft.mdp.communication.SendDataSyncThread;
import tudelft.mdp.communication.VerifyAndroidWearConnectedAsyncTask;
import tudelft.mdp.deviceManager.RequestUserActiveDevicesAsyncTask;
import tudelft.mdp.enums.Constants;
import tudelft.mdp.enums.MessagesProtocol;
import tudelft.mdp.enums.UserPreferences;
import tudelft.mdp.locationTracker.LocationEstimator;
import tudelft.mdp.locationTracker.NetworkInfoObject;
import tudelft.mdp.locationTracker.NetworkScanService;
import tudelft.mdp.locationTracker.RequestGaussiansAsyncTask;
import tudelft.mdp.locationTracker.UploadCurrentLocationAsyncTask;
import tudelft.mdp.utils.Utils;
import tudelft.mdp.weka.UploadMotionLocationFeaturesAsyncTask;
import tudelft.mdp.weka.WekaNetworkScansObject;
import tudelft.mdp.weka.WekaSensorsRawDataObject;

public class MdpWorkerService extends Service implements
        ServiceConnection,
        RequestGaussiansAsyncTask.RequestGaussiansAsyncResponse,
        RequestUserActiveDevicesAsyncTask.RequestUserActiveDevicesAsyncResponse,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String LOGTAG = "MdpWorkerService";
    private static boolean isRunning = false;

    private Messenger mServiceMessenger = null;
    private boolean mIsBound;
    private ServiceConnection mConnection = this;

    private final Messenger mMessenger = new Messenger(new IncomingMessageHandler());
    private List<Messenger> mClients = new ArrayList<Messenger>();

    public static final int MSG_REGISTER_CLIENT = 66;
    public static final int MSG_UNREGISTER_CLIENT = 77;
    public static final int MSG_SCHEDULE_NEXT = 88;
    public static final int MSG_LOCATION_ACQUIRED = 99;
    public static final int MSG_TEST = 20;
    public static final int MSG_LOCATION_STEP_BY_STEP = 21;
    public static final int MSG_LOCATION_GAUSSIANS = 22;
    public static final int MSG_LOG = 23;

    public static final String ARG_TEST = "TEST";
    public static final String ARG_SCHEDULE_NEXT = "SCHEDULE NEXT";
    public static final String ARG_LOCATION_ACQUIRED = "LOCATION ACQUIRED";
    public static final String ARG_LOCATION_STEP_BY_STEP = "LOCATION STEP BY STEP";
    public static final String ARG_LOCATION_GAUSSIANS = "LOCATION GAUSSIANS";

    private String locationCalculated = "";
    private String lastLocation = "";
    private String lastLocationTimestamp = "";
    private boolean mLocationRequestedByTimeTick = false;
    private boolean mLocationRequestedByBroadcast = false;
    private boolean mLocationRequestedByLocatorStepByStep = false;


    GoogleApiClient mGoogleApiClient;
    private Vibrator v;
    private SharedPreferences sharedPrefs;


    private int numScansStepByStep;
    private int numScansCountStepByStep;
    private int numScans;
    private int numScansCount;
    private int numScansForMotionLocation = 4;
    private int numScansCountForMotionLocation;
    private boolean motionTickDone;
    private boolean dataCompleteMotion;
    private boolean dataCompleteLocation;
    private String deviceEvent;
    private int msgCount = 0;
    private Timer mTimerDetection = new Timer();
    private Timer mTimerMotion = new Timer();

    private ArrayList<String> mSensorReadings = new ArrayList<String>();
    private ArrayList<ArrayList<NetworkInfoObject>> mNetworkScansStepByStep = new ArrayList<ArrayList<NetworkInfoObject>>();
    private ArrayList<ArrayList<NetworkInfoObject>> mNetworkScansTimerTick = new ArrayList<ArrayList<NetworkInfoObject>>();
    private ArrayList<ArrayList<NetworkInfoObject>> mNetworkScansBroadcastTick = new ArrayList<ArrayList<NetworkInfoObject>>();
    private ArrayList<ApGaussianRecord> mGaussianRecords = new ArrayList<ApGaussianRecord>();

    private HashMap<Integer, ArrayList<String>> mRecordedSensors = new HashMap<Integer, ArrayList<String>>();
    private Integer currentlyReceivingSensor;

    PowerManager pm;
    WifiManager wm;
    PowerManager.WakeLock wl;
    WifiManager.WifiLock wifiLock;


    //Constructor **********************************************************************************
    public MdpWorkerService() {
    }



    //Lifecycle Routines****************************************************************************
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(LOGTAG, "onBind");
        return mMessenger.getBinder();
    }

    @Override
    public void onCreate() {
        Log.w(LOGTAG, "Location Service Created.");

        configureBroadcastReceivers();

        buildGoogleClient();

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.getApplication());
        numScans = sharedPrefs.getInt(UserPreferences.SCANSAMPLES, 1);

        isRunning = true;
        automaticBinding();

        int scanWindow = sharedPrefs.getInt(UserPreferences.TIME_BETWEEN_LOCATION_DETECTIONS, 30);
        mTimerDetection.scheduleAtFixedRate(new DetectLocationTick(), 0, scanWindow * 1000);

        v = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
        updateGaussians();

        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MDP-PowerManager");
        wl.acquire();
        Log.e(LOGTAG,"WakeLock Acquired");

        wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiLock = wm.createWifiLock("MDP-WifiLock");
        wifiLock.acquire();
        Log.e(LOGTAG,"WifiLock Acquired");

    }



    @Override
    public void onDestroy() {
        super.onDestroy();

        if (wl != null){
            if (wl.isHeld()){
                wl.release();
                Log.e(LOGTAG,"WakeLock Released");
            }
        }
        if (wifiLock != null){
            if (wifiLock.isHeld()){
                wifiLock.release();
                Log.e(LOGTAG,"WifiLock Released");
            }
        }

        isRunning = false;
        try {
            if (mIsBound) {
                automaticUnbinding();
            }
        } catch (Throwable t) {
            Log.e(LOGTAG, "Failed to unbind from the service", t);
        }

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }

        Log.e(LOGTAG, "MDP Background Worker Service Destroyed.");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(LOGTAG, "Received start id " + startId + ": " + intent);
        return START_STICKY; // Run until explicitly stopped.
    }



    @Override
    public void onStart(Intent intent, int startid) {
        Log.i(LOGTAG, "Location Service Started.");
    }

    /**
     * isRunning method
     * @return boolean identifying if the service is running
     */
    public static boolean isRunning()
    {
        return isRunning;
    }






    //Network Scan Service Connection methods*******************************************************

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.i(LOGTAG, "Sensor Service: onServiceDisconnected");
        if (mServiceMessenger != null) {
            mServiceMessenger = null;
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.i(LOGTAG, "Sensor Service: onServiceConnected");
        mServiceMessenger = new Messenger(service);
        try {
            Message msg = Message.obtain(null, NetworkScanService.MSG_REGISTER_CLIENT);
            msg.replyTo = mMessenger;
            mServiceMessenger.send(msg);
        }
        catch (RemoteException e) {
            // In this case the service has crashed before we could even do anything with it
        }
    }

    /**
     * Automatically binds to the service. It starts it if required.
     */
    private void automaticBinding() {
        if (NetworkScanService.isRunning()){
            doBindService();
        } else{
            startServiceNetworkScan();
            doBindService();
        }
        if (v != null) {
            v.vibrate(500);
        }
    }

    /**
     * Automatically unbinds from the service
     */
    private void automaticUnbinding() {
        stopServiceNetworkScan();
    }

    /**
     * Start the service
     */
    public void startServiceNetworkScan(){
        Log.i(LOGTAG, "Network Scan Service: START");
        Intent intent = new Intent(getApplicationContext(), NetworkScanService.class);
        getApplicationContext().startService(intent);
    }

    /**
     * Unbinds from the service
     */
    public void stopServiceNetworkScan(){
        Log.i(LOGTAG, "Network Scan Service: STOP");
        doUnbindService();
    }

    /**
     * Binds to the service
     */
    private void doBindService() {
        getApplicationContext().bindService(new Intent(getApplicationContext(), NetworkScanService.class),
                mConnection, 0);
        mIsBound = true;
    }

    /**
     * Unbinds from the service
     */
    private void doUnbindService() {
        if (mIsBound) {

            // If we have received the service, and hence registered with it, then now is the time to unregister.
            if (mServiceMessenger != null) {
                try {
                    Message msg = Message.obtain(null, NetworkScanService.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mServiceMessenger.send(msg);
                } catch (RemoteException e) {
                    // There is nothing special we need to do if the service has crashed.
                }
            }
            // Detach our existing connection.
            getApplicationContext().unbindService(mConnection);
            mIsBound = false;
        }
    }







    //Android Wear Connection methods***************************************************************

    /**
     * Required for establishing connection with Android Wear
     */
    private void buildGoogleClient(){
        // Build a new GoogleApiClient
        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mGoogleApiClient.connect();

    }

    @Override
    public void onConnected(Bundle connectionHint) {
        sendDataMapToWear(MessagesProtocol.SNDMESSAGE, "Hello from Mobile");
        new VerifyAndroidWearConnectedAsyncTask().execute(this.getApplicationContext(), mGoogleApiClient);
    }

    @Override
    public void onConnectionSuspended(int cause) {}

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {}


    /**
     * Sends a notification to the Wear device with a corresponding command embedded in it.
     * The command starts and stops the sensing service in the Wear device.
     * @param command to start or stop the service in the smartwatch.
     */
    private void sendNotificationToWear(String command) {
        if (mGoogleApiClient.isConnected()) {
            PutDataMapRequest dataMapRequest = PutDataMapRequest.create(MessagesProtocol.NOTIFICATIONPATH);
            // Make sure the data item is unique. Usually, this will not be required, as the payload
            // (in this case the title and the content of the notification) will be different for almost all
            // situations. However, in this example, the text and the content are always the same, so we need
            // to disambiguate the data item by adding a field that contains teh current time in milliseconds.
            dataMapRequest.getDataMap().putDouble(MessagesProtocol.NOTIFICATIONTIMESTAMP, System.currentTimeMillis());
            dataMapRequest.getDataMap().putString(MessagesProtocol.NOTIFICATIONTITLE, "MDP");
            dataMapRequest.getDataMap().putString(MessagesProtocol.NOTIFICATIONCONTENT, "Retrieving sensor information");
            dataMapRequest.getDataMap().putString(MessagesProtocol.NOTIFICATIONCOMMAND, command);
            PutDataRequest putDataRequest = dataMapRequest.asPutDataRequest();
            Wearable.DataApi.putDataItem(mGoogleApiClient, putDataRequest);
        }
        else {
            Log.e(LOGTAG, "No connection to wearable available!");
        }
    }

    /**
     * Sens a DataMap item to the Wear device which holds a commands
     * @param msgType Type of commands
     * @param message Load
     */
    private void sendDataMapToWear(Integer msgType, String message){
        DataMap dataMap = new DataMap();
        dataMap.putInt(MessagesProtocol.SENDER, MessagesProtocol.ID_MOBILE);
        dataMap.putInt(MessagesProtocol.MSGTYPE, msgType);
        dataMap.putString(MessagesProtocol.MESSAGE, message);
        dataMap.putDouble(MessagesProtocol.TIMESTAMP, System.currentTimeMillis());

        if (msgType.equals(MessagesProtocol.STARTSENSING)){
            ArrayList<Integer> mSensorListToRecord = new ArrayList<Integer>();
            mSensorListToRecord.add(Sensor.TYPE_ACCELEROMETER);
            mSensorListToRecord.add(Sensor.TYPE_GYROSCOPE);
            mSensorListToRecord.add(Sensor.TYPE_MAGNETIC_FIELD);
            mSensorListToRecord.add(Sensor.TYPE_LINEAR_ACCELERATION);
            mSensorListToRecord.add(Constants.SAMSUNG_TILT);
            mSensorListToRecord.add(Sensor.TYPE_ROTATION_VECTOR);

            Double hz = 50.0;
            Integer duration = sharedPrefs.getInt(UserPreferences.MOTION_SAMPLE_SECONDS, 6);

            Boolean consolidated = PreferenceManager
                    .getDefaultSharedPreferences(this)
                    .getBoolean(MessagesProtocol.SENSORSCONSOLIDATED, true);

            dataMap.putBoolean(MessagesProtocol.SENSORSCONSOLIDATED, consolidated);

            dataMap.putDouble(MessagesProtocol.SENSORHZ, hz);
            dataMap.putInt(MessagesProtocol.SENSOR_RECORDING_SECONDS, duration);
            dataMap.putIntegerArrayList(MessagesProtocol.SENSORSTORECORD, mSensorListToRecord);
        }

        new SendDataSyncThread(mGoogleApiClient, MessagesProtocol.DATAPATH, dataMap).start();
    }


    /**
     * Handles the data coming from android wear
     * @param msg received from Android Wear
     */
    private void handleWearMessage(String msg){
        String[] parts = msg.split("\\|");
        Integer msgType = Integer.valueOf(parts[0]);
        String msgLoad = parts[1];



        switch (msgType){
            case MessagesProtocol.SENDSENSEORSNAPSHOTREC_START:
                currentlyReceivingSensor = Integer.valueOf(msgLoad);
                msgCount = 0;
                Log.w(LOGTAG,"Start sensor streaming from wear: " + Utils.getSensorName(currentlyReceivingSensor) + " Records Count:" + msgCount);
                break;
            case MessagesProtocol.SENDSENSEORSNAPSHOTHEADER:
                Log.w(LOGTAG, msgLoad);
                break;
            case MessagesProtocol.SENDSENSEORSNAPSHOTREC:
                Log.i(LOGTAG, msgLoad);
                mSensorReadings.add(msgLoad);
                msgCount++;
                break;
            case MessagesProtocol.SENDSENSEORSNAPSHOTREC_FINISH:
                ArrayList<String> recordedSensor = new ArrayList<String>(mSensorReadings);
                mSensorReadings.clear();
                mRecordedSensors.put(currentlyReceivingSensor, recordedSensor);

                Log.w(LOGTAG, "Stop sensor streaming from wear: " + Utils.getSensorName(currentlyReceivingSensor)
                        + " Records Count:" + msgCount
                        + " mSensorReadings:" + mSensorReadings.size()
                        + " mRecordedSensors:" + mRecordedSensors.get(currentlyReceivingSensor).size());
                msgCount = 0;
                break;

            case MessagesProtocol.SENDSENSEORSNAPSHOT_END:
                Log.w(LOGTAG,"Stop sensor service from wear: SENDSENSEORSNAPSHOT_END");
                sendNotificationToWear(MessagesProtocol.STOPSENSINGSERVICE);
                dataCompleteMotion = true;
                consolidateMotionLocationData(dataCompleteMotion, dataCompleteLocation);
                break;

            case MessagesProtocol.SENDSENSEORSNAPSHOTUPDATE:
                Log.w(LOGTAG,"Current sensors readings");
                break;
            default:
                break;
        }
    }





    //Location Detection methods *******************************************************************

    /**
     * applyCalibrationParams to read scans.
     * @param recentScanResult to be adjusted
     */
    private void applyCalibrationParams(ArrayList<NetworkInfoObject> recentScanResult){

        Float calibrationM = Float.valueOf(PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getString(UserPreferences.CALIBRATION_M_PREF, "1.0"));

        Float calibrationB = Float.valueOf(PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getString(UserPreferences.CALIBRATION_B_PREF, "0.0"));

        for (NetworkInfoObject networkInfo : recentScanResult) {
            Double calibratedValue = calibrationM * networkInfo.getRSSI() + calibrationB;
            networkInfo.setMean(calibratedValue);
        }

    }

    /**
     * Estimate the most probable location using the scans done
     * @param mNetworkScans input
     */
    private void estimateLocation(ArrayList<ArrayList<NetworkInfoObject>> mNetworkScans){
        LocationEstimator locationEstimator = new LocationEstimator(mNetworkScans, mGaussianRecords);
        HashMap<String, Double> pmf = locationEstimator.calculateLocationBayessian();

        String placeOfLocation = locationEstimator.determineCurrentPlace();
        Double locationProbability = getProbabilityOfEstimatedLocation(pmf);
        saveEstimatedLocationToDB(placeOfLocation, locationProbability);


        RequestUserActiveDevicesAsyncTask requestUserActiveDevicesAsyncTask = new RequestUserActiveDevicesAsyncTask();
        requestUserActiveDevicesAsyncTask.delegate = this;
        requestUserActiveDevicesAsyncTask.execute(
                sharedPrefs.getString(UserPreferences.USERNAME, "TBD"));

    }

    /**
     * getProbabilityOfEstimatedLocation
     * @param pmf with estimated probability of each zone
     * @return probability
     */
    private Double getProbabilityOfEstimatedLocation(HashMap<String, Double> pmf){
        int i  = 0;
        locationCalculated = "";
        Double locationProbability = 0.0;
        if (pmf != null) {
            for (String zone : pmf.keySet()) {
                if (i < pmf.size() && i < 3) {
                    if (locationCalculated.length() == 0) {
                        locationCalculated = zone;
                        locationProbability = pmf.get(zone);
                    }
                    Log.w(LOGTAG, "Zone:" + zone + " Prob:" + pmf.get(zone));
                    i++;
                } else {
                    break;
                }
            }
        }
        return locationProbability;
    }

    /**
     * Saves the estimated location to DB if it is different from the last estimated one.
     * @param placeOfLocation Home/Office, etc.
     * @param locationProbability 0..1
     */
    private void saveEstimatedLocationToDB(String placeOfLocation, Double locationProbability){
        if (!locationCalculated.equals(lastLocation)) {
            String timestamp = Utils.getCurrentTimestamp();
            LocationLogRecord locationLogRecord = new LocationLogRecord();

            locationLogRecord.setTimestamp(timestamp);
            locationLogRecord.setZone(locationCalculated);
            locationLogRecord.setPlace(placeOfLocation);
            locationLogRecord.setProbability(locationProbability);
            locationLogRecord.setMode(Constants.LOC_BAYESSIAN);
            locationLogRecord.setUser(sharedPrefs.getString(UserPreferences.USERNAME, "TBD"));

            Log.w(LOGTAG, "Uploading current location to DB:" + locationCalculated + "|" + locationProbability);
            new UploadCurrentLocationAsyncTask().execute(locationLogRecord);

            lastLocation = locationCalculated;
            lastLocationTimestamp = timestamp;
        }
    }

    /**
     * Checks if the user has active devices in places different from the current place where he is.
     * @param outputList list of active devices of the user
     */
    public void processFinishRequestUserActiveDevices(List<NfcRecord> outputList){
        ArrayList<String> guiltyDevices = new ArrayList<String>();
        String message = "You have turned on devices in other locations.";
        guiltyDevices.add(message);

        for (NfcRecord device : outputList){
            if (!device.getLocation().toLowerCase().equals(locationCalculated.toLowerCase())){
                Log.i(LOGTAG, "Guilty:" + device.getType() + " at " + device.getLocation());
                guiltyDevices.add(device.getType() + " at " + device.getLocation());
            } else {
                Log.i(LOGTAG, "OK:" + device.getType() + " at " + device.getLocation());
            }
        }

        if (guiltyDevices.size() > 1) {
            startNotification(7777, message, guiltyDevices);
        }
    }


    private void startNotification(int id, String mesage, ArrayList<String> events){
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        NotificationCompat.Builder  mBuilder =
                new NotificationCompat.Builder(this);

        mBuilder.setContentTitle("MDP")
                .setContentText(mesage)
                .setSmallIcon(R.drawable.plug128)
                .setContentIntent(pendingIntent)
                .build();

        if (events != null) {
            /* Add Big View Specific Configuration */
            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

            // Sets a title for the Inbox style big view
            inboxStyle.setBigContentTitle("MDP");
            // Moves events into the big view
            for (String event : events) {
                inboxStyle.addLine(event);
            }
            mBuilder.setStyle(inboxStyle);
        }

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(id, mBuilder.build());
    }



    /**
     * Handles the scan result appropriately
     * @param recentScanResult The last scan result received from the NetworkScanService
     */
    private void handleScanResult(ArrayList<NetworkInfoObject> recentScanResult){
        // Apply calibration params to received scan.
        ArrayList<NetworkInfoObject> newRecentScanResult = new ArrayList<NetworkInfoObject>(recentScanResult);
        applyCalibrationParams(newRecentScanResult);

        if (mLocationRequestedByTimeTick) {
            if (numScansCount < numScans) {
                mNetworkScansTimerTick.add(newRecentScanResult);
                numScansCount++;
                Log.i(LOGTAG, "location by TICK Scan no." + numScansCount);
            } else {

                sendMessageToService(NetworkScanService.MSG_PAUSE_SCANS_TICK);
                Log.i(LOGTAG, "Finished required scans for determining location by TICK.");
                estimateLocation(mNetworkScansTimerTick);
                mLocationRequestedByTimeTick = false;
            }
        }

        if (mLocationRequestedByLocatorStepByStep) {
            if (numScansCountStepByStep < numScansStepByStep) {
                mNetworkScansStepByStep.add(newRecentScanResult);
                numScansCountStepByStep++;
                Log.i(LOGTAG, "location Step by Step Scan no." + numScansCountStepByStep);
            } else {
                sendMessageToService(NetworkScanService.MSG_PAUSE_SCANS_STEPBYSTEP);
                Log.i(LOGTAG, "Finished required scans for determining location step by step.");
                mLocationRequestedByLocatorStepByStep = false;
                sendMessageToUI(MSG_LOCATION_STEP_BY_STEP);
            }
        }


        if (mLocationRequestedByBroadcast){
            if (numScansCountForMotionLocation < numScansForMotionLocation){
                mNetworkScansBroadcastTick.add(newRecentScanResult);
                numScansCountForMotionLocation++;
            } else{

                sendMessageToService(NetworkScanService.MSG_PAUSE_SCANS_BROADCAST);
                Log.i(LOGTAG, "Finished required scans for determining location by BROADCAST.");
                mLocationRequestedByBroadcast = false;
                dataCompleteLocation = true;
                consolidateMotionLocationData(dataCompleteMotion, dataCompleteLocation);
            }
        }
    }

    /**
     * Initializes all required things for detection routine
     */
    private void detectLocation(){
        sendMessageToService(NetworkScanService.MSG_UNPAUSE_SCANS_TICK);
        Log.i(LOGTAG, "Start required scans for determining location by TICK.");
        numScans = sharedPrefs.getInt(UserPreferences.SCANSAMPLES, 1);
        numScansCount = 0;
        mNetworkScansTimerTick.clear();
        mLocationRequestedByTimeTick = true;
    }


    /**
     * Starts the async task to update gaussians from the info in cloud DB
     */
    private void updateGaussians(){
        RequestGaussiansAsyncTask requestGaussiansAsyncTask = new RequestGaussiansAsyncTask();
        requestGaussiansAsyncTask.delegate = this;
        requestGaussiansAsyncTask.execute();
    }

    /**
     * processFinishRequestGaussians
     * @param outputList Updated Gaussians
     */
    public void processFinishRequestGaussians(List<ApGaussianRecord> outputList){
        mGaussianRecords = new ArrayList<ApGaussianRecord>(outputList);
        Log.w(LOGTAG, "Set of Gaussians received. Records: " + mGaussianRecords.size());

    }


    //Location Step By Step ************************************************************************

    /**
     * Initializes all required variables to start location step by step.
     */
    private void initLocationStepByStep(){

        sendMessageToService(NetworkScanService.MSG_UNPAUSE_SCANS_STEPBYSTEP);
        Log.i(LOGTAG, "Start required scans for determining location step by step.");
        numScansStepByStep = sharedPrefs.getInt(UserPreferences.SCANSAMPLES, 1);
        numScansCountStepByStep = 0;
        mNetworkScansStepByStep.clear();
        mLocationRequestedByLocatorStepByStep = true;
    }







    //Timer Tasks***********************************************************************************

    /**
     * Timer that runs every (UserPreferences.TIME_BETWEEN_LOCATION_DETECTIONS) seconds
     * and calculates a new location.
     */
    private class DetectLocationTick extends TimerTask {
        @Override
        public void run() {
            Log.w(LOGTAG, "DetectLocationTick");
            try {
                detectLocation();

            } catch (Throwable t) {
                //you should always ultimately catch all exceptions in timer tasks.
                Log.e("DetectLocationTick", "DetectLocationTick Failed.", t);
            }
        }
    }



    /**
     * Timer that runs every time a Motion Request is asked.
     */
    private class MotionWearTick extends TimerTask {
        @Override
        public void run() {
            Log.w(LOGTAG, "MotionWearTick");
            try {
                if (motionTickDone){
                    //sendDataMapToWear(MessagesProtocol.STOPSENSING,   "STOP: MOTION DATA REQUESTED BY MOBILE");

                    Log.i(LOGTAG, "Stop Motion Tick.");
                    this.cancel();
                }
                if (!motionTickDone){
                    motionTickDone = true;
                }

            } catch (Throwable t) {
                //you should always ultimately catch all exceptions in timer tasks.
                Log.e("MotionWearTick", "MotionWearTick Failed.", t);
            }
        }
    }






    //Motion-Location *********************************************************************
    /**
     * consolidateMotionLocationData
     * @param isDataCompleteMotion True if all motion data has been received from Wear
     * @param isDataCompleteLocation True if all location data has been received from NetworkScanService
     */
    private void consolidateMotionLocationData (boolean isDataCompleteMotion, boolean isDataCompleteLocation){


        Log.w(LOGTAG, "consolidateMotionLocationData");
        if (isDataCompleteLocation && isDataCompleteMotion){
            if (v != null) {
                v.vibrate(500);
            }

            Log.w(LOGTAG, "Data and Motion Completed");
            WekaNetworkScansObject wekaNetworkScansObject = new WekaNetworkScansObject(mNetworkScansBroadcastTick);
            WekaSensorsRawDataObject wekaSensorsRawDataObject;
            Boolean consolidated = sharedPrefs.getBoolean(MessagesProtocol.SENSORSCONSOLIDATED, true);
            if (consolidated) {
                wekaSensorsRawDataObject = new WekaSensorsRawDataObject(mSensorReadings);
            } else {
                wekaSensorsRawDataObject = new WekaSensorsRawDataObject(mRecordedSensors);

                for (Integer t : mRecordedSensors.keySet()){
                    Log.i(LOGTAG, "Array: " + Utils.getSensorName(t) + " size: " + mRecordedSensors.get(t).size());
                }
            }

            uploadFeaturesToDB(wekaNetworkScansObject, wekaSensorsRawDataObject);

            boolean trainingPhase = sharedPrefs.getBoolean(UserPreferences.TRAINING_PHASE, true);
            if (trainingPhase){
                wekaSensorsRawDataObject.saveToFile(deviceEvent, consolidated);
                wekaNetworkScansObject.saveToFile(deviceEvent);
            }
        }

    }

    /**
     * Uploads the sensed data to DB
     * @param wekaNetworkScansObject location features
     * @param wekaSensorsRawDataObject motion features
     */
    private void uploadFeaturesToDB(WekaNetworkScansObject wekaNetworkScansObject, WekaSensorsRawDataObject wekaSensorsRawDataObject){
        Log.i(LOGTAG, "uploadFeaturesToDB:" + deviceEvent);
        //TODO Save/upload both data sets
        Boolean consolidated = sharedPrefs.getBoolean(MessagesProtocol.SENSORSCONSOLIDATED, true);
        String[] parts = deviceEvent.split("_");
        String deviceId = parts[0];
        String deviceType = parts[1];
        String timestamp = parts[2];
        Text motionFeatures = new Text();
        Text locationFeatures = new Text();
        motionFeatures.setValue(wekaSensorsRawDataObject.getFeatures(10000, consolidated));
        locationFeatures.setValue(wekaNetworkScansObject.getFeatures());

        DeviceMotionLocationRecord deviceMotionLocationRecord = new DeviceMotionLocationRecord();
        deviceMotionLocationRecord.setUsername(sharedPrefs.getString(UserPreferences.USERNAME, "TBD"));
        deviceMotionLocationRecord.setEvent(deviceEvent);
        deviceMotionLocationRecord.setDeviceType(deviceType);
        deviceMotionLocationRecord.setDeviceId(deviceId);
        deviceMotionLocationRecord.setTimestamp(timestamp);
        deviceMotionLocationRecord.setMotionFeatures(motionFeatures);
        deviceMotionLocationRecord.setLocationFeatures(locationFeatures);

        new UploadMotionLocationFeaturesAsyncTask().execute(this.getApplicationContext(), deviceMotionLocationRecord);
    }

    /**
     * Initializes the required variables for requesting motion and location data
     */
    private void startMotionLocationDataRecollection(String event){
        deviceEvent = event;

        Log.i(LOGTAG, "Start Motion Location Data Recollection by BROADCAST.");

        msgCount = 0;
        if (v != null) {
            v.vibrate(500);
        }

        dataCompleteLocation = false;
        dataCompleteMotion = true;
        motionTickDone = true;
        if (sharedPrefs.getBoolean(UserPreferences.WEARCONNECTED, false)) {
            dataCompleteMotion = false;
            sendNotificationToWear(MessagesProtocol.STARTSENSINGSERVICE);
            sendDataMapToWear(MessagesProtocol.STARTSENSING,
                    "START: MOTION DATA REQUESTED BY MOBILE");


            Log.i(LOGTAG, "Start Motion Tick.");
            motionTickDone = false;
            int motionWindow = sharedPrefs.getInt(UserPreferences.MOTION_SAMPLE_SECONDS, 6);
            mTimerMotion = new Timer();
            mTimerMotion.scheduleAtFixedRate(new MotionWearTick(), 0, motionWindow * 1000);
        }

        sendMessageToService(NetworkScanService.MSG_UNPAUSE_SCANS_BROADCAST);
        mLocationRequestedByBroadcast = true;
        numScansCountForMotionLocation = 0;

        mSensorReadings.clear();
        mRecordedSensors.clear();
        mNetworkScansBroadcastTick.clear();
    }








    //Communication interaction routines with other services and threads****************************

    private void sendMessageToService(int command) {
        if (mIsBound) {
            if (mServiceMessenger != null) {
                try {
                    Message msg = Message
                            .obtain(null, command);
                    msg.replyTo = mMessenger;
                    mServiceMessenger.send(msg);
                } catch (RemoteException e) {
                    Log.e(LOGTAG, e.getMessage());
                }
            }
        }
    }

    /**
     * SendMessageToUI
     * @param id Identifies which type of message to be sent
     */
    private void sendMessageToUI(int id) {

        Log.d(LOGTAG, "sendMessageToUI: " + id);
        Iterator<Messenger> messengerIterator = mClients.iterator();
        while(messengerIterator.hasNext()) {
            Messenger messenger = messengerIterator.next();
            Bundle bundle = new Bundle();
            Message msg;
            try {
                switch (id) {
                    case MSG_TEST:
                        bundle.putString(ARG_TEST, "Test");
                        msg = Message.obtain(null, MSG_TEST);
                        msg.setData(bundle);
                        messenger.send(msg);
                        break;
                    case MSG_SCHEDULE_NEXT:
                        bundle.putString(ARG_SCHEDULE_NEXT, "Next");
                        msg = Message.obtain(null, MSG_SCHEDULE_NEXT);
                        msg.setData(bundle);
                        messenger.send(msg);
                        break;
                    case MSG_LOCATION_ACQUIRED:
                        bundle.putString(ARG_LOCATION_ACQUIRED, locationCalculated);
                        msg = Message.obtain(null, MSG_LOCATION_ACQUIRED);
                        msg.setData(bundle);
                        messenger.send(msg);
                        break;
                    case MSG_LOCATION_STEP_BY_STEP:
                        bundle.putSerializable(ARG_LOCATION_STEP_BY_STEP, mNetworkScansStepByStep);
                        msg = Message.obtain(null, MSG_LOCATION_STEP_BY_STEP);
                        msg.setData(bundle);
                        messenger.send(msg);
                        break;
                    case MSG_LOCATION_GAUSSIANS:
                        bundle.putSerializable(ARG_LOCATION_GAUSSIANS, mGaussianRecords);
                        msg = Message.obtain(null, MSG_LOCATION_GAUSSIANS);
                        msg.setData(bundle);
                        messenger.send(msg);
                        break;
                    default:
                        break;
                }

            } catch (RemoteException e) {
                // The client is dead. Remove it from the list.
                mClients.remove(messenger);
            }
        }
    }

    /**
     * Handler of incoming messages from clients or from services it is connected to.
     */
    private class IncomingMessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            //Log.d(LOGTAG, "handleMessage: " + msg.what);
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    mClients.add(msg.replyTo);
                    break;
                case MSG_UNREGISTER_CLIENT:
                    mClients.remove(msg.replyTo);
                    break;
                case MSG_LOCATION_STEP_BY_STEP:
                    initLocationStepByStep();
                    break;
                case MSG_LOCATION_GAUSSIANS:
                    sendMessageToUI(MSG_LOCATION_GAUSSIANS);
                    break;
                case NetworkScanService.MSG_SCANRESULT_READY:
                    @SuppressWarnings("unchecked")
                    ArrayList<NetworkInfoObject> scanResult =
                            (ArrayList<NetworkInfoObject>) msg.getData()
                                    .getSerializable(NetworkScanService.ARG_SCANRESULT);

                    handleScanResult(scanResult);

                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    /**
     * Configures the Broadcast receivers that catch a new intent for detecting the current location.
     */
    private void configureBroadcastReceivers(){
        IntentFilter messageFilter = new IntentFilter(MessagesProtocol.COLLECTDATA_MOTIONLOCATION);
        MessageReceiver messageReceiver = new MessageReceiver();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(messageReceiver, messageFilter);

        IntentFilter messageFilter2 = new IntentFilter(MessagesProtocol.COLLECTDATA_LOCATION);
        MessageReceiver messageReceiver2 = new MessageReceiver();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(messageReceiver2, messageFilter2);

        IntentFilter messageFilter3 = new IntentFilter(MessagesProtocol.UPDATE_GAUSSIANS);
        MessageReceiver messageReceiver3 = new MessageReceiver();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(messageReceiver3, messageFilter3);

        IntentFilter messageFilter4 = new IntentFilter(MessagesProtocol.WEARSENSORSMSG);
        MessageReceiver messageReceiver4 = new MessageReceiver();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(messageReceiver4, messageFilter4);

        IntentFilter messageFilter5 = new IntentFilter(MessagesProtocol.MSG_RECEIVED);
        MessageReceiver messageReceiver5 = new MessageReceiver();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(messageReceiver5, messageFilter5);

    }

    /**
     * Receives the Broadcast intents form the rest of the application
     */
    private class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            String broadcastFilter = intent.getAction();
            String msg = intent.getStringExtra(MessagesProtocol.MESSAGE);
            Log.w(LOGTAG, "Broadcast received: " + broadcastFilter);


            if        (broadcastFilter.equals(MessagesProtocol.UPDATE_GAUSSIANS)){
                updateGaussians();
            } else if (broadcastFilter.equals(MessagesProtocol.COLLECTDATA_MOTIONLOCATION)) {
                startMotionLocationDataRecollection(msg);
            } else if (broadcastFilter.equals(MessagesProtocol.WEARSENSORSMSG)){
                handleWearMessage(msg);
            } else if (broadcastFilter.equals(MessagesProtocol.MSG_RECEIVED)){
                // Test the other ones
                String[] parts = msg.split("#");
                Integer msgType = 0;
                String msgLoad = msg;

                if(parts.length > 1) {
                    msgType = Integer.valueOf(parts[0]);
                    msgLoad = parts[1];

                    switch (msgType){
                        case MessagesProtocol.SENDGCM_CMD_UPDATEGAUSSIANS:
                            updateGaussians();
                            break;
                        case MessagesProtocol.SENDGCM_CMD_MOTIONLOCATION:
                            startMotionLocationDataRecollection(msgLoad);
                            break;
                        case MessagesProtocol.SENDSENSEORSNAPSHOTREC_START:
                        case MessagesProtocol.SENDSENSEORSNAPSHOTREC:
                        case MessagesProtocol.SENDSENSEORSNAPSHOTREC_FINISH:
                            String wearMsg = msg.replace("#","\\|");
                            handleWearMessage(wearMsg);
                            break;
                        default:
                            break;
                    }



                }
            }


        }
    }
}
