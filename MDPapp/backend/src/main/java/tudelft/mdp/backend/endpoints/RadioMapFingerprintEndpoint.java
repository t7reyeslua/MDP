package tudelft.mdp.backend.endpoints;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.NotFoundException;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Named;

import tudelft.mdp.backend.OfyService;
import tudelft.mdp.backend.enums.Constants;
import tudelft.mdp.backend.Utils;
import tudelft.mdp.backend.records.ApGaussianRecord;
import tudelft.mdp.backend.records.ApHistogramRecord;
import tudelft.mdp.backend.records.ApHistogramRecordWrapper;
import tudelft.mdp.backend.records.LocationFeaturesRecord;
import tudelft.mdp.backend.records.LocationFingerprintRecord;
import tudelft.mdp.backend.records.LocationFingerprintRecordWrapper;

import static tudelft.mdp.backend.OfyService.ofy;

/** An endpoint class we are exposing */
@Api(name = "radioMapFingerprintEndpoint",
        description = "An API to manage the Histogram and Gaussian info for each AP",
        version = "v1",
        namespace = @ApiNamespace(ownerDomain = "endpoints.backend.mdp.tudelft",
        ownerName = "endpoints.backend.mdp.tudelft",
        packagePath=""))
public class RadioMapFingerprintEndpoint {

    // Make sure to add this endpoint to your web.xml file if this is a web application.

    private static final Logger LOG = Logger.getLogger(RadioMapFingerprintEndpoint.class.getName());


    @ApiMethod(name = "listGaussiansAll", path = "list_gaussians_all")
    public CollectionResponse<ApGaussianRecord> listGaussiansAll() throws NotFoundException {

        LOG.info("Calling listGaussiansAll method");

        List<ApGaussianRecord> records = ofy().load().type(ApGaussianRecord.class)
                .order("zone")
                .order("mean")
                .list();

        if(records == null) {
            throw new NotFoundException("There are no records found with these criteria");
        }

        return CollectionResponse.<ApGaussianRecord>builder().setItems(records).build();
    }

    @ApiMethod(name = "listGaussiansInPlace", path = "list_gaussians_place")
    public CollectionResponse<ApGaussianRecord> listGaussiansInPlace(
            @Named("place") String place) throws NotFoundException {

        LOG.info("Calling listGaussiansInPlace method");

        List<ApGaussianRecord> records = ofy().load().type(ApGaussianRecord.class)
                .filter("place", place)
                .order("zone")
                .order("mean")
                .list();

        if(records == null) {
            throw new NotFoundException("There are no records found with these criteria");
        }

        return CollectionResponse.<ApGaussianRecord>builder().setItems(records).build();
    }


    @ApiMethod(name = "listGaussiansInZone", path = "list_gaussians_zone")
    public CollectionResponse<ApGaussianRecord> listGaussiansInZone(
            @Named("place") String place,
            @Named("zone") String zone) throws NotFoundException {

        LOG.info("Calling listGaussiansInZone method");

        List<ApGaussianRecord> records = ofy().load().type(ApGaussianRecord.class)
                .filter("place", place)
                .filter("zone", zone)
                .order("mean")
                .list();

        if(records == null) {
            throw new NotFoundException("There are no records found with these criteria");
        }

        return CollectionResponse.<ApGaussianRecord>builder().setItems(records).build();
    }


    @ApiMethod(name = "listAPGaussianInZone", path = "list_apgaussian_zone")
    public CollectionResponse<ApGaussianRecord> listAPGaussianInZone(
            @Named("bssid") String bssid,
            @Named("place") String place,
            @Named("zone") String zone) {

        LOG.info("Calling listAPGaussianInZone method");

        List<ApGaussianRecord> records= ofy().load().type(ApGaussianRecord.class)
                .filter("bssid", bssid)
                .filter("place", place)
                .filter("zone", zone)
                .order("mean")
                .list();

        if(records == null) {
            return null;
        }

        return CollectionResponse.<ApGaussianRecord>builder().setItems(records).build();
    }


    @ApiMethod(name = "listHistogramInZone", path = "list_histograms_zone")
     public CollectionResponse<ApHistogramRecord> listHistogramInZone(
            @Named("place") String place,
            @Named("zone") String zone) throws NotFoundException {

        LOG.info("Calling listHistogramInZone method");

        List<ApHistogramRecord> records = ofy().load().type(ApHistogramRecord.class)
                .filter("place", place)
                .filter("zone", zone)
                .order("zone")
                .order("rssi")
                .list();

        if(records == null) {
            throw new NotFoundException("There are no records found with these criteria");
        }

        return CollectionResponse.<ApHistogramRecord>builder().setItems(records).build();
    }

