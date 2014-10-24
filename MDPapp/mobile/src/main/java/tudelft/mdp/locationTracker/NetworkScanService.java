package tudelft.mdp.locationTracker;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;


public class NetworkScanService extends Service {

    private ArrayList<NetworkInfoObject> scanResult = new ArrayList<NetworkInfoObject>();

    public static final int MSG_REGISTER_CLIENT = 6;
    public static final int MSG_UNREGISTER_CLIENT = 7;
    public static final int MSG_SCANRESULT_READY = 8;
    public static final int MSG_PAUSE_SCANS_TICK = 88;
    public static final int MSG_UNPAUSE_SCANS_TICK = 99;
    public static final int MSG_PAUSE_SCANS_CALIBRATION = 888;
    public static final int MSG_UNPAUSE_SCANS_CALIBRATION = 999;
    public static final int MSG_PAUSE_SCANS_BROADCAST = 8888;
    public static final int MSG_UNPAUSE_SCANS_BROADCAST = 9999;
    public static final int MSG_PAUSE_SCANS_STEPBYSTEP = 88888;
    public static final int MSG_UNPAUSE_SCANS_STEPBYSTEP = 99999;

    public static final String ARG_SCANRESULT = "SCAN RESULT";

    private final Messenger mMessenger = new Messenger(new IncomingMessageHandler());
    private List<Messenger> mClients = new ArrayList<Messenger>();

    private WifiManager myWifiManager;

    private static boolean isRunning = false;

    private boolean pauseScansTick = true;
    private boolean pauseScansBroadcast = true;
    private boolean pauseScansCalibration = true;
    private boolean pauseScansStepByStep = true;

    private static final String LOGTAG = "MDP-NetworkScanService";

    public NetworkScanService() {
    }

    //Lifecycle Routines****************************************************************************

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(LOGTAG, "onBind");
        return mMessenger.getBinder();
    }

    @Override
    public void onCreate() {
        Log.w(LOGTAG, "NetworkScanService Created.");
        myWifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        registerReceivers();

        isRunning = true;

    }

    @Override
    public void onDestroy() {
        isRunning = false;
        this.unregisterReceiver(myScanResultsAvailable);

        Log.i(LOGTAG, "Unregistered Scan results receiver");
        Log.e(LOGTAG, "NetworkScanService Destroyed.");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(LOGTAG, "Received start id " + startId + ": " + intent);
        //getNewScanResults();
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
            //Log.i(LOGTAG, "Received new scan results.");
            scanResult.clear();
            for (ScanResult result : myWifiManager.getScanResults()){
                NetworkInfoObject networkInfo = new NetworkInfoObject(
                        result.SSID,
                        result.BSSID,
                        (double) result.level);
                scanResult.add(networkInfo);
            }
            getNewScanResults();
            sendMessageToUI(MSG_SCANRESULT_READY);
        }};

    private void registerReceivers(){
        this.registerReceiver(this.myScanResultsAvailable,
                new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        Log.i(LOGTAG, "Registered Scan Results receiver");
    }

    private void getNewScanResults(){
        if (!pauseScansBroadcast || !pauseScansCalibration || !pauseScansTick || !pauseScansStepByStep) {
            String requester = "";
            if (!pauseScansTick) requester += "TICK|";
            if (!pauseScansBroadcast) requester += "BROADCAST|";
            if (!pauseScansCalibration) requester += "CALIBRATION|";
            if (!pauseScansStepByStep) requester += "STEPBYSTEP|";
            Log.i(LOGTAG, "Request for new Scan Results. Starting scan... : " + requester);
            myWifiManager.startScan();
        }
    }

    //UI interaction Routines***********************************************************************

    private void sendMessageToUI(int id) {

        //Log.d(LOGTAG, "sendMessageToUI: " + id);
        for (Messenger messenger : mClients){
            Bundle bundle = new Bundle();
            Message msg;
            try {
                switch (id) {
                    case MSG_SCANRESULT_READY:
                        bundle.putSerializable(ARG_SCANRESULT, scanResult);
                        msg = Message.obtain(null, MSG_SCANRESULT_READY);
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
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    Log.d(LOGTAG, "handleMessage: " + "RegisterClient");
                    mClients.add(msg.replyTo);
                    break;
                case MSG_UNREGISTER_CLIENT:
                    Log.d(LOGTAG, "handleMessage: " + "UnRegisterClient");
                    mClients.remove(msg.replyTo);
                    break;
                case MSG_PAUSE_SCANS_CALIBRATION:
                    Log.d(LOGTAG, "handleMessage: " + "PAUSE_CALIBRATION");
                    pauseScansCalibration = true;
                    break;
                case MSG_UNPAUSE_SCANS_CALIBRATION:
                    Log.d(LOGTAG, "handleMessage: " + "UNPAUSE_CALIBRATION");
                    pauseScansCalibration = false;
                    getNewScanResults();
                    break;
                case MSG_PAUSE_SCANS_TICK:
                    Log.d(LOGTAG, "handleMessage: " + "PAUSE_TICK");
                    pauseScansTick = true;
                    break;
                case MSG_UNPAUSE_SCANS_TICK:
                    Log.d(LOGTAG, "handleMessage: " + "UNPAUSE_TICK");
                    pauseScansTick = false;
                    getNewScanResults();
                    break;
                case MSG_PAUSE_SCANS_BROADCAST:
                    Log.d(LOGTAG, "handleMessage: " + "PAUSE_BROADCAST");
                    pauseScansBroadcast = true;
                    break;
                case MSG_UNPAUSE_SCANS_BROADCAST:
                    Log.d(LOGTAG, "handleMessage: " + "UNPAUSE_BROADCAST");
                    pauseScansBroadcast = false;
                    getNewScanResults();
                    break;
                case MSG_PAUSE_SCANS_STEPBYSTEP:
                    Log.d(LOGTAG, "handleMessage: " + "PAUSE_STEPBYSTEP");
                    pauseScansStepByStep = true;
                    break;
                case MSG_UNPAUSE_SCANS_STEPBYSTEP:
                    Log.d(LOGTAG, "handleMessage: " + "UNPAUSE_STEPBYSTEP");
                    pauseScansStepByStep = false;
                    getNewScanResults();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }


}
