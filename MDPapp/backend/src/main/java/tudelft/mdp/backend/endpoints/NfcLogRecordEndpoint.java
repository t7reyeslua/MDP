package tudelft.mdp.backend.endpoints;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.ConflictException;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.repackaged.com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Named;

import tudelft.mdp.backend.enums.Constants;
import tudelft.mdp.backend.Utils;
import tudelft.mdp.backend.records.DeviceUsageRecord;
import tudelft.mdp.backend.records.NfcLogRecord;
import tudelft.mdp.backend.records.NfcRecord;
import tudelft.mdp.backend.records.RegistrationRecord;

import static tudelft.mdp.backend.OfyService.ofy;

/** An endpoint class we are exposing */
@Api(name = "deviceLogEndpoint",
        description = "An API to manage the historical log of devices usage (NFC tags)",
        version = "v1",
        namespace = @ApiNamespace(ownerDomain = "endpoints.backend.mdp.tudelft",
        ownerName = "endpoints.backend.mdp.tudelft", packagePath=""))
public class NfcLogRecordEndpoint {


    private static final Logger LOG = Logger.getLogger(NfcLogRecordEndpoint.class.getName());

    /**
     * This method gets the <code>NfcLogRecord</code> object associated with the specified <code>id</code>.
     * @param nfcId The id of the NFC tag of the object to be returned.
     * @return The <code>NfcLogRecord</code> associated with <code>id</code>.
     */
    @ApiMethod(name = "listDeviceLog", path = "list_deviceLog")
    public CollectionResponse<NfcLogRecord> listDeviceLog(@Named("nfcId") String nfcId) {
        // Implement this function
        LOG.info("Calling listDeviceLog method");
        List<NfcLogRecord> records = ofy().load().type(NfcLogRecord.class)
                .filter("nfcId", nfcId)
                .order("timestamp")
                .list();

        return CollectionResponse.<NfcLogRecord>builder().setItems(records).build();

    }

    @ApiMethod(name = "listDeviceLogByDate", path = "list_deviceLog_date")
    public CollectionResponse<NfcLogRecord> listDeviceLogByDate(@Named("nfcId") String nfcId, @Named("minDate") String minDate, @Named("maxDate") String maxDate) {

        LOG.info("Calling listDeviceLogByDate method");
        List<NfcLogRecord> records = ofy().load().type(NfcLogRecord.class)
                .filter("nfcId", nfcId)
                .filter("timestamp >=", minDate)
                .filter("timestamp <=", maxDate)
                .order("timestamp")
                .list();
        return CollectionResponse.<NfcLogRecord>builder().setItems(records).build();

    }
    @ApiMethod(name = "getLastUserLogOfDevice", path = "get_lastUserLog_device")
    public NfcLogRecord getLastUserLogOfDevice(@Named("nfcId") String nfcId, @Named("user") String user) {

        LOG.info("Calling getLastUserLogOfDevice method");

        List<NfcLogRecord> records = ofy().load().type(NfcLogRecord.class)
                .filter("nfcId", nfcId)
                .filter("user", user)
                .order("timestamp")
                .list();

        if (records.size() > 0) {
            return records.get(records.size() - 1);
        } else {
            return null;
        }
    }

    @ApiMethod(name = "getActiveUsersOfDevice", path = "get_active_users_device")
    public NfcRecord getActiveUsersOfDevice(@Named("nfcId") String nfcId) {

        LOG.info("Calling getActiveUsersOfDevice method");

        List<RegistrationRecord> users = ofy().load().type(RegistrationRecord.class).list();

        int activeUsers = 0;
        for (RegistrationRecord user : users){
            NfcLogRecord lastLogOfUser = getLastUserLogOfDevice(nfcId, user.getUsername());
            if (lastLogOfUser != null){
                if (lastLogOfUser.getState()){
                    activeUsers++;
                    LOG.warning("LastLogID: " + lastLogOfUser.getId() + "|" + user.getUsername() + " " + nfcId + " ON. Active Users: " + activeUsers );
                }
            }
        }

        NfcRecordEndpoint nfcRecordEndpoint = new NfcRecordEndpoint();
        NfcRecord deviceInfo;
        try {
            deviceInfo = nfcRecordEndpoint.getNFC(nfcId);
        } catch (NotFoundException e){
            deviceInfo = new NfcRecord();
            deviceInfo.setNfcId(nfcId);
        }
        deviceInfo.setState(activeUsers);
        return deviceInfo;

    }

