package tudelft.mdp;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.Vibrator;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import tudelft.mdp.enums.Constants;
import tudelft.mdp.enums.MessagesProtocol;
import tudelft.mdp.services.SensorReaderService;

public class MainScreen extends Activity implements ServiceConnection {

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
    private WatchViewStub stub;

    private ImageView mImageLogo;

    private int significantMotionTriggerCounter = 0;


    private static final String TAG = "MDP - WearMain";
    private Messenger mServiceMessenger = null;
    private boolean mIsBound;
    private final Messenger mMessenger = new Messenger(new IncomingMessageHandler());
    private ServiceConnection mConnection = this;
    private SharedPreferences sharedPrefs;
    private Vibrator v;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        /*
        final RotateAnimation anim = new RotateAnimation(0f, 350f,   Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setInterpolator(new LinearInterpolator());
        anim.setRepeatCount(Animation.INFINITE);
        anim.setDuration(800);*/


        stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTwAccelerometer = (TextView) stub.findViewById(R.id.textAccelerometer);
                mTwGyroscope = (TextView) stub.findViewById(R.id.textGyroscope);
                mTwGravity = (TextView) stub.findViewById(R.id.textGravity);
                mTwMagnetometer = (TextView) stub.findViewById(R.id.textMagnetometer);
                mTwStepCounter = (TextView) stub.findViewById(R.id.textStepCounter);
                mTwHeartRate = (TextView) stub.findViewById(R.id.textHeartRate);
                mTwSignificantMotion = (TextView) stub.findViewById(R.id.textSignificantMotion);
                mTwStepDetector = (TextView) stub.findViewById(R.id.textStepDetector);
                mTwRotationVector = (TextView) stub.findViewById(R.id.textRotationVector);
                mTwLinearAcceleration = (TextView) stub.findViewById(R.id.textLinearAcceleration);
                mTwTilt = (TextView) stub.findViewById(R.id.textTilt);
                mTwDummy = (TextView) stub.findViewById(R.id.textDummy);

                /*
                mImageLogo = (ImageView) stub.findViewById(R.id.imageLogo);
                mImageLogo.startAnimation(anim);*/
                v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

            }
        });

        automaticBinding();

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

    /* Sensing Service Routines*/
    private void automaticBinding() {
        if (SensorReaderService.isRunning()){
            doBindService();
        } else{
            startSensingService();
            doBindService();
        }
        if (v != null) {
            v.vibrate(500);
        }
    }

    private void automaticUnbinding() {
        stopSensingService();
        if (v != null) {
            v.vibrate(500);
        }
    }

    public void startSensingService(){
        Log.i(TAG, "Sensing Service: START");
        Intent intent = new Intent(this, SensorReaderService.class);
        this.startService(intent);
    }

    public void stopSensingService(){
        Log.i(TAG, "Sensing Service: STOP");
        doUnbindService();
        //this.stopService(new Intent(this, SensorReaderService.class));
    }


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
            Message msg = Message.obtain(null, SensorReaderService.MSG_REGISTER_CLIENT);
            msg.replyTo = mMessenger;
            mServiceMessenger.send(msg);
        }
        catch (RemoteException e) {
            // In this case the service has crashed before we could even do anything with it
        }
    }


    private void doBindService() {
        this.bindService(new Intent(this, SensorReaderService.class), mConnection, 0);
        mIsBound = true;
    }

    private void doUnbindService() {
        if (mIsBound) {

            // If we have received the service, and hence registered with it, then now is the time to unregister.
            if (mServiceMessenger != null) {
                try {
                    Message msg = Message.obtain(null, SensorReaderService.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mServiceMessenger.send(msg);
                } catch (RemoteException e) {
                    // There is nothing special we need to do if the service has crashed.
                }
            }
            // Detach our existing connection.
            this.unbindService(mConnection);
            mIsBound = false;
        }
    }

    private void updateValues(ArrayList<Object> values){

        Integer sensorType = (Integer) values.get(0);
        float [] sensorValues = (float[]) values.get(1);

        switch (sensorType) {
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

    }

    private class IncomingMessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG,"IncomingHandler:handleMessage");
            switch (msg.what) {
                case SensorReaderService.MSG_SET_SENSOR_EVENT_VALUE:
                    ArrayList<Object> values = (ArrayList<Object>) msg.getData().getSerializable(
                            "sensor");
                    updateValues(values);
                    break;
                case SensorReaderService.MSG_SET_BUNDLE_VALUE:
                    Bundle bundle = msg.getData();
                    Integer sender =bundle.getInt(MessagesProtocol.SENDER, 0);
                    if (sender == MessagesProtocol.ID_MOBILE){
                        mTwDummy.setText(bundle.getString(MessagesProtocol.MESSAGE,"Fail!"));
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }



}
