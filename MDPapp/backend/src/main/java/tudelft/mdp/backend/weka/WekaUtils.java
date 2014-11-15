package tudelft.mdp.backend.weka;

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

    public void createInstanceSet(List<DeviceMotionLocationRecord> records, String minDate, String maxDate){
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
            features.add(locationFeatures + "," + motionFeatures + "," + classAttribute);
        }

        Instances wekaInstances = WekaMethods.CreateInstanceSet(relation,
                motionAttributes,
                locationAttributes,
                classAttributes,
                features);

        storeWekaObjects(wekaInstances, minDate, maxDate);
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

    private void storeWekaObjects(Instances wekaInstances, String minDate, String maxDate){
        try {
            byte[] serializedWekaObject = Utils.serialize(wekaInstances);
            LOG.info(Utils.humanReadableByteCount(serializedWekaObject.length, true));

            String description = "Motion-Location records from: " + minDate + " to " + maxDate;
            String timestamp = Utils.getCurrentTimestamp();


            LOG.info(wekaInstances.toSummaryString());

            saveArffToGcs(wekaInstances, "Arff", description, timestamp);
            saveInstancesToGcs(serializedWekaObject, "Instance", description, timestamp);
            saveClsToGcs(createClassifier(wekaInstances,
                            WekaClassifierTypes.J48), "Classifier",
                    description + " | " + getModelType(WekaClassifierTypes.J48), timestamp,
                    WekaClassifierTypes.J48);

        } catch (Exception e){
            LOG.severe(e.getMessage());
        }
    }

    private void saveClsToGcs(Classifier object, String objectType, String description, String timestamp, int clsType){
        try {

            LOG.info(object.toString());

            String filename = objectType + "_" + getModelType(WekaClassifierTypes.J48) + "_" +timestamp + ".model";
            GcsHelper gcsHelper = new GcsHelper();
            gcsHelper.writeWekaClsToGCS(filename, object);
            saveGcsFileDescriptionToDatastore(filename, objectType, description, timestamp);
        } catch (Exception e){
            LOG.severe(e.getMessage());
        }
    }

    private void saveArffToGcs(Instances object, String objectType, String description, String timestamp){
        try {
            String filename = objectType + "_" + timestamp + ".arff";
            GcsHelper gcsHelper = new GcsHelper();
            gcsHelper.writeWekaArffToGCS(filename, object);
            saveGcsFileDescriptionToDatastore(filename, objectType, description, timestamp);
        } catch (Exception e){
            LOG.severe(e.getMessage());
        }
    }

    private void saveInstancesToGcs(byte[] object, String objectType, String description, String timestamp){
        try {
            String filename = objectType + "_" + timestamp + ".instances";
            GcsHelper gcsHelper = new GcsHelper();
            gcsHelper.writeWekaInstanceToGCS(filename, object);
            saveGcsFileDescriptionToDatastore(filename, objectType, description, timestamp);
        } catch (Exception e){
            LOG.severe(e.getMessage());
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

}
