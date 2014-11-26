package tudelft.mdp.backend.weka;

import java.lang.reflect.Array;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

import tudelft.mdp.backend.Utils;
import tudelft.mdp.backend.endpoints.WekaObjectRecordEndpoint;
import tudelft.mdp.backend.enums.Constants;
import tudelft.mdp.backend.enums.WekaClassifierTypes;
import tudelft.mdp.backend.gcs.GcsHelper;
import tudelft.mdp.backend.records.DeviceMotionLocationRecord;
import tudelft.mdp.backend.records.LocationFeaturesRecord;
import tudelft.mdp.backend.records.RegistrationRecord;
import tudelft.mdp.backend.records.WekaObjectRecord;
import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.core.Instances;

public class WekaUtils {

    private static final Logger LOG = Logger.getLogger(WekaUtils.class.getName());

    public WekaUtils() {
    }


    public static String getAxisName(int position){
        String sensorName = "";
        switch (position) {
            case 0:
                sensorName = Constants.SENSOR_LINE;
                break;
            case 1:
                sensorName = Constants.SENSOR_TIME;
                break;
            case 2:
                sensorName = Constants.SENSOR_ACCX;
                break;
            case 3:
                sensorName = Constants.SENSOR_ACCY;
                break;
            case 4:
                sensorName = Constants.SENSOR_ACCZ;
                break;
            case 5:
                sensorName = Constants.SENSOR_GYROX;
                break;
            case 6:
                sensorName = Constants.SENSOR_GYROY;
                break;
            case 7:
                sensorName = Constants.SENSOR_GYROZ;
                break;
            case 8:
                sensorName = Constants.SENSOR_MAGX;
                break;
            case 9:
                sensorName = Constants.SENSOR_MAGY;
                break;
            case 10:
                sensorName = Constants.SENSOR_MAGZ;
                break;
            case 11:
                sensorName = Constants.SENSOR_LACCX;
                break;
            case 12:
                sensorName = Constants.SENSOR_LACCY;
                break;
            case 13:
                sensorName = Constants.SENSOR_LACCZ;
                break;
            case 14:
                sensorName = Constants.SENSOR_TILTX;
                break;
            case 15:
                sensorName = Constants.SENSOR_TILTY;
                break;
            case 16:
                sensorName = Constants.SENSOR_TILTZ;
                break;
            case 17:
                sensorName = Constants.SENSOR_ROTX;
                break;
            case 18:
                sensorName = Constants.SENSOR_ROTY;
                break;
            case 19:
                sensorName = Constants.SENSOR_ROTZ;
                break;

            default:
                break;
        }
        return sensorName;
    }

    public static String getModelType(int model){
        String modelType;

        switch (model){
            case WekaClassifierTypes.J48:
                modelType = "J48";
                break;
            default:
                modelType = "";
                break;
        }

        return modelType;
    }

    public static ArrayList<String> getAttributes(){
        ArrayList<String> AttributesList = new ArrayList<String>();

        for(int i=2;i<(18)+2;i=i+3){
//    		for(int i=2;i<18;i++){

            String[] sensornameX = getAxisName(i).split("_");
            String[] sensornameY = getAxisName(i + 1).split("_");
            String[] sensornameZ = getAxisName(i + 2).split("_");

            AttributesList.add("mean_"+sensornameX[1]+sensornameX[2]);
            AttributesList.add("mean_"+sensornameY[1]+sensornameY[2]);
            AttributesList.add("mean_"+sensornameZ[1]+sensornameZ[2]);
            AttributesList.add("meanMag_"+sensornameX[1]+sensornameX[2]+sensornameY[2]+sensornameZ[2]);

            AttributesList.add("std_"+sensornameX[1]+sensornameX[2]);
            AttributesList.add("std_"+sensornameY[1]+sensornameY[2]);
            AttributesList.add("std_"+sensornameZ[1]+sensornameZ[2]);
            AttributesList.add("stdMag_"+sensornameX[1]+sensornameX[2]+sensornameY[2]+sensornameZ[2]);

            AttributesList.add("var_"+sensornameX[1]+sensornameX[2]);
            AttributesList.add("var_"+sensornameY[1]+sensornameY[2]);
            AttributesList.add("var_"+sensornameZ[1]+sensornameZ[2]);
            AttributesList.add("varMag_"+sensornameX[1]+sensornameX[2]+sensornameY[2]+sensornameZ[2]);

            AttributesList.add("FundF_"+sensornameX[1]+sensornameX[2]);
            AttributesList.add("FundF_"+sensornameY[1]+sensornameY[2]);
            AttributesList.add("FundF_"+sensornameZ[1]+sensornameZ[2]);

            AttributesList.add("ZXing_"+sensornameX[1]+sensornameX[2]);
            AttributesList.add("ZXing_"+sensornameY[1]+sensornameY[2]);
            AttributesList.add("ZXing_"+sensornameZ[1]+sensornameZ[2]);

            AttributesList.add("SCount_"+sensornameX[1]+sensornameX[2]);
            AttributesList.add("SCount_"+sensornameY[1]+sensornameY[2]);
            AttributesList.add("SCount_"+sensornameZ[1]+sensornameZ[2]);

        }

        return AttributesList;
    }

