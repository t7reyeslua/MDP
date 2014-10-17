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
import java.util.HashMap;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.view.CardListView;
import it.gmariotti.cardslib.library.view.CardView;
import tudelft.mdp.R;
import tudelft.mdp.backend.endpoints.radioMapFingerprintEndpoint.model.ApHistogramRecord;
import tudelft.mdp.backend.endpoints.radioMapFingerprintEndpoint.model.LocationFingerprintRecord;
import tudelft.mdp.backend.endpoints.radioMapFingerprintEndpoint.model.LocationFingerprintRecordWrapper;
import tudelft.mdp.enums.Constants;
import tudelft.mdp.enums.UserPreferences;
import tudelft.mdp.ui.FingerprintControlCard;
import tudelft.mdp.ui.FingerprintZoneCard;
import tudelft.mdp.utils.Utils;


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

    private ArrayList<ApHistogramRecord> localHistogram = new ArrayList<ApHistogramRecord>();
    private ArrayList<LocationFingerprintRecord> rawScans = new ArrayList<LocationFingerprintRecord>();


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
            currentSamples = 0;

            localHistogram.clear();
            rawScans.clear();

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
        newNetworkScanned.setDevice(Constants.CALIBRATED_DEVICE);
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

        calibrationM = PreferenceManager.getDefaultSharedPreferences(rootView.getContext())
                .getFloat(UserPreferences.CALIBRATION_M, 1.0f);

        calibrationB = PreferenceManager.getDefaultSharedPreferences(rootView.getContext())
                .getFloat(UserPreferences.CALIBRATION_B, 0.0f);

    }

    /**
     * applyCalibrationParams to read scans.
     * @param recentScanResult to be adjusted
     */
    private void applyCalibrationParams(ArrayList<NetworkInfoObject> recentScanResult){

        for (NetworkInfoObject networkInfo : recentScanResult) {
            Double calibratedValue = calibrationM * networkInfo.getMean() + calibrationB;
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

        applyCalibrationParams(recentScanResult);
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