    @ApiMethod(name = "getApHistogramRssiCountInZone", path = "get_histogram_zone")
    public ApHistogramRecord getApHistogramRssiCountInZone(
            @Named("bssid") String bssid,
            @Named("place") String place,
            @Named("zone") String zone,
            @Named("rssi") Double rssi) {

        LOG.info("Calling getApHistogramInZone method");

        ApHistogramRecord result = null;

        List<ApHistogramRecord> records = ofy().load().type(ApHistogramRecord.class)
                .filter("bssid", bssid)
                .filter("place", place)
                .filter("zone", zone)
                .filter("rssi", rssi)
                .order("zone")
                .list();

        if (records.size() > 0) {
            result = records.get(records.size() - 1);
        }

        return result;

    }

    @ApiMethod(name = "increaseApHistogramRssiCountInZone", path = "increase_histogram_zone")
    public ApHistogramRecord increaseApHistogramRssiCountInZone(ApHistogramRecord apHistogramRecord) {

        LOG.info("Calling increaseApHistogramRssiCountInZone method");

        ApHistogramRecord currentApHistogramRecord = getApHistogramRssiCountInZone(
                apHistogramRecord.getBssid(),
                apHistogramRecord.getPlace(),
                apHistogramRecord.getZone(),
                apHistogramRecord.getRssi()
        );

        if (currentApHistogramRecord != null) {
            currentApHistogramRecord.setCount(
                    currentApHistogramRecord.getCount() + apHistogramRecord.getCount()
            );
        } else {
            apHistogramRecord.setId(null);
            currentApHistogramRecord = apHistogramRecord;
        }

        ofy().save().entity(currentApHistogramRecord).now();

        return currentApHistogramRecord;
    }

    @ApiMethod(name = "increaseApHistogramRssiCountInZoneBulk", path = "increase_histogram_zone_bulk")
    public void increaseApHistogramRssiCountInZoneBulk(ApHistogramRecordWrapper apHistogramRecordWrapper) {

        LOG.info("Calling increaseApHistogramRssiCountInZoneBulk method."
                + "Records to update: " + apHistogramRecordWrapper.getLocalHistogram().size());

        for (ApHistogramRecord apHistogramRecord : apHistogramRecordWrapper.getLocalHistogram()){

            LOG.info("Network to update: " +
                    apHistogramRecord.getSsid()  + "|" +
                    apHistogramRecord.getBssid() + "|" +
                    apHistogramRecord.getRssi()  + "|" +
                    apHistogramRecord.getCount());

            increaseApHistogramRssiCountInZone(apHistogramRecord);

        }
    }


    @ApiMethod(name = "calculateZoneGaussians", path = "calculate_zone_gaussians")
    public ApGaussianRecord calculateZoneGaussians(
            @Named("place") String place,
            @Named("zone") String zone) throws NotFoundException {

        LOG.info("Calling calculateZoneGaussians method");

        //Request the histogram of all APs in this zone
        List<ApHistogramRecord> records = ofy().load().type(ApHistogramRecord.class)
                .filter("place", place)
                .filter("zone", zone)
                .order("zone")
                .order("rssi")
                .list();

        CollectionResponse<ApHistogramRecord> recordCollectionResponse =  CollectionResponse.<ApHistogramRecord>builder().setItems(records).build();

        ArrayList<ApHistogramRecord> zoneHistograms =
                new ArrayList<ApHistogramRecord>(recordCollectionResponse.getItems());

        //Identify existing APs in the zone
        ArrayList<String> zoneAPs = new ArrayList<String>();
        for (ApHistogramRecord record : zoneHistograms) {
            String AP = record.getBssid();
            if (!zoneAPs.contains(AP)){
                zoneAPs.add(AP);
            }
        }

        //Divide full histogram list into the corresponding histograms depending on the AP
        ArrayList<ArrayList<ApHistogramRecord>> apHistograms = new ArrayList<ArrayList<ApHistogramRecord>>();
        for (String AP : zoneAPs){
            ArrayList<ApHistogramRecord> apHistogram = new ArrayList<ApHistogramRecord>();
            for (ApHistogramRecord record : zoneHistograms){
                if (record.getBssid().equals(AP)){
                    apHistogram.add(record);
                }
            }

            Collections.sort(apHistogram, new Comparator<ApHistogramRecord>() {
                @Override
                public int compare(ApHistogramRecord item1, ApHistogramRecord item2) {

                    return item1.getRssi().compareTo(item2.getRssi());
                }
            });

            apHistograms.add(apHistogram);
        }

        //Filter each AP histogram and calculate mean and std
        ArrayList<ApGaussianRecord> apGaussians = new ArrayList<ApGaussianRecord>();
        for (ArrayList<ApHistogramRecord> apHistogram : apHistograms){
            ArrayList<ApHistogramRecord> apHistogramTrimmed = alphaTrimmerFilter(apHistogram);

            ApGaussianRecord apGaussian = new ApGaussianRecord();
            apGaussian.setBssid(apHistogramTrimmed.get(0).getBssid());
            apGaussian.setSsid(apHistogramTrimmed.get(0).getSsid());
            apGaussian.setStd(Utils.getStd(apHistogramTrimmed));
            apGaussian.setMean(Utils.getMean(apHistogramTrimmed));
            apGaussian.setZone(zone);
            apGaussian.setPlace(place);
            apGaussian.setId(null);

            apGaussians.add(apGaussian);

            apHistogramTrimmed.clear();
            apHistogramTrimmed = null;
        }

        // Insert/Update Gaussians in Datastore
        for (ApGaussianRecord apGaussianRecord : apGaussians){
            insertApGaussianForZone(apGaussianRecord);
        }

        ofy().clear();
        return null;
    }


