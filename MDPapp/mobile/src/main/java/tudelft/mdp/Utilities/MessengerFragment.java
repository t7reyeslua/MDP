package tudelft.mdp.Utilities;



import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.view.CardView;
import tudelft.mdp.R;
import tudelft.mdp.enums.MessagesProtocol;
import tudelft.mdp.gcm.GcmMessagingAsyncTask;
import tudelft.mdp.ui.MessengerCard;

public class MessengerFragment extends Fragment {
    private CardView mCardView;
    private Card mCard;

    private View rootView;
    private EditText mEditText;
    private Button mButton;

    public MessengerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView  = inflater.inflate(R.layout.fragment_messenger, container, false);

        configureCard();

        return rootView;
    }


    private void configureCard(){
        mCardView = (CardView) rootView.findViewById(R.id.cardMessenger);

        mCard = new MessengerCard(rootView.getContext());
        mCardView.setCard(mCard);

        mEditText = (EditText) mCardView.findViewById(R.id.msgText);
        mButton = (Button) mCardView.findViewById(R.id.msgButton);

        mButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendMessage();
            }
        });

    }

    private void sendMessage(){
        new GcmMessagingAsyncTask().execute(String.valueOf(MessagesProtocol.SENDGCM_MSG),
                mEditText.getText().toString(),
                rootView.getContext());
    }




}
