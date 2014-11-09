package tudelft.mdp.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;
import tudelft.mdp.R;

/**
 * Created by t7 on 9-11-14.
 */
public class DashboardRankingsCard extends Card {

    private String headerTitle;
    private ExpandableListView mExpandableListAP;

    /**
     * Constructor with a custom inner layout
     * @param context
     */
    public DashboardRankingsCard(Context context, String header) {
        this(context, R.layout.expandable_list_rankings);
        this.headerTitle = header;
        init();
    }

    /**
     *
     * @param context
     * @param innerLayout
     */
    public DashboardRankingsCard(Context context, int innerLayout) {
        super(context, innerLayout);
        init();
    }

    private void init(){
        CardHeader header = new
                LocatorHeader(getContext(), this.headerTitle);

        this.addCardHeader(header);
        this.setShadow(true);

    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        //Retrieve elements
        mExpandableListAP = (ExpandableListView) parent.findViewById(R.id.exp_rankings);


    }


}