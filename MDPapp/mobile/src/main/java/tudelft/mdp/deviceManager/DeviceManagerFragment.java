package tudelft.mdp.deviceManager;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.view.CardListView;
import tudelft.mdp.R;
import tudelft.mdp.backend.endpoints.deviceEndpoint.model.NfcRecord;
import tudelft.mdp.enums.UserPreferences;
import tudelft.mdp.ui.DeviceCardBuilder;


public class DeviceManagerFragment extends Fragment implements
        DeviceListRequestAsyncTask.RequestDeviceListAsyncResponse,
        RequestDeviceUsageByUserAsyncTask.RequestDeviceUsageByUserAsyncResponse{

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private String mUsername;
    private Boolean requestInProcess = false;

    private View rootView;

    private ArrayList<Card> mCardsArrayList;
    private CardArrayAdapter mCardArrayAdapter;
    private CardListView mCardListView;

    private List<NfcRecord> deviceList = new ArrayList<NfcRecord>();

    private ProgressDialog pd = null;

    private static final String TAG = "MDP-DeviceManager";



    public static DeviceManagerFragment newInstance(String param1, String param2) {
        DeviceManagerFragment fragment = new DeviceManagerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    public DeviceManagerFragment() {
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
        rootView = inflater.inflate(R.layout.fragment_device_manager, container, false);
        setHasOptionsMenu(true);

        mUsername = PreferenceManager.getDefaultSharedPreferences(rootView.getContext())
                .getString(UserPreferences.USERNAME, null);

        refreshList();
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.device_manager_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Log.d(TAG, "Refresh clicked");
        switch (item.getItemId()) {
            case R.id.action_refresh:
                refreshList();
                requestInProcess = true;
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void processFinishRequestDeviceList(List<NfcRecord> outputList) {
        mCardsArrayList = new ArrayList<Card>();
        deviceList = outputList;

        if (deviceList != null) {

            for (NfcRecord device : deviceList) {

                DeviceCardBuilder dcBuilder = new DeviceCardBuilder(
                        this,
                        rootView.getContext(),
                        device.getNfcId(),
                        device.getType(),
                        device.getDescription(),
                        device.getLocation(),
                        device.getState(),
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        0
                );

                Card card = dcBuilder.buildDeviceCard();
                mCardsArrayList.add(card);
            }

            mCardArrayAdapter = new CardArrayAdapter(rootView.getContext(), mCardsArrayList);
            mCardListView = (CardListView) rootView.findViewById(R.id.myList);
            if (mCardListView != null) {
                mCardListView.setAdapter(mCardArrayAdapter);
            }
        } else {

            Toast.makeText(rootView.getContext(), "Oops! There aren't any registered devices yet.",
                    Toast.LENGTH_SHORT).show();
        }

        requestInProcess = false;

        if (this.pd != null) {
            this.pd.dismiss();
        }
    }

    public void processFinishRequestDeviceUsageByUser(List<Object> outputList){
        int cardIndex = -1 ;

        String nfcTag = (String) outputList.get(0);
        Double totalTime = (Double) outputList.get(1);
        Double userTime = (Double) outputList.get(2);
        Double totalPower = (Double) outputList.get(3);
        Double userPower = (Double) outputList.get(4);
        Double percentage = (Double) outputList.get(5);
        Integer userStatus = ((Double) outputList.get(6)).intValue();


        String currentlyUsing = "You are currently using this device.";
        if (userStatus == 0){
            currentlyUsing = "You are not currently using this device";
        }

        Toast.makeText(rootView.getContext(), currentlyUsing,
                Toast.LENGTH_SHORT).show();

        Boolean indexFound = false;
        while (!indexFound){
            if (deviceList.get(++cardIndex).getNfcId().equals(nfcTag)){
                indexFound = true;
            }
        }


        if (cardIndex > -1) {
            NfcRecord device = deviceList.get(cardIndex);

            DeviceCardBuilder dcBuilder = new DeviceCardBuilder(
                    this,
                    rootView.getContext(),
                    device.getNfcId(),
                    device.getType(),
                    device.getDescription(),
                    device.getLocation(),
                    device.getState(),
                    totalTime,
                    userTime,
                    totalPower,
                    userPower,
                    percentage,
                    userStatus
            );

            Card card = dcBuilder.buildDeviceCard();
            card.setExpanded(true);

            mCardsArrayList.set(cardIndex, card);
            mCardArrayAdapter.notifyDataSetChanged();
        }


    }

    private void refreshList(){
        if (!requestInProcess) {
            this.pd = ProgressDialog.show(rootView.getContext(), "Working...", "Getting devices information...", true, false);

            DeviceListRequestAsyncTask deviceListAsyncTask = new DeviceListRequestAsyncTask();
            deviceListAsyncTask.delegate = this;
            deviceListAsyncTask.execute();
        }

        /*
        DeviceCardBuilder dcBuilder = new DeviceCardBuilder(
                rootView.getContext(),
                "12340",
                "Device Type",
                null,
                "Home",
                0,
                3601.0,
                3599.0,
                21.5,
                14.78,
                89.2
        );

        Card card = dcBuilder.buildDeviceCard();
        card.setExpanded(true);
        mCardsArrayList.add(card);

        mCardsArrayList.set(0, card);

        mCardArrayAdapter.notifyDataSetChanged();
        */


    }


}
