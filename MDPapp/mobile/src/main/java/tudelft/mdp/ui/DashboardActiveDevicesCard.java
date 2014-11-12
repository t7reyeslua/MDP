package tudelft.mdp.ui;

import com.devspark.robototextview.widget.RobotoTextView;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.prototypes.CardWithList;
import tudelft.mdp.R;
import tudelft.mdp.backend.endpoints.deviceLogEndpoint.model.NfcRecord;

/**
 * Created by t7 on 12-11-14.
 */
public class DashboardActiveDevicesCard extends CardWithList {
    public ArrayList<NfcRecord> activeDevices;

    private static final String TAG = "MDP-DashboardActiveDevicesCard";

    public DashboardActiveDevicesCard(Context context){
        super(context, R.layout.card_dashboard_active_devices);
        this.activeDevices = new ArrayList<NfcRecord>();
    }

    public DashboardActiveDevicesCard(Context context, ArrayList<NfcRecord> activeDevices){
        super(context, R.layout.card_dashboard_active_devices);
        this.activeDevices = new ArrayList<NfcRecord>(activeDevices);
    }

    @Override
    public int getChildLayoutId() {
        return R.layout.card_dashboard_active_devices_inner_item;
    }


    @Override
    public View setupChildView(int childPosition, CardWithList.ListObject object, View convertView, ViewGroup parent) {


        RobotoTextView twType = (RobotoTextView) convertView.findViewById(R.id.twType);
        RobotoTextView twPlace = (RobotoTextView) convertView.findViewById(R.id.twPlace);
        RobotoTextView twZone = (RobotoTextView) convertView.findViewById(R.id.twZone);

        ActiveDevices activeDeviceObject = (ActiveDevices) object;

        twType.setText(activeDeviceObject.getType());
        twPlace.setText(activeDeviceObject.getPlace());
        twZone.setText(activeDeviceObject.getZone());

        return  convertView;
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        //It is very important call the super method!!
        super.setupInnerViewElements(parent, view);
        //Your elements
    }

    @Override
    protected CardHeader initCardHeader() {
        //Add Header
        CardHeader header = new
                CalibrationNetworksHeader(getContext(), "Active Devices");
        //header.setTitle("Networks"); //should use R.string.
        return header;
    }

    @Override
    protected void initCard() {
        /*CardExpand expand = new CalibrationNetworksExpand(getContext());
        this.addCardExpand(expand);
        this.setExpanded(true);*/

        setEmptyViewViewStubLayoutId(R.layout.card_empty_view);
    }

    @Override
    protected List<CardWithList.ListObject> initChildren() {

        List<CardWithList.ListObject> mObjects = new ArrayList<CardWithList.ListObject>();

        for (NfcRecord device : activeDevices){

            ActiveDevices anActiveDevice = new ActiveDevices(this);
            anActiveDevice.setType(device.getType());
            anActiveDevice.setPlace(device.getPlace());
            anActiveDevice.setZone(device.getLocation());
            mObjects.add(anActiveDevice);
        }

        return mObjects;
    }

    public void updateItems(ArrayList<NfcRecord> myList) {
        initCardHeader();

        activeDevices.clear();
        activeDevices = new ArrayList<NfcRecord>(myList);

        ArrayList<ActiveDevices> objs = new ArrayList<ActiveDevices>();
        for (NfcRecord device : myList){
            ActiveDevices anActiveDevice = new ActiveDevices(this);
            anActiveDevice.setType(device.getType());
            anActiveDevice.setPlace(device.getPlace().toLowerCase());
            anActiveDevice.setZone(device.getLocation().toLowerCase());
            objs.add(anActiveDevice);
        }

        getLinearListAdapter().clear();
        getLinearListAdapter().addAll(objs);
    }

    public ArrayList<NfcRecord> getActiveDevices() {
        return activeDevices;
    }



    public void setActiveDevices(ArrayList<NfcRecord> activeDevices) {
        this.activeDevices = activeDevices;
    }

    public class ActiveDevices extends CardWithList.DefaultListObject {

        public String type;
        public String place;
        public String zone;

        public ActiveDevices(Card parentCard){
            super(parentCard);
            init();
        }

        public ActiveDevices(Card parentCard, String type, String place, String zone) {
            super(parentCard);
            this.type = type;
            this.place = place;
            this.zone = zone;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
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

        private void init(){

        }

    }

}