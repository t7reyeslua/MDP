package tudelft.mdp.locationTracker;

import com.devspark.robototextview.widget.RobotoTextView;

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
import tudelft.mdp.backend.endpoints.locationLogEndpoint.model.LocationLogRecord;
import tudelft.mdp.enums.UserPreferences;
import tudelft.mdp.ui.UserHistoryCard;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LocationHistoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class LocationHistoryFragment extends Fragment implements
    RequestUserLocationHistoryAsyncTask.RequestUserLocationHistoryAsyncResponse{

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
    private RobotoTextView twNoDataUsers;

    private ArrayList<Card> mCardsArrayList;
    private CardArrayAdapter mCardArrayAdapter;
    private CardListView mCardListView;

    private List<LocationLogRecord> userLocationHistory = new ArrayList<LocationLogRecord>();

    private ProgressDialog pd = null;

    private static final String TAG = "MDP-LocationHistoryFragment";

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LocationHistoryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LocationHistoryFragment newInstance(String param1, String param2) {
        LocationHistoryFragment fragment = new LocationHistoryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    public LocationHistoryFragment() {
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

        rootView = inflater.inflate(R.layout.fragment_location_history, container, false);

        twNoDataUsers = (RobotoTextView) rootView.findViewById(R.id.empty);
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

    private void refreshList(){
        if (!requestInProcess) {
            this.pd = ProgressDialog.show(rootView.getContext(), "Working...", "Getting user location history...", true, false);

            RequestUserLocationHistoryAsyncTask
                    userLocationHistoryListAsyncTask = new RequestUserLocationHistoryAsyncTask();
            userLocationHistoryListAsyncTask.delegate = this;
            userLocationHistoryListAsyncTask.execute(rootView.getContext());
        }
    }

    public void processFinishRequestUserLocationHistory(List<LocationLogRecord> outputList){
        mCardsArrayList = new ArrayList<Card>();
        userLocationHistory = outputList;

        if (userLocationHistory != null) {

            String lastZone = "";
            List<LocationLogRecord> reducedUserHistory = new ArrayList<LocationLogRecord>();

            for (LocationLogRecord record : userLocationHistory){
                if (record.getZone() != null) {
                    if (!record.getZone().equals(lastZone)) {
                        lastZone = record.getZone();
                        reducedUserHistory.add(record);
                    }
                }
            }


            for (int i = reducedUserHistory.size()-1; i >= 0; i--) {
                    Card card = new UserHistoryCard(rootView.getContext(),
                            reducedUserHistory.get(i));
                    mCardsArrayList.add(card);
            }



            mCardArrayAdapter = new CardArrayAdapter(rootView.getContext(), mCardsArrayList);
            mCardListView = (CardListView) rootView.findViewById(R.id.myList);
            if (mCardListView != null) {
                mCardListView.setAdapter(mCardArrayAdapter);
            }
            twNoDataUsers.setVisibility(View.GONE);
        } else {

            twNoDataUsers.setVisibility(View.VISIBLE);
            mCardArrayAdapter = new CardArrayAdapter(rootView.getContext(), mCardsArrayList);
            mCardListView = (CardListView) rootView.findViewById(R.id.myList);
            if (mCardListView != null) {
                mCardListView.setAdapter(mCardArrayAdapter);
            }
            Toast.makeText(rootView.getContext(), "Oops! There aren't any location history records.",
                    Toast.LENGTH_SHORT).show();
        }

        requestInProcess = false;

        if (this.pd != null) {
            this.pd.dismiss();
        }
    }


}
