package tudelft.mdp.locationTracker;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.view.CardView;
import tudelft.mdp.MdpWorkerService;
import tudelft.mdp.R;
import tudelft.mdp.backend.endpoints.radioMapFingerprintEndpoint.model.ApGaussianRecord;
import tudelft.mdp.ui.CalibrationControlCard;
import tudelft.mdp.ui.ExpandableListAdapterRSSI;
import tudelft.mdp.ui.LocatorBayessianCard;
import tudelft.mdp.ui.LocatorNewScanCard;
import tudelft.mdp.ui.LocatorScanResultCard;

public class LocatorFragment extends Fragment implements ServiceConnection {

    private Messenger mServiceMessenger = null;
    private boolean mIsBound;
    private final Messenger mMessenger = new Messenger(new IncomingMessageHandler());
    private ServiceConnection mConnection = this;

    private View rootView;

    private CardView mCardViewNewScan;
    private CardView mCardViewBayessian;
    private CardView mCardViewScanResult;
    private LocatorScanResultCard mCardScanResult;
    private LocatorNewScanCard mCardNewScan;
    private LocatorBayessianCard mCardBayessian;

    private Button mButtonRequestNewNetworkScan;
    private ProgressBar mProgressBar;

    // Bayessian Card
    private Button mButtonCalculateLocationUsingNextNetwork_Bayessian;
    private Button mButtonCalculateLocation_Bayessian;

    private TextView mTextViewCurrentPlace;
    private TextView mTextViewZone;
    private TextView mTextViewProbability;
    private TextView mTextViewNetwork;
    private TextView mTextViewNetworkId;
    private LinearLayout mLinearLayoutBayessian;


    private LocationEstimator mLocationEstimator = new LocationEstimator();
    private ArrayList<ArrayList<NetworkInfoObject>> mNetworkScans = new ArrayList<ArrayList<NetworkInfoObject>>();
    private ArrayList<ApGaussianRecord> mGaussianRecords = new ArrayList<ApGaussianRecord>();


    private HashMap<String,Double> pmfFinalBayessian = new HashMap<String, Double>();
    private ArrayList<HashMap<String,Double>> pmfIntermediateResultsBayessian = new ArrayList<HashMap<String, Double>>();
    private int networkIdCount;
    private String currentPlace = "TBD";


    private ExpandableListView mExpandableListAP;
    private ArrayList<NetworkInfoObject> groupItem = new ArrayList<NetworkInfoObject>();
    private ArrayList<Object> childItem = new ArrayList<Object>();

    private static final String TAG = "MDP-LocatorFragment";

    public LocatorFragment() {
        // Required empty public constructor
    }


    // Lifecycle methods ***************************************************************************

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView =   inflater.inflate(R.layout.fragment_locator, container, false);


