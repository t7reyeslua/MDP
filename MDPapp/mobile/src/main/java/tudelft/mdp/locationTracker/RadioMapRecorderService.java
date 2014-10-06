package tudelft.mdp.locationTracker;



import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import tudelft.mdp.enums.UserPreferences;

public class RadioMapRecorderService extends Service {

    public static final String ARG_SECTION = "section";
    public static final String ARG_SUBSECTION = "subsection";
    public static final String ARG_PLACE = "place";
    public static final String ARG_ARRAY_SECTIONS = "sections";
    public static final String ARG_HISTOGRAM_COUNT = "histogram_count";

    private final Messenger mMessenger = new Messenger(new IncomingMessageHandler());
    private List<Messenger> mClients = new ArrayList<Messenger>();

    public static final int MSG_REGISTER_CLIENT = 6;
    public static final int MSG_UNREGISTER_CLIENT = 7;
    public static final int MSG_SET_INT_VALUE = 8;
    public static final int MSG_SET_HISTOGRAM_COUNT_VALUE = 12;


    private WifiManager myWifiManager;

    private static boolean isRunning = false;

    private ArrayList<String> sections = new ArrayList<String>();
    public HashMap<String, HashMap<Integer, Integer>> rssi_readings =
       new HashMap<String, HashMap<Integer, Integer>>();
    private int section;
    private int subsection;
    private String place;
    private String sectionName;
    private String subsectionName;
    private String zoneName;
    private int samples_taken;
    private int samples_required;
    private boolean filewrite = false;

    private static final String LOGTAG = "RadioMapRecorderService";

    public RadioMapRecorderService() {
    }

    @Override
    public void onCreate() {
        Log.i(LOGTAG, "Service Created.");

        samples_taken = 0;
        samples_required = UserPreferences.FINGERPRINT_SAMPLES;
        myWifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        registerReceivers();
        myWifiManager.startScan();

        isRunning = true;
    }



    private void registerReceivers(){
        this.registerReceiver(this.myScanResultsAvailable,
                new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        Log.i(LOGTAG, "Registered receiver");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(LOGTAG, "onBind");
        return mMessenger.getBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(LOGTAG, "Received start id " + startId + ": " + intent);

        fingerprintSetup(intent);
        return START_STICKY; // Run until explicitly stopped.
    }

    @Override
    public void onStart(Intent intent, int startid) {
        Log.i(LOGTAG, "Service Started.");
        fingerprintSetup(intent);
    }

    public static boolean isRunning()
    {
        return isRunning;
    }

    private BroadcastReceiver myScanResultsAvailable
            = new BroadcastReceiver(){

        @Override
        public void onReceive(Context arg0, Intent arg1) {

            samples_taken++;
            sendMessageToUI(0, samples_taken);
            saveData();

            Log.i(LOGTAG, "Scan Results available" + samples_taken);
            myWifiManager.startScan();
        }};


    public void saveData(){
        for (int i = 0; i<myWifiManager.getScanResults().size(); i++) {
            ScanResult result = myWifiManager.getScanResults().get(i);
            String s = result.SSID + "|" + result.BSSID;
            if (s.length() == 0) {
                s = result.BSSID;
            }
            int rssi = result.level;

            addToLocalCount(s, rssi);


            /*
            mHistogram.addToHistogram(zoneName, s, rssi);
            */

        }

        if (samples_taken == samples_required){

            /*
            mHistogram.calculateGaussians();

            BackgroundTask taskSaveToDB = new BackgroundTask();
            taskSaveToDB.execute(mHistogram);

            FingerprintFileWriter fw = new FingerprintFileWriter(
                    this.getApplicationContext(),
                    place, sectionName, subsectionName);
            filewrite = fw.writeToFiles(mHistogram, rssi_readings);
            */

            this.stopSelf();
        }
    }

    public void addToLocalCount(String ssid, int level){
        HashMap<Integer, Integer> temp;
        if (rssi_readings.containsKey(ssid)){
            temp = rssi_readings.get(ssid);
        } else {
            temp = new HashMap<Integer, Integer>();
        }
        temp.put(samples_taken,level);
        rssi_readings.put(ssid, temp);
    }



    @Override
    public void onDestroy() {
        isRunning = false;

        this.unregisterReceiver(myScanResultsAvailable);
        Log.i(LOGTAG, "Unregistered receiver");

        Log.i(LOGTAG, "Service Destroyed.");
        if (!filewrite) {
            Toast.makeText(this, "Scan canceled", Toast.LENGTH_SHORT).show();
        }

        sendMessageToUI(1,1);
    }

    public void fingerprintSetup(Intent intent) {
        section = intent.getExtras().getInt(ARG_SECTION);
        subsection = intent.getExtras().getInt(ARG_SUBSECTION);
        sections = intent.getExtras().getStringArrayList(ARG_ARRAY_SECTIONS);

        //mHistogram = (Histogram) intent.getExtras().getSerializable(ARG_HISTOGRAM_COUNT);

        sectionName = sections.get(0);
        subsectionName = sections.get(1);
        place = sections.get(2);
        zoneName  = sectionName + "." +subsectionName;

        Toast.makeText(this, "Starting fingerprint session: @" + place + " " + sectionName + "."
                + subsectionName, Toast.LENGTH_SHORT).show();

    }


    private void sendMessageToUI(int id, int intvaluetosend) {
        Iterator<Messenger> messengerIterator = mClients.iterator();
        while(messengerIterator.hasNext()) {
            Messenger messenger = messengerIterator.next();
            try {
                // Send data as an Integer
                if (id == 0) {
                    messenger.send(Message.obtain(null, MSG_SET_INT_VALUE, intvaluetosend, 0));
                } else {
                    // Send data as a String
                    Bundle bundle = new Bundle();

                    //bundle.putSerializable("histogram_count", mHistogram);
                    Message msg = Message.obtain(null, MSG_SET_HISTOGRAM_COUNT_VALUE);
                    msg.setData(bundle);
                    messenger.send(msg);

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
                case MSG_SET_INT_VALUE:
                    //incrementBy = msg.arg1;
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }


}