    @ApiMethod(name = "listDeviceLogFromUser", path = "list_deviceLog_user")
    public CollectionResponse<NfcLogRecord> listDeviceLogFromUser(@Named("nfcId") String nfcId, @Named("user") String user) {

        LOG.info("Calling listDeviceLogFromUser method");

        List<NfcLogRecord> records= ofy().load().type(NfcLogRecord.class)
                .filter("nfcId", nfcId)
                .filter("user", user)
                .order("timestamp")
                .list();
        return CollectionResponse.<NfcLogRecord>builder().setItems(records).build();
    }

    @ApiMethod(name = "listUserDeviceLog", path = "list_userDeviceLog")
    public CollectionResponse<NfcLogRecord> listUserDeviceLog(@Named("user") String user) {

        LOG.info("Calling listUserDeviceLog method");

        List<NfcLogRecord> records= ofy().load().type(NfcLogRecord.class)
                .filter("user", user)
                .order("timestamp")
                .list();
        return CollectionResponse.<NfcLogRecord>builder().setItems(records).build();

    }

    @ApiMethod(name = "listUserDeviceLogByDate", path = "list_userDeviceLog_date")
    public CollectionResponse<NfcLogRecord> listUserDeviceLogByDate(
            @Named("user") String user,
            @Named("minDate") String minDate,
            @Named("maxDate") String maxDate) {

        LOG.info("Calling listUserDeviceLogByDate method");

        List<NfcLogRecord> records= ofy().load().type(NfcLogRecord.class)
                .filter("user", user)
                .filter("timestamp >=", minDate)
                .filter("timestamp <=", maxDate)
                .order("timestamp")
                .list();
        return CollectionResponse.<NfcLogRecord>builder().setItems(records).build();
    }

    @ApiMethod(name = "listUserDeviceLogByDateDevice", path = "list_userDeviceLog_datedevice")
    public CollectionResponse<NfcLogRecord> listUserDeviceLogByDateDevice(
            @Named("nfcId") String nfcId,
            @Named("user") String user,
            @Named("minDate") String minDate,
            @Named("maxDate") String maxDate) {

        LOG.info("Calling listUserDeviceLogByDateDevice method");

        List<NfcLogRecord> records= ofy().load().type(NfcLogRecord.class)
                .filter("nfcId", nfcId)
                .filter("user", user)
                .filter("timestamp >=", minDate)
                .filter("timestamp <=", maxDate)
                .order("timestamp")
                .list();
        return CollectionResponse.<NfcLogRecord>builder().setItems(records).build();

    }


    @ApiMethod(name = "getUserActiveDevices", path = "get_user_active_devices")
    public CollectionResponse<NfcRecord> getUserActiveDevices(
            @Named("user") String user) {

        LOG.info("Calling getUserActiveDevices method");

        List<NfcRecord> devices = ofy().load().type(NfcRecord.class).list();
        List<NfcRecord> activeDevices = new ArrayList<NfcRecord>();

        for (NfcRecord device : devices){
            NfcLogRecord record = getLastUserLogOfDevice(device.getNfcId(),user);
            if (record != null){
                LOG.info(user + "|" +device.getType() + " active: " + device.getState());
                if (record.getState()){
                    //device.setDescription(record.getTimestamp());
                    activeDevices.add(device);
                }
            }
        }

        return CollectionResponse.<NfcRecord>builder().setItems(activeDevices).build();
    }


