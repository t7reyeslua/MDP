package tudelft.mdp.locationTracker;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import tudelft.mdp.enums.UserPreferences;

public class LocationDetectionService extends Service {

    private static final String LOGTAG = "LocationDetector";
    private static boolean isRunning = false;

    private final Messenger mMessenger = new Messenger(new IncomingMessageHandler());
    private List<Messenger> mClients = new ArrayList<Messenger>();

    public static final int MSG_REGISTER_CLIENT = 6;
    public static final int MSG_UNREGISTER_CLIENT = 7;
    public static final int MSG_SCHEDULE_NEXT = 8;

    public static final String ARG_TEST = "TEST";
    public static final String ARG_SCHEDULE_NEXT = "SCHEDULE NEXT";
    public static final String ARG_SCANMODE = "SCAN MODE";

    public static final int MSG_TEST = 20;


    private WifiManager myWifiManager;

    private SharedPreferences sharedPrefs;
    private int numScans;


    private Timer mTimer = new Timer();


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
        sendMessageToUI(MSG_SCHEDULE_NEXT);
        myWifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        registerReceivers();


        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.getApplication());
        numScans = sharedPrefs.getInt(UserPreferences.SCANSAMPLES, 1);

        isRunning = true;
        mTimer.scheduleAtFixedRate(new DetectLocationTick(), 0, UserPreferences.SCANWINDOW * 1000);

    }

    @Override
    public void onDestroy() {
        isRunning = false;
        this.unregisterReceiver(myScanResultsAvailable);

        Log.i(LOGTAG, "Unregistered Scan results receiver");
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

    public static boolean isRunning()
    {
        return isRunning;
    }


    //Network Scan Routines*************************************************************************

    private BroadcastReceiver myScanResultsAvailable
            = new BroadcastReceiver(){

        @Override
        public void onReceive(Context arg0, Intent arg1) {




        }};

    private void registerReceivers(){
        this.registerReceiver(this.myScanResultsAvailable,
                new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        Log.i(LOGTAG, "Registered Scan Results receiver");
    }

    private void getNewScanResults(){
        Log.i(LOGTAG, "Request for new Scan Results. Starting scan...");
        myWifiManager.startScan();
    }

    public void detectLocation(){
        Log.i(LOGTAG, "Detect current location.");
        getNewScanResults();
    }

    //UI interaction Routines***********************************************************************

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
}
