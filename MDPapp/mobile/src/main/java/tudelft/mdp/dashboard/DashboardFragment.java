package tudelft.mdp.dashboard;

import com.androidplot.LineRegion;
import com.androidplot.ui.AnchorPosition;
import com.androidplot.ui.SeriesRenderer;
import com.androidplot.ui.SizeLayoutType;
import com.androidplot.ui.SizeMetrics;
import com.androidplot.ui.TextOrientationType;
import com.androidplot.ui.XLayoutStyle;
import com.androidplot.ui.YLayoutStyle;
import com.androidplot.ui.widget.TextLabelWidget;
import com.androidplot.util.PixelUtils;
import com.androidplot.xy.BarFormatter;
import com.androidplot.xy.BarRenderer;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PointLabelFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.XYStepMode;
import com.devspark.robototextview.widget.RobotoTextView;
import com.etsy.android.grid.ExtendableListView;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.view.CardView;
import tudelft.mdp.MainActivity;
import tudelft.mdp.MdpWorkerService;
import tudelft.mdp.R;
import tudelft.mdp.backend.endpoints.deviceLogEndpoint.model.DeviceUsageRecord;
import tudelft.mdp.backend.endpoints.deviceLogEndpoint.model.NfcRecord;
import tudelft.mdp.backend.endpoints.energyConsumptionRecordEndpoint.model.EnergyConsumptionRecord;
import tudelft.mdp.deviceManager.RequestAllUsersStatsAsyncTask;
import tudelft.mdp.deviceManager.RequestUserActiveDevicesAsyncTask;
import tudelft.mdp.deviceManager.RequestUserEnergyConsumptionHistoryAsyncTask;
import tudelft.mdp.enums.Constants;
import tudelft.mdp.enums.Devices;
import tudelft.mdp.enums.UserPreferences;
import tudelft.mdp.ui.DashboardActiveDevicesCard;
import tudelft.mdp.ui.DashboardLocationCard;
import tudelft.mdp.ui.DashboardLogCard;
import tudelft.mdp.ui.DashboardRankingsCard;
import tudelft.mdp.ui.DashboardUserHistoryCard;
import tudelft.mdp.ui.ExpandableListEnergyRanking;
import tudelft.mdp.utils.Utils;