    @ApiMethod(name = "insertApGaussianForZone", path = "insert_apzone_gaussian")
    public ApGaussianRecord insertApGaussianForZone(ApGaussianRecord apGaussianRecord) {
        // Implement this function

        LOG.info("Calling insertApGaussianForZone method");

        ApGaussianRecord existingApGaussian = findRecord(apGaussianRecord);
        if (existingApGaussian != null) {
            apGaussianRecord.setId(existingApGaussian.getId());
        }

        //Since our @Id field is a Long, Objectify will generate a unique value for us
        //when we use put
        ofy().save().entity(apGaussianRecord).now();
        return apGaussianRecord;
    }

    private ApGaussianRecord findRecord(ApGaussianRecord apGaussianRecord) {

        ArrayList<ApGaussianRecord> records = new ArrayList<ApGaussianRecord>(listAPGaussianInZone(
                apGaussianRecord.getBssid(),
                apGaussianRecord.getPlace(),
                apGaussianRecord.getZone()).getItems());

        if (records.size() > 0){
            return records.get(0);
        } else {
            return null;
        }

    }

    private ArrayList<ApHistogramRecord> alphaTrimmerFilter(ArrayList<ApHistogramRecord> apHistogram) {
        Double alpha = Constants.ALPHA_TRIMMER_FILTER_COEFF;

        ArrayList<ApHistogramRecord> apHistogramOriginal = (ArrayList<ApHistogramRecord>)apHistogram.clone();

        int samples = 0;
        for (ApHistogramRecord record : apHistogramOriginal){
            samples += record.getCount();
        }

        Double trimmedTemp = Math.floor(samples * alpha);
        Integer elementsToFilter = trimmedTemp.intValue();

        //Trimm front end of array
        int index = 0;
        while (elementsToFilter > 0){
            int rssiCount = apHistogramOriginal.get(index).getCount();

            if (rssiCount >= elementsToFilter){
                apHistogramOriginal.get(index).setCount(rssiCount-elementsToFilter);
                elementsToFilter = 0;
            } else {
                apHistogramOriginal.get(index).setCount(0);
                index++;
                elementsToFilter = elementsToFilter - rssiCount;
            }
        }


        //Trimm back end of array
        index = apHistogramOriginal.size() - 1;
        elementsToFilter = trimmedTemp.intValue();
        while (elementsToFilter > 0){
            int rssiCount = apHistogramOriginal.get(index).getCount();

            if (rssiCount >= elementsToFilter){
                apHistogramOriginal.get(index).setCount(rssiCount-elementsToFilter);
                elementsToFilter = 0;
            } else {
                apHistogramOriginal.get(index).setCount(0);
                index--;
                elementsToFilter = elementsToFilter - rssiCount;
            }
        }


        return apHistogramOriginal;
    }

