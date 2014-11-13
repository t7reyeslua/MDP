package tudelft.mdp.backend.endpoints;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.inject.Named;

import tudelft.mdp.backend.Utils;
import tudelft.mdp.backend.records.DeviceMotionLocationRecord;
import tudelft.mdp.backend.records.NfcRecord;
import tudelft.mdp.backend.weka.WekaMethods;
import tudelft.mdp.backend.weka.WekaMotionUtils;
import weka.core.Instances;

import static tudelft.mdp.backend.OfyService.ofy;

/** An endpoint class we are exposing */
@Api(name = "deviceMotionLocationRecordEndpoint",
        description = "An API to manage the records containing location and motion info related to a specific device",
        version = "v1",
        namespace = @ApiNamespace(ownerDomain = "endpoints.backend.mdp.tudelft",
        ownerName = "endpoints.backend.mdp.tudelft",
        packagePath=""))
public class DeviceMotionLocationRecordEndpoint {

    // Make sure to add this endpoint to your web.xml file if this is a web application.

    private static final Logger LOG = Logger.getLogger(DeviceMotionLocationRecordEndpoint.class.getName());

    /**
     * This method gets the <code>DeviceMotionLocationRecord</code> object associated with the specified <code>id</code>.
     * @param id The id of the object to be returned.
     * @return The <code>DeviceMotionLocationRecord</code> associated with <code>id</code>.
     */
    @ApiMethod(name = "getDeviceMotionLocationRecord", path = "get_device_motion_location")
    public DeviceMotionLocationRecord getDeviceMotionLocationRecord(@Named("id") Long id) {
        // Implement this function

        LOG.info("Calling getDeviceMotionLocationRecord method");

        return ofy().load().type(DeviceMotionLocationRecord.class).id(id).now();

    }

    /**
     * This inserts a new <code>DeviceMotionLocationRecord</code> object.
     * @param deviceMotionLocationRecord The object to be added.
     * @return The object to be added.
     */
    @ApiMethod(name = "insertDeviceMotionLocationRecord", path = "insert_device_motion_location")
    public DeviceMotionLocationRecord insertDeviceMotionLocationRecord(DeviceMotionLocationRecord deviceMotionLocationRecord) {
        LOG.info("Calling insertDeviceMotionLocationRecord method");

        ofy().save().entity(deviceMotionLocationRecord).now();
        return deviceMotionLocationRecord;
    }