    @ApiMethod(name = "getUserStatsOfDevice", path = "get_user_stats_device")
    public CollectionResponse<Double> getUserStatsOfDevice (
            @Named("nfcId") String nfcId,
            @Named("user") String user,
            @Named("minDate") String minDate,
            @Named("maxDate") String maxDate) {

        LOG.info("Calling getUserStatsOfDevice method");

        Double totalTime = 0.0;
        Double totalPower = 0.0;
        Double userTime = 0.0;
        Double userPower = 0.0;
        Double percentage = 0.0;
        Double userStatus = 0.0;

        List<NfcLogRecord> userRecords = new ArrayList<NfcLogRecord>(listUserDeviceLogByDateDevice(nfcId,
                user, minDate, maxDate).getItems());

        String nowTimeStringOriginal = Utils.getCurrentTimestamp();
        String nowTimeString = nowTimeStringOriginal;
        LOG.info("USER NOW " + nowTimeString + " No. of Records: " + userRecords.size()
                + " NFC:" + nfcId
                + " minDate: " + minDate
                + " maxDate: " + maxDate);

        /* ========================================================*/
        /* Calculate the time the user has made use of the device */
        for (int i = userRecords.size() - 1; i >= 0; i--) {

            /* An ON step is detected -> Measure how long it lasted */
            String temp = userRecords.get(i).getTimestamp();
            LOG.info("UserRecord: " + temp + " State:" + userRecords.get(i).getState());
            if (userRecords.get(i).getState()){

                long timeNewest = Utils.convertTimestampToSeconds(nowTimeString);
                long timeOldest = Utils.convertTimestampToSeconds(temp);

                double diff1 = (double) (timeNewest - timeOldest);
                diff1 = diff1 / 1000;


                Double diff = Utils.differenceBetweenDates(nowTimeString, temp);
                userTime += diff;
                LOG.info(nowTimeString + "-" + temp + "="+ diff + "-----------" + timeNewest + "-" + timeOldest + "="+ diff1 );
            }
            nowTimeString = temp;
        }

        /* The first transition was a Falling Edge.
           It was already ON when the time window started */
        if(userRecords.size() > 0) {
            if (!userRecords.get(0).getState()) {
                String temp = String.valueOf(userRecords.get(0).getTimestamp());
                String minDateString = String.valueOf(minDate);
                Double diff = Utils.differenceBetweenDates(temp, minDateString);
                userTime += diff;
                LOG.info(temp + "-" + minDateString + "="+ diff);
            }
        }


        /* ========================================================*/
        /* Calculate the time the device has been used by all users */

       List<NfcLogRecord> deviceRecords = new ArrayList<NfcLogRecord>(listUserDeviceLogByDateDevice(nfcId,
               Constants.ANYUSER, minDate, maxDate).getItems());

        nowTimeString = nowTimeStringOriginal;
        LOG.info("DEVICE NOW " + nowTimeString);

        /* Calculate the time the user has made use of the device */
        for (int i = deviceRecords.size() - 1; i >= 0; i--) {

            /* An ON step is detected -> Measure how long it lasted */
            String temp = deviceRecords.get(i).getTimestamp();
            if (deviceRecords.get(i).getState()){
                Double diff = Utils.differenceBetweenDates(nowTimeString, temp);
                totalTime += diff;
                LOG.info(nowTimeString + "-" + temp + "="+ diff);
            }
            nowTimeString = temp;
        }

        /* The first transition was a Falling Edge.
           It was already ON when the time window started */
        if (deviceRecords.size() > 0) {
            if (!deviceRecords.get(0).getState()) {
                String temp = String.valueOf(deviceRecords.get(0).getTimestamp());
                String minDateString = String.valueOf(minDate);
                Double diff = Utils.differenceBetweenDates(temp, minDateString);
                totalTime += diff;
                LOG.info(temp + "-" + minDateString + "="+ diff);
            }
        }


        /* ========================================================*/

        if (totalTime > 0 ){
            percentage = userTime/totalTime;
        }

        NfcLogRecord lastUserInteractionWithDevice = getLastUserLogOfDevice(nfcId, user);

        if (lastUserInteractionWithDevice != null){
            if (lastUserInteractionWithDevice.getState()){
                userStatus = 1.0;
            }
        }

        NfcRecordEndpoint nfcRecordEndpoint = new NfcRecordEndpoint();
        NfcRecord device;
        try {
            device = nfcRecordEndpoint.getNFC(nfcId);
            totalPower = Utils.getEnergyFromTime(device.getType(), totalTime);
            userPower = Utils.getEnergyFromTime(device.getType(), userTime);
        } catch (NotFoundException e){
            LOG.info(e.getMessage());
        }

        List<Double> result = new ArrayList<Double>();
        result.add(totalTime);
        result.add(userTime);
        result.add(totalPower);
        result.add(userPower);
        result.add(percentage);
        result.add(userStatus);

        return CollectionResponse.<Double>builder().setItems(result).build();
    }

