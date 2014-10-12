package tudelft.mdp.utilities;



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
import android.hardware.Sensor;
import android.os.Bundle;
import android.app.Fragment;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.view.CardListView;
import it.gmariotti.cardslib.library.view.CardView;
import tudelft.mdp.R;
import tudelft.mdp.Utils;
import tudelft.mdp.communication.SendDataSyncThread;
import tudelft.mdp.communication.SendMessageThread;
import tudelft.mdp.enums.Constants;
import tudelft.mdp.enums.MessagesProtocol;
import tudelft.mdp.fileManagement.FileCreator;
import tudelft.mdp.ui.SensorRecControlCard;
import tudelft.mdp.ui.SensorRecInfoCard;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SensorViewerFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class SensorViewerFragment extends Fragment implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    private TextView mTwAccelerometer;
    private TextView mTwGyroscope;
    private TextView mTwGravity;
    private TextView mTwMagnetometer;
    private TextView mTwStepCounter;
    private TextView mTwHeartRate;
    private TextView mTwStepDetector;
    private TextView mTwSignificantMotion;
    private TextView mTwTilt;
    private TextView mTwRotationVector;
    private TextView mTwLinearAcceleration;

    private TextView mTwDummy;

    private int significantMotionTriggerCounter = 0;

    private static final String[] ACTIONS = new String[] {
            "Turn knob right", "Turn knob left"
    };



    private CardView mCardView;
    private Card mCardSensorRec;

    private ArrayList<Card> mCardsArrayList;
    private CardArrayAdapter mCardArrayAdapter;
    private CardListView mCardListView;

    private ProgressBar mProgressBar;
    private ToggleButton mToggleButton;
    private AutoCompleteTextView mActionAutoComplete;
    private Chronometer mChronometer;
    private TextView mCurrentSample;
    private Vibrator v;
    private FileCreator mFileCreator;


    private View rootView;
    GoogleApiClient mGoogleApiClient;

    private static final String LOGTAG = "MDP-SensorViewerFragment";


    public static SensorViewerFragment newInstance(String param1, String param2) {
        SensorViewerFragment fragment = new SensorViewerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    public SensorViewerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }


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
        configureTextViews();
        configureBroadcastReceivers();
        configureCards();
        configureCardList();
        configureAutoComplete();
        configureToggleButton();

        v = (Vibrator) this.getActivity().getSystemService(Context.VIBRATOR_SERVICE);

        return rootView;
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

    private String now() {
        DateFormat dateFormat = android.text.format.DateFormat.getTimeFormat(rootView.getContext());
        return dateFormat.format(new Date());
    }

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

    private void startRecording(){
        mActionAutoComplete.setEnabled(false);
        mProgressBar.setIndeterminate(true);


        //String command = MessagesProtocol.STARTSENSING + "| Start sensing";
        //new SendMessageThread(mGoogleApiClient, MessagesProtocol.MSGPATH, command);

        // Create a DataMap object and send it to the data layer

        String filename = mActionAutoComplete.getText().toString();

        sendNotification(MessagesProtocol.STARTSENSINGSERVICE);

        DataMap dataMap = new DataMap();
        dataMap.putInt(MessagesProtocol.SENDER, MessagesProtocol.ID_MOBILE);
        dataMap.putInt(MessagesProtocol.MSGTYPE, MessagesProtocol.STARTSENSING);
        dataMap.putString(MessagesProtocol.MESSAGE, filename);

        new SendDataSyncThread(mGoogleApiClient, MessagesProtocol.DATAPATH, dataMap).start();


        v.vibrate(500);
        startChronometer();
    }

    private void stopRecording(){

        mActionAutoComplete.setEnabled(true);
        mChronometer.stop();
        mChronometer.setTextColor(getResources().getColor(R.color.DarkGray));


        String filename = mActionAutoComplete.getText().toString();
        // Create a DataMap object and send it to the data layer
        DataMap dataMap = new DataMap();
        dataMap.putInt(MessagesProtocol.SENDER, MessagesProtocol.ID_MOBILE);
        dataMap.putInt(MessagesProtocol.MSGTYPE, MessagesProtocol.STOPSENSING);
        dataMap.putString(MessagesProtocol.MESSAGE, filename);

        new SendDataSyncThread(mGoogleApiClient, MessagesProtocol.DATAPATH, dataMap).start();

        v.vibrate(500);

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

    private void configureCards(){
        mCardView = (CardView) rootView.findViewById(R.id.cardControl);

        mCardSensorRec = new SensorRecControlCard(rootView.getContext());
        mCardSensorRec.setShadow(true);
        mCardView.setCard(mCardSensorRec);

        mProgressBar = (ProgressBar) mCardView.findViewById(R.id.progressBar);
        mToggleButton = (ToggleButton) mCardView.findViewById(R.id.toggleButton);
        mActionAutoComplete = (AutoCompleteTextView) mCardView.findViewById(R.id.acFile);
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

    private void configureAutoComplete(){
        ArrayAdapter<String> actionsAdapter   = new ArrayAdapter<String>(rootView.getContext(), android.R.layout.simple_dropdown_item_1line, ACTIONS);

        mActionAutoComplete.setAdapter(actionsAdapter);
        actionsAdapter.notifyDataSetChanged();
    }

    private void configureTextViews(){
        mTwDummy = (TextView) rootView.findViewById(R.id.textDummy);
        mTwAccelerometer = (TextView) rootView.findViewById(R.id.textAccelerometer);
        mTwGyroscope = (TextView) rootView.findViewById(R.id.textGyroscope);
        mTwGravity = (TextView) rootView.findViewById(R.id.textGravity);
        mTwMagnetometer = (TextView) rootView.findViewById(R.id.textMagnetometer);
        mTwStepCounter = (TextView) rootView.findViewById(R.id.textStepCounter);
        mTwHeartRate = (TextView) rootView.findViewById(R.id.textHeartRate);
        mTwSignificantMotion = (TextView) rootView.findViewById(R.id.textSignificantMotion);
        mTwStepDetector = (TextView) rootView.findViewById(R.id.textStepDetector);
        mTwRotationVector = (TextView) rootView.findViewById(R.id.textRotationVector);
        mTwLinearAcceleration = (TextView) rootView.findViewById(R.id.textLinearAcceleration);
        mTwTilt = (TextView) rootView.findViewById(R.id.textTilt);
    }

    private void configureBroadcastReceivers(){
        // Register the local broadcast receiver, defined in step 3.
        IntentFilter bundleFilter = new IntentFilter(MessagesProtocol.WEARSENSORSBUNDLE);
        DataBundleReceiver dataBundleReceiver = new DataBundleReceiver();
        LocalBroadcastManager.getInstance(rootView.getContext()).registerReceiver(dataBundleReceiver, bundleFilter);


        IntentFilter messageFilter = new IntentFilter(MessagesProtocol.WEARSENSORSMSG);
        MessageReceiver messageReceiver = new MessageReceiver();
        LocalBroadcastManager.getInstance(rootView.getContext()).registerReceiver(messageReceiver, messageFilter);

    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
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

        // Create a DataMap object and send it to the data layer
        DataMap dataMap = new DataMap();
        dataMap.putLong(MessagesProtocol.TIMESTAMP, new Date().getTime());
        dataMap.putInt(MessagesProtocol.SENDER, MessagesProtocol.ID_MOBILE);
        dataMap.putInt(MessagesProtocol.MSGTYPE, MessagesProtocol.SNDMESSAGE);
        dataMap.putString(MessagesProtocol.MESSAGE, "Hello from Mobile");

        new SendDataSyncThread(mGoogleApiClient, MessagesProtocol.DATAPATH, dataMap).start();
    }

    @Override
    public void onConnectionSuspended(int cause) { }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) { }

    private void handleMessage(String msg){
        String[] parts = msg.split("\\|");
        Integer msgType = Integer.valueOf(parts[0]);
        String msgLoad = parts[1];


        switch (msgType){
            case MessagesProtocol.SENDSENSEORSNAPSHOTREC_START:
                Log.w(LOGTAG,"Start saving file");
                if (mActionAutoComplete.getText().length() > 0){
                    mFileCreator = new FileCreator(mActionAutoComplete.getText().toString(), Constants.DIRECTORY_SENSORS);
                    mFileCreator.openFileWriter();

                    Toast.makeText(rootView.getContext(),"Start saving file", Toast.LENGTH_SHORT).show();
                }
                break;
            case MessagesProtocol.SENDSENSEORSNAPSHOTREC_FINISH:
                Log.w(LOGTAG,"Stop saving file");
                if (mActionAutoComplete.getText().length() > 0){
                    mFileCreator.closeFileWriter();
                    Toast.makeText(rootView.getContext(),"File created: " + mFileCreator.getPath(), Toast.LENGTH_SHORT).show();


                    DataMap dataMap = new DataMap();
                    dataMap.putInt(MessagesProtocol.SENDER, MessagesProtocol.ID_MOBILE);
                    dataMap.putInt(MessagesProtocol.MSGTYPE, MessagesProtocol.KILLSERVICE);
                    dataMap.putString(MessagesProtocol.MESSAGE, "Kill service");

                    new SendDataSyncThread(mGoogleApiClient, MessagesProtocol.DATAPATH, dataMap).start();
                    sendNotification(MessagesProtocol.STOPSENSINGSERVICE);
                }
                break;
            case MessagesProtocol.SENDSENSEORSNAPSHOTREC:
                Log.i(LOGTAG, msgLoad);
                if (mActionAutoComplete.getText().length() > 0){
                    mFileCreator.saveData(msgLoad + mActionAutoComplete.getText().toString() +"\n");
                }
                break;
            default:
                insertSensorRecInfoCard(msgType, msgLoad);
                break;
        }

    }

    private void saveFile(ArrayList<String> list) {
        mFileCreator = new FileCreator(mActionAutoComplete.getText().toString(), Constants.DIRECTORY_SENSORS);
        mFileCreator.openFileWriter();

        for (String s : list){
            mFileCreator.saveData(s + "\n");
        }

        mFileCreator.closeFileWriter();
        Toast.makeText(rootView.getContext(),"File saved: " + mFileCreator.getPath(), Toast.LENGTH_SHORT).show();
    }

    private void handleMessage(Bundle bundle){
        Integer sender =bundle.getInt(MessagesProtocol.SENDER, 0);
        if (sender == MessagesProtocol.ID_WEAR){
            Integer msgType = bundle.getInt(MessagesProtocol.MSGTYPE, 0);
            if (msgType == MessagesProtocol.SENSOREVENT){

                float[] sensorValues = bundle.getFloatArray(MessagesProtocol.SENSORVALUE);
                Integer sensorType = bundle.getInt(MessagesProtocol.SENSORTYPE, 0);

            } else if (msgType == MessagesProtocol.SNDMESSAGE) {
                mTwDummy.setText(bundle.getString(MessagesProtocol.MESSAGE, "Fail!"));
            } else if (msgType == MessagesProtocol.SENDSENSEORSNAPSHOTREC) {
                ArrayList<String> recordedSensors = bundle.getStringArrayList(MessagesProtocol.RECORDEDSENSORS);
                Toast.makeText(rootView.getContext(),"Samples taken: " + recordedSensors.size(), Toast.LENGTH_SHORT).show();
                saveFile(recordedSensors);
            }
        }
    }


    private class DataBundleReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            Bundle message = intent.getExtras();
            handleMessage(message);
        }
    }

    private class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            String msg = intent.getStringExtra(MessagesProtocol.MESSAGE);
            handleMessage(msg);
        }
    }

}