    @ApiMethod(name = "insertDeviceMotionLocationRecordTest", path = "insert_device_motion_location_test")
    public DeviceMotionLocationRecord insertDeviceMotionLocationRecordTest() {
        LOG.info("Calling insertDeviceMotionLocationRecord method");

        DeviceMotionLocationRecord deviceMotionLocationRecord = new DeviceMotionLocationRecord();
        Text motionFeatures = new Text("0.472,6.357,6.467,9.081,3.779,2.519,1.083,4.669,14.279,6.344,1.174,15.669,0.000,0.000,0.000,4,10,20,0,0,0,0.008,-0.050,-0.014,0.052,0.695,0.785,2.409,2.627,0.484,0.616,5.803,5.856,0.000,0.000,0.000,38,46,52,0,0,0,13.007,-25.321,-19.318,34.402,19.171,5.094,4.198,20.275,367.508,25.953,17.626,368.845,0.000,0.000,0.000,4,12,10,0,0,0,-0.095,-0.050,0.382,0.397,1.002,0.594,0.561,1.292,1.004,0.352,0.314,1.109,0.000,0.000,0.000,55,68,63,0,0,0,-2.689,7.471,6.562,10.301,0.601,2.200,1.636,2.807,0.361,4.840,2.678,5.543,0.000,0.000,0.000,0,0,0,0,0,0,0.258,0.286,0.654,0.759,0.141,0.147,0.269,0.337,0.020,0.021,0.072,0.078,0.000,0.000,0.000,8,4,12,0,0,0");
        Text locationFeatures = new Text("80:1f:02:e0:7e:a2_mean,80:1f:02:e0:7e:a2_std,80:1f:02:e0:7e:a2_min,80:1f:02:e0:7e:a2_max,80:1f:02:a4:80:40_mean,80:1f:02:a4:80:40_std,80:1f:02:a4:80:40_min,80:1f:02:a4:80:40_max,1c:bd:b9:27:2e:42_mean,1c:bd:b9:27:2e:42_std,1c:bd:b9:27:2e:42_min,1c:bd:b9:27:2e:42_max,30:46:9a:91:4f:c2_mean,30:46:9a:91:4f:c2_std,30:46:9a:91:4f:c2_min,30:46:9a:91:4f:c2_max,64:d1:a3:07:91:70_mean,64:d1:a3:07:91:70_std,64:d1:a3:07:91:70_min,64:d1:a3:07:91:70_max,08:86:3b:4c:2a:78_mean,08:86:3b:4c:2a:78_std,08:86:3b:4c:2a:78_min,08:86:3b:4c:2a:78_max,b0:c5:54:86:3a:70_mean,b0:c5:54:86:3a:70_std,b0:c5:54:86:3a:70_min,b0:c5:54:86:3a:70_max,64:66:b3:d1:40:9c_mean,64:66:b3:d1:40:9c_std,64:66:b3:d1:40:9c_min,64:66:b3:d1:40:9c_max,64:d1:a3:0e:5c:d6_mean,64:d1:a3:0e:5c:d6_std,64:d1:a3:0e:5c:d6_min,64:d1:a3:0e:5c:d6_max,00:25:86:4c:5d:14_mean,00:25:86:4c:5d:14_std,00:25:86:4c:5d:14_min,00:25:86:4c:5d:14_max,b8:c7:5d:05:5d:b7_mean,b8:c7:5d:05:5d:b7_std,b8:c7:5d:05:5d:b7_min,b8:c7:5d:05:5d:b7_max,20:0c:c8:4e:ba:8f_mean,20:0c:c8:4e:ba:8f_std,20:0c:c8:4e:ba:8f_min,20:0c:c8:4e:ba:8f_max,64:d1:a3:04:b5:02_mean,64:d1:a3:04:b5:02_std,64:d1:a3:04:b5:02_min,64:d1:a3:04:b5:02_max,00:23:cd:de:c1:1a_mean,00:23:cd:de:c1:1a_std,00:23:cd:de:c1:1a_min,00:23:cd:de:c1:1a_max,00:16:0a:18:f5:4c_mean,00:16:0a:18:f5:4c_std,00:16:0a:18:f5:4c_min,00:16:0a:18:f5:4c_max,00:1f:33:b7:03:fa_mean,00:1f:33:b7:03:fa_std,00:1f:33:b7:03:fa_min,00:1f:33:b7:03:fa_max,98:fc:11:c2:db:f6_mean,98:fc:11:c2:db:f6_std,98:fc:11:c2:db:f6_min,98:fc:11:c2:db:f6_max,88:53:d4:bf:bd:4c_mean,88:53:d4:bf:bd:4c_std,88:53:d4:bf:bd:4c_min,88:53:d4:bf:bd:4c_max,20:4e:7f:4b:df:98_mean,20:4e:7f:4b:df:98_std,20:4e:7f:4b:df:98_min,20:4e:7f:4b:df:98_max,d8:15:0d:a5:3d:64_mean,d8:15:0d:a5:3d:64_std,d8:15:0d:a5:3d:64_min,d8:15:0d:a5:3d:64_max\n"
                + "-70.75,1.6393596310755,-72.0,-68.0,-87.25,4.380353866983808,-93.0,-83.0,-85.0,2.0,-87.0,-83.0,-82.33333333333333,2.357022603955158,-84.0,-79.0,-93.0,0.0,-93.0,-93.0,-40.0,1.7320508075688772,-41.0,-37.0,-84.75,3.191786333700926,-89.0,-80.0,-77.25,1.479019945774904,-79.0,-75.0,-92.25,3.2691742076555053,-96.0,-87.0,-74.75,2.277608394786075,-77.0,-71.0,-92.25,2.277608394786075,-96.0,-90.0,-87.25,1.0897247358851685,-89.0,-86.0,-58.75,0.82915619758885,-60.0,-58.0,-76.0,3.9370039370059056,-80.0,-70.0,-90.0,0.0,-90.0,-90.0,-93.0,1.4142135623730951,-95.0,-91.0,-80.5,0.5,-81.0,-80.0,-80.0,3.8078865529319543,-85.0,-75.0,-77.75,4.9180788932265,-82.0,-70.0,-84.5,6.98212002188447,-96.0,-78.0");

        deviceMotionLocationRecord.setUsername("Username");
        deviceMotionLocationRecord.setEvent("04014EC22D3581_Computer_20141105191401");
        deviceMotionLocationRecord.setDeviceId("04014EC22D3581");
        deviceMotionLocationRecord.setDeviceType("Computer");
        deviceMotionLocationRecord.setTimestamp(Utils.getCurrentTimestamp());
        deviceMotionLocationRecord.setMotionFeatures(motionFeatures);
        deviceMotionLocationRecord.setLocationFeatures(locationFeatures);

        ofy().save().entity(deviceMotionLocationRecord).now();
        return deviceMotionLocationRecord;
    }


