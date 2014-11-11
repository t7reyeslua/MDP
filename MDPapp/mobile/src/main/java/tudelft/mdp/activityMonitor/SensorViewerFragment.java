package tudelft.mdp.activityMonitor;



import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.hardware.Sensor;
import android.os.Bundle;
import android.app.Fragment;
import android.os.SystemClock;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import tudelft.mdp.enums.UserPreferences;
import tudelft.mdp.ui.SensorRecAvailableListCard;
import tudelft.mdp.utils.Utils;
import tudelft.mdp.communication.SendDataSyncThread;
import tudelft.mdp.enums.Constants;
import tudelft.mdp.enums.MessagesProtocol;
import tudelft.mdp.fileManagement.FileCreator;
import tudelft.mdp.ui.SensorRecControlCard;
import tudelft.mdp.ui.SensorRecInfoCard;


public class SensorViewerFragment extends Fragment implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String[] ACTIONS = new String[] {
            "Turn knob right",
            "Turn knob left",
            "Brushing Teeth"
    };

    private CardView mCardView;
    private Card mCardSensorRec;
    private CardView mCardViewSensorsAvailable;
    private Card mCardSensorsAvailable;
    private ArrayList<Card> mCardsArrayList;
    private CardArrayAdapter mCardArrayAdapter;
    private CardListView mCardListView;

    private ProgressBar mProgressBar;
    private ToggleButton mToggleButton;
    private AutoCompleteTextView mActionAutoComplete;
    private Chronometer mChronometer;

    private EditText mEditTextHz;
    private EditText mEditTextDuration;
    private LinearLayout mLinearLayoutAvailableSensors;
    private LinearLayout mLinearLayoutAvailableSensors2;

    private Vibrator v;
    private FileCreator mFileCreator;

    private DataMap dataMap = new DataMap();
    private boolean fileCreated = false;
    private boolean sessionRequested = false;

    private View rootView;
    GoogleApiClient mGoogleApiClient;

    private ArrayList<Integer> mSensorList = new ArrayList<Integer>();
    private ArrayList<Integer> mSensorListToRecord = new ArrayList<Integer>();
    private ArrayList<CheckBox> mSensorListCheckBoxes = new ArrayList<CheckBox>();

    private HashMap<Integer, ArrayList<String>> mRecordedSensors = new HashMap<Integer, ArrayList<String>>();
    private Integer currentlyReceivingSensor;
    private ArrayList<String> currentlyReceivingSensorValues = new ArrayList<String>();
    private static final String LOGTAG = "MDP-SensorViewerFragment";


    public SensorViewerFragment() {
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
        rootView =  inflater.inflate(R.layout.fragment_sensor_viewer, container, false);

        if (mGoogleApiClient != null){
            mGoogleApiClient.connect();
        }

        buildGoogleClient();
        configureBroadcastReceivers();
        configureControlCard();
        configureAvailableSensorsCard();
        configureCardList();
        configureAutoComplete();
        configureToggleButton();

        v = (Vibrator) this.getActivity().getSystemService(Context.VIBRATOR_SERVICE);

        return rootView;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }


    private void startRecording(){
        // Create a DataMap object and send it to the smartwatch
        String filename = mActionAutoComplete.getText().toString();
        fileCreated = false;
        sessionRequested  = true;
        sendNotification(MessagesProtocol.STARTSENSINGSERVICE);

        sendDataMap(MessagesProtocol.STARTSENSING, filename);

        v.vibrate(500);

        mActionAutoComplete.setEnabled(false);
        mProgressBar.setIndeterminate(true);
        startChronometer();
    }

    private void stopRecording(){

        String filename = mActionAutoComplete.getText().toString();
        sendDataMap(MessagesProtocol.STOPSENSING, filename);

        stopRecording_UI();
        sendNotification(MessagesProtocol.STOPSENSINGSERVICE);
    }

    private void stopRecording_UI(){
        v.vibrate(500);

        mActionAutoComplete.setEnabled(true);
        mChronometer.stop();
        mChronometer.setTextColor(getResources().getColor(R.color.DarkGray));
        mToggleButton.setEnabled(true);
        mToggleButton.setChecked(false);
        mProgressBar.setIndeterminate(false);
        mProgressBar.setProgress(0);


    }

    private void startChronometer() {
        mChronometer.setTextColor(getResources().getColor(R.color.ForestGreen));
        mChronometer.setText("-00:00");
        mChronometer.setBase(SystemClock.elapsedRealtime());
        mChronometer.start();
    }



    private void configureControlCard(){
        mCardView = (CardView) rootView.findViewById(R.id.cardControl);

        mCardSensorRec = new SensorRecControlCard(rootView.getContext());
        mCardSensorRec.setShadow(true);
        mCardView.setCard(mCardSensorRec);

        mProgressBar = (ProgressBar) mCardView.findViewById(R.id.progressBar);
        mToggleButton = (ToggleButton) mCardView.findViewById(R.id.toggleButton);
        mChronometer = (Chronometer) mCardView.findViewById(R.id.chronometer);

    }

    private void configureAvailableSensorsCard(){
        mCardViewSensorsAvailable = (CardView) rootView.findViewById(R.id.cardSensorsAvailable);
        mCardSensorsAvailable = new SensorRecAvailableListCard(rootView.getContext());
        mCardSensorsAvailable.setShadow(true);
        mCardViewSensorsAvailable.setCard(mCardSensorsAvailable);


        mActionAutoComplete = (AutoCompleteTextView) mCardViewSensorsAvailable.findViewById(R.id.acFile);
        mEditTextHz = (EditText) mCardViewSensorsAvailable.findViewById(R.id.editHz);
        mEditTextDuration = (EditText) mCardViewSensorsAvailable.findViewById(R.id.editDuration);
        mLinearLayoutAvailableSensors = (LinearLayout) mCardViewSensorsAvailable.findViewById(R.id.llSensorsAvailable);
        mLinearLayoutAvailableSensors2 = (LinearLayout) mCardViewSensorsAvailable.findViewById(R.id.llSensorsAvailable2);

        mCardViewSensorsAvailable.setVisibility(View.GONE);
    }

    private void refreshAvailableSensorsCard(ArrayList<Integer> availableSensors){
        ArrayList<Integer> sensorBlackList = new ArrayList<Integer>();
        sensorBlackList.add(Sensor.TYPE_SIGNIFICANT_MOTION);
        sensorBlackList.add(Sensor.TYPE_HEART_RATE);
        sensorBlackList.add(Sensor.TYPE_ORIENTATION);


        if (mLinearLayoutAvailableSensors != null && mLinearLayoutAvailableSensors2 != null) {
            int i = 0;
            int halfList = availableSensors.size()/2;
            for (Integer sensorType : availableSensors) {
                if (!sensorBlackList.contains(sensorType)) {
                    i++;
                    CheckBox ch = new CheckBox(mCardViewSensorsAvailable.getContext());
                    ch.setText(Utils.getSensorName(sensorType));
                    ch.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                    ch.setTextColor(Color.GRAY);
                    ch.setId(sensorType);
                    mSensorListCheckBoxes.add(ch);

                    if (i <= halfList) {
                        mLinearLayoutAvailableSensors.addView(ch);
                    } else {
                        mLinearLayoutAvailableSensors2.addView(ch);
                    }
                }
            }
        }
        mCardViewSensorsAvailable.setVisibility(View.VISIBLE);
    }

    private void configureCardList(){
        mCardsArrayList = new ArrayList<Card>();
        mCardArrayAdapter = new CardArrayAdapter(rootView.getContext(), mCardsArrayList);
        mCardListView = (CardListView) rootView.findViewById(R.id.myList);
        if (mCardListView != null) {
            mCardListView.setAdapter(mCardArrayAdapter);
        }
    }

    private Card createSensorRecInfoCard(String sensorName, String values){
        Card card = new SensorRecInfoCard(rootView.getContext(),
                sensorName,
                values);
        return card;
    }

    private void insertSensorRecInfoCard(Integer sensorType, String values){

        String sensorName = Utils.getSensorName(sensorType);
        Card card = createSensorRecInfoCard(sensorName, values);

        int index = findCardIndex(sensorName);
        if (index >= 0) {
            mCardsArrayList.set(index, card);
        } else {
            mCardsArrayList.add(card);
        }
        mCardArrayAdapter.notifyDataSetChanged();
    }

    private void reportProgress(String progress){
        String sensorName = "Progress";
        Card card = createSensorRecInfoCard(sensorName, progress);

        int index = findCardIndex(sensorName);
        if (index >= 0) {
            mCardsArrayList.set(index, card);
        } else {
            mCardsArrayList.add(card);
        }
        mCardArrayAdapter.notifyDataSetChanged();
    }

    private int findCardIndex(String sensorName){
        int index = -1;

        for (int i = 0; i < mCardsArrayList.size(); i++){
            SensorRecInfoCard tempCard = (SensorRecInfoCard) mCardsArrayList.get(i);
            if (tempCard.getSensorType().equals(sensorName)){
                return i;
            }
        }
        return index;
    }



    private void configureToggleButton(){
        mToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    startRecording();

                } else {
                    stopRecording();
                }
            }
        });
    }

    private void configureAutoComplete(){
        ArrayAdapter<String> actionsAdapter   = new ArrayAdapter<String>(rootView.getContext(), android.R.layout.simple_dropdown_item_1line, ACTIONS);

        mActionAutoComplete.setAdapter(actionsAdapter);
        actionsAdapter.notifyDataSetChanged();
    }

    private void configureBroadcastReceivers(){
        IntentFilter messageFilter = new IntentFilter(MessagesProtocol.WEARSENSORSMSG);
        MessageReceiver messageReceiver = new MessageReceiver();
        LocalBroadcastManager.getInstance(rootView.getContext()).registerReceiver(messageReceiver, messageFilter);


        IntentFilter bundleFilter = new IntentFilter(MessagesProtocol.WEARSENSORSBUNDLE);
        DataBundleReceiver dataBundleReceiver = new DataBundleReceiver();
        LocalBroadcastManager.getInstance(rootView.getContext()).registerReceiver(dataBundleReceiver, bundleFilter);
    }



    private void buildGoogleClient(){
        // Build a new GoogleApiClient
        mGoogleApiClient = new GoogleApiClient.Builder(rootView.getContext())
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mGoogleApiClient.connect();

    }

    @Override
    public void onConnected(Bundle connectionHint) {
        //sendDataMap(MessagesProtocol.SNDMESSAGE, "Hello from Mobile");

        if (PreferenceManager.getDefaultSharedPreferences(rootView.getContext()).getBoolean(
                UserPreferences.WEARCONNECTED, false)) {
            Log.i(LOGTAG, "Android Wear connected. Asking for sensor list");
            sendNotification(MessagesProtocol.STARTSENSINGSERVICE);
            sendDataMap(MessagesProtocol.QUERYSENSORLIST, "Query Sensor List");
        } else {
            Log.i(LOGTAG, "Oops! No Android Wear device connected.");
            Toast.makeText(rootView.getContext(), "Oops! No Android Wear device connected.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionSuspended(int cause) { }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) { }


    private void sendNotification(String command) {
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

    private void sendDataMap(Integer msgType, String message){

        dataMap.putInt(MessagesProtocol.SENDER, MessagesProtocol.ID_MOBILE);
        dataMap.putInt(MessagesProtocol.MSGTYPE, msgType);
        dataMap.putString(MessagesProtocol.MESSAGE, message);
        dataMap.putDouble(MessagesProtocol.TIMESTAMP, System.currentTimeMillis());

        if (msgType.equals(MessagesProtocol.STARTSENSING)){
            mSensorListToRecord.clear();
            for (CheckBox ch : mSensorListCheckBoxes){
                if (ch.isChecked()){
                    mSensorListToRecord.add(ch.getId());
                }
            }

            Double hz = 50.0;
            if (mEditTextHz.getText().toString().length() > 0){
                hz = Double.valueOf(mEditTextHz.getText().toString());
            }

            Integer duration = 10;
            if (mEditTextHz.getText().toString().length() > 0){
                duration = Integer.valueOf(mEditTextDuration.getText().toString());
            }


            Boolean consolidated = PreferenceManager
                    .getDefaultSharedPreferences(rootView.getContext())
                    .getBoolean(MessagesProtocol.SENSORSCONSOLIDATED, true);

            dataMap.putBoolean(MessagesProtocol.SENSORSCONSOLIDATED, consolidated);
            dataMap.putDouble(MessagesProtocol.SENSORHZ, hz);
            dataMap.putInt(MessagesProtocol.SENSOR_RECORDING_SECONDS, duration);
            dataMap.putIntegerArrayList(MessagesProtocol.SENSORSTORECORD, mSensorListToRecord);
        }

        new SendDataSyncThread(mGoogleApiClient, MessagesProtocol.DATAPATH, dataMap).start();
    }


    private void handleBundle(Bundle bundle){
        Integer msgType = bundle.getInt(MessagesProtocol.MSGTYPE);
        Integer msgSender = bundle.getInt(MessagesProtocol.SENDER);


        Log.i(LOGTAG, "MSGTYPE:" + msgType + " MSGSENDER:" + msgSender);
        if (msgSender.equals(MessagesProtocol.ID_WEAR)) {
            switch (msgType) {
                case MessagesProtocol.QUERYSENSORLISTRESPONSE:
                    mSensorList = bundle.getIntegerArrayList(MessagesProtocol.SENSORLISTRESULT);
                    Log.i(LOGTAG,  "No. of sensors:" + mSensorList.size());
                    refreshAvailableSensorsCard(mSensorList);
                    //Toast.makeText(rootView.getContext(), "No. of sensors:" + mSensorList.size(), Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    }


    private void handleMessage(String msg){
        String[] parts = msg.split("\\|");
        Integer msgType = Integer.valueOf(parts[0]);
        String msgLoad = parts[1];

        switch (msgType){
            case MessagesProtocol.SENDSENSEORSNAPSHOTREC_START:
                stopRecording_UI();
                Log.w(LOGTAG, "FileCreated:" + fileCreated + " mAClength:" + mActionAutoComplete.getText().length() + " SessionRequested:" + sessionRequested);
                if (mActionAutoComplete.getText().length() > 0){
                    if (!fileCreated) {
                        Log.w(LOGTAG,"Start saving file");
                        fileCreated = true;
                        mFileCreator = new FileCreator(
                                mActionAutoComplete.getText().toString() + "_" +
                                Utils.getSensorName(Integer.valueOf(msgLoad)),
                                Constants.DIRECTORY_SENSORS);
                        mFileCreator.openFileWriter();
                        Integer sensorType = Integer.valueOf(msgLoad);
                        if (sensorType == 0) {
                            mFileCreator.saveData(buildHeader());
                        } else {
                            String header = "No.\tTimestamp\t";
                            header += " [" + Utils.getSensorLength(sensorType) + "]" + Utils.getSensorName(sensorType) + "\n";
                            mFileCreator.saveData(header);
                        }
                        Toast.makeText(rootView.getContext(), "Start saving file: " + Utils.getSensorName(Integer.valueOf(msgLoad)),
                                Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case MessagesProtocol.SENDSENSEORSNAPSHOTHEADER:
                Log.i(LOGTAG, msgLoad);
                if (mActionAutoComplete.getText().length() > 0){
                    if (mFileCreator.isOpen()) {
                        mFileCreator.saveData(msgLoad + "\n");
                    }
                }
                break;
            case MessagesProtocol.SENDSENSEORSNAPSHOTREC:
                Log.i(LOGTAG, msgLoad);
                if (mActionAutoComplete.getText().length() > 0){
                    if (mFileCreator.isOpen()) {
                        mFileCreator.saveData(msgLoad + "\n");
                    }
                }
                break;
            case MessagesProtocol.SENDSENSEORSNAPSHOTREC_FINISH:
                Log.w(LOGTAG,"Stop saving file");
                if (mActionAutoComplete.getText().length() > 0){
                    mFileCreator.closeFileWriter();
                    Toast.makeText(rootView.getContext(),"File created: " + mFileCreator.getPath(), Toast.LENGTH_SHORT).show();
                    fileCreated = false;
                    //sendDataMap(MessagesProtocol.KILLSERVICE, "Kill service");
                }
                break;

            case MessagesProtocol.SENDSENSEORSNAPSHOTUPDATE:
                Log.w(LOGTAG,"Current sensors readings");
                reportProgress(msgLoad + " samples taken");
                break;


            case MessagesProtocol.SENDSENSEORSNAPSHOT_END:
                Log.w(LOGTAG,"Stop sensor service from wear: SENDSENSEORSNAPSHOT_END");
                sessionRequested = false;
                sendNotification(MessagesProtocol.STOPSENSINGSERVICE);
                break;

            default:
                insertSensorRecInfoCard(msgType, msgLoad);
                break;
        }

    }

    private String buildHeader(){
        String header = "No.\tTimestamp\t";
        for(Integer sensorType : mSensorListToRecord){
            header += " [" + Utils.getSensorLength(sensorType) + "]" + Utils.getSensorName(sensorType) + "\t";
        }
        header += "\n";
        return header;
    }

    private class DataBundleReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.w(LOGTAG, "DataBundle received from Wear");
            Bundle message = intent.getExtras();
            handleBundle(message);
        }
    }

    private class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String msg = intent.getStringExtra(MessagesProtocol.MESSAGE);
            if (sessionRequested) {
                handleMessage(msg);
            }
        }
    }

}