public class DashboardFragment extends Fragment implements
        ServiceConnection,
        RequestUserActiveDevicesAsyncTask.RequestUserActiveDevicesAsyncResponse,
        RequestUserEnergyConsumptionHistoryAsyncTask.RequestUserEnergyConsumptionHistoryAsyncResponse,
        RequestAllUsersStatsAsyncTask.RequestAllUsersStatsAsyncResponse{


    private Messenger mServiceMessenger = null;
    private boolean mIsBound;
    private ServiceConnection mConnection = this;
    private final Messenger mMessenger = new Messenger(new IncomingMessageHandler());

    private static final String LOGTAG = "MDP-DashboardFragment";
    private Boolean requestInProcess = false;
    private View rootView;

    private String user;

    private ArrayList<EnergyConsumptionRecord> userEnergyHistory = new ArrayList<EnergyConsumptionRecord>();
    private ArrayList<DeviceUsageRecord> userStatsRaw = new ArrayList<DeviceUsageRecord>();
    private HashMap<String, HashMap<Integer,ArrayList<DeviceUsageRecord>>> userStatsHM = new HashMap<String, HashMap<Integer, ArrayList<DeviceUsageRecord>>>();
    private HashMap<String, HashMap<Integer,ArrayList<DeviceUsageRecord>>> deviceStatsHM = new HashMap<String, HashMap<Integer, ArrayList<DeviceUsageRecord>>>();
    private HashMap<Integer, ArrayList<DeviceUsageRecord>> usersTotals = new HashMap<Integer, ArrayList<DeviceUsageRecord>>();
    private HashMap<Integer, ArrayList<DeviceUsageRecord>> devicesTotals = new HashMap<Integer, ArrayList<DeviceUsageRecord>>();

    private CardView mCardViewUserRankings;
    private DashboardRankingsCard mCardUserRankings;
    private ExpandableListView mExpandableListUsers;
    private ArrayList<DeviceUsageRecord> groupItemUsers = new ArrayList<DeviceUsageRecord>();
    private ArrayList<Object> childItemUsers = new ArrayList<Object>();
    private RobotoTextView twNoDataUsers;

    private CardView mCardViewDeviceRankings;
    private DashboardRankingsCard mCardDeviceRankings;
    private ExpandableListView mExpandableListDevices;
    private ArrayList<DeviceUsageRecord> groupItemDevices = new ArrayList<DeviceUsageRecord>();
    private ArrayList<Object> childItemDevices = new ArrayList<Object>();
    private RobotoTextView twNoDataDevices;

    private CardView mCardViewLocation;
    private DashboardLocationCard mCardLocation;
    private RobotoTextView twLocationTimestamp;
    private RobotoTextView twLocationPlace;
    private RobotoTextView twLocationZone;

    private CardView mCardViewLog;
    private DashboardLogCard mCardLog;
    private RobotoTextView twLog;


    private CardView mCardViewUserHistory;
    private DashboardUserHistoryCard mCardUserHistory;

    private CardView mCardViewActiveDevices;
    private DashboardActiveDevicesCard mCardActiveDevices;


    private String placeOfLocation = "TBD";
    private String locationCalculated = "TBD";


    private static final String NO_SELECTION_TXT = "Touch bar to select";
    private XYPlot plot;
    private MyBarFormatter formatter1;
    private MyBarFormatter formatter2;
    private MyBarFormatter selectionFormatter;
    private TextLabelWidget selectionWidget;
    private Pair<Integer, XYSeries> selection;

    public DashboardFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView =  inflater.inflate(R.layout.fragment_dashboard, container, false);
        setHasOptionsMenu(true);

        user = PreferenceManager.getDefaultSharedPreferences(rootView.getContext())
                   .getString(UserPreferences.USERNAME, "TBD");
        Log.i(LOGTAG, "Username:" + user);

        configureCardsInit();
        automaticBinding();
        requestUsersStats();


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
            Log.e(LOGTAG, "Failed to unbind from the service", t);
        }
    }

    // Configure Cards ****************************************************************************

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.device_manager_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Log.d(LOGTAG, "Refresh clicked");
        switch (item.getItemId()) {
            case R.id.action_refresh:
                requestUsersStats();
                Toast.makeText(rootView.getContext(),"Refreshing Data...", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void configureCardsInit(){
        configureUserRankingsCard();
        configureDeviceRankingsCard();
        configureLocationCard();
        configureLogCard();
        configureUserHistoryCard();
        configureActiveDevicesCard();
    }

    private void configureActiveDevicesCard(){
        mCardActiveDevices = new DashboardActiveDevicesCard(rootView.getContext());
        mCardActiveDevices.init();
        mCardActiveDevices.setShadow(true);

        mCardViewActiveDevices = (CardView) rootView.findViewById(R.id.cardActiveDevices);
        mCardViewActiveDevices.setCard(mCardActiveDevices);


        ArrayList<NfcRecord> updatedResults = new ArrayList<NfcRecord>();
        NfcRecord noDevice = new NfcRecord();
        noDevice.setType("");
        noDevice.setPlace("Requesting");
        noDevice.setLocation("Data");
        updatedResults.add(noDevice);
        mCardActiveDevices.updateItems(updatedResults);
    }

    private void refreshActiveDevicesCard(ArrayList<NfcRecord> updatedResults){
        if (updatedResults != null) {
           if (updatedResults.size() > 0) {
               mCardActiveDevices.updateItems(updatedResults);
               return;
           }
        }
        updatedResults = new ArrayList<NfcRecord>();
        NfcRecord noDevice = new NfcRecord();
        noDevice.setType("");
        noDevice.setPlace("zero");
        noDevice.setLocation("Active Devices");
        updatedResults.add(noDevice);
        mCardActiveDevices.updateItems(updatedResults);

    }

    private void configureUserHistoryCard(){
        mCardViewUserHistory = (CardView) rootView.findViewById(R.id.cardUserHistory);
        mCardUserHistory = new DashboardUserHistoryCard(rootView.getContext());
        mCardUserHistory.setShadow(true);
        mCardViewUserHistory.setCard(mCardUserHistory);

        setUpPlot();
    }

    private void setUpPlot(){
        // initialize our XYPlot reference:
        plot = (XYPlot) mCardViewUserHistory.findViewById(R.id.mySimpleXYPlot);

        formatter1 = new MyBarFormatter(Color.argb(200, 100, 150, 100), Color.LTGRAY);
        formatter2 = new MyBarFormatter(Color.argb(200, 100, 100, 150), Color.LTGRAY);
        selectionFormatter = new MyBarFormatter(Color.YELLOW, Color.WHITE);


        selectionWidget = new TextLabelWidget(plot.getLayoutManager(), NO_SELECTION_TXT,
                new SizeMetrics(
                        PixelUtils.dpToPix(100), SizeLayoutType.ABSOLUTE,
                        PixelUtils.dpToPix(100), SizeLayoutType.ABSOLUTE),
                TextOrientationType.HORIZONTAL);

        selectionWidget.getLabelPaint().setTextSize(PixelUtils.dpToPix(10));

        // add a dark, semi-transparent background to the selection label widget:
        Paint p = new Paint();
        p.setARGB(100, 0, 0, 0);
        selectionWidget.setBackgroundPaint(p);

        selectionWidget.position(
                0, XLayoutStyle.RELATIVE_TO_CENTER,
                PixelUtils.dpToPix(0), YLayoutStyle.ABSOLUTE_FROM_TOP,
                AnchorPosition.TOP_MIDDLE);
        selectionWidget.pack();


        // reduce the number of range labels
        plot.setTicksPerRangeLabel(2);
        plot.setRangeLowerBoundary(0, BoundaryMode.FIXED);
        plot.getGraphWidget().setGridPadding(30, 10, 30, 0);
        //plot.setTicksPerDomainLabel(5);

        plot.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    onPlotClicked(new PointF(motionEvent.getX(), motionEvent.getY()));
                }
                return true;
            }
        });

        updatePlot(null);

    }

    private void updatePlot(ArrayList<EnergyConsumptionRecord> userEnergyHistory) {

        if (userEnergyHistory == null){
            return;
        }

        ArrayList<String> labels = new ArrayList<String>();
        ArrayList<Double> userEnergy = new ArrayList<Double>();
        ArrayList<Double> groupEnergy = new ArrayList<Double>();

        for (EnergyConsumptionRecord energyConsumptionRecord : userEnergyHistory){
            Log.i("UserEnergyHistory", energyConsumptionRecord.getUserEnergy() + "|"+ energyConsumptionRecord.getGroupEnergy());

            String fullTimestamp = String.valueOf(energyConsumptionRecord.getTimestamp());
            String MM   = fullTimestamp.substring(4,6);
            String dd   = fullTimestamp.substring(6,8);
            labels.add(MM +"-" + dd);

            // TODO: remove this
            if (energyConsumptionRecord.getGroupEnergy() == 22532.0){
                energyConsumptionRecord.setGroupEnergy(75.0);
            }

            userEnergy.add(energyConsumptionRecord.getUserEnergy());
            groupEnergy.add(energyConsumptionRecord.getGroupEnergy());
        }

        // Remove all current series from each plot
        Iterator<XYSeries> iterator1 = plot.getSeriesSet().iterator();
        while(iterator1.hasNext()) {
            XYSeries setElement = iterator1.next();
            plot.removeSeries(setElement);
        }

        // Create a couple arrays of y-values to plot:
        Number[] series1Numbers = {1, 5, 5, 2, 7, 4, 9};
        Number[] series2Numbers = {4, 6, 8, 8, 9, 10, 12};

        // Setup our Series with the selected number of elements
        XYSeries series1 = new SimpleXYSeries(userEnergy, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "  You");
        XYSeries series2 = new SimpleXYSeries(groupEnergy, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "  Everybody");

        // add a new series' to the xyplot:
        plot.addSeries(series1, formatter1);
        plot.addSeries(series2, formatter2);

        // Setup the BarRenderer with our selected options
        MyBarRenderer renderer = ((MyBarRenderer)plot.getRenderer(MyBarRenderer.class));
        renderer.setBarRenderStyle(BarRenderer.BarRenderStyle.OVERLAID);
        renderer.setBarWidthStyle(BarRenderer.BarWidthStyle.FIXED_WIDTH);
        renderer.setBarWidth(60f);
        renderer.setBarGap(5f);


        final Object[] xLabels = labels.toArray();
        plot.setDomainValueFormat(new Format() {
            @Override
            public StringBuffer format(Object object, StringBuffer buffer, FieldPosition field) {
                // TODO Auto-generated method stub
                int parsedInt = Math.round(Float.parseFloat(object.toString()));
                Log.d("test", parsedInt + " " + buffer + " " + field);
                String labelString = (String) xLabels[parsedInt];
                buffer.append(labelString);
                return buffer;
            }

            @Override
            public Object parseObject(String string, ParsePosition position) {
                return java.util.Arrays.asList(xLabels).indexOf(string);
            }
        });

        plot.setDomainStep(XYStepMode.SUBDIVIDE, labels.size());

        plot.setRangeTopMin(0);
        plot.redraw();

    }

    private void onPlotClicked(PointF point) {

        // make sure the point lies within the graph area.  we use gridrect
        // because it accounts for margins and padding as well.
        if (plot.getGraphWidget().getGridRect().contains(point.x, point.y)) {
            Number x = plot.getXVal(point);
            Number y = plot.getYVal(point);

            selection = null;
            double xDistance = 0;
            double yDistance = 0;

            // find the closest value to the selection:
            for (XYSeries series : plot.getSeriesSet()) {
                for (int i = 0; i < series.size(); i++) {
                    Number thisX = series.getX(i);
                    Number thisY = series.getY(i);
                    if (thisX != null && thisY != null) {
                        double thisXDistance =
                                LineRegion.measure(x, thisX).doubleValue();
                        double thisYDistance =
                                LineRegion.measure(y, thisY).doubleValue();
                        if (selection == null) {
                            selection = new Pair<Integer, XYSeries>(i, series);
                            xDistance = thisXDistance;
                            yDistance = thisYDistance;
                        } else if (thisXDistance < xDistance) {
                            selection = new Pair<Integer, XYSeries>(i, series);
                            xDistance = thisXDistance;
                            yDistance = thisYDistance;
                        } else if (thisXDistance == xDistance &&
                                thisYDistance < yDistance &&
                                thisY.doubleValue() >= y.doubleValue()) {
                            selection = new Pair<Integer, XYSeries>(i, series);
                            xDistance = thisXDistance;
                            yDistance = thisYDistance;
                        }
                    }
                }
            }

        } else {
            // if the press was outside the graph area, deselect:
            selection = null;
        }

        if(selection == null) {
            selectionWidget.setText(NO_SELECTION_TXT);
        } else {
            selectionWidget.setText("Selected: " + selection.second.getTitle() +
                    " | " + String.format("%.2f",selection.second.getY(selection.first).doubleValue()) + " kWh");
        }
        plot.redraw();
    }

    private void configureUserRankingsCard(){
        mCardViewUserRankings = (CardView) rootView.findViewById(R.id.cardUserRankings);
        mCardUserRankings = new DashboardRankingsCard(rootView.getContext(), "User Rankings");
        mCardUserRankings.setShadow(true);
        mCardViewUserRankings.setCard(mCardUserRankings);

        mExpandableListUsers = (ExpandableListView) mCardViewUserRankings.findViewById(R.id.exp_rankings);
        twNoDataUsers = (RobotoTextView) mCardViewUserRankings.findViewById(R.id.empty);

    }

    private void configureDeviceRankingsCard(){
        mCardViewDeviceRankings = (CardView) rootView.findViewById(R.id.cardDeviceRankings);
        mCardDeviceRankings = new DashboardRankingsCard(rootView.getContext(), "Device Rankings");
        mCardDeviceRankings.setShadow(true);
        mCardViewDeviceRankings.setCard(mCardDeviceRankings);

        mExpandableListDevices = (ExpandableListView) mCardViewDeviceRankings.findViewById(R.id.exp_rankings);
        twNoDataDevices = (RobotoTextView) mCardViewDeviceRankings.findViewById(R.id.empty);
    }

    private void configureLocationCard(){
        mCardViewLocation = (CardView) rootView.findViewById(R.id.cardLocation);
        mCardLocation = new DashboardLocationCard(rootView.getContext());
        mCardLocation.setShadow(true);
        mCardViewLocation.setCard(mCardLocation);

        twLocationTimestamp = (RobotoTextView) mCardViewLocation.findViewById(R.id.twTimestamp);
        twLocationPlace = (RobotoTextView) mCardViewLocation.findViewById(R.id.twPlace);
        twLocationZone  = (RobotoTextView) mCardViewLocation.findViewById(R.id.twZone);

        refreshLocationCard(Utils.getCurrentTimestampMillis(), placeOfLocation, locationCalculated);


        //mCardViewLocation.setVisibility(View.GONE);
    }

    private void refreshLocationCard(String timestamp, String place, String zone){
        twLocationTimestamp.setText(timestamp);
        twLocationPlace.setText(place);
        twLocationZone.setText(zone);
    }

    private void configureLogCard(){
        mCardViewLog = (CardView) rootView.findViewById(R.id.cardLog);
        mCardLog = new DashboardLogCard(rootView.getContext());
        mCardLog.setShadow(true);
        mCardViewLog.setCard(mCardLog);

        twLog = (RobotoTextView) mCardViewLog.findViewById(R.id.twLog);
        twLog.setMovementMethod(new ScrollingMovementMethod());

        mCardViewLog.setVisibility(View.GONE);

    }

    private void refreshLogCard(String data){
        String currentLog = twLog.getText().toString();
        int count = currentLog.length() - currentLog.replace("\n", "").length();
        if (count > 100){
            currentLog = "> Init";
        }
        twLog.setText(currentLog + "\n> " + Utils.getCurrentTimestampMillis() + "  " + data);
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = rootView.getContext().getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    private class ExpDrawerGroupClickListenerUsers implements ExpandableListView.OnGroupClickListener {
        @Override
        public boolean onGroupClick(ExpandableListView parent, View v,
                int groupPosition, long id) {


            ViewGroup.LayoutParams layoutParams = parent.getLayoutParams();
            int n = 0;
            if (usersTotals != null){
                int nUsers = usersTotals.get(groupPosition).size() - 1;
                n = nUsers * dpToPx(16);
                Log.w("ExpListUsers", nUsers + "|"+ n + "|"+ layoutParams.height);
            }
            if (parent.isGroupExpanded(groupPosition)){
                parent.collapseGroup(groupPosition);
                if ((layoutParams.height - n) < dpToPx(120)){
                    layoutParams.height = dpToPx(120);
                } else {
                    layoutParams.height = layoutParams.height - n;
                }
            }else {
                parent.expandGroup(groupPosition, true);
                layoutParams.height = layoutParams.height + n;
            }
            parent.setLayoutParams(layoutParams);
            return true;
        }
    }

    private class ExpDrawerGroupClickListenerDevices implements ExpandableListView.OnGroupClickListener {
        @Override
        public boolean onGroupClick(ExpandableListView parent, View v,
                int groupPosition, long id) {


            ViewGroup.LayoutParams layoutParams = parent.getLayoutParams();
            int n = 0;
            if (devicesTotals != null){
                int nUsers = devicesTotals.get(groupPosition).size();
                n = nUsers *  dpToPx(17);
                Log.w("ExpListDevices", nUsers + "|"+ n + "|"+ layoutParams.height);
            }
            //n=0;
            if (parent.isGroupExpanded(groupPosition)){
                parent.collapseGroup(groupPosition);
                if ((layoutParams.height - n) < dpToPx(120)){
                    layoutParams.height = dpToPx(120);
                } else {
                    layoutParams.height = layoutParams.height - n;
                }
            }else {
                parent.expandGroup(groupPosition, true);
                layoutParams.height = layoutParams.height + n;
            }
            parent.setLayoutParams(layoutParams);
            return true;
        }
    }
    private void updateUsersRankings(){
        setUsersRankingsData();

        mExpandableListUsers.setAdapter(new ExpandableListEnergyRanking(rootView.getContext(), groupItemUsers, childItemUsers));
        mExpandableListUsers.setOnGroupClickListener(new ExpDrawerGroupClickListenerUsers());

/*
        ViewGroup.LayoutParams layoutParams = mExpandableListUsers.getLayoutParams();
        int n = 0;
        if (usersTotals != null){
            n = 3 * 40;
            layoutParams.height = n;
            Log.w("ExpListUsers INIT", 3 + "|"+ n + "|"+ mExpandableListUsers.getLayoutParams().height);
            mExpandableListUsers.setLayoutParams(layoutParams);
            Log.w("ExpListUsers INIT", 3 + "|"+ n + "|"+ mExpandableListUsers.getLayoutParams().height);
        }
*/
        //mExpandableListUsers.expandGroup(0);

    }

    private void updateDevicesRankings(){

        setDevicesRankingsData();

        mExpandableListDevices.setAdapter(new ExpandableListEnergyRanking(rootView.getContext(), groupItemDevices, childItemDevices));
        mExpandableListDevices.setOnGroupClickListener(new ExpDrawerGroupClickListenerDevices());

        /*
        ViewGroup.LayoutParams layoutParams = mExpandableListDevices.getLayoutParams();
        int n = 0;
        if (devicesTotals != null){
            n = 3 * 30;
            final float scale = rootView.getContext().getResources().getDisplayMetrics().density;
            int pixels = (int) (n * scale + 0.5f);
            layoutParams.height = pixels;
            Log.w("ExpListUsers INIT", 3 + "|"+ n + "|"+ mExpandableListDevices.getLayoutParams().height);
            mExpandableListDevices.setLayoutParams(layoutParams);
            Log.w("ExpListUsers INIT", 3 + "|"+ n + "|"+ mExpandableListDevices.getLayoutParams().height);
        }*/

        //mExpandableListDevices.expandGroup(0);
    }

    private void setUsersRankingsData(){
        groupItemUsers.clear();
        childItemUsers.clear();

        for (Integer timeSpan : usersTotals.keySet()){
            DeviceUsageRecord anyUser = new DeviceUsageRecord();
            int anyUserIndex = -1;
            for (DeviceUsageRecord deviceUsageRecord : usersTotals.get(timeSpan)){
                anyUserIndex++;
                if (deviceUsageRecord.getUsername().equals(Constants.ANYUSER)){
                    anyUser = deviceUsageRecord;
                    break;
                }
            }
            groupItemUsers.add(anyUser);
            ArrayList<DeviceUsageRecord> child = new ArrayList<DeviceUsageRecord>(usersTotals.get(timeSpan));
            /*Collections.sort(child, new Comparator<DeviceUsageRecord>() {
                @Override
                public int compare(DeviceUsageRecord item1, DeviceUsageRecord item2) {

                    return item1.getUserTime().compareTo(item2.getUserTime());
                }
            });*/

            child.remove(anyUserIndex);
            childItemUsers.add(child);
        }
        if (groupItemUsers.size() > 0){
            twNoDataUsers.setVisibility(View.GONE);
        }
    }



    private void setDevicesRankingsData(){
        groupItemDevices.clear();
        childItemDevices.clear();

        for (Integer timeSpan : devicesTotals.keySet()){
            DeviceUsageRecord anyUser = new DeviceUsageRecord();
            anyUser.setTimespan(timeSpan);
            groupItemDevices.add(anyUser);
            Log.e(LOGTAG, "Devices Group:" + anyUser.getUsername()
                    + " Timespan: " + anyUser.getTimespan()
                    + " Energy: " + anyUser.getUserTime()
                    + " Type:" + anyUser.getDeviceType());

            ArrayList<DeviceUsageRecord> child = devicesTotals.get(timeSpan);
            for (DeviceUsageRecord deviceUsageRecord : child){

                Log.w(LOGTAG, "Devices Child:" + deviceUsageRecord.getUsername()
                        + " Timespan: " + deviceUsageRecord.getTimespan()
                        + " Energy: " + deviceUsageRecord.getUserTime()
                        + " Type:" + deviceUsageRecord.getDeviceType());
            }
            /*Collections.sort(child, new Comparator<DeviceUsageRecord>() {
                @Override
                public int compare(DeviceUsageRecord item1, DeviceUsageRecord item2) {

                    return item1.getUserTime().compareTo(item2.getUserTime());
                }
            });*/

            childItemDevices.add(child);
        }

        if (groupItemDevices.size() > 0){
            twNoDataDevices.setVisibility(View.GONE);
        }
    }

    //Request Users Stats *************************************************************************


    private void requestUsersStats(){
        if (!requestInProcess) {
            refreshLogCard("RequestAllUsersStatsAsyncTask invoked");
            RequestAllUsersStatsAsyncTask deviceListAsyncTask = new RequestAllUsersStatsAsyncTask();
            deviceListAsyncTask.delegate = this;
            deviceListAsyncTask.execute();

            String logdata = "EnergyConsumptionHistory Request:" + user + " " + Utils.getDateDaysAgo(
                    8) + " " + Utils.getCurrentTimestamp();
            Log.w(LOGTAG, logdata);
            refreshLogCard(logdata);
            RequestUserEnergyConsumptionHistoryAsyncTask energyConsumptionHistory = new RequestUserEnergyConsumptionHistoryAsyncTask();
            energyConsumptionHistory.delegate = this;
            energyConsumptionHistory.execute(user, Utils.getDateDaysAgo(7),
                    Utils.getCurrentTimestamp());



            RequestUserActiveDevicesAsyncTask requestUserActiveDevicesAsyncTask = new RequestUserActiveDevicesAsyncTask();
            requestUserActiveDevicesAsyncTask.delegate = this;
            requestUserActiveDevicesAsyncTask.execute(
                    PreferenceManager.getDefaultSharedPreferences(rootView.getContext())
                            .getString(UserPreferences.USERNAME, "TBD"));
        }
        requestInProcess = true;
    }

    public void processFinishRequestUserActiveDevices(List<NfcRecord> outputList){
        ArrayList<NfcRecord> activeDevices = new ArrayList<NfcRecord>(outputList);
        refreshActiveDevicesCard(activeDevices);
    }

    public void processFinishRequestEnergyConsumptionHistory(List<EnergyConsumptionRecord> outputList){
        if (outputList == null){
            //No history of user energy consumption. This data is for showing historical bar graph.
            refreshLogCard("EnergyConsumptionRecord Null");
            return;
        }
        userEnergyHistory = new ArrayList<EnergyConsumptionRecord>(outputList);
        Log.i(LOGTAG, "processFinishRequestEnergyConsumptionHistory " + userEnergyHistory.size());
        refreshLogCard( "processFinishRequestEnergyConsumptionHistory " + userEnergyHistory.size());

        updatePlot(userEnergyHistory);
        //TODO graph
    }


    public void processFinishRequestAllUsers(List<DeviceUsageRecord> outputList){
        if (outputList == null){
            refreshLogCard("AllUserStats data Null");
            return;
        }
        Log.i(LOGTAG, "processFinishRequestAllUsers");
        requestInProcess = false;
        userStatsRaw = new ArrayList<DeviceUsageRecord>(outputList);
        userStatsHM.clear();
        deviceStatsHM.clear();
        usersTotals.clear();
        devicesTotals.clear();

        for (DeviceUsageRecord deviceUsageRecord : userStatsRaw){
            addDeviceUsageRecordToUserInfo(deviceUsageRecord);
            addDeviceUsageRecordToDeviceInfo(deviceUsageRecord);
        }

        calculateUsersTotalEnergyConsumption();
        calculateDevicesTotalEnergyConsumption();
        printRankingsLog();
        estimateDailyTarget();

        updateUsersRankings();
        updateDevicesRankings();
        //TODO graph
    }

    private void estimateDailyTarget(){
        Double groupEnergy = 0.0;
        Double userEnergy = 0.0;
        Double groupDailyLimit = Double.valueOf(PreferenceManager.getDefaultSharedPreferences(rootView.getContext())
                .getString(UserPreferences.TARGET_KWH_GROUP, "40.0"));
        Double userDailyLimit = Double.valueOf(PreferenceManager.getDefaultSharedPreferences(rootView.getContext())
                .getString(UserPreferences.TARGET_KWH_INDIVIDUAL, "10.0"));


        for (DeviceUsageRecord deviceUsageRecord : usersTotals.get(UserPreferences.TODAY)){
            if (deviceUsageRecord.getUsername().equals(Constants.ANYUSER)) {
                groupEnergy = deviceUsageRecord.getUserTime();
            }
            if (deviceUsageRecord.getUsername().equals(user)) {
                userEnergy = deviceUsageRecord.getUserTime();
            }
        }

        Log.i(LOGTAG, "GROUP: " + groupEnergy + "|" + groupDailyLimit
                    + " USER: " + userEnergy  + "|" + userDailyLimit );

        if (groupEnergy > groupDailyLimit){
            startNotification(7775, "Oops! Energy consumption in the house is over the target!");
        }
        if (userEnergy > userDailyLimit){
            startNotification(7776,"Oops! Your personal energy consumption is over the target!");
        }

        // TODO save overconsumption in DB

    }

    private void startNotification(int id, String message){
        Intent notificationIntent = new Intent(rootView.getContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(rootView.getContext(), 0,
                notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(rootView.getContext())
                .setContentTitle("MDP")
                .setContentText(message)
                .setSmallIcon(R.drawable.plug128)
                .setContentIntent(pendingIntent).build();

        NotificationManager mNotificationManager =
                (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(id, notification);
    }


    private void printRankingsLog(){

        Log.w(LOGTAG, "Rankings USERS");
        for (Integer timeSpan : usersTotals.keySet()){
            for (DeviceUsageRecord deviceUsageRecord : usersTotals.get(timeSpan)){
                    Log.i(LOGTAG, timeSpan + "|" + deviceUsageRecord.getUsername() + "|"
                            + deviceUsageRecord.getUserTime());
                    refreshLogCard(timeSpan + "|" + deviceUsageRecord.getUsername() + "|"
                            + deviceUsageRecord.getUserTime());
            }
        }

        Log.w(LOGTAG, "Rankings DEVICES");
        for (Integer timeSpan : devicesTotals.keySet()){
            for (DeviceUsageRecord deviceUsageRecord : devicesTotals.get(timeSpan)){
                Log.i(LOGTAG, timeSpan + "|" + deviceUsageRecord.getUsername() + "|" + deviceUsageRecord.getUserTime());
                refreshLogCard( timeSpan + "|" + deviceUsageRecord.getUsername() + "|" + deviceUsageRecord.getUserTime());
            }
        }
    }

    private void calculateUsersTotalEnergyConsumption(){
        Log.i(LOGTAG, "calculateUsersTotalEnergyConsumption");
        for (String username : userStatsHM.keySet()){
            for (Integer timeSpan : userStatsHM.get(username).keySet()){
                ArrayList<DeviceUsageRecord> usageRecordsInTimeSpan = userStatsHM.get(username).get(timeSpan);
                DeviceUsageRecord totalEnergyUsage = new DeviceUsageRecord();
                totalEnergyUsage.setUsername(username);
                totalEnergyUsage.setTimespan(timeSpan);
                totalEnergyUsage.setDeviceType(Devices.TOTAL);
                totalEnergyUsage.setDeviceId(Devices.TOTAL);
                Double energy = 0.0;
                for (DeviceUsageRecord deviceUsageRecord : usageRecordsInTimeSpan){
                    energy += Utils.getEnergyFromTime(deviceUsageRecord.getDeviceType(), deviceUsageRecord.getUserTime());
                }
                totalEnergyUsage.setUserTime(energy);

                ArrayList<DeviceUsageRecord> usersTotalsInTimeSpan = usersTotals.get(timeSpan);
                if (usersTotalsInTimeSpan == null){
                    usersTotalsInTimeSpan = new ArrayList<DeviceUsageRecord>();
                }
                usersTotalsInTimeSpan.add(totalEnergyUsage);
                usersTotals.put(timeSpan, usersTotalsInTimeSpan);
                Log.i(LOGTAG, username + "|" + timeSpan + "|" + energy);
            }
        }

        for (Integer timeSpan : usersTotals.keySet()){
            ArrayList<DeviceUsageRecord> sorted = new ArrayList<DeviceUsageRecord>(sortByEnergyConsumption(usersTotals.get(timeSpan)));
            usersTotals.put(timeSpan, sorted);
        }
    }

    private void calculateDevicesTotalEnergyConsumption(){
        Log.i(LOGTAG, "calculateDevicesTotalEnergyConsumption");
        for (String device : deviceStatsHM.keySet()){
            for (Integer timeSpan : deviceStatsHM.get(device).keySet()){
                ArrayList<DeviceUsageRecord> deviceUsageRecordsInTimeSpan = deviceStatsHM.get(device).get(timeSpan);
                DeviceUsageRecord totalEnergyUsage = new DeviceUsageRecord();
                totalEnergyUsage.setUsername(device);
                totalEnergyUsage.setTimespan(timeSpan);
                totalEnergyUsage.setDeviceType(Devices.TOTAL);
                totalEnergyUsage.setDeviceId(Devices.TOTAL);
                Double energy = 0.0;
                for (DeviceUsageRecord deviceUsageRecord : deviceUsageRecordsInTimeSpan){
                    if (deviceUsageRecord.getUsername().equals(Constants.ANYUSER)) {
                        energy += Utils.getEnergyFromTime(deviceUsageRecord.getDeviceType(),
                                deviceUsageRecord.getUserTime());
                    }
                }
                totalEnergyUsage.setUserTime(energy);

                ArrayList<DeviceUsageRecord> devicesTotalsInTimeSpan = devicesTotals.get(timeSpan);
                if (devicesTotalsInTimeSpan == null){
                    devicesTotalsInTimeSpan = new ArrayList<DeviceUsageRecord>();
                }
                devicesTotalsInTimeSpan.add(totalEnergyUsage);
                devicesTotals.put(timeSpan, devicesTotalsInTimeSpan);
                Log.i(LOGTAG, device + "|" + timeSpan + "|" + energy);
            }
        }

        for (Integer timeSpan : devicesTotals.keySet()){
            ArrayList<DeviceUsageRecord> sorted = new ArrayList<DeviceUsageRecord>(sortByEnergyConsumption(devicesTotals.get(timeSpan)));
            devicesTotals.put(timeSpan, sorted);
        }
    }

    private ArrayList<DeviceUsageRecord> sortByEnergyConsumption(ArrayList<DeviceUsageRecord> unsortedList){
        Collections.sort(unsortedList, new Comparator<DeviceUsageRecord>() {
            @Override
            public int compare(DeviceUsageRecord item1, DeviceUsageRecord item2) {

                return item2.getUserTime().compareTo(item1.getUserTime());
            }
        });

        return unsortedList;
    }

    private void addDeviceUsageRecordToUserInfo(DeviceUsageRecord deviceUsageRecord){
        HashMap<Integer,ArrayList<DeviceUsageRecord>> userRecords = userStatsHM.get(deviceUsageRecord.getUsername());
        ArrayList<DeviceUsageRecord> userRecordsInTimeSpan;
        if (userRecords == null){
            userRecords = new HashMap<Integer, ArrayList<DeviceUsageRecord>>();
            userRecordsInTimeSpan = new ArrayList<DeviceUsageRecord>();
        } else {
            userRecordsInTimeSpan = userRecords.get(deviceUsageRecord.getTimespan());
            if (userRecordsInTimeSpan == null){
                userRecordsInTimeSpan = new ArrayList<DeviceUsageRecord>();
            }
        }
        userRecordsInTimeSpan.add(deviceUsageRecord);
        userRecords.put(deviceUsageRecord.getTimespan(), userRecordsInTimeSpan);
        userStatsHM.put(deviceUsageRecord.getUsername(), userRecords);
    }

    private void addDeviceUsageRecordToDeviceInfo(DeviceUsageRecord deviceUsageRecord){
        HashMap<Integer,ArrayList<DeviceUsageRecord>> deviceRecords = deviceStatsHM.get(deviceUsageRecord.getDeviceType());
        ArrayList<DeviceUsageRecord> deviceRecordsInTimeSpan;
        if (deviceRecords == null){
            deviceRecords = new HashMap<Integer, ArrayList<DeviceUsageRecord>>();
            deviceRecordsInTimeSpan = new ArrayList<DeviceUsageRecord>();
        } else {
            deviceRecordsInTimeSpan = deviceRecords.get(deviceUsageRecord.getTimespan());
            if (deviceRecordsInTimeSpan == null){
                deviceRecordsInTimeSpan = new ArrayList<DeviceUsageRecord>();
            }
        }
        deviceRecordsInTimeSpan.add(deviceUsageRecord);
        deviceRecords.put(deviceUsageRecord.getTimespan(), deviceRecordsInTimeSpan);
        deviceStatsHM.put(deviceUsageRecord.getDeviceType(), deviceRecords);
    }




    //Service Connection methods *******************************************************************

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.i(LOGTAG, "Sensor Service: onServiceDisconnected");
        if (mServiceMessenger != null) {
            mServiceMessenger = null;
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.i(LOGTAG, "Sensor Service: onServiceConnected");
        mServiceMessenger = new Messenger(service);
        try {
            Message msg = Message.obtain(null, MdpWorkerService.MSG_REGISTER_CLIENT);
            msg.replyTo = mMessenger;
            mServiceMessenger.send(msg);
        }
        catch (RemoteException e) {
            // In this case the service has crashed before we could even do anything with it
        }
    }

    /**
     * Automatically binds to the service. It starts it if required.
     */
    private void automaticBinding() {
        if (MdpWorkerService.isRunning()){
            doBindService();
        } else{
            startServiceNetworkScan();
            doBindService();
        }
    }

    /**
     * Automatically unbinds from the service
     */
    private void automaticUnbinding() {
        stopServiceNetworkScan();
    }

    /**
     * Start the service
     */
    public void startServiceNetworkScan(){
        Log.i(LOGTAG, "Network Scan Service: START");
        Intent intent = new Intent(rootView.getContext(), MdpWorkerService.class);
        rootView.getContext().startService(intent);
    }

    /**
     * Unbinds from the service
     */
    public void stopServiceNetworkScan(){
        Log.i(LOGTAG, "Network Scan Service: STOP");
        doUnbindService();
    }

    /**
     * Binds to the service
     */
    private void doBindService() {
        rootView.getContext().bindService(new Intent(rootView.getContext(), MdpWorkerService.class),
                mConnection, 0);
        mIsBound = true;
    }

    /**
     * Unbinds from the service
     */
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
            rootView.getContext().unbindService(mConnection);
            mIsBound = false;
        }
    }


    //Communication interaction routines with service **********************************************

    private void sendMessageToService(int command) {
        if (mIsBound) {
            if (mServiceMessenger != null) {
                try {
                    Message msg = Message
                            .obtain(null, command);
                    msg.replyTo = mMessenger;
                    mServiceMessenger.send(msg);
                } catch (RemoteException e) {
                    Log.e(LOGTAG, e.getMessage());
                }
            }
        }
    }

    /**
     * Handler of incoming messages from clients or from services it is connected to.
     */
    private class IncomingMessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            //Log.d(LOGTAG, "handleMessage: " + msg.what);
            switch (msg.what) {
                case MdpWorkerService.MSG_LOG:
                    String data = msg.getData().getString(MdpWorkerService.ARG_LOG);
                    refreshLogCard(data);
                    break;
                case MdpWorkerService.MSG_LOCATION_ACQUIRED:
                    String location = msg.getData().getString(MdpWorkerService.ARG_LOCATION_ACQUIRED);
                    String[] parts = location.split("\\|");
                    if (parts.length > 1) {
                        refreshLocationCard(Utils.getCurrentTimestampMillis(), parts[0], parts[1]);
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    class MyBarFormatter extends BarFormatter {
        public MyBarFormatter(int fillColor, int borderColor) {
            super(fillColor, borderColor);
        }

        @Override
        public Class<? extends SeriesRenderer> getRendererClass() {
            return MyBarRenderer.class;
        }

        @Override
        public SeriesRenderer getRendererInstance(XYPlot plot) {
            return new MyBarRenderer(plot);
        }
    }

    class MyBarRenderer extends BarRenderer<MyBarFormatter> {

        public MyBarRenderer(XYPlot plot) {
            super(plot);
        }

        /**
         * Implementing this method to allow us to inject our
         * special selection formatter.
         * @param index index of the point being rendered.
         * @param series XYSeries to which the point being rendered belongs.
         * @return
         */
        @Override
        public MyBarFormatter getFormatter(int index, XYSeries series) {
            if(selection != null &&
                    selection.second == series &&
                    selection.first == index) {
                return selectionFormatter;
            } else {
                return getFormatter(series);
            }
        }
    }


}
