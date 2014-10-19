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
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.view.CardListView;
import it.gmariotti.cardslib.library.view.CardView;
import tudelft.mdp.R;
import tudelft.mdp.utils.Utils;
import tudelft.mdp.enums.UserPreferences;
import tudelft.mdp.ui.CalibrationControlCard;
import tudelft.mdp.ui.CalibrationCurrentValuesCard;
import tudelft.mdp.ui.CalibrationExecuteCard;
import tudelft.mdp.ui.CalibrationNetworksCard;
import tudelft.mdp.utils.SLinearRegression;

public class LocationCalibrationFragment extends Fragment implements ServiceConnection {

    private Messenger mServiceMessenger = null;
    private boolean mIsBound;
    private final Messenger mMessenger = new Messenger(new IncomingMessageHandler());
    private ServiceConnection mConnection = this;


    private ArrayList<NetworkInfoObject> aggregatedScanResults = new ArrayList<NetworkInfoObject>();

    private View rootView;
    private CardView mCardView;
    private Card mCardCalibration;


    private CardView mCardViewValues;
    private Card mCardValues;

    private CardView mCardViewProgress;
    private Card mCardProgress;

    private CardView mCardViewNetworks;
    private CalibrationNetworksCard mCardNetworks;

    private CardView mCardViewCalibrateExecute;
    private Card mCardCalibrateExecute;

    private ArrayList<Card> mCardsArrayList;
    private CardArrayAdapter mCardArrayAdapter;
    private CardListView mCardListView;

    private ProgressBar mProgressBar;
    private ToggleButton mToggleButton;
    private Button mButtonRegression;
    private Button mButtonCalibrate;
    private Switch mSwitch;
    private EditText mEditTextM;
    private EditText mEditTextB;
    private Vibrator v;

    private boolean isMaster = false;

    private boolean mCalibrated;
    private boolean mRegressionDone = false;
    private float calibrationM;
    private float calibrationB;

    private float regressionM = 1.0f;
    private float regressionB = 0.0f;

    private int calibrationScans;
    private int calibrationScansCount;

    private static final String TAG = "MDP-LocationCalibrationFragment";
    public LocationCalibrationFragment() {
        // Required empty public constructor
    }

    /* Lifecycle methods*/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView =  inflater.inflate(R.layout.fragment_location_calibration, container, false);


        configureCardsInit();

        v = (Vibrator) this.getActivity().getSystemService(Context.VIBRATOR_SERVICE);

        mProgressBar = (ProgressBar) mCardView.findViewById(R.id.progressBar);
        mToggleButton = (ToggleButton) mCardView.findViewById(R.id.toggleButton);
        mSwitch = (Switch) mCardView.findViewById(R.id.swMaster);
        mButtonRegression = (Button) mCardViewNetworks.findViewById(R.id.buttonRegression);
        mButtonCalibrate = (Button) mCardViewCalibrateExecute.findViewById(R.id.buttonCalibration);
        mEditTextB = (EditText) mCardViewCalibrateExecute.findViewById(R.id.editBvalue);
        mEditTextM = (EditText) mCardViewCalibrateExecute.findViewById(R.id.editMvalue);


        configureRegressionButton();
        configureCalibrationButton();
        configureToggleButton();
        configureSwitch();

