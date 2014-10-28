package tudelft.mdp.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;
import tudelft.mdp.R;

/**
 * Created by t7 on 24-10-14.
 */
public class MessengerCard extends Card {

    private Context context;
    private Button mButton;
    private EditText mEditText;

    public MessengerCard(Context context) {
        super(context, R.layout.card_messenger);
        this.context = context;
        init();
    }

    public MessengerCard(Context context, int innerLayout) {
        super(context, innerLayout);
        this.context = context;
    }

    private void init(){
        CardHeader header = new LocatorHeader(context, "Broadcast Message");
        this.addCardHeader(header);
        this.setShadow(true);
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {

        mEditText = (EditText) parent.findViewById(R.id.msgText);
        mButton = (Button) parent.findViewById(R.id.msgButton);
    }

}