    public ArrayList<String> createInstanceSetLocation(List<LocationFeaturesRecord> records, String minDate, String maxDate, String filteredPlaces){
        String relation  = "Events";
        ArrayList<String> classAttributes;
        ArrayList<String> locationAttributes;
        ArrayList<String> features = new ArrayList<String>();


        HashSet<String> classAttributesSet = new HashSet<String>();
        HashSet<String> locationAttributesSet = new HashSet<String>();

        // Build the class and location attributes. (Motion Attributes are always fixed)
        for (LocationFeaturesRecord locationFeaturesRecord : records){
            String classAttr = locationFeaturesRecord.getPlace().replaceAll("\\s", "") + "-"
                             + locationFeaturesRecord.getZone().replaceAll("\\s", "");
            classAttributesSet.add(classAttr);

            String locationData = locationFeaturesRecord.getLocationFeatures().getValue();
            String lines[] = locationData.split("\\n");
            String locationAttributesStr = lines[0];

            String allNetworksWithSuffix[] = locationAttributesStr.split(",");
            locationAttributesSet.addAll(Arrays.asList(allNetworksWithSuffix));
        }

        classAttributes     = new ArrayList<String>(classAttributesSet);
        locationAttributes  = new ArrayList<String>(locationAttributesSet);
        Collections.sort(locationAttributes);


        LOG.info("Distinct Class Records:" + classAttributes.size());
        LOG.info("Distinct Networks:" + (locationAttributes.size()/4));
        LOG.info("Distinct Location Attributes:" + locationAttributes.size());

        for (String attr : classAttributes){
            LOG.info("Class: " + attr);
        }
        for (String attr : locationAttributes){
            LOG.info("Location: " + attr);
        }

        // Once you know all existing location attributes, build the location features
        for (LocationFeaturesRecord locationFeaturesRecord : records){
            String locationData = locationFeaturesRecord.getLocationFeatures().getValue();
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
            String classAttr = locationFeaturesRecord.getPlace().replaceAll("\\s", "") + "-"
                    + locationFeaturesRecord.getZone().replaceAll("\\s", "");
            //features.add(locationFeatures + "," + motionFeatures);

            features.add(locationFeatures + "," + classAttr);
        }

        Instances wekaInstances = WekaMethods.CreateLocationInstanceSet(relation,
                locationAttributes,
                classAttributes,
                features);

        ArrayList<String> filesCreated = storeWekaObjects(wekaInstances, minDate, maxDate, "Location-only", filteredPlaces);
        return filesCreated;
    }

    public ArrayList<String> createInstanceSetLocationFromMotLoc(List<DeviceMotionLocationRecord> records, String minDate, String maxDate, String filteredUsers){
        String relation  = "Events";
        ArrayList<String> classAttributes;
        ArrayList<String> locationAttributes;
        ArrayList<String> features = new ArrayList<String>();


        HashSet<String> classAttributesSet = new HashSet<String>();
        HashSet<String> locationAttributesSet = new HashSet<String>();

        // Build the class and location attributes. (Motion Attributes are always fixed)
        for (DeviceMotionLocationRecord deviceMotionLocationRecord : records){
            String[] parts = deviceMotionLocationRecord.getEvent().split("-");
            if (parts[0].equals("Microwave")){
                parts[1] = "Kitchen";
            }
            String classAttr = "Home-"+parts[1];
            classAttributesSet.add(classAttr.replaceAll("\\s",""));

            String locationData = deviceMotionLocationRecord.getLocationFeatures().getValue();
            String lines[] = locationData.split("\\n");
            String locationAttributesStr = lines[0];

            String allNetworksWithSuffix[] = locationAttributesStr.split(",");
            locationAttributesSet.addAll(Arrays.asList(allNetworksWithSuffix));
        }

        classAttributes     = new ArrayList<String>(classAttributesSet);
        locationAttributes  = new ArrayList<String>(locationAttributesSet);
        Collections.sort(locationAttributes);


        LOG.info("Distinct Class Records:" + classAttributes.size());
        LOG.info("Distinct Networks:" + (locationAttributes.size()/4));
        LOG.info("Distinct Location Attributes:" + locationAttributes.size());

        for (String attr : classAttributes){
            LOG.info("Class: " + attr);
        }
        for (String attr : locationAttributes){
            LOG.info("Location: " + attr);
        }

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


            String[] parts = deviceMotionLocationRecord.getEvent().split("-");
            if (parts[0].equals("Microwave")){
                parts[1] = "Kitchen";
            }
            String classAttr = "Home-"+parts[1];
            String classAttribute = classAttr.replaceAll("\\s","");
            //features.add(locationFeatures + "," + motionFeatures);

            features.add(locationFeatures + "," + classAttribute);
        }

        Instances wekaInstances = WekaMethods.CreateLocationInstanceSet(relation,
                locationAttributes,
                classAttributes,
                features);

