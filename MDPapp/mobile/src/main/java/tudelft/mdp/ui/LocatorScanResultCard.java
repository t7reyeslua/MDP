package tudelft.mdp.ui;


import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;
import tudelft.mdp.R;


public class LocatorScanResultCard extends Card {



    private ExpandableListView mExpandableListAP;

    /**
     * Constructor with a custom inner layout
     * @param context
     */
    public LocatorScanResultCard(Context context) {
        this(context, R.layout.expandable_list_ap);
    }

    /**
     *
     * @param context
     * @param innerLayout
     */
    public LocatorScanResultCard(Context context, int innerLayout) {
        super(context, innerLayout);
        init();
    }

    private void init(){
        CardHeader header = new
                LocatorHeader(getContext(), "Scan Results");

        this.addCardHeader(header);
        this.setShadow(true);

    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        //Retrieve elements
        mExpandableListAP = (ExpandableListView) parent.findViewById(R.id.ap_exp);


    }


}
