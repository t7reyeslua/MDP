package tudelft.mdp.backend.cron;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import tudelft.mdp.backend.endpoints.EnergyConsumptionRecordEndpoint;
import tudelft.mdp.backend.enums.Constants;
import tudelft.mdp.backend.enums.Devices;
import tudelft.mdp.backend.Utils;
import tudelft.mdp.backend.endpoints.NfcLogRecordEndpoint;
import tudelft.mdp.backend.records.DeviceUsageRecord;
import tudelft.mdp.backend.records.EnergyConsumptionRecord;


@SuppressWarnings("serial")
public class CalculateDailyEnergyConsumptionCronServlet extends HttpServlet {

    private static final Logger _logger = Logger.getLogger(CalculateDailyEnergyConsumptionCronServlet.class.getName());
    private HashMap<String, HashMap<Integer,ArrayList<DeviceUsageRecord>>> userStatsHM = new HashMap<String, HashMap<Integer, ArrayList<DeviceUsageRecord>>>();
    private HashMap<String, HashMap<Integer,ArrayList<DeviceUsageRecord>>> deviceStatsHM = new HashMap<String, HashMap<Integer, ArrayList<DeviceUsageRecord>>>();
    private HashMap<Integer, ArrayList<DeviceUsageRecord>> usersTotals = new HashMap<Integer, ArrayList<DeviceUsageRecord>>();
    private HashMap<Integer, ArrayList<DeviceUsageRecord>> devicesTotals = new HashMap<Integer, ArrayList<DeviceUsageRecord>>();
    private String timestamp;

    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        try {
            _logger.info("CalculateDailyEnergyConsumptionCronServlet Cron Job is being executed");

            timestamp = Utils.getCurrentTimestamp();
            ArrayList<DeviceUsageRecord> userStats = requestUserStats();
            processUserStats(userStats);
            saveUserStatsToDB();

        } catch (Exception ex) {
            _logger.severe("Oops: " + ex.getMessage());
        }
    }

    public ArrayList<DeviceUsageRecord> requestUserStats(){
        NfcLogRecordEndpoint nfcLogRecordEndpoint = new NfcLogRecordEndpoint();
        return new ArrayList<DeviceUsageRecord>(nfcLogRecordEndpoint.getUsersStatsOfAllDevices().getItems());
    }


    public void processUserStats(ArrayList<DeviceUsageRecord> userStatsRaw){
        _logger.info("processFinishRequestAllUsers");
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

    }

    public void saveUserStatsToDB(){
        _logger.info("saveUserStatsToDB");
        EnergyConsumptionRecordEndpoint energyConsumptionRecordEndpoint = new EnergyConsumptionRecordEndpoint();

        Double groupEnergy = 0.0;
        for (DeviceUsageRecord deviceUsageRecord : usersTotals.get(Constants.TODAY)){
            if (deviceUsageRecord.getUsername().equals(Constants.ANYUSER)) {
                groupEnergy = deviceUsageRecord.getUserTime();
            }
        }

        for (DeviceUsageRecord deviceUsageRecord : usersTotals.get(Constants.TODAY)){
            if (!deviceUsageRecord.getUsername().equals(Constants.ANYUSER)) {
                Double userEnergy = deviceUsageRecord.getUserTime();

                EnergyConsumptionRecord energyConsumptionRecord = new EnergyConsumptionRecord();
                energyConsumptionRecord.setUsername(deviceUsageRecord.getUsername());
                energyConsumptionRecord.setGroupEnergy(groupEnergy);
                energyConsumptionRecord.setUserEnergy(userEnergy);
                energyConsumptionRecord.setTimestamp(timestamp);

                energyConsumptionRecordEndpoint.insertEnergyConsumptionRecord(energyConsumptionRecord);

            }
        }
    }


    private void printRankingsLog(){

        _logger.info("Rankings USERS");
        for (Integer timeSpan : usersTotals.keySet()){
            for (DeviceUsageRecord deviceUsageRecord : usersTotals.get(timeSpan)){
                if (!deviceUsageRecord.getUsername().equals(Constants.ANYUSER)) {
                    _logger.info(timeSpan + "|" + deviceUsageRecord.getUsername() + "|"
                            + deviceUsageRecord.getUserTime());
                }
            }
        }
        _logger.info("Rankings DEVICES");
        for (Integer timeSpan : devicesTotals.keySet()){
            for (DeviceUsageRecord deviceUsageRecord : devicesTotals.get(timeSpan)){
                _logger.info(
                        timeSpan + "|" + deviceUsageRecord.getUsername() + "|" + deviceUsageRecord
                                .getUserTime());
            }
        }
    }

    private void calculateUsersTotalEnergyConsumption(){
        _logger.info("calculateUsersTotalEnergyConsumption");
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
                    energy += Utils.getEnergyFromTime(deviceUsageRecord.getDeviceType(),
                            deviceUsageRecord.getUserTime());
                }
                totalEnergyUsage.setUserTime(energy);

                ArrayList<DeviceUsageRecord> usersTotalsInTimeSpan = usersTotals.get(timeSpan);
                if (usersTotalsInTimeSpan == null){
                    usersTotalsInTimeSpan = new ArrayList<DeviceUsageRecord>();
                }
                usersTotalsInTimeSpan.add(totalEnergyUsage);
                usersTotals.put(timeSpan, usersTotalsInTimeSpan);
                _logger.info(username + "|" + timeSpan + "|" + energy);
            }
        }

        for (Integer timeSpan : usersTotals.keySet()){
            ArrayList<DeviceUsageRecord> sorted = new ArrayList<DeviceUsageRecord>(sortByEnergyConsumption(usersTotals.get(timeSpan)));
            usersTotals.put(timeSpan, sorted);
        }
    }

    private void calculateDevicesTotalEnergyConsumption(){
        _logger.info("calculateDevicesTotalEnergyConsumption");
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
                _logger.info(device + "|" + timeSpan + "|" + energy);
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


    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doGet(req, resp);
    }
}