        ArrayList<String> filesCreated = storeWekaObjects(wekaInstances, minDate, maxDate, "Location-only", filteredUsers + " (from Motion and Loc data)");
        return filesCreated;
    }

    public ArrayList<String> createInstanceSet(List<RegistrationRecord>users, List<DeviceMotionLocationRecord> records, String minDate, String maxDate, String filteredUsers){
        String relation  = "Events";
        ArrayList<String> classAttributes;
        ArrayList<String> locationAttributes;
        ArrayList<String> motionAttributes;
        ArrayList<String> features = new ArrayList<String>();
        ArrayList<String> usernames = new ArrayList<String>();


        HashSet<String> classAttributesSet = new HashSet<String>();
        HashSet<String> locationAttributesSet = new HashSet<String>();

        for(RegistrationRecord registrationRecord : users){
            usernames.add(removeAccents(registrationRecord.getUsername().replaceAll("\\s","")));
        }

        // Build the class and location attributes. (Motion Attributes are always fixed)
        for (DeviceMotionLocationRecord deviceMotionLocationRecord : records){
            classAttributesSet.add(deviceMotionLocationRecord.getEvent().replaceAll("\\s",""));

            String locationData = deviceMotionLocationRecord.getLocationFeatures().getValue();
            String lines[] = locationData.split("\\n");
            String locationAttributesStr = lines[0];

            String allNetworksWithSuffix[] = locationAttributesStr.split(",");
            locationAttributesSet.addAll(Arrays.asList(allNetworksWithSuffix));
        }

        classAttributes     = new ArrayList<String>(classAttributesSet);
        motionAttributes    = new ArrayList<String>(WekaUtils.getAttributes());
        locationAttributes  = new ArrayList<String>(locationAttributesSet);
        Collections.sort(locationAttributes);


        LOG.info("Distinct Class Records:" + classAttributes.size());
        LOG.info("Distinct Networks:" + (locationAttributes.size()/4));
        LOG.info("Distinct Location Attributes:" + locationAttributes.size());
        LOG.info("Distinct Motion Attributes:" + motionAttributes.size());

        for (String attr : classAttributes){
            LOG.info("Class: " + attr);
        }
        for (String attr : motionAttributes){
            LOG.info("Motion: " + attr);
        }
        for (String attr : locationAttributes){
            LOG.info("Location: " + attr);
        }

        // Once you know all existing location attributes, build the location features
        for (DeviceMotionLocationRecord deviceMotionLocationRecord : records){
            String user = removeAccents(deviceMotionLocationRecord.getUsername().replaceAll("\\s", ""));
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

            //Hack to solve bug in ugly way
            if (motionFeatures.length() < 50){
                motionFeatures =  motionFeatures + ","
                        + motionFeatures + ","
                        + motionFeatures + ","
                        + motionFeatures + ","
                        + motionFeatures + ","
                        + motionFeatures;
            }
            features.add(locationFeatures + "," + motionFeatures + "," + classAttribute + "," + user);
        }

        Instances wekaInstances = WekaMethods.CreateInstanceSet(relation,
                usernames,
                motionAttributes,
                locationAttributes,
                classAttributes,
                features);

        ArrayList<String> filesCreated = storeWekaObjects(wekaInstances, minDate, maxDate, "Motion-Location", filteredUsers);
        return filesCreated;
    }

    public String removeAccents(String text) {
        return Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }
    private Classifier createClassifier(Instances wekaInstances, Integer clsType){
        Classifier cls;
        wekaInstances.setClassIndex(wekaInstances.numAttributes() - 1);

        switch (clsType){
            case WekaClassifierTypes.J48:
                cls = new J48();
                break;
            default:
                cls = null;
                break;
        }

        if (cls != null) {
            try {
                cls.buildClassifier(wekaInstances);
            } catch (Exception e) {
                LOG.severe(e.getMessage());
            }
        }
        return cls;
    }

    private ArrayList<String> storeWekaObjects(Instances wekaInstances, String minDate, String maxDate, String origin, String filter){
        try {
            byte[] serializedWekaObject = Utils.serialize(wekaInstances);
            LOG.info(Utils.humanReadableByteCount(serializedWekaObject.length, true));

            String description = origin + "|" + minDate + "-" + maxDate + "|" + filter;
            String timestamp = Utils.getCurrentTimestamp();


            LOG.info(wekaInstances.toSummaryString());

            ArrayList<String> filesCreated = new ArrayList<String>();
            String fileArff = saveArffToGcs(wekaInstances, "Arff", description, timestamp, origin);
            String fileInst = saveInstancesToGcs(serializedWekaObject, "Instance", description, timestamp, origin);
            String fileClsJ48 = saveClsToGcs(createClassifier(wekaInstances,
                            WekaClassifierTypes.J48), "Classifier",
                    description + "|" + getModelType(WekaClassifierTypes.J48), timestamp,
                    WekaClassifierTypes.J48, origin);

            filesCreated.add(fileArff);
            filesCreated.add(fileInst);
            filesCreated.add(fileClsJ48);
            return filesCreated;

        } catch (Exception e){
            LOG.severe(e.getMessage());
            return null;
        }
    }

    private String saveClsToGcs(Classifier object, String objectType, String description, String timestamp, int clsType, String origin){
        try {

            LOG.info(object.toString());

            String filename = origin + "_" + objectType + "_" + getModelType(clsType) + "_" +timestamp + ".model";
            GcsHelper gcsHelper = new GcsHelper();
            gcsHelper.writeWekaClsToGCS(filename, object);
            saveGcsFileDescriptionToDatastore(filename, objectType, description, timestamp);

            return filename;
        } catch (Exception e){
            LOG.severe(e.getMessage());
            return "";
        }
    }

    private String saveArffToGcs(Instances object, String objectType, String description, String timestamp, String origin){
        try {
            String filename = origin + "_" + objectType + "_" + timestamp + ".arff";
            GcsHelper gcsHelper = new GcsHelper();
            gcsHelper.writeWekaArffToGCS(filename, object);
            saveGcsFileDescriptionToDatastore(filename, objectType, description, timestamp);

            return filename;
        } catch (Exception e){
            LOG.severe(e.getMessage());
            return "";
        }
    }

    private String saveInstancesToGcs(byte[] object, String objectType, String description, String timestamp, String origin){
        try {
            String filename = origin + "_" + objectType + "_" + timestamp + ".instances";
            GcsHelper gcsHelper = new GcsHelper();
            gcsHelper.writeWekaInstanceToGCS(filename, object);
            saveGcsFileDescriptionToDatastore(filename, objectType, description, timestamp);

            return filename;
        } catch (Exception e){
            LOG.severe(e.getMessage());
            return "";
        }
    }

    private void saveGcsFileDescriptionToDatastore(String filename, String objectType, String description, String timestamp){
        WekaObjectRecord wekaObjectRecord = new WekaObjectRecord();
        wekaObjectRecord.setFilename(filename);
        wekaObjectRecord.setTimestamp(timestamp);
        wekaObjectRecord.setObjectType(objectType);
        wekaObjectRecord.setDescription(description);

        WekaObjectRecordEndpoint wekaObjectRecordEndpoint = new WekaObjectRecordEndpoint();
        wekaObjectRecordEndpoint.insertWekaObjectRecord(wekaObjectRecord);
    }

    public String createMockArff(int nUsers){
        int otherUsers = nUsers - 1;
        String[] classAttributes = {
                "Kitchen-Boiler", "Kitchen-CoffeeMachine", "Kitchen-Oven", "Kitchen-Hotplates",
                "Kitchen-Microwave", "Kitchen-Grill", "Kitchen-Lights",
                "BedroomA-Computer", "BedroomA-Lights", "BedroomA-Fridge",
                "BedroomB-Computer", "BedroomB-Lights", "BedroomB-Fridge",
                "LivingRoom-Lights", "LivingRoom-Computer", "LivingRoom-Television",
                "Shower-Lights", "Toilet-Lights" };
        String[] locationAttributes = {
                "BedroomA", "BedroomB", "LivingRoom", "Kitchen", "Shower", "Toilet", "Unknown"};
        String[] motionAttributesDevices = {
                "Boiler", "CoffeeMachine", "Oven", "Hotplates", "Microwave", "Grill",
                "Lights", "Fridge", "Computer", "Television" };
        String[] motionAttributesOther = {"Idle", "Active", "Random"};

        ArrayList<String> classAttr = new ArrayList<String>(Arrays.asList(classAttributes));
        ArrayList<String> locAttr = new ArrayList<String>(Arrays.asList(locationAttributes));
        ArrayList<String> motionAttr = new ArrayList<String>();
        motionAttr.addAll(Arrays.asList(motionAttributesDevices));
        motionAttr.addAll(Arrays.asList(motionAttributesOther));

        ArrayList<String> arff =  createMockArffHeaders(classAttributes, locationAttributes, motionAttributesDevices, motionAttributesOther);
        ArrayList<String> features = new ArrayList<String>();


        // Event user has smartwatch
        int eventsUserWithSmartwatch = 0;
        int instancesUserWithSmartwatch = 0;
        for (String event : classAttributes){
            String[] parts = event.split("-");
            String location = parts[0];
            String motion   = parts[1];
            String userSmartwatchDoingEventInstance = motion + "," + location + "," + event;

            List<String[]> permutations = Permutations.get(otherUsers, String.class, locationAttributes);
            eventsUserWithSmartwatch = permutations.size();

            ArrayList<String> permSort = new ArrayList<String>();
            for (String[] perm : permutations){
                String temp = "";
                for (String s : perm){
                    temp += s + ",";
                }
                temp = temp.substring(0, temp.length()-1);
                permSort.add(temp);
            }
            Collections.sort(permSort);

            for (String p : permSort){
                arff.add(userSmartwatchDoingEventInstance);
                features.add(userSmartwatchDoingEventInstance);

                instancesUserWithSmartwatch++;
                String[] n = p.split(",");
                for (String otherUsersLoc : n){
                    arff.add("?," + otherUsersLoc + "," + event);
                    features.add("?," + otherUsersLoc + "," + event);
                    instancesUserWithSmartwatch++;
                }
            }
        }

        LOG.info("Event with user with smartwatch triggering the event: "
                + eventsUserWithSmartwatch
                + ". N users:"  + nUsers
                + ". N events:" + classAttributes.length
                + nUsers + ". N instances:" + (instancesUserWithSmartwatch));



        // Event user does not have smartwatch
        int eventsUserWithoutSmartwatch = 0;
        int instancesUserWithoutSmartwatch = 0;
        for (String event : classAttributes){
            String[] parts = event.split("-");
            String location = parts[0];
            String userNoSmartwatchDoingEventInstance =  "?," + location + "," + event;

            ArrayList<String> events = createOtherUsersEventInstancesMotion(otherUsers);
            eventsUserWithoutSmartwatch = events.size();

            for (String p : events){
                arff.add(userNoSmartwatchDoingEventInstance);
                features.add(userNoSmartwatchDoingEventInstance);
                instancesUserWithoutSmartwatch++;
                String[] n = p.split("\\|");
                for (String otherUsersFt : n){
                    arff.add(otherUsersFt + "," + event);
                    features.add(otherUsersFt + "," + event);
                    instancesUserWithoutSmartwatch++;
                }
            }

        }

        LOG.info("Event with user without smartwatch triggering the event: "
                + eventsUserWithoutSmartwatch
                + ". N users:"  + nUsers
                + ". N events:" + classAttributes.length
                + ". N instances:" + (instancesUserWithoutSmartwatch));
        LOG.info("Arff lines " + arff.size());


        String filename = "Experiment-Mock_" + Utils.getCurrentTimestamp() + ".arff";
        GcsHelper gcsHelper = new GcsHelper();
        //gcsHelper.writeToGCS(filename, arff);

        try {
            Instances instances = WekaMethods
                    .CreateMockInstanceSet("Events", locAttr, motionAttr, classAttr, features);

            LOG.info(instances.toString());

            byte[] serializedWekaObject = Utils.serialize(instances);
            LOG.info(Utils.humanReadableByteCount(serializedWekaObject.length, true));
            gcsHelper.writeWekaArffToGCS(filename, instances);
        } catch (Exception e){
            LOG.severe(e.getMessage());
        }
        return filename;
    }


    public ArrayList<String> createMockArffFiltered(int nUsers, String filter){
        int otherUsers = nUsers - 1;
        String[] classAttributes = {
                "Kitchen-Boiler", "Kitchen-CoffeeMachine", "Kitchen-Oven", "Kitchen-Hotplates",
                "Kitchen-Microwave", "Kitchen-Grill", "Kitchen-Lights",
                "BedroomA-Computer", "BedroomA-Lights", "BedroomA-Fridge",
                "BedroomB-Computer", "BedroomB-Lights", "BedroomB-Fridge",
                "LivingRoom-Lights", "LivingRoom-Computer", "LivingRoom-Television",
                "Shower-Lights", "Toilet-Lights" };
        String[] locationAttributes = {
                "BedroomA", "BedroomB", "LivingRoom", "Kitchen", "Shower", "Toilet", "Unknown"};
        String[] motionAttributesDevices = {
                "Boiler", "CoffeeMachine", "Oven", "Hotplates", "Microwave", "Grill",
                "Lights", "Fridge", "Computer", "Television" };
        String[] motionAttributesOther = {"Idle", "Active", "Random"};

        String[] filterParts = filter.replaceAll("\\s", "").split(",");


        boolean filtered = false;
        ArrayList<String> classAttr;
        if (filter.equals(("NONE").toLowerCase())){
            classAttr = new ArrayList<String>(Arrays.asList(classAttributes));
        } else {
            filtered = true;
            classAttr = new ArrayList<String>(Arrays.asList(filterParts));
        }

        ArrayList<String> locAttr = new ArrayList<String>(Arrays.asList(locationAttributes));
        ArrayList<String> motionAttr = new ArrayList<String>();
        motionAttr.addAll(Arrays.asList(motionAttributesDevices));
        motionAttr.addAll(Arrays.asList(motionAttributesOther));

        ArrayList<String> arff =  createMockArffHeaders(filterParts, locationAttributes, motionAttributesDevices, motionAttributesOther);
        ArrayList<String> features = new ArrayList<String>();
        ArrayList<ArrayList<String>> userFeatures = new ArrayList<ArrayList<String>>();
        for (int i = 0; i<nUsers; i++){
            ArrayList<String> userFeatureArray = new ArrayList<String>();
            userFeatures.add(userFeatureArray);
        }

        // Event user has smartwatch
        int eventsUserWithSmartwatch = 0;
        int instancesUserWithSmartwatch = 0;
        for (String event : classAttr){
            String[] parts = event.split("-");
            String location = parts[0];
            String motion   = parts[1];
            String userSmartwatchDoingEventInstance = motion + "," + location + "," + event;

            List<String[]> permutations = Permutations.get(otherUsers, String.class, locationAttributes);
            eventsUserWithSmartwatch = permutations.size();

            ArrayList<String> permSort = new ArrayList<String>();
            for (String[] perm : permutations){
                String temp = "";
                for (String s : perm){
                    temp += s + ",";
                }
                temp = temp.substring(0, temp.length()-1);
                permSort.add(temp);
            }
            Collections.sort(permSort);

            for (String p : permSort){
                arff.add(userSmartwatchDoingEventInstance);
                features.add(userSmartwatchDoingEventInstance);
                userFeatures.get(0).add(userSmartwatchDoingEventInstance);

                instancesUserWithSmartwatch++;
                String[] n = p.split(",");
                int k = 1;
                for (String otherUsersLoc : n){
                    arff.add("?," + otherUsersLoc + "," + event);
                    features.add("?," + otherUsersLoc + "," + event);
                    userFeatures.get(k++).add("?," + otherUsersLoc + "," + event);
                    instancesUserWithSmartwatch++;
                }
            }
        }

        LOG.info("Event with user with smartwatch triggering the event: "
                + eventsUserWithSmartwatch
                + ". N users:"  + nUsers
                + ". N events:" + classAttr.size()
                + nUsers + ". N instances:" + (instancesUserWithSmartwatch));



        // Event user does not have smartwatch
        int eventsUserWithoutSmartwatch = 0;
        int instancesUserWithoutSmartwatch = 0;
        for (String event : classAttr){
            String[] parts = event.split("-");
            String location = parts[0];
            String userNoSmartwatchDoingEventInstance =  "?," + location + "," + event;

            ArrayList<String> events = createOtherUsersEventInstancesMotion(otherUsers);
            eventsUserWithoutSmartwatch = events.size();

            for (String p : events){
                String[] n = p.split("\\|");

                String userWithSmartwatch = n[0] + "," + event;
                arff.add(userWithSmartwatch);
                features.add(userWithSmartwatch);
                userFeatures.get(0).add(userWithSmartwatch);
                instancesUserWithoutSmartwatch++;

                arff.add(userNoSmartwatchDoingEventInstance);
                features.add(userNoSmartwatchDoingEventInstance);
                userFeatures.get(1).add(userNoSmartwatchDoingEventInstance);
                instancesUserWithoutSmartwatch++;

                for (int i = 1; i < n.length; i++){
                    String otherUsersFt = n[i];
                    arff.add(otherUsersFt + "," + event);
                    features.add(otherUsersFt + "," + event);
                    userFeatures.get(i).add(otherUsersFt + "," + event);
                    instancesUserWithoutSmartwatch++;
                }

            }

        }

        LOG.info("Event with user without smartwatch triggering the event: "
                + eventsUserWithoutSmartwatch
                + ". N users:"  + nUsers
                + ". N events:" + classAttr.size()
                + ". N instances:" + (instancesUserWithoutSmartwatch));
        LOG.info("Arff lines " + arff.size());

        ArrayList<String> filenames = new ArrayList<String>();
        String timestamp =  Utils.getCurrentTimestamp();
        String filenameAll = "Experiment-Mock-Filtered-All_" + timestamp + ".arff";
        if (filtered){
            filenameAll =  "Experiment-Mock-Filtered-All_"+ filter + timestamp + ".arff";
        }

        filenames.add(filenameAll);
        int i = 0;
        for (ArrayList<String> uFts : userFeatures){
            String file = "Experiment-Mock-Filtered-User"+ i +"_" + timestamp + ".arff";
            if (filtered){
                file =  "Experiment-Mock-Filtered-User"+ i +"_" + filter + "_" + timestamp + ".arff";
            }
            filenames.add(file);
            i++;
        }

        GcsHelper gcsHelper = new GcsHelper();

        try {
            Instances instances = WekaMethods
                    .CreateMockInstanceSet("Events", locAttr, motionAttr, classAttr, features);

            LOG.info(instances.toString());
            byte[] serializedWekaObject = Utils.serialize(instances);
            LOG.info(Utils.humanReadableByteCount(serializedWekaObject.length, true));
            gcsHelper.writeWekaArffToGCS(filenameAll, instances);

            i = 1;
            for (ArrayList<String> uFts : userFeatures){
                Instances userInstances = WekaMethods
                        .CreateMockInstanceSet("Events", locAttr, motionAttr, classAttr, uFts);
                LOG.info(userInstances.toString());
                gcsHelper.writeWekaArffToGCS(filenames.get(i++), userInstances);
            }


        } catch (Exception e){
            LOG.severe(e.getMessage());
        }
        return filenames;
    }


    public ArrayList<String> createMockArffFilteredTimestamp(int nUsers, String filter){
        int otherUsers = nUsers - 1;
        String[] classAttributes = {
                "Kitchen-Boiler", "Kitchen-CoffeeMachine", "Kitchen-Oven", "Kitchen-Hotplates",
                "Kitchen-Microwave", "Kitchen-Grill", "Kitchen-Lights",
                "BedroomA-Computer", "BedroomA-Lights", "BedroomA-Fridge",
                "BedroomB-Computer", "BedroomB-Lights", "BedroomB-Fridge",
                "LivingRoom-Lights", "LivingRoom-Computer", "LivingRoom-Television",
                "Shower-Lights", "Toilet-Lights" };
        String[] locationAttributes = {
                "BedroomA", "BedroomB", "LivingRoom", "Kitchen", "Shower", "Toilet", "Unknown"};
        String[] motionAttributesDevices = {
                "Boiler", "CoffeeMachine", "Oven", "Hotplates", "Microwave", "Grill",
                "Lights", "Fridge", "Computer", "Television" };
        String[] motionAttributesOther = {"Idle", "Active", "Random"};

        String[] filterParts = filter.replaceAll("\\s", "").split(",");


        boolean filtered = false;
        ArrayList<String> classAttr;
        if (filter.equals(("NONE").toLowerCase())){
            classAttr = new ArrayList<String>(Arrays.asList(classAttributes));
        } else {
            filtered = true;
            classAttr = new ArrayList<String>(Arrays.asList(filterParts));
        }

        ArrayList<String> locAttr = new ArrayList<String>(Arrays.asList(locationAttributes));
        ArrayList<String> motionAttr = new ArrayList<String>();
        motionAttr.addAll(Arrays.asList(motionAttributesDevices));
        motionAttr.addAll(Arrays.asList(motionAttributesOther));

        ArrayList<String> arff =  createMockArffHeaders(filterParts, locationAttributes, motionAttributesDevices, motionAttributesOther);
        ArrayList<String> features = new ArrayList<String>();
        ArrayList<ArrayList<String>> userFeatures = new ArrayList<ArrayList<String>>();
        for (int i = 0; i<nUsers; i++){
            ArrayList<String> userFeatureArray = new ArrayList<String>();
            userFeatures.add(userFeatureArray);
        }

        // Event user has smartwatch
        int eventsUserWithSmartwatch = 0;
        int instancesUserWithSmartwatch = 0;
        int timestamp = 0;
        for (String event : classAttr){
            String[] parts = event.split("-");
            String location = parts[0];
            String motion   = parts[1];
            String userSmartwatchDoingEventInstance = motion + "," + location + "," + event;

            List<String[]> permutations = Permutations.get(otherUsers, String.class, locationAttributes);
            eventsUserWithSmartwatch = permutations.size();

            ArrayList<String> permSort = new ArrayList<String>();
            for (String[] perm : permutations){
                String temp = "";
                for (String s : perm){
                    temp += s + ",";
                }
                temp = temp.substring(0, temp.length()-1);
                permSort.add(temp);
            }
            Collections.sort(permSort);

            for (String p : permSort){
                arff.add(timestamp +"," +userSmartwatchDoingEventInstance);
                features.add(timestamp +"," +userSmartwatchDoingEventInstance);
                userFeatures.get(0).add(timestamp +"," + userSmartwatchDoingEventInstance);

                instancesUserWithSmartwatch++;
                String[] n = p.split(",");
                int k = 1;
                for (String otherUsersLoc : n){
                    arff.add(timestamp +"," +"?," + otherUsersLoc + "," + event);
                    features.add(timestamp +"," +"?," + otherUsersLoc + "," + event);
                    userFeatures.get(k++).add(timestamp +"," +"?," + otherUsersLoc + "," + event);
                    instancesUserWithSmartwatch++;
                }
                timestamp++;
            }
        }

        LOG.info("Event with user with smartwatch triggering the event: "
                + eventsUserWithSmartwatch
                + ". N users:"  + nUsers
                + ". N events:" + classAttr.size()
                + nUsers + ". N instances:" + (instancesUserWithSmartwatch));



        // Event user does not have smartwatch
        int eventsUserWithoutSmartwatch = 0;
        int instancesUserWithoutSmartwatch = 0;
        timestamp = 0;
        for (String event : classAttr){
            String[] parts = event.split("-");
            String location = parts[0];
            String userNoSmartwatchDoingEventInstance =  "?," + location + "," + event;

            ArrayList<String> events = createOtherUsersEventInstancesMotion(otherUsers);
            eventsUserWithoutSmartwatch = events.size();

            for (String p : events){
                String[] n = p.split("\\|");

                String userWithSmartwatch = timestamp +"," + n[0] + "," + event;
                arff.add(userWithSmartwatch);
                features.add(userWithSmartwatch);
                userFeatures.get(0).add(userWithSmartwatch);
                instancesUserWithoutSmartwatch++;

                arff.add(timestamp +"," + userNoSmartwatchDoingEventInstance);
                features.add(timestamp +"," + userNoSmartwatchDoingEventInstance);
                userFeatures.get(1).add(timestamp +"," + userNoSmartwatchDoingEventInstance);
                instancesUserWithoutSmartwatch++;

                for (int i = 1; i < n.length; i++){
                    String otherUsersFt = timestamp +"," + n[i];
                    arff.add(otherUsersFt + "," + event);
                    features.add(otherUsersFt + "," + event);
                    userFeatures.get(i).add(otherUsersFt + "," + event);
                    instancesUserWithoutSmartwatch++;
                }
                timestamp++;
            }

        }

        LOG.info("Event with user without smartwatch triggering the event: "
                + eventsUserWithoutSmartwatch
                + ". N users:"  + nUsers
                + ". N events:" + classAttr.size()
                + ". N instances:" + (instancesUserWithoutSmartwatch));
        LOG.info("Arff lines " + arff.size());

        ArrayList<String> filenames = new ArrayList<String>();
        String stimestamp =  Utils.getCurrentTimestamp();

        String filenameAll = "Experiment-Mock-Filtered-All_Timestamp_" + stimestamp + ".arff";
        if (filtered){
            filenameAll =  "Experiment-Mock-Filtered-All_Timestamp_"+ filter + stimestamp + ".arff";
        }

        filenames.add(filenameAll);
        int i = 0;
        for (ArrayList<String> uFts : userFeatures){
            String file = "Experiment-Mock-Filtered-User"+ i +"_Timestamp_" + stimestamp + ".arff";
            if (filtered){
                file =  "Experiment-Mock-Filtered-User"+ i +"_Timestamp_" + filter + "_" + stimestamp + ".arff";
            }
            filenames.add(file);
            i++;
        }

        GcsHelper gcsHelper = new GcsHelper();

        try {
            Instances instances = WekaMethods
                    .CreateMockInstanceSetTimestamp("Events", locAttr, motionAttr, classAttr, features);

            LOG.info(instances.toString());
            byte[] serializedWekaObject = Utils.serialize(instances);
            LOG.info(Utils.humanReadableByteCount(serializedWekaObject.length, true));
            gcsHelper.writeWekaArffToGCS(filenameAll, instances);

            i = 1;
            for (ArrayList<String> uFts : userFeatures){
                Instances userInstances = WekaMethods
                        .CreateMockInstanceSetTimestamp("Events", locAttr, motionAttr, classAttr, uFts);
                LOG.info(userInstances.toString());
                gcsHelper.writeWekaArffToGCS(filenames.get(i++), userInstances);
            }

        } catch (Exception e){
            LOG.severe(e.getMessage());
        }
        return filenames;
    }

    public String createMockArffAllIn1(int nUsers){
        int otherUsers = nUsers - 1;
        String[] classAttributes = {
                "Kitchen-Boiler", "Kitchen-CoffeeMachine", "Kitchen-Oven", "Kitchen-Hotplates",
                "Kitchen-Microwave", "Kitchen-Grill", "Kitchen-Lights",
                "BedroomA-Computer", "BedroomA-Lights", "BedroomA-Fridge",
                "BedroomB-Computer", "BedroomB-Lights", "BedroomB-Fridge",
                "LivingRoom-Lights", "LivingRoom-Computer", "LivingRoom-Television",
                "Shower-Lights", "Toilet-Lights" };
        String[] locationAttributes = {
                "BedroomA", "BedroomB", "LivingRoom", "Kitchen", "Shower", "Toilet", "Unknown"};
        String[] motionAttributesDevices = {
                "Boiler", "CoffeeMachine", "Oven", "Hotplates", "Microwave", "Grill",
                "Lights", "Fridge", "Computer", "Television" };
        String[] motionAttributesOther = {"Idle", "Active", "Random"};

        ArrayList<String> classAttr = new ArrayList<String>(Arrays.asList(classAttributes));
        ArrayList<String> locAttr = new ArrayList<String>(Arrays.asList(locationAttributes));
        ArrayList<String> motionAttr = new ArrayList<String>();
        motionAttr.addAll(Arrays.asList(motionAttributesDevices));
        motionAttr.addAll(Arrays.asList(motionAttributesOther));

        ArrayList<String> arff =  createMockArffHeaders(classAttributes, locationAttributes, motionAttributesDevices, motionAttributesOther);
        ArrayList<String> features = new ArrayList<String>();


        // Event user has smartwatch
        int eventsUserWithSmartwatch = 0;
        int instancesUserWithSmartwatch = 0;
        for (String event : classAttributes){
            String[] parts = event.split("-");
            String location = parts[0];
            String motion   = parts[1];
            String userSmartwatchDoingEventInstance = motion + "," + location + ",";

            List<String[]> permutations = Permutations.get(otherUsers, String.class, locationAttributes);
            eventsUserWithSmartwatch = permutations.size();

            ArrayList<String> permSort = new ArrayList<String>();
            for (String[] perm : permutations){
                String temp = "";
                for (String s : perm){
                    temp += s + ",";
                }
                temp = temp.substring(0, temp.length()-1);
                permSort.add(temp);
            }
            Collections.sort(permSort);

            for (String p : permSort){

                instancesUserWithSmartwatch++;
                String temp = userSmartwatchDoingEventInstance;
                String[] n = p.split(",");
                for (String otherUsersLoc : n){
                    temp += "?," + otherUsersLoc + ",";
                }
                temp += event;
                arff.add(temp);
                features.add(temp);

                //LOG.info(p + "||" + temp);
            }
        }

        LOG.info("Event with user with smartwatch triggering the event: "
                + eventsUserWithSmartwatch
                + ". N users:"  + nUsers
                + ". N events:" + classAttributes.length
                + ". N instances:" + (instancesUserWithSmartwatch));



        // Event user does not have smartwatch
        int eventsUserWithoutSmartwatch = 0;
        int instancesUserWithoutSmartwatch = 0;
        for (String event : classAttributes){
            String[] parts = event.split("-");
            String location = parts[0];
            String userNoSmartwatchDoingEventInstance =  "?," + location + ",";

            ArrayList<String> events = createOtherUsersEventInstancesMotion(otherUsers);
            eventsUserWithoutSmartwatch = events.size();

            for (String p : events){
                instancesUserWithoutSmartwatch++;
                String[] n = p.split("\\|");
                String temp = n[0] + "," + userNoSmartwatchDoingEventInstance;
                for (int i = 1; i < n.length; i++){
                    String otherUsersFt = n[i];
                    temp += otherUsersFt + ",";
                }

                temp += event;
                arff.add(temp);
                features.add(temp);

                //LOG.info(p + "||" + temp);

            }

        }

        LOG.info("Event with user without smartwatch triggering the event: "
                + eventsUserWithoutSmartwatch
                + ". N users:"  + nUsers
                + ". N events:" + classAttributes.length
                + ". N instances:" + (instancesUserWithoutSmartwatch));
        LOG.info("Arff lines " + features.size());


        String filename = "Experiment-Mock-AllIn1_" + Utils.getCurrentTimestamp() + ".arff";
        GcsHelper gcsHelper = new GcsHelper();
        //gcsHelper.writeToGCS(filename, arff);

        try {
            Instances instances = WekaMethods
                    .CreateMockInstanceSetAllIn1(nUsers, "Events", locAttr, motionAttr, classAttr,
                            features);

            LOG.info(instances.toString());

            byte[] serializedWekaObject = Utils.serialize(instances);
            LOG.info(Utils.humanReadableByteCount(serializedWekaObject.length, true));
            gcsHelper.writeWekaArffToGCS(filename, instances);
        } catch (Exception e){
            LOG.severe(e.getMessage());
        }
        return filename;
    }


    public ArrayList<String> createOtherUsersEventInstancesMotion(
            int users) {
        String[] locationAttributes = {
                "BedroomA", "BedroomB", "LivingRoom", "Kitchen", "Shower", "Toilet", "Unknown"};
        String[] motionAttributesDevices = {
                "Boiler"," CoffeeMachine", "Oven", "Hotplates", "Microwave", "Grill",
                "Lights", "Fridge", "Computer", "Television" };
        String[] motionAttributesOther = {"Idle", "Active", "Random"};

        ArrayList<String> result = new ArrayList<String>();
        ArrayList<String> finalResult = new ArrayList<String>();

        List<String[]> permutations = Permutations.get(users, String.class, locationAttributes);
        for (String[] perm : permutations){
            String temp = "";
            for (String s : perm){
                temp += s + "|?,";
            }
            temp = temp.substring(0, temp.length()-3);
            result.add(temp);
        }

        for (String temp : result){
            for (String motion : motionAttributesOther){
                String p = motion + "," + temp;
                finalResult.add(p);
            }
        }
        Collections.sort(result);

        return finalResult;
    }


    public ArrayList<String> createOtherUsersEventInstances(
            int users) {
        String[] locationAttributes = {
                "BedroomA", "BedroomB", "LivingRoom", "Kitchen", "Shower", "Toilet", "Unknown"};

        ArrayList<String> result = new ArrayList<String>();

        List<String[]> permutations = Permutations.get(users, String.class, locationAttributes);

        for (String[] perm : permutations){
            String temp = "";
            for (String s : perm){
                temp += s + ",";
            }
            temp = temp.substring(0, temp.length()-1);
           result.add(temp);
        }

        return result;
    }

    private ArrayList<String> createMockArffHeaders(
            String[] classAttributes,
            String[] locationAttributes,
            String[] motionAttributesDevices,
            String[] motionAttributesOther){


        ArrayList<String> arff = new ArrayList<String>();
        String relation  = "Events";

        //Add headers
        arff.add("@relation" + relation);
        arff.add("");

        //@attribute Motion {a,b,c,x}
        String motAttrs = "";
        for (String attr : motionAttributesDevices){
            motAttrs += attr + ",";
        }
        for (String attr : motionAttributesOther){
            motAttrs += attr + ",";
        }
        motAttrs = motAttrs.substring(0, motAttrs.length()-1);
        arff.add("@attribute Motion {" + motAttrs + "}");

        //@attribute Location {a,b,c,x}
        String locAttrs = "";
        for (String attr : locationAttributes){
            locAttrs += attr + ",";
        }
        locAttrs = locAttrs.substring(0, locAttrs.length()-1);
        arff.add("@attribute Location {" + locAttrs + "}");

        //@attribute Activity {a,b,c,x}
        String activityAttrs = "";
        for (String attr : classAttributes){
            activityAttrs += attr + ",";
        }
        activityAttrs = activityAttrs.substring(0, activityAttrs.length()-1);
        arff.add("@attribute Activity {" + activityAttrs + "}");
        arff.add("");

        arff.add("@data");
        return  arff;
    }

    public static final class Permutations {
        private Permutations() {}

        public static <T> List<T[]> get(Class<T> itemClass, T... itemsPool) {
            return get(itemsPool.length, itemClass, itemsPool);
        }

        public static <T> List<T[]> get(int size, Class<T> itemClass, T... itemsPool) {
            if (size < 1) {
                return new ArrayList<T[]>();
            }

            int itemsPoolCount = itemsPool.length;

            List<T[]> permutations = new ArrayList<T[]>();
            for (int i = 0; i < Math.pow(itemsPoolCount, size); i++) {
                T[] permutation = (T[]) Array.newInstance(itemClass, size);
                for (int j = 0; j < size; j++) {
                    // Pick the appropriate item from the item pool given j and i
                    int itemPoolIndex = (int) Math.floor((double) (i % (int) Math.pow(itemsPoolCount, j + 1)) / (int) Math.pow(itemsPoolCount, j));
                    permutation[j] = itemsPool[itemPoolIndex];
                }
                permutations.add(permutation);
            }

            return permutations;
        }
    }
}
