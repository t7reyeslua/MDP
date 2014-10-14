package tudelft.mdp.locationTracker;



import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.os.SystemClock;
import android.os.Vibrator;
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

import java.util.ArrayList;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.view.CardListView;
import it.gmariotti.cardslib.library.view.CardView;
import tudelft.mdp.R;
import tudelft.mdp.ui.FingerprintControlCard;
import tudelft.mdp.ui.FingerprintZoneCard;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LocationFingerprintFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class LocationFingerprintFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

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
    private TextView mCurrentSample;
    private Vibrator v;



    private static final String TAG = "MDP-LocationFingerprintFragment";

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



    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LocationFingerprintFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LocationFingerprintFragment newInstance(String param1, String param2) {
        LocationFingerprintFragment fragment = new LocationFingerprintFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    public LocationFingerprintFragment() {
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
        rootView =  inflater.inflate(R.layout.fragment_location_fingerprint, container, false);
        mCardView = (CardView) rootView.findViewById(R.id.cardFingerprint);

        mCardFingerprint = new FingerprintControlCard(rootView.getContext());
        mCardFingerprint.setShadow(true);
        mCardView.setCard(mCardFingerprint);


        v = (Vibrator) this.getActivity().getSystemService(Context.VIBRATOR_SERVICE);

        mProgressBar = (ProgressBar) mCardView.findViewById(R.id.progressBar);
        mToggleButton = (ToggleButton) mCardView.findViewById(R.id.toggleButton);
        mPlaceAutoComplete = (AutoCompleteTextView) mCardView.findViewById(R.id.acPlace);
        mZoneAutoComplete = (AutoCompleteTextView) mCardView.findViewById(R.id.acZone);
        mChronometer = (Chronometer) mCardView.findViewById(R.id.chronometer);

        configureAutoComplete();
        configureToggleButton();
        configureCardList();

        return rootView;
    }

    private void configureCardList(){
        mCardsArrayList = new ArrayList<Card>();
        mCardArrayAdapter = new CardArrayAdapter(rootView.getContext(), mCardsArrayList);
        mCardListView = (CardListView) rootView.findViewById(R.id.myList);
        if (mCardListView != null) {
            mCardListView.setAdapter(mCardArrayAdapter);
        }
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

    private void startFingerprint(){

        if ((mPlaceAutoComplete.getText().length() > 0) && (mZoneAutoComplete.getText().length() > 0)) {

            mPlaceAutoComplete.setEnabled(false);
            mZoneAutoComplete.setEnabled(false);
            mProgressBar.setIndeterminate(true);
            //startSensingService();

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

    private void stopFingerprint(){
        mPlaceAutoComplete.setEnabled(true);
        mZoneAutoComplete.setEnabled(true);
        mChronometer.stop();
        mChronometer.setTextColor(getResources().getColor(R.color.DarkGray));


        replaceFingerprintCard(12);

        //doUnbindService();
        //this.getActivity().stopService(new Intent(this.getActivity(), FingerprintRSSIrecorder.class));


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



}