        configureCardsInit();
        automaticBinding();
        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (mIsBound) {
                automaticUnbinding();
            }
        } catch (Throwable t) {
            Log.e(TAG, "Failed to unbind from the service", t);
        }
    }


    // Configure Cards *****************************************************************************

    private void configureCardsInit(){
        configureNewScanCard();
        configureBayessianCard();
        configureScanResultsCard();
    }

    private void configureNewScanCard(){
        mCardViewNewScan = (CardView) rootView.findViewById(R.id.cardNewScan);
        mCardNewScan = new LocatorNewScanCard(rootView.getContext());
        mCardNewScan.setShadow(true);
        mCardViewNewScan.setCard(mCardNewScan);


        mProgressBar = (ProgressBar) mCardViewNewScan.findViewById(R.id.progressBar);
        mButtonRequestNewNetworkScan = (Button) mCardViewNewScan.findViewById(R.id.btnNewScan);


        mButtonRequestNewNetworkScan.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(rootView.getContext(), "Requesting new scan...", Toast.LENGTH_SHORT).show();
                sendMessageToService(MdpWorkerService.MSG_LOCATION_STEP_BY_STEP);
                mProgressBar.setIndeterminate(true);
            }
        });
    }

    private void configureScanResultsCard(){

        mCardViewScanResult = (CardView) rootView.findViewById(R.id.cardScanResults);
        mCardScanResult = new LocatorScanResultCard(rootView.getContext());
        mCardScanResult.setShadow(true);
        mCardViewScanResult.setCard(mCardScanResult);

        mExpandableListAP = (ExpandableListView) mCardViewScanResult.findViewById(R.id.ap_exp);
        //mExpandableListAP = (ExpandableListView) rootView.findViewById(R.id.ap_exp1);

        updateScanResultsList(mockLocationEstimator());
        mCardViewScanResult.setVisibility(View.INVISIBLE);
    }


    private void configureBayessianCard(){
        mCardBayessian = new LocatorBayessianCard(rootView.getContext());
        mCardBayessian.init();
        mCardBayessian.setShadow(true);


        mCardViewBayessian = (CardView) rootView.findViewById(R.id.cardBayessian);
        mCardViewBayessian.setCard(mCardBayessian);


        mButtonCalculateLocationUsingNextNetwork_Bayessian = (Button) mCardViewBayessian.findViewById(R.id.btnNextAP);
        mButtonCalculateLocation_Bayessian = (Button) mCardViewBayessian.findViewById(R.id.btnFinalResultBayessian);
        mButtonCalculateLocationUsingNextNetwork_Bayessian.setEnabled(false);
        mButtonCalculateLocation_Bayessian.setEnabled(false);

        mTextViewCurrentPlace = (TextView) mCardViewBayessian.findViewById(R.id.twPlace);
        mTextViewZone = (TextView) mCardViewBayessian.findViewById(R.id.twZone);
        mTextViewProbability = (TextView) mCardViewBayessian.findViewById(R.id.twProb);
        mTextViewNetwork = (TextView) mCardViewBayessian.findViewById(R.id.twNetworkName);
        mTextViewNetworkId = (TextView) mCardViewBayessian.findViewById(R.id.twNetworkNumber);
        mLinearLayoutBayessian = (LinearLayout) mCardViewBayessian.findViewById(R.id.resultBayessian);


        mButtonCalculateLocationUsingNextNetwork_Bayessian.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               // Toast.makeText(rootView.getContext(), "Calculating location...", Toast.LENGTH_SHORT).show();
                calculateNextAPBayessian();
            }
        });

        mButtonCalculateLocation_Bayessian.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
              //  Toast.makeText(rootView.getContext(), "Calculating final location...", Toast.LENGTH_SHORT).show();
                calculateFinalLocationBayessian();
            }
        });

        refreshBayessianCard(new HashMap<String, Double>(), "TBD", 0);

        mCardViewBayessian.setVisibility(View.INVISIBLE);

    }

    private void refreshBayessianCard( HashMap<String, Double> updatedResults, String currentPlace, int index){

        String networkId;
        String networkName;
        if ( index < mLocationEstimator.getNetworkScans().size() && index >= 0){
            networkId = "[" + (index + 1) + "/"+ mLocationEstimator.getNetworkScans().size() +"]";
            networkName = mLocationEstimator.getNetworkScans().get(index).getSSID();
        } else {
            networkId = "[-]";
            networkName = "Undetermined";
        }

        String zone = "Unknown";
        Double max = 0.0;
        HashMap<String, Double> sortedPMF = new HashMap<String, Double>();
        if (updatedResults != null){
            if (updatedResults.size() > 0) {
                //mLinearLayoutBayessian.setVisibility(View.VISIBLE);
                sortedPMF = LocationEstimator.sortByProbability(updatedResults);
                zone = (String) sortedPMF.keySet().toArray()[0];
                max = sortedPMF.get(zone);
            } else {
                //mLinearLayoutBayessian.setVisibility(View.INVISIBLE);
            }
        }

        mTextViewCurrentPlace.setText(currentPlace);
        mTextViewZone.setText(zone);
        mTextViewProbability.setText(String.format("%.2f",max * 100) + "%");
        mTextViewNetworkId.setText(networkId);
        mTextViewNetwork.setText(networkName);

        mCardBayessian.setCurrentPlace(currentPlace);
        mCardBayessian.updateItems(sortedPMF);
    }

    private HashMap<String, Double> mockZoneInfo (){
        HashMap<String, Double> mockPMF = new HashMap<String, Double>();
        mockPMF.put("Room A", 0.15);
        mockPMF.put("Room B", 0.7532);
        mockPMF.put("Room C", 0.05);
        mockPMF.put("Room D", 0.03);
        mockPMF.put("Room E", 0.02);

        return mockPMF;
    }

    private LocationEstimator mockLocationEstimator(){
        LocationEstimator locationEstimator = new LocationEstimator();

        ArrayList<ArrayList<NetworkInfoObject>> mNetworkScansRawMock = new ArrayList<ArrayList<NetworkInfoObject>>();
        ArrayList<ApGaussianRecord> mGaussianRecordsMock = new ArrayList<ApGaussianRecord>();

        NetworkInfoObject network1 = new NetworkInfoObject("OO:AA::BB:CC:DD", "Network 1", -75.00);
        NetworkInfoObject network2 = new NetworkInfoObject("O1:AA::BB:CC:DD", "Network 2", -85.00);
        NetworkInfoObject network3 = new NetworkInfoObject("O2:AA::BB:CC:DD", "Network 3", -95.00);
        NetworkInfoObject network4 = new NetworkInfoObject("O3:AA::BB:CC:DD", "Network 4", -65.00);
        NetworkInfoObject network5 = new NetworkInfoObject("O4:AA::BB:CC:DD", "Network 5", -55.00);
        ArrayList<NetworkInfoObject> networkInfoObjects = new ArrayList<NetworkInfoObject>();

        NetworkInfoObject network1b = new NetworkInfoObject("OO:AA::BB:CC:DD", "Network 1", -77.00);
        NetworkInfoObject network2b = new NetworkInfoObject("O1:AA::BB:CC:DD", "Network 2", -87.00);
        NetworkInfoObject network3b = new NetworkInfoObject("O2:AA::BB:CC:DD", "Network 3", -97.00);
        NetworkInfoObject network4b = new NetworkInfoObject("O3:AA::BB:CC:DD", "Network 4", -67.00);
        ArrayList<NetworkInfoObject> networkInfoObjectsB = new ArrayList<NetworkInfoObject>();

        networkInfoObjects.add(network1);
        networkInfoObjects.add(network2);
        networkInfoObjects.add(network3);
        networkInfoObjects.add(network4);
        networkInfoObjects.add(network5);
        mNetworkScansRawMock.add(networkInfoObjects);

        networkInfoObjectsB.add(network1b);
        networkInfoObjectsB.add(network2b);
        networkInfoObjectsB.add(network3b);
        networkInfoObjectsB.add(network4b);
        mNetworkScansRawMock.add(networkInfoObjectsB);

        ApGaussianRecord apGaussianRecord1a = createMockApGaussianRecord("OO:AA::BB:CC:DD", "Network 1", -75.00, 1.00, "Home", "Kitchen");
        ApGaussianRecord apGaussianRecord2a = createMockApGaussianRecord("O0:AA::BB:CC:DD", "Network 1", -85.00, 1.00, "Home", "Room A");
        ApGaussianRecord apGaussianRecord3a = createMockApGaussianRecord("O0:AA::BB:CC:DD", "Network 1", -95.00, 1.00, "Home", "Room B");
        ApGaussianRecord apGaussianRecord1b = createMockApGaussianRecord("O1:AA::BB:CC:DD", "Network 2", -75.00, 1.00, "Home", "Kitchen");
        ApGaussianRecord apGaussianRecord2b = createMockApGaussianRecord("O1:AA::BB:CC:DD", "Network 2", -85.00, 1.00, "Home", "Room A");
        ApGaussianRecord apGaussianRecord3b = createMockApGaussianRecord("O1:AA::BB:CC:DD", "Network 2", -95.00, 1.00, "Home", "Room B");
        ApGaussianRecord apGaussianRecord1c = createMockApGaussianRecord("O1:AA::BB:CC:DD", "Network 2", -75.00, 1.00, "Home", "Kitchen");
        ApGaussianRecord apGaussianRecord2c = createMockApGaussianRecord("O2:AA::BB:CC:DD", "Network 3", -85.00, 1.00, "Home", "Room A");
        ApGaussianRecord apGaussianRecord3c = createMockApGaussianRecord("O2:AA::BB:CC:DD", "Network 3", -95.00, 1.00, "Home", "Room B");
        ApGaussianRecord apGaussianRecord1d = createMockApGaussianRecord("O2:AA::BB:CC:DD", "Network 3", -75.00, 1.00, "Home", "Kitchen");
        ApGaussianRecord apGaussianRecord2d = createMockApGaussianRecord("O5:AA::BB:CC:DD", "Network 6", -85.00, 1.00, "Home", "Room A");
        ApGaussianRecord apGaussianRecord3d = createMockApGaussianRecord("O5:AA::BB:CC:DD", "Network 6", -95.00, 1.00, "Home", "Room B");
        ApGaussianRecord apGaussianRecord1e = createMockApGaussianRecord("O4:AA::BB:CC:DD", "Network 5", -59.00, 1.00, "Home", "Kitchen");
        ApGaussianRecord apGaussianRecord2e = createMockApGaussianRecord("O4:AA::BB:CC:DD", "Network 5", -85.00, 1.00, "Home", "Room A");
        ApGaussianRecord apGaussianRecord3e = createMockApGaussianRecord("O4:AA::BB:CC:DD", "Network 5", -95.00, 1.00, "Home", "Room B");
        mGaussianRecordsMock.add(apGaussianRecord1a);
        mGaussianRecordsMock.add(apGaussianRecord2a);
        mGaussianRecordsMock.add(apGaussianRecord3a);
        mGaussianRecordsMock.add(apGaussianRecord1b);
        mGaussianRecordsMock.add(apGaussianRecord2b);
        mGaussianRecordsMock.add(apGaussianRecord3b);
        mGaussianRecordsMock.add(apGaussianRecord1c);
        mGaussianRecordsMock.add(apGaussianRecord2c);
        mGaussianRecordsMock.add(apGaussianRecord3c);
        mGaussianRecordsMock.add(apGaussianRecord1d);
        mGaussianRecordsMock.add(apGaussianRecord2d);
        mGaussianRecordsMock.add(apGaussianRecord3d);
        mGaussianRecordsMock.add(apGaussianRecord1e);
        mGaussianRecordsMock.add(apGaussianRecord2e);
        mGaussianRecordsMock.add(apGaussianRecord3e);

        locationEstimator.setNetworkScansRaw(mNetworkScansRawMock);
        locationEstimator.setGaussianRecords(mGaussianRecordsMock);

        locationEstimator.consolidateNetworkScans();
        locationEstimator.sortNetworksByRSSI();
        locationEstimator.ignoreUnknownNetworks();

        return locationEstimator;
    }

    private ApGaussianRecord createMockApGaussianRecord(String SSID, String BSSID, Double mean, Double std, String place, String zone){
        ApGaussianRecord apGaussianRecord = new ApGaussianRecord();
        apGaussianRecord.setZone(zone);
        apGaussianRecord.setPlace(place);
        apGaussianRecord.setStd(std);
        apGaussianRecord.setMean(mean);
        apGaussianRecord.setBssid(BSSID);
        apGaussianRecord.setSsid(SSID);
        return apGaussianRecord;
    }


    // Receive data from Background Service ********************************************************

    private void handleGaussianResult(ArrayList<ApGaussianRecord> gaussianRecords){
        mGaussianRecords = new ArrayList<ApGaussianRecord>(gaussianRecords);
        Toast.makeText(rootView.getContext(), "Gaussians received.", Toast.LENGTH_SHORT).show();
    }

    private void handleScanResult(ArrayList<ArrayList<NetworkInfoObject>> networkScans) {
        Toast.makeText(rootView.getContext(),"Scan Results ready",Toast.LENGTH_SHORT).show();
        initBayessianLocation(networkScans);

        updateScanResultsList(mLocationEstimator);
        mCardViewScanResult.setVisibility(View.VISIBLE);
    }


    private void updateScanResultsList(LocationEstimator locationEstimator){
        setExpListScanResultData(locationEstimator);

        mExpandableListAP.setAdapter(new ExpandableListAdapterRSSI(rootView.getContext(), groupItem, childItem));
        mExpandableListAP.setOnGroupClickListener(new ExpDrawerGroupClickListener());
    }

    private class ExpDrawerGroupClickListener implements ExpandableListView.OnGroupClickListener {
        @Override
        public boolean onGroupClick(ExpandableListView parent, View v,
                int groupPosition, long id) {

            if (parent.isGroupExpanded(groupPosition)){
                parent.collapseGroup(groupPosition);
            }else {
                parent.expandGroup(groupPosition, true);
            }
            return true;
        }
    }

    private void setExpListScanResultData(LocationEstimator locationEstimator){
        groupItem.clear();
        childItem.clear();

        for (NetworkInfoObject networkInfoObject : locationEstimator.getNetworkScans()){
            groupItem.add(networkInfoObject);
            ArrayList<ApGaussianRecord> child = locationEstimator.getGaussianRecordsOfNetwork(networkInfoObject);
            //Sorting according to Zone name
            Collections.sort(child, new Comparator<ApGaussianRecord>() {
                @Override
                public int compare(ApGaussianRecord item1, ApGaussianRecord item2) {

                    return item1.getZone().compareTo(item2.getZone());
                }
            });

            childItem.add(child);
        }
    }





    // Bayessian calculations **********************************************************************

    private void initBayessianLocation(ArrayList<ArrayList<NetworkInfoObject>> networkScans){
        mNetworkScans = new ArrayList<ArrayList<NetworkInfoObject>>(networkScans);
        mLocationEstimator = new LocationEstimator(mNetworkScans, mGaussianRecords);
        //mLocationEstimator = mockLocationEstimator();
        pmfFinalBayessian = mLocationEstimator.calculateLocationBayessian();
        pmfIntermediateResultsBayessian = mLocationEstimator.calculateLocationBayessian_IntermediatePMFs();
        currentPlace = mLocationEstimator.determineCurrentPlace();
        networkIdCount = 0;

        mProgressBar.setIndeterminate(false);

        mCardViewBayessian.setVisibility(View.VISIBLE);
        mButtonCalculateLocationUsingNextNetwork_Bayessian.setEnabled(true);
        mButtonCalculateLocation_Bayessian.setEnabled(true);
    }

    private void calculateNextAPBayessian(){
        if (currentPlace == null){
            refreshBayessianCard(pmfFinalBayessian, "Unknown" , -1);
        } else {
            refreshBayessianCard(pmfIntermediateResultsBayessian.get(networkIdCount), currentPlace, networkIdCount);
            if (networkIdCount < (mLocationEstimator.getNetworkScans().size()-1)){
                networkIdCount++;
            } else {
                networkIdCount = 0;
            }

        }
    }

    private void calculateFinalLocationBayessian(){
        if (currentPlace == null){
            refreshBayessianCard(pmfFinalBayessian, "Unknown" , -1);
        } else {
            int index  = -1;
            if (pmfFinalBayessian != null) {
                index = findNetworkWithBestResult();
            }
            refreshBayessianCard(pmfFinalBayessian, currentPlace, index);
        }
    }

    private int findNetworkWithBestResult(){
        String zone = "Unknown";
        Double max = 0.0;
        if (pmfFinalBayessian.size() > 0) {
            zone = (String) pmfFinalBayessian.keySet().toArray()[0];
            max = pmfFinalBayessian.get(zone);
        }

        int index = -1;
        for (int i = 0; i < pmfIntermediateResultsBayessian.size(); i++){
            Double maxProb = pmfIntermediateResultsBayessian.get(i).get(zone);
            if (max.equals(maxProb)){
                return i;
            }
        }
        return index;
    }









    // Service connection methods ******************************************************************
    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.i(TAG, "Sensor Service: onServiceDisconnected");
        if (mServiceMessenger != null) {
            mServiceMessenger = null;
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.i(TAG, "Sensor Service: onServiceConnected");
        mServiceMessenger = new Messenger(service);
        try {
            Message msg = Message.obtain(null, MdpWorkerService.MSG_REGISTER_CLIENT);
            msg.replyTo = mMessenger;
            mServiceMessenger.send(msg);
        }
        catch (RemoteException e) {
            // In this case the service has crashed before we could even do anything with it
        }

        sendMessageToService(MdpWorkerService.MSG_LOCATION_GAUSSIANS);
    }

    private void automaticBinding() {
        if (MdpWorkerService.isRunning()){
            doBindService();
        } else{
            startServiceNetworkScan();
            doBindService();
        }
    }

    private void automaticUnbinding() {
        stopServiceNetworkScan();
    }

    public void startServiceNetworkScan(){
        Log.i(TAG, "MdpWorkerService : START");
        Intent intent = new Intent(this.getActivity(), MdpWorkerService.class);
        this.getActivity().startService(intent);
    }

    public void stopServiceNetworkScan(){
        Log.i(TAG, "MdpWorkerService : Unbind");
        doUnbindService();
    }

    private void doBindService() {
        this.getActivity().bindService(new Intent(this.getActivity(), MdpWorkerService.class),
                mConnection, 0);
        mIsBound = true;
    }

    private void doUnbindService() {
        if (mIsBound) {

            // If we have received the service, and hence registered with it, then now is the time to unregister.
            if (mServiceMessenger != null) {
                try {
                    Message msg = Message.obtain(null, MdpWorkerService.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mServiceMessenger.send(msg);
                } catch (RemoteException e) {
                    // There is nothing special we need to do if the service has crashed.
                }
            }
            // Detach our existing connection.
            this.getActivity().unbindService(mConnection);
            mIsBound = false;
        }
    }

    private void sendMessageToService(int command) {
        if (mIsBound) {
            if (mServiceMessenger != null) {
                try {
                    Message msg = Message
                            .obtain(null, command);
                    msg.replyTo = mMessenger;
                    mServiceMessenger.send(msg);
                } catch (RemoteException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }
    }

    private class IncomingMessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "IncomingHandler:handleMessage");
            switch (msg.what) {
                case MdpWorkerService.MSG_LOCATION_STEP_BY_STEP:
                    @SuppressWarnings("unchecked")
                    ArrayList<ArrayList<NetworkInfoObject>> scanResult =
                            (ArrayList<ArrayList<NetworkInfoObject>> ) msg.getData()
                                    .getSerializable(MdpWorkerService.ARG_LOCATION_STEP_BY_STEP);

                    handleScanResult(scanResult);

                    break;
                case MdpWorkerService.MSG_LOCATION_GAUSSIANS:
                    @SuppressWarnings("unchecked")
                    ArrayList<ApGaussianRecord> gaussianRecords =
                            (ArrayList<ApGaussianRecord> ) msg.getData()
                                    .getSerializable(MdpWorkerService.ARG_LOCATION_GAUSSIANS);

                    handleGaussianResult(gaussianRecords);

                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

}