    @ApiMethod(name = "getSingleUserDeviceTime", path = "get_single_user_device_time")
    public CollectionResponse<Double> getSingleUserDeviceTime (
            @Named("nfcId") String nfcId,
            @Named("user") String user,
            @Named("minDate") String minDate,
            @Named("maxDate") String maxDate) {

        //LOG.info("Calling getSingleUserDeviceTime method");

        Double userTime = 0.0;

        List<NfcLogRecord> userRecords = new ArrayList<NfcLogRecord>(listUserDeviceLogByDateDevice(nfcId,
                user, minDate, maxDate).getItems());

        String nowTimeString = Utils.getCurrentTimestamp();
        if (userRecords.size() > 0) {
            LOG.severe("USER NOW " + nowTimeString + " No. of Records: " + userRecords.size()
                    + " NFC:" + nfcId
                    + " minDate: " + minDate
                    + " maxDate: " + maxDate);
        }

        /* ========================================================*/
        /* Calculate the time the user has made use of the device */
        for (int i = userRecords.size() - 1; i >= 0; i--) {

            /* An ON step is detected -> Measure how long it lasted */
            String temp = userRecords.get(i).getTimestamp();
            //LOG.info("UserRecord: "+ user + "|" + temp + " State:" + userRecords.get(i).getState());
            if (userRecords.get(i).getState()){

                long timeNewest = Utils.convertTimestampToSeconds(nowTimeString);
                long timeOldest = Utils.convertTimestampToSeconds(temp);

                double diff1 = (double) (timeNewest - timeOldest);
                diff1 = diff1 / 1000;


                Double diff = Utils.differenceBetweenDates(nowTimeString, temp);
                userTime += diff;
                LOG.info(user + "|" + nfcId + "|" + minDate + "|"+ "-----------" +nowTimeString + "-" + temp + "="+ diff + "-----------" + timeNewest + "-" + timeOldest + "="+ diff1 );
            }
            nowTimeString = temp;
        }

        /* The first transition was a Falling Edge.
           It was already ON when the time window started */
        if(userRecords.size() > 0) {
            if (!userRecords.get(0).getState()) {
                String temp = String.valueOf(userRecords.get(0).getTimestamp());
                String minDateString = String.valueOf(minDate);
                Double diff = Utils.differenceBetweenDates(temp, minDateString);
                userTime += diff;
                LOG.info(temp + "-" + minDateString + "="+ diff);
            }
        }


        List<Double> result = new ArrayList<Double>();
        result.add(userTime);

        return CollectionResponse.<Double>builder().setItems(result).build();
    }

