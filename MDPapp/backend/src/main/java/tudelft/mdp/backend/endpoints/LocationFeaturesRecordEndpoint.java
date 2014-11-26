package tudelft.mdp.backend.endpoints;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.datastore.Text;

import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.cmd.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.inject.Named;

import tudelft.mdp.backend.OfyService;
import tudelft.mdp.backend.records.LocationFeaturesRecord;
import tudelft.mdp.backend.records.LocationFeaturesRecordWrapper;
import tudelft.mdp.backend.weka.WekaUtils;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * WARNING: This generated code is intended as a sample or starting point for using a
 * Google Cloud Endpoints RESTful API with an Objectify entity. It provides no data access
 * restrictions and no data validation.
 * <p/>
 * DO NOT deploy this code unchanged as part of a real application to real users.
 */
@Api(
        name = "locationFeaturesRecordApi",
        version = "v1",
        description = "An API to manage the records containing the weka location features",
        resource = "locationFeaturesRecord",
        namespace = @ApiNamespace(
                ownerDomain = "endpoints.backend.mdp.tudelft",
                ownerName = "endpoints.backend.mdp.tudelft",
                packagePath = ""
        )
)
public class LocationFeaturesRecordEndpoint {

    private static final Logger logger = Logger
            .getLogger(LocationFeaturesRecordEndpoint.class.getName());

    private static final int DEFAULT_LIST_LIMIT = 20;

    /*
    static {
        // Typically you would register this inside an OfyService wrapper. See: https://code.google.com/p/objectify-appengine/wiki/BestPractices
        ObjectifyService.register(LocationFeaturesRecord.class);
    }*/

    /**
     * Returns the {@link LocationFeaturesRecord} with the corresponding ID.
     *
     * @param id the ID of the entity to be retrieved
     * @return the entity with the corresponding ID
     * @throws NotFoundException if there is no {@code LocationFeaturesRecord} with the provided
     *                           ID.
     */
    @ApiMethod(
            name = "get",
            path = "locationFeaturesRecord/{id}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public LocationFeaturesRecord get(@Named("id") Long id) throws NotFoundException {
        logger.info("Getting LocationFeaturesRecord with ID: " + id);
        LocationFeaturesRecord locationFeaturesRecord = ofy().load()
                .type(LocationFeaturesRecord.class).id(id).now();
        if (locationFeaturesRecord == null) {
            throw new NotFoundException("Could not find LocationFeaturesRecord with ID: " + id);
        }
        return locationFeaturesRecord;
    }

    /**
     * Inserts a new {@code LocationFeaturesRecord}.
     */
    @ApiMethod(
            name = "insert",
            path = "locationFeaturesRecord",
            httpMethod = ApiMethod.HttpMethod.POST)
    public LocationFeaturesRecord insert(LocationFeaturesRecord locationFeaturesRecord) {
        ofy().save().entity(locationFeaturesRecord).now();
        logger.info("Created LocationFeaturesRecord.");

        return ofy().load().entity(locationFeaturesRecord).now();
    }

    @ApiMethod(
            name = "insertBulk",
            path = "locationFeaturesRecordBulk",
            httpMethod = ApiMethod.HttpMethod.POST)
    public void insertBulk(LocationFeaturesRecordWrapper locationFeaturesRecords) {
        logger.info("Created LocationFeaturesRecord BULK." + locationFeaturesRecords.getLocationFeaturesRecords().size());
        for (LocationFeaturesRecord locationFeaturesRecord : locationFeaturesRecords.getLocationFeaturesRecords()) {
            ofy().save().entity(locationFeaturesRecord).now();
        }
    }

