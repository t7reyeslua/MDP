package tudelft.mdp.locationTracker;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.view.CardListView;
import it.gmariotti.cardslib.library.view.CardView;
import tudelft.mdp.R;
import tudelft.mdp.backend.endpoints.locationFeaturesRecordApi.model.LocationFeaturesRecord;
import tudelft.mdp.backend.endpoints.locationFeaturesRecordApi.model.Text;
import tudelft.mdp.backend.endpoints.radioMapFingerprintEndpoint.model.ApHistogramRecord;
import tudelft.mdp.backend.endpoints.radioMapFingerprintEndpoint.model.LocationFingerprintRecord;
import tudelft.mdp.enums.UserPreferences;
import tudelft.mdp.ui.FingerprintControlCard;
import tudelft.mdp.ui.FingerprintZoneCard;
import tudelft.mdp.utils.Utils;
import tudelft.mdp.weka.WekaNetworkScansObject;


public class LocationFingerprintFragment extends Fragment implements ServiceConnection {

    private View rootView;
    private CardView mCardView;
    private Card mCardFingerprint;

    private ArrayList<Card> mCardsArrayList;
    private CardArrayAdapter mCardArrayAdapter;
    private CardListView mCardListView;

    private ProgressBar mProgressBar;
    private ToggleButton mToggleButton;
    private AutoCompleteTextView mPlaceAutoComplete;
    private AutoCompleteTextView mZoneAutoComplete;
    private Chronometer mChronometer;
    private Vibrator v;

    private Messenger mServiceMessenger = null;
    private boolean mIsBound;
    private ServiceConnection mConnection = this;
    private final Messenger mMessenger = new Messenger(new IncomingMessageHandler());

    private static final String LOGTAG = "MDP-LocationFingerprintFragment";
    private int currentSamples = 0;


    private ArrayList<WekaNetworkScansObject> mWekaNetworkScansObjects = new ArrayList<WekaNetworkScansObject>();
    private ArrayList<ArrayList<NetworkInfoObject>> mNetworkScans = new ArrayList<ArrayList<NetworkInfoObject>>();
    private ArrayList<LocationFeaturesRecord> mLocationFeaturesRecords = new ArrayList<LocationFeaturesRecord>();
    private ArrayList<ApHistogramRecord> localHistogram = new ArrayList<ApHistogramRecord>();
    private ArrayList<LocationFingerprintRecord> rawScans = new ArrayList<LocationFingerprintRecord>();


    private int fingerprintSamples;
    private boolean mCalibrated;
    private float calibrationM;
    private float calibrationB;

    private static final String[] ZONES = new String[] {
            "Kitchen",
            "Shower",
            "Bathroom",
            "Toilet",
            "Laundry room",
            "Living room",
            "Bedroom A",
            "Bedroom B",
            "Bedroom C",
            "Bedroom D",
            "Hall",
            "Coffee Room"
    };

    private static final String[] PLACES = new String[] {
            "Home", "Office"
    };


    public LocationFingerprintFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView =  inflater.inflate(R.layout.fragment_location_fingerprint, container, false);
        v = (Vibrator) this.getActivity().getSystemService(Context.VIBRATOR_SERVICE);


        //Toast.makeText(rootView.getContext(), "Getting prev calibration values", Toast.LENGTH_SHORT).show();
        getPreviousCalibrationValues();