        getPreviousCalibrationValues();
        calibrationScans = PreferenceManager.getDefaultSharedPreferences(rootView.getContext())
                .getInt(UserPreferences.CALIBRATION_SCANS, UserPreferences.CALIBRATION_NUM_SCANS);

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
            Log.e(TAG, "Failed to unbind from the service", t);
        }
    }

    /* UI cards methods*/
    private void configureCardsInit(){
        configureControlCard();
        configureValuesCard();
        configureProgressCard(0, View.GONE);
        configureNetworkCard();
        configureCalibrateExecuteCard();
        configureCardList();
    }

    private void configureControlCard(){
        mCardView = (CardView) rootView.findViewById(R.id.cardCalibration);
        mCardCalibration = new CalibrationControlCard(rootView.getContext());
        mCardCalibration.setShadow(true);
        mCardView.setCard(mCardCalibration);
    }

    private void configureValuesCard(){

        getPreviousCalibrationValues();


        String calibration = "Regression Results";
        String calibrationValues = "m = " + String.format("%.3f", calibrationM) + "     b = " + String.format("%.3f", calibrationB);

        mCardViewValues = (CardView) rootView.findViewById(R.id.cardValues);
        mCardValues = new CalibrationCurrentValuesCard(rootView.getContext(), calibration, calibrationValues);
        mCardValues.setShadow(true);
        mCardViewValues.setCard(mCardValues);
        mCardViewValues.setVisibility(View.GONE);

        toastCalibration();
    }

    private void refreshValuesCard(String calibration, Float m, Float b){
        String calibrationValues = "m = " + String.format("%.3f", m) + "     b = " + String.format("%.3f", b);

        mCardViewValues = (CardView) rootView.findViewById(R.id.cardValues);
        mCardValues = new CalibrationCurrentValuesCard(rootView.getContext(), calibration, calibrationValues);
        mCardValues.setShadow(true);
        mCardViewValues.refreshCard(mCardValues);
    }


    private void toastCalibration(){
        getPreviousCalibrationValues();
        String calibrationValues = "m = " + String.format("%.3f", calibrationM) + "     b = " + String.format("%.3f", calibrationB);

        String calibration;
        if (mCalibrated){
            calibration = "Previously calibrated: ";
        } else {
            calibration = "Not calibrated yet: ";
        }
        Toast.makeText(rootView.getContext(), calibration + calibrationValues, Toast.LENGTH_SHORT).show();
    }

    private void configureProgressCard(int scans, int visibility){
        mCardViewProgress = (CardView) rootView.findViewById(R.id.cardProgress);
        mCardProgress = new CalibrationCurrentValuesCard(rootView.getContext(), "Progress", "No. of scans: " + scans);
        mCardProgress.setShadow(true);
        mCardViewProgress.setCard(mCardProgress);
        mCardViewProgress.setVisibility(visibility);
    }

    private void refreshProgressCard(int scans, int visibility){
        mCardViewProgress = (CardView) rootView.findViewById(R.id.cardProgress);
        mCardProgress = new CalibrationCurrentValuesCard(rootView.getContext(), "Progress", "No. of scans: " + scans);
        mCardProgress.setShadow(true);
        mCardViewProgress.refreshCard(mCardProgress);
        mCardViewProgress.setVisibility(visibility);
    }

    private void configureNetworkCard(){
        ArrayList<NetworkInfoObject> mock = mockNetworks();
        mCardNetworks = new CalibrationNetworksCard(rootView.getContext(), mock);
        mCardNetworks.init();
        mCardNetworks.setShadow(true);

        mCardViewNetworks = (CardView) rootView.findViewById(R.id.cardNetworks);

        mCardViewNetworks.setCard(mCardNetworks);
        mCardViewNetworks.setVisibility(View.GONE);
    }

    private void refreshNetworkCard(ArrayList<NetworkInfoObject> updatedResults){

        mCardNetworks.updateItems(updatedResults);
    }

    private void configureCalibrateExecuteCard(){
        mCardViewCalibrateExecute = (CardView) rootView.findViewById(R.id.cardCalibrateExecute);
        mCardCalibrateExecute = new CalibrationExecuteCard(rootView.getContext());
        mCardCalibrateExecute.setShadow(true);
        mCardViewCalibrateExecute.setCard(mCardCalibrateExecute);
        mCardViewCalibrateExecute.setVisibility(View.GONE);
    }

    private void configureCardList(){
        mCardsArrayList = new ArrayList<Card>();
        mCardArrayAdapter = new CardArrayAdapter(rootView.getContext(), mCardsArrayList);
        mCardListView = (CardListView) rootView.findViewById(R.id.myList);
        if (mCardListView != null) {
            mCardListView.setAdapter(mCardArrayAdapter);
        }
    }

    private void configureCalibrationButton(){
        mButtonCalibrate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (mEditTextM.getText().toString().length() == 0 ||
                        mEditTextB.getText().toString().length() == 0) {

                    Toast.makeText(rootView.getContext(), "Please fill the required fields.", Toast.LENGTH_SHORT).show();
                    return;
                }

                Toast.makeText(rootView.getContext(), "Calibrating...", Toast.LENGTH_SHORT).show();
                executeCalibration();
            }
        });
    }

    private void configureRegressionButton(){
        mButtonRegression.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(rootView.getContext(), "Calculating regression...", Toast.LENGTH_SHORT).show();
                executeRegression();
            }
        });
    }

    private void configureSwitch(){
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    isMaster = true;

                    mCardViewCalibrateExecute.setVisibility(View.GONE);

                } else {
                    isMaster = false;
                    if(mRegressionDone) {
                        mCardViewCalibrateExecute.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }

    private void configureToggleButton(){
        mToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    startCalibration();

                } else {
                    stopCalibration();
                }
            }
        });
    }



    /* Calibration methods */
    private void startCalibration(){
        automaticBinding();
        calibrationScansCount = 0;
        getPreviousCalibrationValues();

        aggregatedScanResults.clear();
        mProgressBar.setIndeterminate(true);
        mSwitch.setEnabled(false);
        refreshProgressCard(1, View.VISIBLE);
        mCardViewNetworks.setVisibility(View.GONE);
        mCardViewCalibrateExecute.setVisibility(View.GONE);
        mCardViewValues.setVisibility(View.GONE);
        v.vibrate(500);

        mEditTextB.getText().clear();
        mEditTextM.getText().clear();
        mRegressionDone = false;
    }

    private void stopCalibration(){
        automaticUnbinding();

        sortArrayList(aggregatedScanResults);
        calculateScansMeanAndStd(aggregatedScanResults);
        applyCalibrationParams();

        mProgressBar.setIndeterminate(false);
        mSwitch.setEnabled(true);
        mToggleButton.setChecked(false);
        mCardViewProgress.setVisibility(View.GONE);
        mCardViewNetworks.setVisibility(View.VISIBLE);


        refreshNetworkCard(aggregatedScanResults);
        v.vibrate(500);


    }

    private void calculateScansMeanAndStd(ArrayList<NetworkInfoObject> scans){
        for (NetworkInfoObject accumulatedScan : scans){

            ArrayList<Integer> trimmedList = alphaTrimmerFilter(accumulatedScan.getRSSIarray());
            Double mean = Utils.getMean(trimmedList);
            Double std = Utils.getStd(trimmedList);

            accumulatedScan.setStd(std);
            accumulatedScan.setMean(mean);
            accumulatedScan.setCount(trimmedList.size());
        }
    }

    private ArrayList<Integer> alphaTrimmerFilter(ArrayList<Integer> unfilteredList){
        //Apply Alpha Trimmer
        Collections.sort(unfilteredList);
        int size = unfilteredList.size();
        int  elementsToTrimm = (int) Math.floor(size * UserPreferences.ALPHA_TRIMMER_COEFF_VALUE);
        ArrayList<Integer> filteredList = new ArrayList<Integer>(unfilteredList.subList(elementsToTrimm, size - elementsToTrimm ));

        return filteredList;
    }


    private void sortArrayList(ArrayList<NetworkInfoObject> unsortedList){
        //Sorting according to BSSID
        Collections.sort(unsortedList, new Comparator<NetworkInfoObject>() {
            @Override
            public int compare(NetworkInfoObject item1, NetworkInfoObject item2) {

                return item1.getBSSID().compareTo(item2.getBSSID());
            }
        });
    }


    private void applyCalibrationParams(){
        //Apply calibration params if you are MASTER (already calibrated)
        if (isMaster){
            for (NetworkInfoObject network : aggregatedScanResults){
                Double calibratedValue = calibrationM * network.getMean() + calibrationB;
                network.setMean(calibratedValue);
            }
            saveCalibrationValues(true, calibrationM, calibrationB);
        }
    }

    private void executeRegression(){

        // Use only filtered list
        aggregatedScanResults = mCardNetworks.getNetworks();
        ArrayList<Double> data = new ArrayList<Double>();
        for (NetworkInfoObject netowrk : aggregatedScanResults){
            data.add(netowrk.getMean());
        }

        // Regression and calculate m/b values
        double [] regressionValues = SLinearRegression.SimpleLinearRegression(data);

        regressionM = (float) regressionValues[0];
        regressionB = (float) regressionValues[1];


        mRegressionDone = true;
        refreshValuesCard("Regression Results", regressionM ,regressionB);

        mCardViewValues.setVisibility(View.VISIBLE);
        if (!mSwitch.isChecked()){
            mCardViewCalibrateExecute.setVisibility(View.VISIBLE);
        } else {
            mCardViewCalibrateExecute.setVisibility(View.GONE);
        }
    }

    private void executeCalibration(){

        // Calibrate
        double masterM = Double.valueOf(mEditTextM.getText().toString());
        double masterB = Double.valueOf(mEditTextB.getText().toString());

        double [] calibrationValues = SLinearRegression.CalibratrionFactor(
                masterM,
                masterB,
                (double) regressionM,
                (double) regressionB);

        Float m = (float) calibrationValues[0];
        Float b = (float) calibrationValues[1];

        refreshValuesCard("Calibration Results", m ,b);
        saveCalibrationValues(true, m, b);

        Toast.makeText(rootView.getContext(), "Calibration completed", Toast.LENGTH_SHORT).show();
    }


    private void saveCalibrationValues(boolean calibrated, Float calM, Float calB){
        PreferenceManager.getDefaultSharedPreferences(rootView.getContext())
                .edit().putBoolean(UserPreferences.CALIBRATED, calibrated).commit();

        String calM_str = String.valueOf(calM);
        String calB_str = String.valueOf(calB);

        PreferenceManager.getDefaultSharedPreferences(rootView.getContext())
                .edit().putString(UserPreferences.CALIBRATION_M_PREF, calM_str).commit();
        PreferenceManager.getDefaultSharedPreferences(rootView.getContext())
                .edit().putString(UserPreferences.CALIBRATION_B_PREF, calB_str).commit();
    }

    private void getPreviousCalibrationValues(){
        mCalibrated = PreferenceManager.getDefaultSharedPreferences(rootView.getContext())
                .getBoolean(UserPreferences.CALIBRATED, false);

        calibrationM = Float.valueOf(PreferenceManager.getDefaultSharedPreferences(rootView.getContext())
                .getString(UserPreferences.CALIBRATION_M_PREF, "1.0"));

        calibrationB = Float.valueOf(PreferenceManager.getDefaultSharedPreferences(rootView.getContext())
                .getString(UserPreferences.CALIBRATION_B_PREF, "0.0"));

    }

    private ArrayList<NetworkInfoObject> mockNetworks (){
        ArrayList<NetworkInfoObject> networks = new ArrayList<NetworkInfoObject>();
        NetworkInfoObject network1 =  new NetworkInfoObject(
                "Mierdify",
                "00:EE:43:FF:12",
                75.2,
                0.0,
                35
        );
        NetworkInfoObject network2 =  new NetworkInfoObject(
                "Eduroam",
                "00:EE:43:FF:12",
                85.2,
                0.0,
                25
        );
        NetworkInfoObject network3 =  new NetworkInfoObject(
                "Wifi",
                "00:EE:43:FF:12",
                25.2,
                0.0,
                45
        );


        networks.add(network1);
        networks.add(network2);
        networks.add(network3);
        networks.add(network1);
        networks.add(network2);
        networks.add(network3);
        networks.add(network1);
        networks.add(network2);
        networks.add(network3);
        networks.add(network1);
        networks.add(network2);
        networks.add(network3);
        return networks;
    }


    private void aggregateScanResult(ArrayList<NetworkInfoObject> newScanResult){

        for (NetworkInfoObject networkScan : newScanResult){

            // Look if it has already been seen
            boolean previouslySeen = false;
            for (NetworkInfoObject knownNetwork : aggregatedScanResults){
                if (networkScan.getBSSID().equals(knownNetwork.getBSSID())){
                    // aggregate result
                    previouslySeen = true;
                    knownNetwork.setCount(knownNetwork.getCount() + 1);
                    knownNetwork.setRSSI(knownNetwork.getRSSI() + networkScan.getRSSI());
                    knownNetwork.addRSSI(networkScan.getRSSI());
                    break;
                }
            }

            // New network found. Add it.
            if (!previouslySeen){
                networkScan.addRSSI(networkScan.getRSSI());
                aggregatedScanResults.add(networkScan);
            }
        }
    }



    /* Service connection methods */
    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.i(TAG, "Sensor Service: onServiceDisconnected");
        if (mServiceMessenger != null) {
            mServiceMessenger = null;
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.i(TAG, "Sensor Service: onServiceConnected");
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

    private void automaticUnbinding() {
        stopServiceNetworkScan();
    }

    public void startServiceNetworkScan(){
        Log.i(TAG, "Network Scan Service: START");
        Intent intent = new Intent(this.getActivity(), NetworkScanService.class);
        this.getActivity().startService(intent);
    }

    public void stopServiceNetworkScan(){
        Log.i(TAG, "Network Scan Service: STOP");
        doUnbindService();
    }

    private void doBindService() {
        this.getActivity().bindService(new Intent(this.getActivity(), NetworkScanService.class),
                mConnection, 0);
        mIsBound = true;
    }

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
            this.getActivity().unbindService(mConnection);
            mIsBound = false;
        }
    }



    private class IncomingMessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG,"IncomingHandler:handleMessage");
            switch (msg.what) {
                case NetworkScanService.MSG_SCANRESULT_READY:
                    @SuppressWarnings("unchecked")
                    ArrayList<NetworkInfoObject> scanResult =
                            (ArrayList<NetworkInfoObject>) msg.getData()
                                    .getSerializable(NetworkScanService.ARG_SCANRESULT);

                    calibrationScansCount++;
                    refreshProgressCard(calibrationScansCount, View.VISIBLE);
                    aggregateScanResult(scanResult);

                    if (calibrationScansCount == calibrationScans) {
                        stopCalibration();
                    }

                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

}
