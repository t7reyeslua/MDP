package tudelft.mdp.backend.endpoints;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Named;

import tudelft.mdp.backend.Utils;
import tudelft.mdp.backend.records.LocationLogRecord;

import static tudelft.mdp.backend.OfyService.ofy;

/** An endpoint class we are exposing */
@Api(name = "locationLogEndpoint",
        description = "An API to manage the location history of each user",
        version = "v1",
        namespace = @ApiNamespace(ownerDomain = "endpoints.backend.mdp.tudelft",
        ownerName = "endpoints.backend.mdp.tudelft", packagePath=""))
public class LocationLogRecordEndpoint {


    private static final Logger LOG = Logger.getLogger(LocationLogRecordEndpoint.class.getName());


    @ApiMethod(name = "insertLocationLogRecord", path = "insert_location_record")
    public LocationLogRecord insertLocationLogRecord(LocationLogRecord locationLogRecord) {

        LOG.info("Calling insertLocationLogRecord method");

        locationLogRecord.setTimestamp(Double.valueOf(Utils.getCurrentTimestamp()));
        ofy().save().entity(locationLogRecord).now();

        return locationLogRecord;
    }

    @ApiMethod(name = "insertLocationLogRecordTimestamp", path = "insert_location_record_timestamp")
    public LocationLogRecord insertLocationLogRecordTimestamp(LocationLogRecord locationLogRecord, @Named("timestamp") String timestamp) {

        LOG.info("Calling insertLocationLogRecordTimestamp method");

        locationLogRecord.setTimestamp(Double.valueOf(timestamp));
        ofy().save().entity(locationLogRecord).now();

        return locationLogRecord;
    }

    @ApiMethod(name = "getServerTimestamp", path = "get_server_timestamp")
    public LocationLogRecord getServerTimestamp() {

        LOG.info("Calling getServerTimestamp method");

        LocationLogRecord locationLogRecord = new LocationLogRecord();
        locationLogRecord.setTimestamp(Double.valueOf(Utils.getCurrentTimestamp()));

        return locationLogRecord;
    }


    @ApiMethod(name = "listLocationLogByUser", path = "list_location_user")
    public CollectionResponse<LocationLogRecord> listLocationLogByUser(@Named("user") String user) {

        LOG.info("Calling listLocationLogByUser method");

        List<LocationLogRecord> records= ofy().load().type(LocationLogRecord.class)
                .filter("user", user)
                .list();

        records = sortByTimestamp(records);

        LOG.info("Records:" + records.size());

        return CollectionResponse.<LocationLogRecord>builder().setItems(records).build();
    }

    @ApiMethod(name = "listLocationLogByUserDate", path = "list_location_user_date")
    public CollectionResponse<LocationLogRecord> listLocationLogByUserDate(
            @Named("user") String user,
            @Named("minDate") Double minDate,
            @Named("maxDate") Double maxDate) {

        LOG.info("Calling listLocationLogByUserDate method");

        List<LocationLogRecord> records= ofy().load().type(LocationLogRecord.class)
                .filter("user", user)
                .filter("timestamp >=", minDate)
                .filter("timestamp <=", maxDate)
                .list();

        records = sortByTimestamp(records);
        LOG.info("Records:" + records.size());

        return CollectionResponse.<LocationLogRecord>builder().setItems(records).build();
    }

    @ApiMethod(name = "listLocationLogByUserDateZone", path = "list_location_user_date_zone")
    public CollectionResponse<LocationLogRecord> listLocationLogByUserDateZone(
            @Named("user") String user,
            @Named("place") String place,
            @Named("zone") String zone,
            @Named("minDate") Double minDate,
            @Named("maxDate") Double maxDate) {

        LOG.info("Calling listLocationLogByUserDateZone method");

        List<LocationLogRecord> records= ofy().load().type(LocationLogRecord.class)
                .filter("user", user)
                .filter("place", place)
                .filter("zone", zone)
                .filter("timestamp >=", minDate)
                .filter("timestamp <=", maxDate)
                .list();

        records = sortByTimestamp(records);
        LOG.info("Records:" + records.size());

        return CollectionResponse.<LocationLogRecord>builder().setItems(records).build();
    }