        configureControlCard();
        configureAutoComplete();
        configureToggleButton();
        configureCardList();

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (mIsBound) {
                automaticUnbinding();
            }
        } catch (Throwable t) {
            Log.e(LOGTAG, "Failed to unbind from the service", t);
        }
        Log.e(LOGTAG, "Location Service Destroyed.");
    }





    private void configureAutoComplete(){
        ArrayAdapter<String> placesAdapter   = new ArrayAdapter<String>(rootView.getContext(), android.R.layout.simple_dropdown_item_1line, PLACES);
        ArrayAdapter<String> zonesAdapter = new ArrayAdapter<String>(rootView.getContext(), android.R.layout.simple_dropdown_item_1line, ZONES);

        mPlaceAutoComplete.setAdapter(placesAdapter);
        mZoneAutoComplete.setAdapter(zonesAdapter);

        placesAdapter.notifyDataSetChanged();
        zonesAdapter.notifyDataSetChanged();
    }

    private void configureToggleButton(){
        mToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    startFingerprint();

                } else {
                    stopFingerprint();
                }
            }
        });
    }

    private void configureControlCard(){
        mCardView = (CardView) rootView.findViewById(R.id.cardFingerprint);
        mCardFingerprint = new FingerprintControlCard(rootView.getContext());
        mCardFingerprint.setShadow(true);
        mCardView.setCard(mCardFingerprint);


        mProgressBar = (ProgressBar) mCardView.findViewById(R.id.progressBar);
        mToggleButton = (ToggleButton) mCardView.findViewById(R.id.toggleButton);
        mPlaceAutoComplete = (AutoCompleteTextView) mCardView.findViewById(R.id.acPlace);
        mZoneAutoComplete = (AutoCompleteTextView) mCardView.findViewById(R.id.acZone);
        mChronometer = (Chronometer) mCardView.findViewById(R.id.chronometer);
    }

    private void configureCardList(){
        mCardsArrayList = new ArrayList<Card>();
        mCardArrayAdapter = new CardArrayAdapter(rootView.getContext(), mCardsArrayList);
        mCardListView = (CardListView) rootView.findViewById(R.id.myList);
        if (mCardListView != null) {
            mCardListView.setAdapter(mCardArrayAdapter);
        }
    }

    private Card createFingerprintInfoCard(int samples){
        Card card = new FingerprintZoneCard(rootView.getContext(),
                mPlaceAutoComplete.getText().toString(),
                mZoneAutoComplete.getText().toString(),
                samples);
        return card;
    }

    private void replaceFingerprintCard(int samples){
        Card card = createFingerprintInfoCard(samples);
        mCardsArrayList.set(0, card);
        mCardArrayAdapter.notifyDataSetChanged();

    }

    private void startChronometer() {
        mChronometer.setTextColor(getResources().getColor(R.color.ForestGreen));
        mChronometer.setText("-00:00");
        mChronometer.setBase(SystemClock.elapsedRealtime());
        mChronometer.start();
    }





    private void startFingerprint(){


        if (!mCalibrated){
            Toast.makeText(rootView.getContext(), "Please calibrate your phone before starting to fingerprint", Toast.LENGTH_SHORT).show();
            return;
        }

        if ((mPlaceAutoComplete.getText().length() > 0) && (mZoneAutoComplete.getText().length() > 0)) {
            Toast.makeText(rootView.getContext(), "Starting fingerprint session", Toast.LENGTH_SHORT).show();
            currentSamples = 0;

            localHistogram.clear();
            rawScans.clear();
            mWekaNetworkScansObjects.clear();
            mLocationFeaturesRecords.clear();

            fingerprintSamples = PreferenceManager.getDefaultSharedPreferences(rootView.getContext()).getInt(UserPreferences.SCANSAMPLES, 4);
            automaticBinding();
            mPlaceAutoComplete.setEnabled(false);
            mZoneAutoComplete.setEnabled(false);
            mProgressBar.setIndeterminate(true);


            Card card = createFingerprintInfoCard(0);
            mCardsArrayList.add(0, card);
            mCardArrayAdapter.notifyDataSetChanged();

            startChronometer();
        }else {
            mToggleButton.setChecked(false);
            Toast.makeText(this.getActivity(), "Please indicate the place and zone you are fingerprinting.",
                    Toast.LENGTH_SHORT)
                    .show();
        }
    }

    private void stopFingerprint(){
        String place = mPlaceAutoComplete.getText().toString();
        String zone  = mZoneAutoComplete.getText().toString();


        sendMessageToService(NetworkScanService.MSG_PAUSE_SCANS_FINGERPRINT);
        automaticUnbinding();
        mPlaceAutoComplete.setEnabled(true);
        mZoneAutoComplete.setEnabled(true);
        mChronometer.stop();
        mChronometer.setTextColor(getResources().getColor(R.color.DarkGray));

        v.vibrate(500);

        mToggleButton.setEnabled(true);
        mToggleButton.setChecked(false);
        mProgressBar.setIndeterminate(false);
        mProgressBar.setProgress(0);


        buildLocationFeaturesRecordArray(place, zone, Utils.getCurrentTimestamp());

    }

    private void buildLocationFeaturesRecordArray(String place, String zone, String timestamp){
        int chunkSize = fingerprintSamples;
        ArrayList<ArrayList<ArrayList<NetworkInfoObject>>> wrapperChunks = new ArrayList<ArrayList<ArrayList<NetworkInfoObject>>>();
        Log.w(LOGTAG, "1.WTF-WrapperChunks:" + wrapperChunks.size());
        Log.w(LOGTAG, "1.WTF-mNetworkScans:" + mNetworkScans.size());
        Log.w(LOGTAG, "1.WTF-mWekaNetworkScansObjects:" + mWekaNetworkScansObjects.size());

        for (int i = 0; i < mNetworkScans.size(); i += chunkSize){
            ArrayList<ArrayList<NetworkInfoObject>> subList = new ArrayList<ArrayList<NetworkInfoObject>>(
                    mNetworkScans.subList(i, i + (Math.min(chunkSize, mNetworkScans.size() - i))));
            wrapperChunks.add(subList);
        }

        Log.w(LOGTAG, "2.WTF-WrapperChunks:" + wrapperChunks.size());
        for (ArrayList<ArrayList<NetworkInfoObject>> chunk : wrapperChunks){
            if (chunk.size() == fingerprintSamples) {
                WekaNetworkScansObject wekaNetworkScansObject = new WekaNetworkScansObject(chunk);
                wekaNetworkScansObject.buildNetworkFeatures();
                mWekaNetworkScansObjects.add(wekaNetworkScansObject);
            }
        }
        Log.w(LOGTAG, "2.WTF-mWekaNetworkScansObjects:" + mWekaNetworkScansObjects.size());

        ArrayList<LocationFeaturesRecord> tempLocationFeaturesRecords = new ArrayList<LocationFeaturesRecord>();
        Log.w(LOGTAG, "1.WTF-tempLocationFeaturesRecords:" + tempLocationFeaturesRecords.size());
        for (WekaNetworkScansObject wekaNetworkScansObject : mWekaNetworkScansObjects){
            LocationFeaturesRecord locationFeaturesRecord = new LocationFeaturesRecord();


            String user = PreferenceManager.getDefaultSharedPreferences(rootView.getContext()).getString(
                    UserPreferences.USERNAME, "Unknown");
            Text locationFeatures = new Text();
            locationFeatures.setValue(wekaNetworkScansObject.getFeatures());
            locationFeaturesRecord.setLocationFeatures(locationFeatures);
            locationFeaturesRecord.setPlace(place);
            locationFeaturesRecord.setZone(zone);
            locationFeaturesRecord.setTimestamp(timestamp);
            locationFeaturesRecord.setUsername(user);

            tempLocationFeaturesRecords.add(locationFeaturesRecord);
        }

        Log.w(LOGTAG, "2.WTF-tempLocationFeaturesRecords:" + tempLocationFeaturesRecords.size());

        Log.w(LOGTAG, "Samples taken: " + mNetworkScans.size()
                + "| Groups formed: " +  wrapperChunks.size()
                + "| Discarded scans: " +  (wrapperChunks.size() - tempLocationFeaturesRecords.size()));


        uploadScanDataToCloud(place, zone, tempLocationFeaturesRecords);

        mWekaNetworkScansObjects.clear();
        mNetworkScans.clear();
        wrapperChunks.clear();

    }


    private void uploadScanDataToCloud(String place, String zone, ArrayList<LocationFeaturesRecord> tempLocationFeaturesRecords){

        Toast.makeText(rootView.getContext(), "Updating data...", Toast.LENGTH_SHORT).show();

        Log.w(LOGTAG, "uploadScanDataToCloud: Weka Features Data");
        new UploadLocationFeaturesAsyncTask().execute(rootView.getContext(), tempLocationFeaturesRecords);
        Log.w(LOGTAG, "uploadScanDataToCloud: Histograms");
        //new UploadLocationHistogramsAsyncTask().execute(rootView.getContext(), localHistogram, place, zone);
        //Log.w(LOGTAG, "uploadScanDataToCloud: Raw Data");
        //new UploadLocationRawDataAsyncTask().execute(rootView.getContext(), rawScans);
    }


    /**
     * createNewApHistogramRecord
     * @param networkInfo with the info required to build ApHistogramRecord
     * @return created ApHistogramRecord object
     */
    private ApHistogramRecord createNewApHistogramRecord(NetworkInfoObject networkInfo){
        ApHistogramRecord newNetworkScanned = new ApHistogramRecord();
        newNetworkScanned.setSsid(networkInfo.getSSID());
        newNetworkScanned.setBssid(networkInfo.getBSSID());
        newNetworkScanned.setRssi(networkInfo.getRSSI());
        newNetworkScanned.setCount(1);
        newNetworkScanned.setPlace(mPlaceAutoComplete.getText().toString().toLowerCase());
        newNetworkScanned.setZone(mZoneAutoComplete.getText().toString().toLowerCase());

        return newNetworkScanned;
    }

    /**
     * createNewLocationFingerprintRecord
     * @param networkInfo with the info required to build ApHistogramRecord
     * @return created LocationFingerprintRecord
     */
    private LocationFingerprintRecord createNewLocationFingerprintRecord(NetworkInfoObject networkInfo){
        LocationFingerprintRecord locationFingerprintRecord = new LocationFingerprintRecord();
        locationFingerprintRecord.setSsid(networkInfo.getSSID());
        locationFingerprintRecord.setBssid(networkInfo.getBSSID());
        locationFingerprintRecord.setRssi(networkInfo.getRSSI());
        locationFingerprintRecord.setPlace(mPlaceAutoComplete.getText().toString().toLowerCase());
        locationFingerprintRecord.setZone(mZoneAutoComplete.getText().toString().toLowerCase());
        locationFingerprintRecord.setTimeOfDay(Utils.getCurrentTimeOfDay());

        return locationFingerprintRecord;
    }

    /**
     * Gets the store calibration parameter values
     */
    private void getPreviousCalibrationValues(){
        mCalibrated = PreferenceManager.getDefaultSharedPreferences(rootView.getContext())
                .getBoolean(UserPreferences.CALIBRATED, false);

        calibrationM = Float.valueOf(PreferenceManager.getDefaultSharedPreferences(rootView.getContext())
                .getString(UserPreferences.CALIBRATION_M_PREF, "1.0"));

        calibrationB = Float.valueOf(PreferenceManager.getDefaultSharedPreferences(rootView.getContext())
                .getString(UserPreferences.CALIBRATION_B_PREF, "0.0"));

    }

    /**
     * applyCalibrationParams to read scans.
     * @param recentScanResult to be adjusted
     */
    private void applyCalibrationParams(ArrayList<NetworkInfoObject> recentScanResult){

        for (NetworkInfoObject networkInfo : recentScanResult) {
            Double calibratedValue = calibrationM * networkInfo.getRSSI() + calibrationB;
            networkInfo.setMean(calibratedValue);
        }

    }


    /**
     * Aggregates the result to the already info of each network.
     * Used for the Bayessian Method to build the histograms.
     * @param recentScanResult scan result of APs
     */
    private void addScanToAggregatedResults(ArrayList<NetworkInfoObject> recentScanResult){
        for(NetworkInfoObject networkInfo : recentScanResult){
            // Check if the particular read RSSI level has been seen before for this particular network
            boolean alreadyExists = false;
            // TODO: check if RSSI value needs to be rounded after calibration.

            for (ApHistogramRecord existentInfo : localHistogram){
                if ((networkInfo.getRSSI().equals(existentInfo.getRssi())) &&
                    (networkInfo.getBSSID().equals(existentInfo.getBssid()))){
                    alreadyExists = true;
                    existentInfo.setCount(existentInfo.getCount() + 1);
                    break;
                }
            }
            if (!alreadyExists){
                ApHistogramRecord newNetworkScanned = createNewApHistogramRecord(networkInfo);
                localHistogram.add(newNetworkScanned);
            }

        }

    }

    /**
     * Aggregates the raw scan results
     * Used for the Weka Method.
     * @param recentScanResult scan result of APs
     */
    private void addScanToRawResults(ArrayList<NetworkInfoObject> recentScanResult){
        // TODO Add scan to raw results
        for (NetworkInfoObject networkInfo : recentScanResult) {
            LocationFingerprintRecord locationFingerprintRecord
                    = createNewLocationFingerprintRecord(networkInfo);
            rawScans.add(locationFingerprintRecord);
        }
    }


    private void handleScanResult(ArrayList<NetworkInfoObject> recentScanResult){
        currentSamples ++;
        replaceFingerprintCard(currentSamples);

        //Toast.makeText(rootView.getContext(), "Samples:" + currentSamples, Toast.LENGTH_SHORT).show();

        applyCalibrationParams(recentScanResult);

        ArrayList<NetworkInfoObject> newScan = new ArrayList<NetworkInfoObject>(recentScanResult);
        mNetworkScans.add(newScan);
        addScanToAggregatedResults(recentScanResult);
        addScanToRawResults(recentScanResult);
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


        sendMessageToService(NetworkScanService.MSG_UNPAUSE_SCANS_FINGERPRINT);
    }

    /**
     * Automatically binds to the service. It starts it if required.
     */
    private void automaticBinding() {
        if (NetworkScanService.isRunning()){
            doBindService();
            //Toast.makeText(rootView.getContext(), "Binding to service", Toast.LENGTH_SHORT).show();
        } else{
            startServiceNetworkScan();
            doBindService();
           // Toast.makeText(rootView.getContext(), "Starting service", Toast.LENGTH_SHORT).show();
        }
        if (v != null) {
            v.vibrate(500);
        }
    }

    /**
     * Automatically unbinds from the service
     */
    private void automaticUnbinding() {

       // Toast.makeText(rootView.getContext(), "Unbinding from service", Toast.LENGTH_SHORT).show();
        stopServiceNetworkScan();
    }

    /**
     * Start the service
     */
    public void startServiceNetworkScan(){
        Log.i(LOGTAG, "Network Scan Service: START");
        Intent intent = new Intent(rootView.getContext(), NetworkScanService.class);
        rootView.getContext().startService(intent);
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
        rootView.getContext().bindService(
                new Intent(rootView.getContext(), NetworkScanService.class),
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
            rootView.getContext().unbindService(mConnection);
            mIsBound = false;
        }
    }

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
     * Private class that handles all incoming messages from the service
     */
    private class IncomingMessageHandler extends Handler { // Handler of incoming messages from clients.
        @Override
        public void handleMessage(Message msg) {
            Log.d(LOGTAG, "handleMessage: " + msg.what);
            switch (msg.what) {
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



}
