package tudelft.mdp.locationTracker;

import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.internal.CardExpand;
import it.gmariotti.cardslib.library.view.CardListView;
import it.gmariotti.cardslib.library.view.CardView;
import tudelft.mdp.R;
import tudelft.mdp.enums.UserPreferences;
import tudelft.mdp.ui.CalibrationControlCard;
import tudelft.mdp.ui.CalibrationCurrentValuesCard;
import tudelft.mdp.ui.CalibrationExecuteCard;
import tudelft.mdp.ui.CalibrationNetworksCard;

public class LocationCalibrationFragment extends Fragment {


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
    private Vibrator v;

    private boolean isMaster = false;

    private boolean mCalibrated;
    private boolean mRegressionDone = false;
    private float calibrationM;
    private float calibrationB;


    private static final String TAG = "MDP-LocationCalibrationFragment";
    public LocationCalibrationFragment() {
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
        rootView =  inflater.inflate(R.layout.fragment_location_calibration, container, false);


        configureCardsInit();

        v = (Vibrator) this.getActivity().getSystemService(Context.VIBRATOR_SERVICE);

        mProgressBar = (ProgressBar) mCardView.findViewById(R.id.progressBar);
        mToggleButton = (ToggleButton) mCardView.findViewById(R.id.toggleButton);
        mSwitch = (Switch) mCardView.findViewById(R.id.swMaster);
        mButtonRegression = (Button) mCardViewNetworks.findViewById(R.id.buttonRegression);
        mButtonCalibrate = (Button) mCardViewCalibrateExecute.findViewById(R.id.buttonCalibration);

        configureRegressionButton();
        configureCalibrationButton();
        configureToggleButton();
        configureSwitch();

        return rootView;
    }

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

        String calibration = "Results";
        String calibrationValues = "m = " + String.format("%.3f", calibrationM) + "     b = " + String.format("%.3f", calibrationB);

        /*
        if (mCalibrated){
            calibration = "Previously calibrated";
        }
        */


        mCardViewValues = (CardView) rootView.findViewById(R.id.cardValues);
        mCardValues = new CalibrationCurrentValuesCard(rootView.getContext(), calibration, calibrationValues);
        mCardValues.setShadow(true);
        mCardViewValues.setCard(mCardValues);
        mCardViewValues.setVisibility(View.GONE);
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
        ArrayList<CalibrationNetworkObject> mock = mockNetworks();
        mCardNetworks = new CalibrationNetworksCard(rootView.getContext(), mock);
        mCardNetworks.init();
        mCardNetworks.setShadow(true);

        mCardViewNetworks = (CardView) rootView.findViewById(R.id.cardNetworks);

        mCardViewNetworks.setCard(mCardNetworks);
        mCardViewNetworks.setVisibility(View.GONE);
    }

    private void refreshNetworkCard(){

        mCardNetworks.updateItems(mockNetworks());
    }


    private void configureCalibrateExecuteCard(){
        mCardViewCalibrateExecute = (CardView) rootView.findViewById(R.id.cardCalibrateExecute);
        mCardCalibrateExecute = new CalibrationExecuteCard(rootView.getContext());
        mCardCalibrateExecute.setShadow(true);
        mCardViewCalibrateExecute.setCard(mCardCalibrateExecute);
        mCardViewCalibrateExecute.setVisibility(View.GONE);
    }


    private void getPreviousCalibrationValues(){
        mCalibrated = PreferenceManager.getDefaultSharedPreferences(rootView.getContext())
                .getBoolean(UserPreferences.CALIBRATED, false);

        calibrationM = PreferenceManager.getDefaultSharedPreferences(rootView.getContext())
                .getFloat(UserPreferences.CALIBRATION_M, 1.0f);

        calibrationB = PreferenceManager.getDefaultSharedPreferences(rootView.getContext())
                .getFloat(UserPreferences.CALIBRATION_B, 0.0f);

    }

    private void configureCardList(){
        mCardsArrayList = new ArrayList<Card>();
        mCardArrayAdapter = new CardArrayAdapter(rootView.getContext(), mCardsArrayList);
        mCardListView = (CardListView) rootView.findViewById(R.id.myList);
        if (mCardListView != null) {
            mCardListView.setAdapter(mCardArrayAdapter);
        }
    }


    private void executeRegression(){

        //TODO:Regression and calculate m/b values

        mRegressionDone = true;
        mCardViewValues.setVisibility(View.VISIBLE);
        if (!mSwitch.isChecked()){
            mCardViewCalibrateExecute.setVisibility(View.VISIBLE);
        } else {
            mCardViewCalibrateExecute.setVisibility(View.GONE);
        }
    }

    private void executeCalibration(){

        //TODO:Calibrate

    }

    private void configureCalibrationButton(){
        mButtonCalibrate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
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

    private void startCalibration(){
        mProgressBar.setIndeterminate(true);
        mSwitch.setEnabled(false);
        refreshProgressCard(1, View.VISIBLE);
        mCardViewNetworks.setVisibility(View.GONE);
        mCardViewCalibrateExecute.setVisibility(View.GONE);
        mCardViewValues.setVisibility(View.GONE);
        v.vibrate(500);

        mRegressionDone = false;
    }

    private void stopCalibration(){
        mProgressBar.setIndeterminate(false);
        mSwitch.setEnabled(true);
        mCardViewProgress.setVisibility(View.GONE);
        mCardViewNetworks.setVisibility(View.VISIBLE);
        refreshNetworkCard();
        v.vibrate(500);

    }

    private ArrayList<CalibrationNetworkObject> mockNetworks (){
        ArrayList<CalibrationNetworkObject> networks = new ArrayList<CalibrationNetworkObject>();
        CalibrationNetworkObject network1 =  new CalibrationNetworkObject(
                "Mierdify",
                "00:EE:43:FF:12",
                75.2f,
                35
        );
        CalibrationNetworkObject network2 =  new CalibrationNetworkObject(
                "Eduroam",
                "00:EE:43:FF:12",
                85.2f,
                25
        );
        CalibrationNetworkObject network3 =  new CalibrationNetworkObject(
                "Wifi",
                "00:EE:43:FF:12",
                25.2f,
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



}
