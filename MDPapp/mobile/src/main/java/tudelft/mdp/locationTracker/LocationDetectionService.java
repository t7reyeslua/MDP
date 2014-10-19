package tudelft.mdp.locationTracker;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import tudelft.mdp.backend.endpoints.radioMapFingerprintEndpoint.model.ApGaussianRecord;
import tudelft.mdp.enums.MessagesProtocol;
import tudelft.mdp.enums.UserPreferences;

public class LocationDetectionService extends Service implements
        ServiceConnection,
        RequestGaussiansAsyncTask.RequestGaussiansAsyncResponse{

    private static final String LOGTAG = "LocationDetectionService";
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

    public static final String ARG_TEST = "TEST";
    public static final String ARG_SCHEDULE_NEXT = "SCHEDULE NEXT";
    public static final String ARG_LOCATION_ACQUIRED = "LOCATION ACQUIRED";
    public static final String ARG_SCANMODE = "SCAN MODE";

    private String locationCalculated;
    private boolean mLocationRequestedByTimeTick = false;
    private boolean mLocationRequestedByBroadcast = false;

    private Vibrator v;
    private SharedPreferences sharedPrefs;
    private int numScans;
    private int numScansCount;
    private String deviceBroadcast = "";

    private Timer mTimer = new Timer();
    private ArrayList<ApGaussianRecord> mGaussianRecords = new ArrayList<ApGaussianRecord>();


    public LocationDetectionService() {
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

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.getApplication());
        numScans = sharedPrefs.getInt(UserPreferences.SCANSAMPLES, 1);

        isRunning = true;
        automaticBinding();

        int scanWindow = sharedPrefs.getInt(UserPreferences.TIME_BETWEEN_LOCATION_DETECTIONS, 30);
        mTimer.scheduleAtFixedRate(new DetectLocationTick(), 0, scanWindow * 1000);

        updateGaussians();


    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        isRunning = false;
        try {
            if (mIsBound) {
                automaticUnbinding();
            }
        } catch (Throwable t) {
            Log.e(LOGTAG, "Failed to unbind from the service", t);
        }
        Log.e(LOGTAG, "Location Service Destroyed.");
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

    //NetworkScanService Routines*************************************************************************

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
    }

    /* Service connection methods */

    /**
     * Required method for implementing ServiceConnection
     * @param name name
     */
    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.i(LOGTAG, "Sensor Service: onServiceDisconnected");
        if (mServiceMessenger != null) {
            mServiceMessenger = null;
        }
    }

    /**
     * Required method for implementing ServiceConnection
     * @param name name
     * @param service service
     */
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

    //Manage scan results ***********************************************************************

    private void handleScanResult(ArrayList<NetworkInfoObject> recentScanResult){
        //TODO manage scan result received
        if (numScansCount > numScans){

            numScansCount++;
        } else {

        }

    }

    private void detectLocation(){
        //TODO: Initialize all required things for detection routine

        mLocationRequestedByTimeTick = true;
        numScans = sharedPrefs.getInt(UserPreferences.SCANSAMPLES, 1);
        numScansCount = 0;
    }

    //UI interaction Routines***********************************************************************

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
                    default:
                        break;
                }

            } catch (RemoteException e) {
                // The client is dead. Remove it from the list.
                mClients.remove(messenger);
            }
        }
    }



    private class IncomingMessageHandler extends Handler { // Handler of incoming messages from clients.
        @Override
        public void handleMessage(Message msg) {
            Log.d(LOGTAG, "handleMessage: " + msg.what);
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    mClients.add(msg.replyTo);
                    break;
                case MSG_UNREGISTER_CLIENT:
                    mClients.remove(msg.replyTo);
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

    private class DetectLocationTick extends TimerTask {
        @Override
        public void run() {
            Log.w(LOGTAG, "DetectLocationTick");
            try {
                detectLocation();

            } catch (Throwable t) { //you should always ultimately catch all exceptions in timer tasks.
                Log.e("DetectLocationTick", "DetectLocationTick Failed.", t);
            }
        }
    }


    private void updateGaussians(){
        RequestGaussiansAsyncTask requestGaussiansAsyncTask = new RequestGaussiansAsyncTask();
        requestGaussiansAsyncTask.delegate = this;
        requestGaussiansAsyncTask.execute();
    }

    public void processFinishRequestGaussians(List<ApGaussianRecord> outputList){
        mGaussianRecords = new ArrayList<ApGaussianRecord>(outputList);
    }



    private class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            String command = intent.getStringExtra(MessagesProtocol.MESSAGE);
            Log.i(LOGTAG, "Broadcast received: " + command);

            if (command.equals(MessagesProtocol.UPDATE_GAUSSIANS)){
                updateGaussians();
            } else if (command.equals(MessagesProtocol.COLLECTDATA_MOTIONLOCATION) ||
                       command.equals(MessagesProtocol.COLLECTDATA_LOCATION)) {
                mLocationRequestedByBroadcast = true;
                deviceBroadcast = command;
            }

        }
    }
}
