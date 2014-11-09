package tudelft.mdp.dashboard;

import com.devspark.robototextview.widget.RobotoTextView;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.view.CardView;
import tudelft.mdp.MainActivity;
import tudelft.mdp.MdpWorkerService;
import tudelft.mdp.R;
import tudelft.mdp.backend.endpoints.deviceLogEndpoint.model.DeviceUsageRecord;
import tudelft.mdp.backend.endpoints.energyConsumptionRecordEndpoint.model.EnergyConsumptionRecord;
import tudelft.mdp.deviceManager.RequestAllUsersStatsAsyncTask;
import tudelft.mdp.deviceManager.RequestUserEnergyConsumptionHistoryAsyncTask;
import tudelft.mdp.enums.Constants;
import tudelft.mdp.enums.Devices;
import tudelft.mdp.enums.UserPreferences;
import tudelft.mdp.ui.DashboardLocationCard;
import tudelft.mdp.ui.DashboardLogCard;
import tudelft.mdp.ui.DashboardRankingsCard;
import tudelft.mdp.ui.ExpandableListEnergyRanking;
import tudelft.mdp.utils.Utils;


public class DashboardFragment extends Fragment implements
        ServiceConnection,
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

    private String placeOfLocation = "TBD";
    private String locationCalculated = "TBD";

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

    private void configureCardsInit(){
        configureUserRankingsCard();
        configureDeviceRankingsCard();
        configureLocationCard();
        configureLogCard();
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

    private void updateUsersRankings(){
        setUsersRankingsData();

        mExpandableListUsers.setAdapter(new ExpandableListEnergyRanking(rootView.getContext(), groupItemUsers, childItemUsers));
        mExpandableListUsers.setOnGroupClickListener(new ExpDrawerGroupClickListener());

        mExpandableListUsers.expandGroup(0);

    }

    private void updateDevicesRankings(){
        setDevicesRankingsData();

        mExpandableListDevices.setAdapter(new ExpandableListEnergyRanking(rootView.getContext(), groupItemDevices, childItemDevices));
        mExpandableListDevices.setOnGroupClickListener(new ExpDrawerGroupClickListener());

        mExpandableListDevices.expandGroup(0);
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

            String logdata = "EnergyConsumptionHistory Request:" + user + " " + Utils.getMinTimestamp(UserPreferences.WEEK) + " " + Utils.getCurrentTimestamp();
            Log.w(LOGTAG, logdata);
            refreshLogCard(logdata);
            RequestUserEnergyConsumptionHistoryAsyncTask energyConsumptionHistory = new RequestUserEnergyConsumptionHistoryAsyncTask();
            energyConsumptionHistory.delegate = this;
            energyConsumptionHistory.execute(user, Utils.getMinTimestamp(UserPreferences.WEEK),
                    Utils.getCurrentTimestamp());
        }
        requestInProcess = true;
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
            startNotification("Oops! Energy consumption in the house is over the target!");
        }
        if (userEnergy > userDailyLimit){
            startNotification("Oops! Your personal energy consumption is over the target!");
        }

        // TODO save overconsumption in DB

    }

    private void startNotification(String message){
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

        mNotificationManager.notify(7777, notification);
    }


    private void printRankingsLog(){

        Log.w(LOGTAG, "Rankings USERS");
        for (Integer timeSpan : usersTotals.keySet()){
            for (DeviceUsageRecord deviceUsageRecord : usersTotals.get(timeSpan)){
                if (!deviceUsageRecord.getUsername().equals(Constants.ANYUSER)) {
                    Log.i(LOGTAG, timeSpan + "|" + deviceUsageRecord.getUsername() + "|"
                            + deviceUsageRecord.getUserTime());
                    refreshLogCard(timeSpan + "|" + deviceUsageRecord.getUsername() + "|"
                            + deviceUsageRecord.getUserTime());
                }
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
                    if (!deviceUsageRecord.getUsername().equals(Constants.ANYUSER)) {
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
                    refreshLocationCard(Utils.getCurrentTimestampMillis(), parts[0], parts[1]);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }


}
