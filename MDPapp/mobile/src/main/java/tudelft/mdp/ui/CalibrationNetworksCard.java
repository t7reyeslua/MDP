package tudelft.mdp.ui;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardExpand;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.prototypes.CardWithList;
import it.gmariotti.cardslib.library.prototypes.LinearListView;
import tudelft.mdp.R;
import tudelft.mdp.locationTracker.CalibrationNetworkObject;

/**
 * Created by t7 on 13-10-14.
 */
public class CalibrationNetworksCard extends CardWithList {
    public ArrayList<CalibrationNetworkObject> networks;

    private Button mButton;


    private static final String TAG = "MDP-CalibrationNetworksCard";

    public CalibrationNetworksCard(Context context){
        super(context, R.layout.card_calibration_networks);
        this.networks = new ArrayList<CalibrationNetworkObject>();
    }

    public CalibrationNetworksCard(Context context, ArrayList<CalibrationNetworkObject> networks){
        super(context, R.layout.card_calibration_networks);
        this.networks = new ArrayList<CalibrationNetworkObject>(networks);
    }

    @Override
    public int getChildLayoutId() {
        return R.layout.card_calibration_networks_inner_item;
    }


    @Override
    public View setupChildView(int childPosition, ListObject object, View convertView, ViewGroup parent) {


        TextView twSsid = (TextView) convertView.findViewById(R.id.twSSID);
        TextView twBssid = (TextView) convertView.findViewById(R.id.twBSSID);
        TextView twMean = (TextView) convertView.findViewById(R.id.twMean);
        TextView twCount = (TextView) convertView.findViewById(R.id.twCount);

        CalibrationNetworks networkObject = (CalibrationNetworks) object;
        twSsid.setText(networkObject.getSSID());
        twBssid.setText(networkObject.getBSSID());
        twMean.setText(networkObject.getMean().toString() + " db");
        twCount.setText(networkObject.getCount().toString() + " samples");

        return  convertView;
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        //It is very important call the super method!!
        super.setupInnerViewElements(parent, view);


        mButton = (Button) view.findViewById(R.id.buttonRegression);
        //Your elements
    }

    @Override
    protected CardHeader initCardHeader() {
        //Add Header
        CardHeader header = new
                CalibrationNetworksHeader(getContext(), "Scanned Networks");
        //header.setTitle("Networks"); //should use R.string.
        return header;
    }

    @Override
    protected void initCard() {
        CardExpand expand = new CalibrationNetworksExpand(getContext());
        this.addCardExpand(expand);
        this.setExpanded(true);

        setEmptyViewViewStubLayoutId(R.layout.card_empty_view);
    }

    @Override
    protected List<ListObject> initChildren() {

        List<ListObject> mObjects = new ArrayList<ListObject>();

        for (CalibrationNetworkObject singleNetwork : networks){
            CalibrationNetworks network = new CalibrationNetworks(this);
            network.setBSSID(singleNetwork.getBSSID());
            network.setSSID(singleNetwork.getSSID());
            network.setMean(singleNetwork.getMean());
            network.setCount(singleNetwork.getCount());
            mObjects.add(network);
        }

        return mObjects;
    }

    public void updateItems(ArrayList<CalibrationNetworkObject> myList) {
        initCardHeader();

        networks.clear();
        networks = new ArrayList<CalibrationNetworkObject>(myList);

        ArrayList<CalibrationNetworks> objs = new ArrayList<CalibrationNetworks>();
        for (CalibrationNetworkObject singleNetwork : myList){
            CalibrationNetworks network = new CalibrationNetworks(this);
            network.setBSSID(singleNetwork.getBSSID());
            network.setSSID(singleNetwork.getSSID());
            network.setMean(singleNetwork.getMean());
            network.setCount(singleNetwork.getCount());
            objs.add(network);
        }

        getLinearListAdapter().clear();
        getLinearListAdapter().addAll(objs);
    }

    public ArrayList<CalibrationNetworkObject> getNetworks() {
        return networks;
    }

    public void setNetworks(ArrayList<CalibrationNetworkObject> networks) {
        this.networks = networks;
    }

    public class CalibrationNetworks extends CardWithList.DefaultListObject {

        public String SSID;
        public String BSSID;
        public Float mean;
        public Integer count;

        public CalibrationNetworks(Card parentCard){
            super(parentCard);
            init();
        }

        public CalibrationNetworks(Card parentCard, String SSID, String BSSID, Float mean,
                Integer count) {
            super(parentCard);
            this.SSID = SSID;
            this.BSSID = BSSID;
            this.mean = mean;
            this.count = count;
        }

        public String getSSID() {
            return SSID;
        }

        public void setSSID(String SSID) {
            this.SSID = SSID;
        }

        public String getBSSID() {
            return BSSID;
        }

        public void setBSSID(String BSSID) {
            this.BSSID = BSSID;
        }

        public Float getMean() {
            return mean;
        }

        public void setMean(Float mean) {
            this.mean = mean;
        }

        public Integer getCount() {
            return count;
        }

        public void setCount(Integer count) {
            this.count = count;
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
                    //Toast.makeText(getContext(), "Swipe on " + object.getObjectId(), Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

}