    /**
     * Updates an existing {@code LocationFeaturesRecord}.
     *
     * @param id                     the ID of the entity to be updated
     * @param locationFeaturesRecord the desired state of the entity
     * @return the updated version of the entity
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code LocationFeaturesRecord}
     */
    @ApiMethod(
            name = "update",
            path = "locationFeaturesRecord/{id}",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public LocationFeaturesRecord update(@Named("id") Long id,
            LocationFeaturesRecord locationFeaturesRecord) throws NotFoundException {
        // TODO: You should validate your ID parameter against your resource's ID here.
        checkExists(id);
        ofy().save().entity(locationFeaturesRecord).now();
        logger.info("Updated LocationFeaturesRecord: " + locationFeaturesRecord);
        return ofy().load().entity(locationFeaturesRecord).now();
    }

    /**
     * Deletes the specified {@code LocationFeaturesRecord}.
     *
     * @param id the ID of the entity to delete
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code LocationFeaturesRecord}
     */
    @ApiMethod(
            name = "remove",
            path = "locationFeaturesRecord/{id}",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void remove(@Named("id") Long id) throws NotFoundException {
        checkExists(id);
        ofy().delete().type(LocationFeaturesRecord.class).id(id).now();
        logger.info("Deleted LocationFeaturesRecord with ID: " + id);
    }

    @ApiMethod(
            name = "removeAll",
            path = "remove_all_location_feature_records")
    public void removeAll() throws NotFoundException {
        List<LocationFeaturesRecord> records= OfyService.ofy().load().type(LocationFeaturesRecord.class)
                .list();
        for (LocationFeaturesRecord record : records){
            remove(record.getId());
        }
    }


    /**
     * List all entities.
     *
     * @param cursor used for pagination to determine which page to return
     * @param limit  the maximum number of entries to return
     * @return a response that encapsulates the result list and the next page token/cursor
     */
    @ApiMethod(
            name = "list",
            path = "locationFeaturesRecord",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<LocationFeaturesRecord> list(@Nullable @Named("cursor") String cursor,
            @Nullable @Named("limit") Integer limit) {
        limit = limit == null ? DEFAULT_LIST_LIMIT : limit;
        Query<LocationFeaturesRecord> query = ofy().load().type(LocationFeaturesRecord.class)
                .limit(limit);
        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }
        QueryResultIterator<LocationFeaturesRecord> queryIterator = query.iterator();
        List<LocationFeaturesRecord> locationFeaturesRecordList
                = new ArrayList<LocationFeaturesRecord>(limit);
        while (queryIterator.hasNext()) {
            locationFeaturesRecordList.add(queryIterator.next());
        }
        return CollectionResponse.<LocationFeaturesRecord>builder()
                .setItems(locationFeaturesRecordList)
                .setNextPageToken(queryIterator.getCursor().toWebSafeString()).build();
    }

    private void checkExists(Long id) throws NotFoundException {
        try {
            ofy().load().type(LocationFeaturesRecord.class).id(id).safe();
        } catch (com.googlecode.objectify.NotFoundException e) {
            throw new NotFoundException("Could not find LocationFeaturesRecord with ID: " + id);
        }
    }

    @ApiMethod(name = "listLocationFeatures", path = "list_location_features")
    public CollectionResponse<LocationFeaturesRecord> listLocationFeatures(
            @Named("minDate") String minDate,
            @Named("maxDate") String maxDate) {

        logger.info("Calling listLocationFeatures method");

        List<LocationFeaturesRecord> records= OfyService.ofy().load().type(LocationFeaturesRecord.class)
                .filter("timestamp >=", minDate)
                .filter("timestamp <=", maxDate)
                .order("timestamp")
                .list();

        //records = sortByTimestamp(records);
        logger.info("Records:" + records.size());

        return CollectionResponse.<LocationFeaturesRecord>builder().setItems(records).build();
    }

    @ApiMethod(name = "listLocationFeaturesByPlace", path = "list_location_features_by_place")
    public CollectionResponse<LocationFeaturesRecord> listLocationFeaturesByPlace(
            @Named("minDate") String minDate,
            @Named("maxDate") String maxDate,
            @Named("place") String place) {

        logger.info("Calling listLocationFeaturesByPlace method");

        List<LocationFeaturesRecord> records= OfyService.ofy().load().type(LocationFeaturesRecord.class)
                .filter("timestamp >=", minDate)
                .filter("timestamp <=", maxDate)
                .filter("place", place)
                .order("timestamp")
                .list();

        //records = sortByTimestamp(records);
        logger.info("Records:" + records.size());

        return CollectionResponse.<LocationFeaturesRecord>builder().setItems(records).build();
    }

    @ApiMethod(name = "listLocationFeaturesByPlaceFiltered", path = "list_location_features_by_place_filtered")
    public CollectionResponse<LocationFeaturesRecord> listLocationFeaturesByPlaceFiltered(
            @Named("minDate") String minDate,
            @Named("maxDate") String maxDate,
            @Named("place") String place,
            @Named("zone") String zones) {

        logger.info("Calling listLocationFeaturesByPlaceFiltered method from: " + minDate + " to " + maxDate + " of " + zones + " in " + place);

        String[] filteredZones = zones.split(",");

        List<LocationFeaturesRecord> records = new ArrayList<LocationFeaturesRecord>();
        for (String zone : filteredZones) {
            List<LocationFeaturesRecord> zoneRecords = OfyService.ofy().load().type(
                    LocationFeaturesRecord.class)
                    .filter("place", place)
                    .filter("zone", zone)
                    .filter("timestamp >=", minDate)
                    .filter("timestamp <=", maxDate)
                    .order("timestamp")
                    .list();

            records.addAll(zoneRecords);
            logger.info("Records of " + zone +  " in " + place +":" + zoneRecords.size());
        }

        logger.info("Records:" + records.size());


        return CollectionResponse.<LocationFeaturesRecord>builder().setItems(records).build();
    }

    @ApiMethod(name = "createWekaObjects", path = "create_weka_objects")
    public LocationFeaturesRecord createWekaObjects(
            @Named("minDate") String minDate,
            @Named("maxDate") String maxDate,
            @Named("place") String place) {

        logger.info("Calling createWekaObjects method from: " + minDate + " to " + maxDate + " of " + place);

        List<LocationFeaturesRecord> records= OfyService.ofy().load().type(
                LocationFeaturesRecord.class)
                .filter("timestamp >=", minDate)
                .filter("timestamp <=", maxDate)
                .filter("place", place)
                .order("timestamp")
                .list();

        logger.info("Records:" + records.size());

        WekaUtils wekaUtils = new WekaUtils();
        ArrayList<String> filesCreated = wekaUtils.createInstanceSetLocation(records, minDate, maxDate, place);
        LocationFeaturesRecord response = new LocationFeaturesRecord();

        String files = "";
        for (String file : filesCreated){
            files += file + "|";
        }
        response.setPlace(files);
        return response;
    }

    @ApiMethod(name = "createWekaObjectsFilterZones", path = "create_weka_objects_filter")
    public LocationFeaturesRecord createWekaObjectsFilterZones(
            @Named("minDate") String minDate,
            @Named("maxDate") String maxDate,
            @Named("place") String place,
            @Named("blacklist") String blacklistZones) {

        logger.info("Calling createWekaObjectsFilterZones method from: " + minDate + " to " + maxDate + " of " + place + " without " + blacklistZones);

        List<LocationFeaturesRecord> records= OfyService.ofy().load().type(
                LocationFeaturesRecord.class)
                .filter("timestamp >=", minDate)
                .filter("timestamp <=", maxDate)
                .filter("place", place)
                .order("timestamp")
                .list();

        logger.info("Records:" + records.size());

        String[] filteredZones = blacklistZones.split(",");
        ArrayList<String> filter = new ArrayList<String>(Arrays.asList(filteredZones));

        List<LocationFeaturesRecord> recordsFiltered= new ArrayList<LocationFeaturesRecord>();
        for (LocationFeaturesRecord locationFeaturesRecord : records){
            if (!filter.contains(locationFeaturesRecord.getZone())){
                recordsFiltered.add(locationFeaturesRecord);
            }
        }
        logger.info("Records filtered:" + recordsFiltered.size());




        WekaUtils wekaUtils = new WekaUtils();
        ArrayList<String> filesCreated = wekaUtils.createInstanceSetLocation(recordsFiltered, minDate, maxDate, place + " without " + blacklistZones);
        LocationFeaturesRecord response = new LocationFeaturesRecord();

        String files = "";
        for (String file : filesCreated){
            files += file + "|";
        }
        response.setPlace(files);
        return response;
    }

    private ArrayList<String> getClassAttributesSTR(
            String minDate,
            String maxDate, String place){

        List<LocationFeaturesRecord> records= OfyService.ofy().load().type(
                LocationFeaturesRecord.class)
                .filter("timestamp >=", minDate)
                .filter("timestamp <=", maxDate)
                .filter("place", place)
                .order("timestamp")
                .list();

        logger.info("Records:" + records.size());
        HashSet<String> hashSet = new HashSet<String>();
        for (LocationFeaturesRecord locationFeaturesRecord : records){
            hashSet.add(locationFeaturesRecord.getPlace().replaceAll("\\s", "") +"-" +locationFeaturesRecord.getZone().replaceAll(
                    "\\s", ""));
        }
        logger.info("Distinct Records:" + hashSet.size());

        return new ArrayList<String>(hashSet);

    }

    @ApiMethod(name = "getClassAttributes", path = "get_class_attributes")
    public CollectionResponse<LocationFeaturesRecord> getClassAttributes(
            @Named("minDate") String minDate,
            @Named("maxDate") String maxDate,
            @Named("place") String place) {

        logger.info("Calling getClassAttributes method from: " + minDate + " to " + maxDate);

        ArrayList<String> classAttributes    = new ArrayList<String>(getClassAttributesSTR(minDate,maxDate,place));
        ArrayList<LocationFeaturesRecord> records = new ArrayList<LocationFeaturesRecord>();

        for (String s : classAttributes){
            LocationFeaturesRecord locationFeaturesRecord = new LocationFeaturesRecord();
            locationFeaturesRecord.setPlace(s);
            records.add(locationFeaturesRecord);
        }

        return CollectionResponse.<LocationFeaturesRecord>builder().setItems(records).build();
    }


    @ApiMethod(name = "testPermutations", path = "testPermutations")
    public LocationFeaturesRecord testPermutations(
            @Named("users") Integer users) {

        logger.info("Calling testPermutations method");

        WekaUtils wekaUtils = new WekaUtils();
        ArrayList<String> result  = wekaUtils.createOtherUsersEventInstances(users);

        Collections.sort(result);
        String temp = "";
        for (String s : result){
            temp += s + "\n";
        }
        Text text = new Text(temp);

        LocationFeaturesRecord locationFeaturesRecord = new LocationFeaturesRecord();
        locationFeaturesRecord.setLocationFeatures(text);

        return locationFeaturesRecord;
    }

    @ApiMethod(name = "testPermutationsMotion", path = "testPermutationsMotion")
    public LocationFeaturesRecord testPermutationsMotion(
            @Named("users") Integer users) {

        logger.info("Calling testPermutationsMotion method");

        WekaUtils wekaUtils = new WekaUtils();
        ArrayList<String> result  = wekaUtils.createOtherUsersEventInstancesMotion(users);

        //Collections.sort(result);
        String temp = "";
        for (String s : result){
            temp += s + "\n";
        }
        Text text = new Text(temp);

        LocationFeaturesRecord locationFeaturesRecord = new LocationFeaturesRecord();
        locationFeaturesRecord.setLocationFeatures(text);

        return locationFeaturesRecord;
    }


    @ApiMethod(name = "createMockArff", path = "createMockArff")
    public LocationFeaturesRecord createMockArff(
            @Named("users") Integer users) {

        logger.info("Calling createMockArff method");

        WekaUtils wekaUtils = new WekaUtils();
        String filename  = "http://storage.cloud.google.com/tudelft-mdp.appspot.com/" +  wekaUtils.createMockArff(users);

        Text text = new Text(filename);

        LocationFeaturesRecord locationFeaturesRecord = new LocationFeaturesRecord();
        locationFeaturesRecord.setLocationFeatures(text);

        return locationFeaturesRecord;
    }

    @ApiMethod(name = "createMockArffFiltered", path = "createMockArffFiltered")
    public LocationFeaturesRecord createMockArffFiltered(
            @Named("users") Integer users,
            @Named("filter") String filter) {

        logger.info("Calling createMockArffFiltered method");

        WekaUtils wekaUtils = new WekaUtils();
        ArrayList<String> filenames  = wekaUtils.createMockArffFiltered(users, filter);
        String filename = "";
        for (String file : filenames){
            filename += "http://storage.cloud.google.com/tudelft-mdp.appspot.com/" + file + " | ";
        }

        Text text = new Text(filename);

        LocationFeaturesRecord locationFeaturesRecord = new LocationFeaturesRecord();
        locationFeaturesRecord.setLocationFeatures(text);

        return locationFeaturesRecord;
    }

    @ApiMethod(name = "createMockArffFilteredTimestamp", path = "createMockArffFilteredTimestamp")
    public LocationFeaturesRecord createMockArffFilteredTimestamp(
            @Named("users") Integer users,
            @Named("filter") String filter) {

        logger.info("Calling createMockArffFilteredTimestamp method");

        WekaUtils wekaUtils = new WekaUtils();
        ArrayList<String> filenames  = wekaUtils.createMockArffFilteredTimestamp(users, filter);
        String filename = "";
        for (String file : filenames){
            filename += "http://storage.cloud.google.com/tudelft-mdp.appspot.com/" + file + " | ";
        }

        Text text = new Text(filename);

        LocationFeaturesRecord locationFeaturesRecord = new LocationFeaturesRecord();
        locationFeaturesRecord.setLocationFeatures(text);

        return locationFeaturesRecord;
    }


    @ApiMethod(name = "createMockArffAllIn1", path = "createMockArffAllIn1")
    public LocationFeaturesRecord createMockArffAllIn1(
            @Named("users") Integer users) {

        logger.info("Calling createMockArffAllIn1 method");

        WekaUtils wekaUtils = new WekaUtils();
        String filename  = "http://storage.cloud.google.com/tudelft-mdp.appspot.com/" +  wekaUtils.createMockArffAllIn1(users);

        Text text = new Text(filename);

        LocationFeaturesRecord locationFeaturesRecord = new LocationFeaturesRecord();
        locationFeaturesRecord.setLocationFeatures(text);

        return locationFeaturesRecord;
    }


}