    @ApiMethod(name = "insertRawLocationFingerprintForZone", path = "insert_raw_location_fingerprint")
    public LocationFingerprintRecord insertRawLocationFingerprintForZone(LocationFingerprintRecord locationFingerprintRecord) {
        // Implement this function

        LOG.info("Calling insertRawLocationFingerprintForZone method");

        //Since our @Id field is a Long, Objectify will generate a unique value for us
        //when we use put
        ofy().save().entity(locationFingerprintRecord).now();
        return locationFingerprintRecord;
    }

    @ApiMethod(name = "insertRawLocationFingerprintForZoneBulk", path = "insert_raw_location_fingerprint_bulk")
    public void insertRawLocationFingerprintForZoneBulk(LocationFingerprintRecordWrapper locationFingerprintRecordWrapper) {
        LOG.info("Calling insertRawLocationFingerprintForZoneBulk method."
                + " \nRecords to insert: " + locationFingerprintRecordWrapper.getLocationFingerprintRecordWrapperArrayList().size());
        for (LocationFingerprintRecord locationFingerprintRecord : locationFingerprintRecordWrapper.getLocationFingerprintRecordWrapperArrayList()){
            //Since our @Id field is a Long, Objectify will generate a unique value for us
            //when we use put
            ofy().save().entity(locationFingerprintRecord).now();
        }
    }

    @ApiMethod(name = "listLocationFingerprintRecordAll", path = "list_location_fingerprint_raw_all")
    public CollectionResponse<LocationFingerprintRecord> listLocationFingerprintRecordAll() throws NotFoundException {

        LOG.info("Calling listLocationFingerprintRecordAll method");

        List<LocationFingerprintRecord> records = ofy().load().type(LocationFingerprintRecord.class)
                .list();

        if(records == null) {
            throw new NotFoundException("There are no records found with these criteria");
        }

        return CollectionResponse.<LocationFingerprintRecord>builder().setItems(records).build();
    }

    @ApiMethod(name = "deleteAllLocationFingerprintRaw", path = "delete_all_location_fingerprint_raw")
    public void deleteAllLocationFingerprintRaw(){
        LOG.info("Calling deleteAllLocationFingerprintRaw method");

        List<Key<LocationFingerprintRecord>> records = ofy().load().type(LocationFingerprintRecord.class).keys().list();

        LOG.info("Record keys:" + records.size());
        ofy().delete().keys(records).now();

    }
    /*

    private void checkExistsApGaussian(Long id) throws NotFoundException {
        try {
            ObjectifyService.ofy().load().type(ApGaussianRecord.class).id(id).safe();
        } catch (com.googlecode.objectify.NotFoundException e) {
            throw new NotFoundException("Could not find ApGaussianRecord with ID: " + id);
        }
    }

    @ApiMethod(
            name = "removeApGaussian",
            path = "apGaussianRecord/{id}",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void removeApGaussian(@Named("id") Long id) throws NotFoundException {
        checkExistsApGaussian(id);
        ObjectifyService.ofy().delete().type(ApGaussianRecord.class).id(id).now();
        LOG.info("Deleted ApGaussianRecord with ID: " + id);
    }

    @ApiMethod(
            name = "removeAllGaussians",
            path = "remove_all_gaussians}")
    public void removeAllGaussians() throws NotFoundException {
        List<ApGaussianRecord> records= OfyService.ofy().load().type(ApGaussianRecord.class)
                .list();
        for (ApGaussianRecord record : records){
            removeApGaussian(record.getId());
        }
    }
*/
    private void checkExistsApHistogram(Long id) throws NotFoundException {
        try {
            ObjectifyService.ofy().load().type(ApHistogramRecord.class).id(id).safe();
        } catch (com.googlecode.objectify.NotFoundException e) {
            throw new NotFoundException("Could not find ApHistogramRecord with ID: " + id);
        }
    }

    @ApiMethod(
            name = "removeHistogram",
            path = "apHistogramRecord/{id}",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void removeApHistogram(@Named("id") Long id) throws NotFoundException {
        checkExistsApHistogram(id);
        ObjectifyService.ofy().delete().type(ApHistogramRecord.class).id(id).now();
        LOG.info("Deleted ApHistogramRecord with ID: " + id);
    }

    @ApiMethod(
            name = "removeAllHistograms",
            path = "remove_all_histograms}")
    public void removeAllHistograms() throws NotFoundException {
        List<ApHistogramRecord> records= OfyService.ofy().load().type(ApHistogramRecord.class)
                .list();
        for (ApHistogramRecord record : records){
            removeApHistogram(record.getId());
        }
    }


}