    @ApiMethod(name = "listLocationLogByUserDateZoneProb", path = "list_location_user_date_zone_prob")
    public CollectionResponse<LocationLogRecord> listLocationLogByUserDateZoneProb(
            @Named("user") String user,
            @Named("place") String place,
            @Named("zone") String zone,
            @Named("probability") Double probability,
            @Named("minDate") Double minDate,
            @Named("maxDate") Double maxDate) {

        LOG.info("Calling listLocationLogByUserDateZoneProb method");

        List<LocationLogRecord> records=  new ArrayList<LocationLogRecord>(listLocationLogByUserDateZone(
                user, place, zone, minDate, maxDate).getItems());

        List<LocationLogRecord> result = new ArrayList<LocationLogRecord>();
        for (LocationLogRecord record : records){
            if (record.getProbability() >= probability){
                result.add(record);
            }
        }


        LOG.info("Records result:" + result.size());

        return CollectionResponse.<LocationLogRecord>builder().setItems(result).build();
    }

    @ApiMethod(name = "listLocationLogByDateZoneProb", path = "list_location_date_zone_prob")
    public CollectionResponse<LocationLogRecord> listLocationLogByDateZoneProb(
            @Named("place") String place,
            @Named("zone") String zone,
            @Named("probability") Double probability,
            @Named("minDate") Double minDate,
            @Named("maxDate") Double maxDate) {

        LOG.info("Calling listLocationLogByDateZoneProb method");

        List<LocationLogRecord> records=  new ArrayList<LocationLogRecord>(listLocationLogByDateZone(
                place, zone, minDate, maxDate).getItems());

        List<LocationLogRecord> result = new ArrayList<LocationLogRecord>();
        for (LocationLogRecord record : records){
            if (record.getProbability() >= probability){
                result.add(record);
            }
        }
        LOG.info("Records result :" + result.size());

        return CollectionResponse.<LocationLogRecord>builder().setItems(result).build();
    }

    @ApiMethod(name = "listLocationLogByDateZone", path = "list_location_date_zone")
    public CollectionResponse<LocationLogRecord> listLocationLogByDateZone(
            @Named("place") String place,
            @Named("zone") String zone,
            @Named("minDate") Double minDate,
            @Named("maxDate") Double maxDate) {

        LOG.info("Calling listLocationLogByDateZone method");

        List<LocationLogRecord> records= ofy().load().type(LocationLogRecord.class)
                .filter("place", place)
                .filter("zone", zone)
                .filter("timestamp >=", minDate)
                .filter("timestamp <=", maxDate)
                .list();

        records = sortByTimestamp(records);

        LOG.info("Records:" + records.size());

        return CollectionResponse.<LocationLogRecord>builder().setItems(records).build();
    }

    @ApiMethod(name = "listLocationLogByZone", path = "list_location_zone")
    public CollectionResponse<LocationLogRecord> listLocationLogByZone(
            @Named("place") String place,
            @Named("zone") String zone) {

        LOG.info("Calling listLocationLogByZone method");

        List<LocationLogRecord> records= ofy().load().type(LocationLogRecord.class)
                .filter("place", place)
                .filter("zone", zone)
                .list();

        records = sortByTimestamp(records);

        LOG.info("Records:" + records.size());

        return CollectionResponse.<LocationLogRecord>builder().setItems(records).build();
    }



    private List<LocationLogRecord> sortByTimestamp(List<LocationLogRecord> unsortedList){
        Collections.sort(unsortedList, new Comparator<LocationLogRecord>() {
            @Override
            public int compare(LocationLogRecord item1, LocationLogRecord item2) {

                return item1.getTimestamp().compareTo(item2.getTimestamp());
            }
        });

        return unsortedList;
    }


}