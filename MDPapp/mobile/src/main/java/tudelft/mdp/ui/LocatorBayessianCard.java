package tudelft.mdp.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardExpand;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.prototypes.CardWithList;
import it.gmariotti.cardslib.library.prototypes.LinearListView;
import tudelft.mdp.R;
import tudelft.mdp.locationTracker.LocationEstimator;
import tudelft.mdp.locationTracker.NetworkInfoObject;

/**
 * Created by t7 on 13-10-14.
 */
public class LocatorBayessianCard extends CardWithList {

    private HashMap<String,Double> pmf = new HashMap<String, Double>();

    private Button mButtonNextAP;
    private Button mButtonFinalResult;

    private String currentPlace = "";

    private static final String TAG = "MDP-LocatorBayessianCard";

    public LocatorBayessianCard(Context context){
        super(context, R.layout.card_locator_bayessian);
        this.pmf = new HashMap<String, Double>();
    }

    public LocatorBayessianCard(Context context, HashMap<String,Double> pmf){
        super(context, R.layout.card_locator_bayessian);
        this.pmf = new HashMap<String, Double>(pmf);
    }

    @Override
    public int getChildLayoutId() {
        return R.layout.card_locator_bayessian_inner_item;
    }


    @Override
    public View setupChildView(int childPosition, ListObject object, View convertView, ViewGroup parent) {
        TextView twPlace = (TextView) convertView.findViewById(R.id.twName1);
        TextView twZone = (TextView) convertView.findViewById(R.id.twName2);
        TextView twPercentage = (TextView) convertView.findViewById(R.id.twNumber1);
        TextView twDummy = (TextView) convertView.findViewById(R.id.twNumber2);
        twDummy.setVisibility(View.GONE);

        ZoneBayessianProbability zoneBayessianProbabilityObject = (ZoneBayessianProbability) object;

        String probability = String.format("%.2f", zoneBayessianProbabilityObject.getPercentage() * 100);
        twPlace.setText(zoneBayessianProbabilityObject.getPlace());
        twZone.setText(zoneBayessianProbabilityObject.getZone());
        twPercentage.setText(probability + "%");

        return  convertView;
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        //It is very important call the super method!!
        super.setupInnerViewElements(parent, view);


        mButtonNextAP = (Button) view.findViewById(R.id.btnNextAP);
        mButtonFinalResult = (Button) view.findViewById(R.id.btnFinalResultBayessian);
        //Your elements
    }

    @Override
    protected CardHeader initCardHeader() {
        //Add Header
        CardHeader header = new
                LocatorHeader(getContext(), "Bayessian");
        //header.setButtonExpandVisible(true);
        //header.setTitle("Networks"); //should use R.string.
        return header;
    }

    @Override
    protected void initCard() {
        setEmptyViewViewStubLayoutId(R.layout.card_empty_view);
    }

    @Override
    protected List<ListObject> initChildren() {

        List<ListObject> mObjects = new ArrayList<ListObject>();

        for (String zoneName : pmf.keySet()){
            ZoneBayessianProbability zoneInfo = new ZoneBayessianProbability(this);
            zoneInfo.setPlace(currentPlace);
            zoneInfo.setZone(zoneName);
            zoneInfo.setPercentage(pmf.get(zoneName));
            mObjects.add(zoneInfo);
        }

        return mObjects;
    }

    public String getCurrentPlace() {
        return currentPlace;
    }

    public void setCurrentPlace(String currentPlace) {
        this.currentPlace = currentPlace;
    }


    public void updateItems(HashMap<String, Double> myList) {
        initCardHeader();

        pmf.clear();

        pmf = LocationEstimator.sortByProbability(myList);

        ArrayList<ZoneBayessianProbability> objs = new ArrayList<ZoneBayessianProbability>();
        for (String zoneName : pmf.keySet()){
            ZoneBayessianProbability zoneInfo = new ZoneBayessianProbability(this);
            zoneInfo.setPlace(currentPlace);
            zoneInfo.setZone(zoneName);
            zoneInfo.setPercentage(pmf.get(zoneName));
            objs.add(zoneInfo);
        }

        getLinearListAdapter().clear();
        getLinearListAdapter().addAll(objs);
    }

    public HashMap<String, Double> getPMF() {
        return pmf;
    }

    public void setPmf(HashMap<String, Double> pmf) {
        this.pmf = pmf;
    }

    public class ZoneBayessianProbability extends CardWithList.DefaultListObject {

        public String place;
        public String zone;
        public Double percentage;

        public ZoneBayessianProbability(Card parentCard){
            super(parentCard);
            init();
        }

        public ZoneBayessianProbability(Card parentCard, String place, String zone, Double percentage) {
            super(parentCard);
            this.place = place;
            this.zone = zone;
            this.percentage = percentage;
        }

        public String getPlace() {
            return place;
        }

        public void setPlace(String place) {
            this.place = place;
        }

        public String getZone() {
            return zone;
        }

        public void setZone(String zone) {
            this.zone = zone;
        }

        public Double getPercentage() {
            return percentage;
        }

        public void setPercentage(Double percentage) {
            this.percentage = percentage;
        }

        private void init(){
            //OnClick Listener
            setOnItemClickListener(new CardWithList.OnItemClickListener() {
                @Override
                public void onItemClick(LinearListView parent, View view, int position, CardWithList.ListObject object) {
                    int i = 0;
                    //Toast.makeText(getContext(), "Click on " + getObjectId(), Toast.LENGTH_SHORT).show();
                }
            });

            //OnItemSwipeListener
            setOnItemSwipeListener(new CardWithList.OnItemSwipeListener() {
                @Override
                public void onItemSwipe(CardWithList.ListObject object, boolean dismissRight) {
                    int i = 0;
                    //ZoneBayessianProbability swipedNetwork = (ZoneBayessianProbability) object;
                    //removeFromNetworksList(swipedNetwork);
                    //Toast.makeText(getContext(), "Swipe on " + object.getObjectId(), Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

}
