package tudelft.mdp.utilities;



import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Date;

import tudelft.mdp.R;
import tudelft.mdp.enums.Constants;
import tudelft.mdp.enums.MessagesProtocol;

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
        mTwDummy = (TextView) rootView.findViewById(R.id.textDummy);

        // Register the local broadcast receiver, defined in step 3.
        IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
        MessageReceiver messageReceiver = new MessageReceiver();
        LocalBroadcastManager.getInstance(rootView.getContext()).registerReceiver(messageReceiver, messageFilter);

        return rootView;
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

    // Send a data object when the data layer connection is successful.
    @Override
    public void onConnected(Bundle connectionHint) {

        // Create a DataMap object and send it to the data layer
        DataMap dataMap = new DataMap();
        dataMap.putLong(MessagesProtocol.TIMESTAMP, new Date().getTime());
        dataMap.putInt(MessagesProtocol.SENDER, MessagesProtocol.ID_MOBILE);
        dataMap.putInt(MessagesProtocol.MSGTYPE, MessagesProtocol.SNDMESSAGE);
        dataMap.putString(MessagesProtocol.MESSAGE, "Hello from Mobile");
    }

    // Placeholders for required connection callbacks
    @Override
    public void onConnectionSuspended(int cause) { }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) { }


    private void handleMessage(Bundle bundle){
        Integer sender =bundle.getInt(MessagesProtocol.SENDER, 0);
        if (sender == MessagesProtocol.ID_WEAR){
            Integer msgType = bundle.getInt(MessagesProtocol.MSGTYPE, 0);
            if (msgType == MessagesProtocol.SENSOREVENT){

                float[] sensorValues = bundle.getFloatArray(MessagesProtocol.SENSORVALUE);
                Integer sensorType = bundle.getInt(MessagesProtocol.SENSORTYPE, 0);
                switch (sensorType){
                    case Sensor.TYPE_ACCELEROMETER:
                        //Log.i(LOGTAG, "Sensed data.");
                        if (mTwAccelerometer != null){
                            String text = "Accelerometer: ";
                            for(float value : sensorValues){
                                text += String.format("%.2f", value) + " ";
                            }
                            mTwAccelerometer.setText(text);
                        }
                        break;
                    case Sensor.TYPE_GYROSCOPE:
                        //Log.i(LOGTAG, "Sensed data.");
                        if (mTwGyroscope != null){
                            String text = "Gyroscope: ";
                            for(float value : sensorValues){
                                text += String.format("%.2f", value) + " ";
                            }
                            mTwGyroscope.setText(text);
                        }
                        break;
                    case Sensor.TYPE_GRAVITY:
                        //Log.i(LOGTAG, "Sensed data.");
                        if (mTwGravity != null){
                            String text = "Gravity: ";
                            for(float value : sensorValues){
                                text += String.format("%.2f", value) + " ";
                            }
                            mTwGravity.setText(text);
                        }
                        break;
                    case Sensor.TYPE_MAGNETIC_FIELD:
                        //Log.i(LOGTAG, "Sensed data.");
                        if (mTwMagnetometer != null){
                            String text = "Magnetic: ";
                            for(float value : sensorValues){
                                text += String.format("%.2f", value) + " ";
                            }
                            mTwMagnetometer.setText(text);
                        }
                        break;
                    case Sensor.TYPE_STEP_COUNTER:
                        //Log.i(LOGTAG, "Sensed data.");
                        if (mTwStepCounter != null){
                            String text = "Step Counter: ";
                            for(float value : sensorValues){
                                text += String.format("%.2f", value) + " ";
                            }
                            mTwStepCounter.setText(text);
                        }
                        break;
                    case Sensor.TYPE_HEART_RATE:
                        //Log.i(LOGTAG, "Sensed data.");
                        if (mTwHeartRate != null){
                            String text = "Heart Rate: ";
                            for(float value : sensorValues){
                                text += String.format("%.2f", value) + " ";
                            }
                            mTwHeartRate.setText(text);
                        }
                        break;
                    case Constants.SAMSUNG_HEART_RATE:
                        //Log.i(LOGTAG, "Sensed data.");
                        if (mTwHeartRate != null){
                            String text = "Heart Rate: ";
                            for(int i = 0; i < 3; i++){
                                float value = sensorValues[i];
                                text += String.format("%.2f", value) + " ";
                            }
                            mTwHeartRate.setText(text);
                        }
                        break;
                    case Constants.SAMSUNG_TILT:
                        //Log.i(LOGTAG, "Sensed data.");
                        if (mTwTilt != null){
                            String text = "Tilt: ";
                            for(int i = 0; i < 3; i++){
                                float value = sensorValues[i];
                                text += String.format("%.2f", value) + " ";
                            }
                            mTwTilt.setText(text);
                        }
                        break;
                    case Sensor.TYPE_LINEAR_ACCELERATION:
                        //Log.i(LOGTAG, "Sensed data.");
                        if (mTwHeartRate != null){
                            String text = "Linear Accel: ";
                            for(float value : sensorValues){
                                text += String.format("%.2f", value) + " ";
                            }
                            mTwLinearAcceleration.setText(text);
                        }
                        break;
                    case Sensor.TYPE_STEP_DETECTOR:
                        //Log.i(LOGTAG, "Sensed data.");
                        if (mTwHeartRate != null){
                            String text = "Step Detector: ";
                            String currentText = mTwStepDetector.getText().toString();
                            float currentSteps = 0;
                            if (currentText != null){
                                currentText = currentText.replace(text, "");
                                if (currentText.length() > 0){
                                    currentSteps = Double.valueOf(currentText).intValue();
                                }
                            }
                            for(float value : sensorValues){
                                text += String.format("%.2f", value + currentSteps) + " ";
                            }
                            mTwStepDetector.setText(text);
                        }
                        break;
                    case Sensor.TYPE_SIGNIFICANT_MOTION:
                        //Log.i(LOGTAG, "Sensed data.");

                        significantMotionTriggerCounter++;
                        if (mTwHeartRate != null){
                            String text = "Significant Motion: " + significantMotionTriggerCounter + "|";
                            for(float value : sensorValues){
                                text += String.format("%.2f", value) + " ";
                            }
                            mTwSignificantMotion.setText(text);
                        }
                        break;
                    case Sensor.TYPE_ROTATION_VECTOR:
                        //Log.i(LOGTAG, "Sensed data.");
                        if (mTwHeartRate != null){
                            String text = "RotVect: ";
                            for(float value : sensorValues){
                                text += String.format("%.2f", value) + " ";
                            }
                            mTwRotationVector.setText(text);
                        }
                        break;
                    default:
                        break;
                }
            } else if (msgType == MessagesProtocol.SNDMESSAGE) {
                mTwDummy.setText(bundle.getString(MessagesProtocol.MESSAGE, "Fail!"));
            }
        }
    }


    private class SendToDataLayerThread extends Thread {
        String path;
        DataMap dataMap;

        // Constructor for sending data objects to the data layer
        SendToDataLayerThread(String p, DataMap data) {
            path = p;
            dataMap = data;
        }

        public void run() {
            NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
            for (Node node : nodes.getNodes()) {

                // Construct a DataRequest and send over the data layer
                PutDataMapRequest putDMR = PutDataMapRequest.create(path);
                putDMR.getDataMap().putAll(dataMap);
                PutDataRequest request = putDMR.asPutDataRequest();
                DataApi.DataItemResult result = Wearable.DataApi.putDataItem(mGoogleApiClient,request).await();
                if (result.getStatus().isSuccess()) {
                    Log.v(LOGTAG, "DataMap: " + dataMap + " sent to: " + node.getDisplayName());
                } else {
                    // Log an error
                    Log.v(LOGTAG, "ERROR: failed to send DataMap");
                }
            }
        }
    }

    private class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle message = intent.getExtras();
            handleMessage(message);
            // Display message in UI
        }
    }


}
