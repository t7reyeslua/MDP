package tudelft.mdp.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;
import tudelft.mdp.R;

public class DashboardLocationCard extends Card {

    private Context context;

    public DashboardLocationCard(Context context) {
        super(context, R.layout.card_dashboard_current_place);
        this.context = context;
        init();
    }

    public DashboardLocationCard(Context context, int innerLayout) {
        super(context, innerLayout);
        this.context = context;
    }

    private void init(){
        CardHeader header = new LocatorHeader(context, "Current Location");
        this.addCardHeader(header);
        this.setShadow(true);
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {

    }

}
