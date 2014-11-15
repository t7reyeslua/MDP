package tudelft.mdp.Utilities;



import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.view.CardView;
import tudelft.mdp.R;
import tudelft.mdp.ui.GetArffCard;
import tudelft.mdp.weka.RequestWekaInstanceAsyncTask;

public class DatabaseManagerFragment extends Fragment {

    private static final String LOGTAG = "MDP-DatabaseManagerFragment";
    private Boolean requestInProcess = false;
    private View rootView;


    private CardView mCardView;
    private Card mCard;

    private Button mButtonGetArff;


    public DatabaseManagerFragment() {
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
        rootView =  inflater.inflate(R.layout.fragment_database_manager, container, false);

        configureCard();

        return rootView;
    }


    private void configureCard(){
        mCardView = (CardView) rootView.findViewById(R.id.cardArff);

        mCard = new GetArffCard(rootView.getContext());
        mCardView.setCard(mCard);

        mButtonGetArff = (Button) mCardView.findViewById(R.id.btnGetArff);

        mButtonGetArff.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(rootView.getContext(), "Generating Arff. Be patient, this could take some time...", Toast.LENGTH_SHORT).show();
                getArff();
            }
        });

    }


    private void getArff(){
        new RequestWekaInstanceAsyncTask().execute(rootView.getContext(), "WekaMotionLocation");

    }
}