    @ApiMethod(name = "listMotionLocationFeaturesByDate", path = "list_motion_location_features_by_date")
    public CollectionResponse<DeviceMotionLocationRecord> listMotionLocationFeaturesByDate(
            @Named("minDate") String minDate,
            @Named("maxDate") String maxDate) {

        LOG.info("Calling listMotionLocationFeaturesByDate method");

        List<DeviceMotionLocationRecord> records= ofy().load().type(DeviceMotionLocationRecord.class)
                .filter("timestamp >=", minDate)
                .filter("timestamp <=", maxDate)
                .order("timestamp")
                .list();

        //records = sortByTimestamp(records);
        LOG.info("Records:" + records.size());

        return CollectionResponse.<DeviceMotionLocationRecord>builder().setItems(records).build();
    }


    @ApiMethod(name = "listMotionLocationFeaturesByUser", path = "list_motion_location_features_by_user")
    public CollectionResponse<DeviceMotionLocationRecord> listMotionLocationFeaturesByUser(
            @Named("user") String user,
            @Named("minDate") String minDate,
            @Named("maxDate") String maxDate) {

        LOG.info("Calling listMotionLocationFeaturesByUser method");

        List<DeviceMotionLocationRecord> records= ofy().load().type(DeviceMotionLocationRecord.class)
                .filter("username", user)
                .filter("timestamp >=", minDate)
                .filter("timestamp <=", maxDate)
                .order("timestamp")
                .list();

        //records = sortByTimestamp(records);
        LOG.info("Records:" + records.size());

        return CollectionResponse.<DeviceMotionLocationRecord>builder().setItems(records).build();
    }

    @ApiMethod(name = "listMotionLocationFeaturesByDeviceType", path = "list_motion_location_features_by_device_type")
    public CollectionResponse<DeviceMotionLocationRecord> listMotionLocationFeaturesByDeviceType(
            @Named("deviceType") String deviceType,
            @Named("minDate") String minDate,
            @Named("maxDate") String maxDate) {

        LOG.info("Calling listMotionLocationFeaturesByDeviceType method");

        List<DeviceMotionLocationRecord> records= ofy().load().type(DeviceMotionLocationRecord.class)
                .filter("deviceType", deviceType)
                .filter("timestamp >=", minDate)
                .filter("timestamp <=", maxDate)
                .order("timestamp")
                .list();

        LOG.info("Records:" + records.size());

        return CollectionResponse.<DeviceMotionLocationRecord>builder().setItems(records).build();
    }

    @ApiMethod(name = "listMotionLocationFeaturesByDeviceId", path = "list_motion_location_features_by_device_id")
    public CollectionResponse<DeviceMotionLocationRecord> listMotionLocationFeaturesByDeviceId(
            @Named("deviceId") String deviceId,
            @Named("minDate") String minDate,
            @Named("maxDate") String maxDate) {

        LOG.info("Calling listMotionLocationFeaturesByDeviceId method");

        List<DeviceMotionLocationRecord> records= ofy().load().type(DeviceMotionLocationRecord.class)
                .filter("deviceId", deviceId)
                .filter("timestamp >=", minDate)
                .filter("timestamp <=", maxDate)
                .order("timestamp")
                .list();

        LOG.info("Records:" + records.size());

        return CollectionResponse.<DeviceMotionLocationRecord>builder().setItems(records).build();
    }


    private ArrayList<String> getLocationAttributesSTR(
            String minDate,
            String maxDate){

        List<DeviceMotionLocationRecord> records= ofy().load().type(DeviceMotionLocationRecord.class)
                .filter("timestamp >=", minDate)
                .filter("timestamp <=", maxDate)
                .order("timestamp")
                .list();

        LOG.info("Records:" + records.size());
        HashSet<String> locationAttributesSet = new HashSet<String>();
        for (DeviceMotionLocationRecord deviceMotionLocationRecord : records){
            String locationData = deviceMotionLocationRecord.getLocationFeatures().getValue();
            String lines[] = locationData.split("\\n");
            String locationAttributes = lines[0];

            String allNetworksWithSuffix[] = locationAttributes.split(",");
            locationAttributesSet.addAll(Arrays.asList(allNetworksWithSuffix));

        }
        LOG.info("Distinct Attributes:" + locationAttributesSet.size());
        LOG.info("Distinct Networks:" + (locationAttributesSet.size()/4));

        ArrayList<String> result  = new ArrayList<String>(locationAttributesSet);
        Collections.sort(result);
        return result;

    }

