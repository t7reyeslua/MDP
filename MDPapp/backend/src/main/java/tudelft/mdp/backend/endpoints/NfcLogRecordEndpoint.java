package tudelft.mdp.backend.endpoints;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.ConflictException;
import com.google.api.server.spi.response.NotFoundException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Named;

import tudelft.mdp.backend.Constants;
import tudelft.mdp.backend.Utils;
import tudelft.mdp.backend.records.NfcLogRecord;
import tudelft.mdp.backend.records.NfcRecord;

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
    public CollectionResponse<NfcLogRecord> listDeviceLogByDate(@Named("nfcId") String nfcId, @Named("minDate") Double minDate, @Named("maxDate") Double maxDate) {

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
    public CollectionResponse<NfcLogRecord> listUserDeviceLogByDate(@Named("user") String user, @Named("minDate") Double minDate, @Named("maxDate") Double maxDate) {

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
            @Named("minDate") Double minDate,
            @Named("maxDate") Double maxDate) {

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

    @ApiMethod(name = "getUserStatsOfDevice", path = "get_user_stats_device")
    public CollectionResponse<Double> getUserStatsOfDevice (
            @Named("nfcId") String nfcId,
            @Named("user") String user,
            @Named("minDate") Double minDate,
            @Named("maxDate") Double maxDate) {

        LOG.info("Calling getUserStatsOfDevice method");

        Double totalTime = 0.0;
        Double totalPower = 0.0;
        Double userTime = 0.0;
        Double userPower = 0.0;
        Double percentage = 0.0;
        Double userStatus = 0.0;

        List<NfcLogRecord> userRecords = new ArrayList<NfcLogRecord>(listUserDeviceLogByDateDevice(nfcId,
                user, minDate, maxDate).getItems());

        String nowTimeString = Utils.getCurrentTimestamp();
        Double nowTime = Double.valueOf(nowTimeString);

        /* ========================================================*/
        /* Calculate the time the user has made use of the device */
        for (int i = userRecords.size() - 1; i >= 0; i--) {

            /* An ON step is detected -> Measure how long it lasted */
            if (userRecords.get(i).getState()){
                userTime += (nowTime - userRecords.get(i).getTimestamp());
            }
            nowTime = userRecords.get(i).getTimestamp();
        }

        /* The first transition was a Falling Edge.
           It was already ON when the time window started */
        if(userRecords.size() > 0) {
            if (!userRecords.get(0).getState()) {
                userTime += (userRecords.get(0).getTimestamp() - minDate);
            }
        }


        /* ========================================================*/
        /* Calculate the time the device has been used by all users */

       List<NfcLogRecord> deviceRecords = new ArrayList<NfcLogRecord>(listUserDeviceLogByDateDevice(nfcId,
               Constants.ANYUSER, minDate, maxDate).getItems());

        nowTime = Double.valueOf(nowTimeString);

        /* Calculate the time the user has made use of the device */
        for (int i = deviceRecords.size() - 1; i >= 0; i--) {

            /* An ON step is detected -> Measure how long it lasted */
            if (deviceRecords.get(i).getState()){
                totalTime += (nowTime - deviceRecords.get(i).getTimestamp());
            }
            nowTime = deviceRecords.get(i).getTimestamp();
        }

        /* The first transition was a Falling Edge.
           It was already ON when the time window started */
        if (deviceRecords.size() > 0) {
            if (!deviceRecords.get(0).getState()) {
                totalTime += (deviceRecords.get(0).getTimestamp() - minDate);
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


        List<Double> result = new ArrayList<Double>();
        result.add(totalTime);
        result.add(userTime);
        result.add(totalPower);
        result.add(userPower);
        result.add(percentage);
        result.add(userStatus);

        return CollectionResponse.<Double>builder().setItems(result).build();
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
            nfcLogRecord.setTimestamp(Double.valueOf(stringTS));
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