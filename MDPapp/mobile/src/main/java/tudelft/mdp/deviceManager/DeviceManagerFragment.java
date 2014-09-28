package tudelft.mdp.deviceManager;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.internal.CardExpand;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.view.CardListView;
import it.gmariotti.cardslib.library.view.CardView;
import tudelft.mdp.R;
import tudelft.mdp.enums.UserPreferences;
import tudelft.mdp.ui.DeviceCard;
import tudelft.mdp.ui.DeviceCardExpand;
import tudelft.mdp.ui.DeviceCardHeader;


public class DeviceManagerFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private String mUsername;

    private View rootView;

    private ArrayList<Card> mCardsArrayList;
    private CardArrayAdapter mCardArrayAdapter;
    private CardListView mCardListView;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DeviceManagerFragment.
     */
    // TODO: Rename and change types and number of parameters
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

        populateCardList();
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.device_manager_menu, menu);
    }


    private void populateCardList(){
        mCardsArrayList = new ArrayList<Card>();


        for (int i = 1; i < 5; i++) {
            //Create a Card

            /*
            Card card = new Card(rootView.getContext(), R.layout.card_inner_layout);
            */

            Boolean active = false;

            if (i<2) {
                active = true;
            }

            Card card = new DeviceCard(rootView.getContext(),
                    "1234" + i,
                    "" + i,
                    active);

            DeviceCardHeader header = new DeviceCardHeader(rootView.getContext(), "Device " + i, "Device description @Location " + i);

            /*
            CardHeader header = new CardHeader(rootView.getContext());
            */

            header.setTitle("Header Title " + i);
            header.setButtonExpandVisible(true);
            card.addCardHeader(header);

            DeviceCardExpand expand = new DeviceCardExpand(rootView.getContext(),
                    "12:31:06", "08:56:21", 25.1 + " kw", 17.6 + " kw ", 75 + "%");
            card.addCardExpand(expand);

            card.setShadow(true);

            mCardsArrayList.add(card);
        }

        CardArrayAdapter mCardArrayAdapter = new CardArrayAdapter(rootView.getContext() ,mCardsArrayList);
        CardListView listView = (CardListView) rootView.findViewById(R.id.myList);
        if (listView!=null){
            listView.setAdapter(mCardArrayAdapter);
        }


        Card card = new DeviceCard(rootView.getContext(),
                "12340",
                "0",
                false);


        DeviceCardHeader header = new DeviceCardHeader(rootView.getContext(), "Device 0", "Device description @Location 0");
        header.setButtonExpandVisible(true);
        card.addCardHeader(header);

        DeviceCardExpand expand = new DeviceCardExpand(rootView.getContext(),
                "22:31:06", "18:56:21", 28.1 + " kw", 19.6 + " kw ", 85 + "%");
        expand.setTitle("Expand Title");
        card.addCardExpand(expand);

        card.setShadow(true);

        mCardsArrayList.set(0, card);





        //mCardArrayAdapter.notifyDataSetChanged();
    }


}