    @ApiMethod(name = "getLocationAttributes", path = "get_location_attributes")
    public CollectionResponse<DeviceMotionLocationRecord> getLocationAttributes(
            @Named("minDate") String minDate,
            @Named("maxDate") String maxDate) {

        LOG.info("Calling getLocationAttributes method from: " + minDate + " to " + maxDate);

        ArrayList<String> locationAttributes    = new ArrayList<String>(getLocationAttributesSTR(
                minDate, maxDate));
        ArrayList<DeviceMotionLocationRecord> records = new ArrayList<DeviceMotionLocationRecord>();

        for (String s : locationAttributes){
            DeviceMotionLocationRecord deviceMotionLocationRecord = new DeviceMotionLocationRecord();
            deviceMotionLocationRecord.setEvent(s);
            records.add(deviceMotionLocationRecord);
        }

        return CollectionResponse.<DeviceMotionLocationRecord>builder().setItems(records).build();
    }


    private ArrayList<String> getClassAttributesSTR(
            String minDate,
            String maxDate){

        List<DeviceMotionLocationRecord> records= ofy().load().type(DeviceMotionLocationRecord.class)
                .filter("timestamp >=", minDate)
                .filter("timestamp <=", maxDate)
                .order("timestamp")
                .list();

        LOG.info("Records:" + records.size());
        HashSet<String> hashSet = new HashSet<String>();
        for (DeviceMotionLocationRecord deviceMotionLocationRecord : records){
            hashSet.add(deviceMotionLocationRecord.getEvent().replaceAll("\\s",""));
        }
        LOG.info("Distinct Records:" + hashSet.size());

        return new ArrayList<String>(hashSet);

    }

    @ApiMethod(name = "getClassAttributes", path = "get_class_attributes")
    public CollectionResponse<DeviceMotionLocationRecord> getClassAttributes(
            @Named("minDate") String minDate,
            @Named("maxDate") String maxDate) {

        LOG.info("Calling getClassAttributes method from: " + minDate + " to " + maxDate);

        ArrayList<String> classAttributes    = new ArrayList<String>(getClassAttributesSTR(minDate,maxDate));
        ArrayList<DeviceMotionLocationRecord> records = new ArrayList<DeviceMotionLocationRecord>();

        for (String s : classAttributes){
            DeviceMotionLocationRecord deviceMotionLocationRecord = new DeviceMotionLocationRecord();
            deviceMotionLocationRecord.setEvent(s);
            records.add(deviceMotionLocationRecord);
        }

        return CollectionResponse.<DeviceMotionLocationRecord>builder().setItems(records).build();
    }

    @ApiMethod(name = "getMotionLocationFeaturesProcessed", path = "get_motion_location_features_processed")
    public CollectionResponse<DeviceMotionLocationRecord> getMotionLocationFeaturesProcessed(
            @Named("minDate") String minDate,
            @Named("maxDate") String maxDate) {

        LOG.info("Calling getMotionLocationFeaturesProcessed method from: " + minDate + " to " + maxDate);

        // Ask for the corresponding records
        List<DeviceMotionLocationRecord> records= ofy().load().type(
                DeviceMotionLocationRecord.class)
                .filter("timestamp >=", minDate)
                .filter("timestamp <=", maxDate)
                .order("timestamp")
                .list();
        LOG.info("Records:" + records.size());

        ArrayList<String> locationAttributes;
        HashSet<String> locationAttributesSet = new HashSet<String>();

        // Build the class and location attributes. (Motion Attributes are always fixed)
        for (DeviceMotionLocationRecord deviceMotionLocationRecord : records){
            String locationData = deviceMotionLocationRecord.getLocationFeatures().getValue();
            String lines[] = locationData.split("\\n");
            String locationAttributesStr = lines[0];

            String allNetworksWithSuffix[] = locationAttributesStr.split(",");
            locationAttributesSet.addAll(Arrays.asList(allNetworksWithSuffix));
        }
        LOG.info("Distinct Attributes:" + locationAttributesSet.size());
        LOG.info("Distinct Networks:" + (locationAttributesSet.size()/4));

        locationAttributes  = new ArrayList<String>(locationAttributesSet);
        Collections.sort(locationAttributes);

        // Once you know all existing location attributes, build the location features
        for (DeviceMotionLocationRecord deviceMotionLocationRecord : records){
            String locationData = deviceMotionLocationRecord.getLocationFeatures().getValue();
            String lines[] = locationData.split("\\n");
            String locationAttributesStr = lines[0];
            String locationFeaturesStr = lines[1];

            String attributes[] = locationAttributesStr.split(",");
            String values[]     = locationFeaturesStr.split(",");

            HashMap<String,String> recordFeatures = new HashMap<String, String>();
            for (int i = 0; i < attributes.length; i++){
                recordFeatures.put(attributes[i], values[i]);
            }

            String recordLocationFeatures = "";
            for (String locationAttribute : locationAttributes){
                if (recordFeatures.containsKey(locationAttribute)){
                    recordLocationFeatures += recordFeatures.get(locationAttribute) + ",";
                } else {
                    recordLocationFeatures += "?,";
                }
            }
            //remove the last comma
            recordLocationFeatures = recordLocationFeatures.substring(0, recordLocationFeatures.length()-1);
            deviceMotionLocationRecord.setLocationFeatures(new Text(recordLocationFeatures));
        }
        return CollectionResponse.<DeviceMotionLocationRecord>builder().setItems(records).build();
    }