    @ApiMethod(name = "getUsersStatsOfAllDevices", path = "get_users_stats_all_devices")
    public CollectionResponse<DeviceUsageRecord> getUsersStatsOfAllDevices () {

        LOG.info("Calling getUserStatsOfAllDevices method");

        List<NfcRecord> devices = ofy().load().type(NfcRecord.class).list();
        List<RegistrationRecord> users = ofy().load().type(RegistrationRecord.class).list();
        List<DeviceUsageRecord> usageRecords = new ArrayList<DeviceUsageRecord>();
        List<Integer> timespans = new ArrayList<Integer>();
        timespans.add(Constants.TODAY);
        timespans.add(Constants.WEEK);
        timespans.add(Constants.MONTH);

        LOG.info("Devices: " + devices.size() + " Users: " + users.size());
        RegistrationRecord anyUserRegistrationRecord = new RegistrationRecord();
        anyUserRegistrationRecord.setUsername(Constants.ANYUSER);
        users.add(anyUserRegistrationRecord);

        for (RegistrationRecord user : users){
            for (NfcRecord device : devices){
                   for (Integer timespan : timespans){
                       String minTimestamp = Utils.getMinTimestamp(timespan);
                       String maxTimestamp = Utils.getCurrentTimestamp();


                       Collection<Double> userTimeResponse = getSingleUserDeviceTime(
                               device.getNfcId(),
                               user.getUsername(),
                               minTimestamp,
                               maxTimestamp).getItems();
                       Double userTime = Iterables.get(userTimeResponse, 0);

                       DeviceUsageRecord deviceUsageRecord = new DeviceUsageRecord();
                       deviceUsageRecord.setUsername(user.getUsername());
                       deviceUsageRecord.setDeviceId(device.getNfcId());
                       deviceUsageRecord.setDeviceType(device.getType());
                       deviceUsageRecord.setTimespan(timespan);
                       deviceUsageRecord.setUserTime(userTime);

                       usageRecords.add(deviceUsageRecord);

                       if (userTime > 0) {
                           LOG.warning("Stats of: "
                                   + user.getUsername() + " "
                                   + device.getNfcId() + "-" + device.getType() + " "
                                   + "timeSpan: " + timespan + " "
                                   + "from: " + minTimestamp + " "
                                   + "to: " + maxTimestamp + " "
                                   + "userTime: " + userTime);
                       }
                   }
            }
        }

        return CollectionResponse.<DeviceUsageRecord>builder().setItems(usageRecords).build();
    }


    /**
     * This inserts a new <code>NfcLogRecord</code> object.
     * @param nfcLogRecord The object to be added.
     * @return The object to be added.
     */
    @ApiMethod(name = "insertDeviceLog")
    public NfcLogRecord insertDeviceLog(NfcLogRecord nfcLogRecord) throws ConflictException {
        // Implement this function

        LOG.info("Calling insertDeviceLog method");

        Long id = nfcLogRecord.getId();
        if (id != null) {
            if (findRecord(id) != null) {
                throw new ConflictException("Object already exists");
            }
        }


        if (!nfcLogRecord.getUser().equals(Constants.ANYUSER)) {
            String stringTS = Utils.getCurrentTimestamp();
            nfcLogRecord.setTimestamp(stringTS);
        }

        //Since our @Id field is a Long, Objectify will generate a unique value for us
        //when we use put
        ofy().save().entity(nfcLogRecord).now();

        return nfcLogRecord;
    }


    /**
     * This deletes an existing <code>NfcLogRecord</code> object.
     * @param id The id of the object to be deleted.
     */
    @ApiMethod(name = "deleteDeviceLog")
    public void deleteDeviceLog(@Named("id") Long id) throws NotFoundException {

        LOG.info("Calling deleteNFC method");
        NfcLogRecord record = findRecord(id);
        if(record == null) {
            throw new NotFoundException("NFC Log Record does not exist");
        }
        ofy().delete().entity(record).now();
    }

    private NfcLogRecord findRecord(Long id) {
        return ofy().load().type(NfcLogRecord.class).id(id).now();
    }
}