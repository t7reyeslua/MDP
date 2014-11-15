package tudelft.mdp.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;
import tudelft.mdp.R;

public class GetArffCard extends Card {

    private Context context;
    private Button mButton;

    public GetArffCard(Context context) {
        super(context, R.layout.card_get_arff);
        this.context = context;
        init();
    }

    public GetArffCard(Context context, int innerLayout) {
        super(context, innerLayout);
        this.context = context;
    }

    private void init(){
        CardHeader header = new LocatorHeader(context, "Request Weka Arff");
        this.addCardHeader(header);
        this.setShadow(true);
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        mButton = (Button) parent.findViewById(R.id.btnGetArff);
    }

}