    @ApiMethod(name = "createInstanceSetFromDB", path = "create_instance_set_from_db")
    public void createInstanceSetFromDB(
            @Named("minDate") String minDate,
            @Named("maxDate") String maxDate) {

        LOG.info("Calling createInstanceSetFromDB method from: " + minDate + " to " + maxDate);

        // Ask for the corresponding records
        List<DeviceMotionLocationRecord> records= ofy().load().type(
                DeviceMotionLocationRecord.class)
                .filter("timestamp >=", minDate)
                .filter("timestamp <=", maxDate)
                .order("timestamp")
                .list();

        LOG.info("Records:" + records.size());

        createInstanceSet(records);

    }


    private void createInstanceSet(List<DeviceMotionLocationRecord> records){
        String relation  = "Events";
        ArrayList<String> classAttributes;
        ArrayList<String> locationAttributes;
        ArrayList<String> motionAttributes;
        ArrayList<String> features = new ArrayList<String>();


        HashSet<String> classAttributesSet = new HashSet<String>();
        HashSet<String> locationAttributesSet = new HashSet<String>();

        // Build the class and location attributes. (Motion Attributes are always fixed)
        for (DeviceMotionLocationRecord deviceMotionLocationRecord : records){
            classAttributesSet.add(deviceMotionLocationRecord.getEvent().replaceAll("\\s",""));

            String locationData = deviceMotionLocationRecord.getLocationFeatures().getValue();
            String lines[] = locationData.split("\\n");
            String locationAttributesStr = lines[0];

            String allNetworksWithSuffix[] = locationAttributesStr.split(",");
            locationAttributesSet.addAll(Arrays.asList(allNetworksWithSuffix));
        }
        LOG.info("Distinct Class Records:" + classAttributesSet.size());
        LOG.info("Distinct Attributes:" + locationAttributesSet.size());
        LOG.info("Distinct Networks:" + (locationAttributesSet.size()/4));

        classAttributes     = new ArrayList<String>(classAttributesSet);
        motionAttributes    = new ArrayList<String>(WekaMotionUtils.getAttributes());
        locationAttributes  = new ArrayList<String>(locationAttributesSet);
        Collections.sort(locationAttributes);

        // Once you know all existing location attributes, build the location features
        for (DeviceMotionLocationRecord deviceMotionLocationRecord : records){
            String locationData = deviceMotionLocationRecord.getLocationFeatures().getValue();
            String motionFeatures = deviceMotionLocationRecord.getMotionFeatures().getValue();
            String lines[] = locationData.split("\\n");
            String locationAttributesStr = lines[0];
            String locationFeaturesStr = lines[1];

            String attributes[] = locationAttributesStr.split(",");
            String values[]     = locationFeaturesStr.split(",");

            HashMap<String,String> recordFeatures = new HashMap<String, String>();
            for (int i = 0; i < attributes.length; i++){
                recordFeatures.put(attributes[i], values[i]);
            }

            String locationFeatures = "";
            for (String locationAttribute : locationAttributes){
                if (recordFeatures.containsKey(locationAttribute)){
                    locationFeatures += recordFeatures.get(locationAttribute) + ",";
                } else {
                    locationFeatures += "?,";
                }
            }
            //remove the last comma
            locationFeatures = locationFeatures.substring(0, locationFeatures.length()-1);
            String classAttribute = deviceMotionLocationRecord.getEvent().replaceAll("\\s", "");
            //features.add(locationFeatures + "," + motionFeatures);
            features.add(locationFeatures + "," + motionFeatures + "," + classAttribute);
        }

        Instances wekaInstances = WekaMethods.CreateInstanceSet(relation,
                                                                motionAttributes,
                                                                locationAttributes,
                                                                classAttributes,
                                                                features);

        



    }


}