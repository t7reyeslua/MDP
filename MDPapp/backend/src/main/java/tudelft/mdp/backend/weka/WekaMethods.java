package tudelft.mdp.backend.weka;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import tudelft.mdp.backend.records.DeviceMotionLocationRecord;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.Debug.Random;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.ConverterUtils.DataSource;

public class WekaMethods {

    private static final Logger LOG = Logger.getLogger(WekaMethods.class.getName());
    /**
     * Create a dataset as read from an .arff file.
     *
     * @param relation, name relation of the .arff , e.g. "Events"
     * @param motionAttributes, Smartwatch attributes
     * @param locationAttributes, Wifi networks attributes, watch that networks start with an alphabetic character
     * @param classAttributes, name of appliances
     * @param features; list of features, should be coherent in order with locationAttributes and then with motionAttributes,
     * 		watch out that for a non existing Motion, shall be filled with "?"'s
     **/
    public static Instances CreateInstanceSet2(String relation,ArrayList<String> usernames, ArrayList<String> motionAttributes,ArrayList<String> locationAttributes,ArrayList<String> classAttributes,ArrayList<String> features){

        int numOfAttributes = motionAttributes.size()+locationAttributes.size()+3;
        FastVector fvWekaAttributes = new FastVector(numOfAttributes);

        fvWekaAttributes.addElement(ALStringtoNominalAttribute("User",usernames));
        //Creating/adding the location attributes
        for(int i=0;i<locationAttributes.size();i++){
            fvWekaAttributes.addElement(StringtoNumericAttribute(locationAttributes.get(i)));
        }

        //Creating/adding the motion attributes
        for(int i=0;i<motionAttributes.size();i++){
            fvWekaAttributes.addElement(StringtoNumericAttribute(motionAttributes.get(i)));
        }

        //Creating/adding the Classes
        fvWekaAttributes.addElement(ALStringtoNominalAttribute("Activity",classAttributes));
        fvWekaAttributes.addElement(ALStringtoNominalAttribute("UserPredict",usernames));

        //Creating the dataset and adding the features
        Instances dataset = new Instances(relation, fvWekaAttributes, numOfAttributes);
        dataset.setClassIndex(0);

        //create the features (Instance) group

        for(int i=0;i<features.size();i++){
            String[] parts=features.get(i).split(",");
            Instance ft1Instance = new Instance(numOfAttributes);
            String attrDebug = "";

            try {
                //LOG.info(fvWekaAttributes.elementAt(0).toString() + "-" + parts[0]);
                attrDebug = fvWekaAttributes.elementAt(0).toString();
                ft1Instance.setValue((Attribute) fvWekaAttributes.elementAt(0), parts[0]);
                for (int j = 1; j < parts.length - 2; j++) {

                    boolean numtest = true;
                    double InstanceValue = 0;
                    try {
                        InstanceValue = Double.parseDouble(parts[j]);
                    } catch (NumberFormatException nfe) {
                        numtest = false;
                    }

                    if (numtest == true) {

                        attrDebug = fvWekaAttributes.elementAt(j).toString();
                        ft1Instance
                                .setValue((Attribute) fvWekaAttributes.elementAt(j), InstanceValue);
                    }


                }
                //System.out.println(parts[parts.length-1]);

                attrDebug = fvWekaAttributes.elementAt(parts.length - 2).toString();
                //LOG.info(fvWekaAttributes.elementAt(parts.length - 2).toString() + "-" + parts[       parts.length - 2]);
                ft1Instance.setValue((Attribute) fvWekaAttributes.elementAt(parts.length - 2),
                        parts[parts.length - 2]);
                //ft1Instance.setValue((Attribute)fvWekaAttributes.elementAt(parts.length-1),parts[parts.length-1]);

                dataset.add(ft1Instance);
            } catch (Exception e){
                LOG.severe(e.getLocalizedMessage() + "|" + e.getMessage() + "|" + attrDebug + "|" + features.get(i));
            }
        }

        return dataset;
    }


    public static Instances CreateInstanceSet1(String relation,ArrayList<String> usernames, ArrayList<String> motionAttributes,ArrayList<String> locationAttributes,ArrayList<String> classAttributes,ArrayList<String> features){

        int numOfAttributes = motionAttributes.size()+locationAttributes.size()+2;
        FastVector fvWekaAttributes = new FastVector(numOfAttributes);

        //Creating/adding the location attributes
        for(int i=0;i<locationAttributes.size();i++){
            fvWekaAttributes.addElement(StringtoNumericAttribute(locationAttributes.get(i)));
        }

        //Creating/adding the motion attributes
        for(int i=0;i<motionAttributes.size();i++){
            fvWekaAttributes.addElement(StringtoNumericAttribute(motionAttributes.get(i)));
        }

        //Creating/adding the Classes
        fvWekaAttributes.addElement(ALStringtoNominalAttribute("Activity",classAttributes));
        fvWekaAttributes.addElement(ALStringtoNominalAttribute("User",usernames));

        //Creating the dataset and adding the features
        Instances dataset = new Instances(relation, fvWekaAttributes, numOfAttributes);
        dataset.setClassIndex(numOfAttributes - 1);

        //create the features (Instance) group

        for(int i=0;i<features.size();i++){
            String[] parts=features.get(i).split(",");
            Instance ft1Instance = new Instance(numOfAttributes);
            String attrDebug = "";

            try {
                //LOG.info(fvWekaAttributes.elementAt(0).toString() + "-" + parts[0]);
                attrDebug = fvWekaAttributes.elementAt(0).toString();
                for (int j = 0; j < parts.length - 2; j++) {

                    boolean numtest = true;
                    double InstanceValue = 0;
                    try {
                        InstanceValue = Double.parseDouble(parts[j]);
                    } catch (NumberFormatException nfe) {
                        numtest = false;
                    }

                    if (numtest == true) {

                        attrDebug = fvWekaAttributes.elementAt(j).toString();
                        ft1Instance
                                .setValue((Attribute) fvWekaAttributes.elementAt(j), InstanceValue);
                    }


                }
                //System.out.println(parts[parts.length-1]);

                ft1Instance.setValue((Attribute) fvWekaAttributes.elementAt(parts.length - 2), parts[parts.length - 2]);
                ft1Instance.setValue((Attribute) fvWekaAttributes.elementAt(parts.length - 1), parts[parts.length - 1]);

                attrDebug = fvWekaAttributes.elementAt(parts.length - 2).toString();

                dataset.add(ft1Instance);
            } catch (Exception e){
                LOG.severe(e.getLocalizedMessage() + "|" + attrDebug + "|" + features.get(i));
            }
        }

        return dataset;
    }

    public static Instances CreateInstanceSet(String relation,ArrayList<String> usernames, ArrayList<String> motionAttributes,ArrayList<String> locationAttributes,ArrayList<String> classAttributes,ArrayList<String> features){

        int numOfAttributes = motionAttributes.size()+locationAttributes.size()+3;
        FastVector fvWekaAttributes = new FastVector(numOfAttributes);

        //Creating/adding the location attributes
        for(int i=0;i<locationAttributes.size();i++){
            fvWekaAttributes.addElement(StringtoNumericAttribute(locationAttributes.get(i)));
        }

        //Creating/adding the motion attributes
        for(int i=0;i<motionAttributes.size();i++){
            fvWekaAttributes.addElement(StringtoNumericAttribute(motionAttributes.get(i)));
        }

        //Creating/adding the Classes
        fvWekaAttributes.addElement(ALStringtoNominalAttribute("Activity",classAttributes));
        fvWekaAttributes.addElement(ALStringtoNominalAttribute("User",usernames));
        fvWekaAttributes.addElement(ALStringtoNominalAttribute("UserPredict", usernames));

        //Creating the dataset and adding the features
        Instances dataset = new Instances(relation, fvWekaAttributes, numOfAttributes);
        dataset.setClassIndex(numOfAttributes - 1);

        //create the features (Instance) group

        for(int i=0;i<features.size();i++){
            String[] parts=features.get(i).split(",");
            Instance ft1Instance = new Instance(numOfAttributes);
            String attrDebug = "";

            try {
                //LOG.info(fvWekaAttributes.elementAt(0).toString() + "-" + parts[0]);
                attrDebug = fvWekaAttributes.elementAt(0).toString();
                for (int j = 0; j < parts.length - 3; j++) {

                    boolean numtest = true;
                    double InstanceValue = 0;
                    try {
                        InstanceValue = Double.parseDouble(parts[j]);
                    } catch (NumberFormatException nfe) {
                        numtest = false;
                    }

                    if (numtest == true) {

                        attrDebug = fvWekaAttributes.elementAt(j).toString();
                        ft1Instance
                                .setValue((Attribute) fvWekaAttributes.elementAt(j), InstanceValue);
                    }


                }
                //System.out.println(parts[parts.length-1]);

                ft1Instance.setValue((Attribute) fvWekaAttributes.elementAt(parts.length - 3), parts[parts.length - 3]);
                ft1Instance.setValue((Attribute) fvWekaAttributes.elementAt(parts.length - 2), parts[parts.length - 2]);

                if (!parts[parts.length - 1].equals("?")) {
                    ft1Instance.setValue((Attribute) fvWekaAttributes.elementAt(parts.length - 1), parts[parts.length - 1]);
                }

                attrDebug = fvWekaAttributes.elementAt(parts.length - 3).toString();

                dataset.add(ft1Instance);
            } catch (Exception e){
                LOG.severe(e.getLocalizedMessage() + "|" + attrDebug + "|" + features.get(i));
            }
        }

        return dataset;
    }

    public static Instances CreateInstanceSetLocationExclusively(String relation,ArrayList<String> locationAttributes,ArrayList<String> classAttributes,ArrayList<String> features){

        int numOfAttributes = locationAttributes.size()+1;
        FastVector fvWekaAttributes = new FastVector(numOfAttributes);

        //Creating/adding the location attributes
        for(int i=0;i<locationAttributes.size();i++){
            fvWekaAttributes.addElement(StringtoNumericAttribute(locationAttributes.get(i)));
        }

        //Creating/adding the Classes
        fvWekaAttributes.addElement(ALStringtoNominalAttribute("Activity",classAttributes));
        //Creating the dataset and adding the features
        Instances dataset = new Instances(relation, fvWekaAttributes, numOfAttributes);
        dataset.setClassIndex(numOfAttributes - 1);

        //create the features (Instance) group

        for(int i=0;i<features.size();i++){
            String[] parts=features.get(i).split(",");
            Instance ft1Instance = new Instance(numOfAttributes);
            String attrDebug = "";

            try {
                //LOG.info(fvWekaAttributes.elementAt(0).toString() + "-" + parts[0]);
                attrDebug = fvWekaAttributes.elementAt(0).toString();
                for (int j = 0; j < parts.length - 1; j++) {

                    boolean numtest = true;
                    double InstanceValue = 0;
                    try {
                        InstanceValue = Double.parseDouble(parts[j]);
                    } catch (NumberFormatException nfe) {
                        numtest = false;
                    }

                    if (numtest == true) {

                        attrDebug = fvWekaAttributes.elementAt(j).toString();
                        ft1Instance
                                .setValue((Attribute) fvWekaAttributes.elementAt(j), InstanceValue);
                    }


                }
                //System.out.println(parts[parts.length-1]);

                ft1Instance.setValue((Attribute) fvWekaAttributes.elementAt(parts.length - 1), parts[parts.length - 1]);

                attrDebug = fvWekaAttributes.elementAt(parts.length - 1).toString();

                dataset.add(ft1Instance);
            } catch (Exception e){
                LOG.severe(e.getLocalizedMessage() + "|" + attrDebug + "|" + features.get(i));
            }
        }

        return dataset;
    }

    public static Instances CreateInstanceSetMotionExclusively(String relation, ArrayList<String> motionAttributes,ArrayList<String> classAttributes,ArrayList<String> features){

        int numOfAttributes = motionAttributes.size()+1;
        FastVector fvWekaAttributes = new FastVector(numOfAttributes);

        //Creating/adding the motion attributes
        for(int i=0;i<motionAttributes.size();i++){
            fvWekaAttributes.addElement(StringtoNumericAttribute(motionAttributes.get(i)));
        }

        //Creating/adding the Classes
        fvWekaAttributes.addElement(ALStringtoNominalAttribute("Activity",classAttributes));

        //Creating the dataset and adding the features
        Instances dataset = new Instances(relation, fvWekaAttributes, numOfAttributes);
        dataset.setClassIndex(numOfAttributes - 1);

        //create the features (Instance) group

        for(int i=0;i<features.size();i++){
            String[] parts=features.get(i).split(",");
            Instance ft1Instance = new Instance(numOfAttributes);
            String attrDebug = "";

            try {
                //LOG.info(fvWekaAttributes.elementAt(0).toString() + "-" + parts[0]);
                attrDebug = fvWekaAttributes.elementAt(0).toString();
                for (int j = 0; j < parts.length - 1; j++) {

                    boolean numtest = true;
                    double InstanceValue = 0;
                    try {
                        InstanceValue = Double.parseDouble(parts[j]);
                    } catch (NumberFormatException nfe) {
                        numtest = false;
                    }

                    if (numtest == true) {

                        attrDebug = fvWekaAttributes.elementAt(j).toString();
                        ft1Instance
                                .setValue((Attribute) fvWekaAttributes.elementAt(j), InstanceValue);
                    }


                }
                //System.out.println(parts[parts.length-1]);

                ft1Instance.setValue((Attribute) fvWekaAttributes.elementAt(parts.length - 1), parts[parts.length - 1]);
                attrDebug = fvWekaAttributes.elementAt(parts.length - 1).toString();

                dataset.add(ft1Instance);
            } catch (Exception e){
                LOG.severe(e.getLocalizedMessage() + "|" + attrDebug + "|" + features.get(i));
            }
        }

        return dataset;
    }

    /**
     * Create a dataset as readed from an .arff file, only for Location.
     *
     * @param relation, name relation of the .arff , e.g. "Events"
     * @param locationAttributes, Wifi networks attributes, watch that networks start with an alphabetic character
     * @param classAttributes, name of appliances
     * @param features; list of features, should be coherent in order with locationAttributes and then with motionAttributes,
     * 		watch out that for a non existing Motion, shall be filled with "?"'s
     **/
    public static Instances CreateLocationInstanceSet(String relation,ArrayList<String> locationAttributes,ArrayList<String> classAttributes,ArrayList<String> features){

        int numofAttributes = locationAttributes.size()+1;
        FastVector fvWekaAttributes = new FastVector(numofAttributes);

        //Creating/adding the location attributes
        for(int i=0;i<locationAttributes.size();i++){
            fvWekaAttributes.addElement(StringtoNumericAttribute(locationAttributes.get(i)));
        }


        //Creating/adding the Classes
        fvWekaAttributes.addElement(ALStringtoNominalAttribute("Activity",classAttributes));

        //Creating the dataset and adding the features
        Instances dataset = new Instances(relation, fvWekaAttributes, numofAttributes);
        dataset.setClassIndex(numofAttributes - 1);

        //create the features (Instance) group

        for(int i=0;i<features.size();i++){
            String[] parts=features.get(i).split(",");
            Instance ft1Instance = new Instance(numofAttributes);
            for(int j=0;j<parts.length-1;j++){

                boolean numtest=true;
                double InstanceValue=0;
                try
                {
                    InstanceValue = Double.parseDouble(parts[j]);
                }
                catch(NumberFormatException nfe)
                {
                    numtest= false;
                }

                if(numtest==true)
                    ft1Instance.setValue((Attribute)fvWekaAttributes.elementAt(j),InstanceValue);


            }
            System.out.println(parts[parts.length-1]);

            ft1Instance.setValue((Attribute)fvWekaAttributes.elementAt(parts.length-1),parts[parts.length-1]);

            dataset.add(ft1Instance);
        }


        return dataset;
    }

    public static Instances CreateMockInstanceSetTimestamp(String relation,ArrayList<String> locationAttributes, ArrayList<String> motionAttributes,ArrayList<String> classAttributes,ArrayList<String> features){

        int numofAttributes = 4;
        FastVector fvWekaAttributes = new FastVector(numofAttributes);


        //Creating/adding the Classes
        fvWekaAttributes.addElement(StringtoNumericAttribute("Timestamp"));
        fvWekaAttributes.addElement(ALStringtoNominalAttribute("Motion",motionAttributes));
        fvWekaAttributes.addElement(ALStringtoNominalAttribute("Location",locationAttributes));
        fvWekaAttributes.addElement(ALStringtoNominalAttribute("Activity",classAttributes));


        //Creating the dataset and adding the features
        Instances dataset = new Instances(relation, fvWekaAttributes, numofAttributes);
        dataset.setClassIndex(numofAttributes - 1);

        LOG.info("No. classes: " + dataset.numClasses());
        LOG.info("Timestamp attr: " + dataset.attribute(0).toString() + "|" + dataset.attribute(0).name());
        LOG.info("Motion attr: " + dataset.attribute(1).toString() + "|" + dataset.attribute(1).name() + ":" + dataset.attribute(1).numValues());
        LOG.info("Location attr: " + dataset.attribute(2).toString() + "|" + dataset.attribute(2).name() + ":" + dataset.attribute(2).numValues());
        LOG.info("Activity attr: " + dataset.attribute(3).toString() + "|" + dataset.attribute(3).name() + ":" + dataset.attribute(3).numValues());
        //create the features (Instance) group

        for(int i=0;i<features.size();i++){
            String[] parts=features.get(i).split(",");
            //LOG.info(i + "|" +features.get(i));
            Instance ft1Instance = new Instance(numofAttributes);

            try
            {
                ft1Instance.setValue((Attribute)fvWekaAttributes.elementAt(0),Double.parseDouble(parts[0]));
            }
            catch(NumberFormatException nfe)
            {
                LOG.warning("Timestamp oops");
            }

            if (!parts[1].equals("?")) {
                ft1Instance.setValue((Attribute) fvWekaAttributes.elementAt(1), parts[1]);
            }
            if (!parts[2].equals("?")) {
                ft1Instance.setValue((Attribute)fvWekaAttributes.elementAt(2),parts[2]);
            }
            ft1Instance.setValue((Attribute)fvWekaAttributes.elementAt(3),parts[3]);
            //LOG.info(ft1Instance.toString());
            dataset.add(ft1Instance);
        }

        return dataset;
    }

    public static Instances CreateMockInstanceSet(String relation,ArrayList<String> locationAttributes, ArrayList<String> motionAttributes,ArrayList<String> classAttributes,ArrayList<String> features){

        int numofAttributes = 3;
        FastVector fvWekaAttributes = new FastVector(numofAttributes);


        //Creating/adding the Classes
        fvWekaAttributes.addElement(ALStringtoNominalAttribute("Motion",motionAttributes));
        fvWekaAttributes.addElement(ALStringtoNominalAttribute("Location",locationAttributes));
        fvWekaAttributes.addElement(ALStringtoNominalAttribute("Activity",classAttributes));


        //Creating the dataset and adding the features
        Instances dataset = new Instances(relation, fvWekaAttributes, numofAttributes);
        dataset.setClassIndex(numofAttributes - 1);

        LOG.info("No. classes: " + dataset.numClasses());
        LOG.info("Motion attr: " + dataset.attribute(0).toString() + "|" + dataset.attribute(0).name() + ":" + dataset.attribute(0).numValues());
        LOG.info("Location attr: " + dataset.attribute(1).toString() + "|" + dataset.attribute(1).name() + ":" + dataset.attribute(1).numValues());
        LOG.info("Activity attr: " + dataset.attribute(2).toString() + "|" + dataset.attribute(2)
                .name() + ":" + dataset.attribute(2).numValues());
        //create the features (Instance) group

        for(int i=0;i<features.size();i++){
            String[] parts=features.get(i).split(",");
            //LOG.info(i + "|" +features.get(i));
            Instance ft1Instance = new Instance(numofAttributes);
            if (!parts[0].equals("?")) {
                ft1Instance.setValue((Attribute) fvWekaAttributes.elementAt(0), parts[0]);
            }
            if (!parts[1].equals("?")) {
                ft1Instance.setValue((Attribute)fvWekaAttributes.elementAt(1),parts[1]);
            }
            ft1Instance.setValue((Attribute)fvWekaAttributes.elementAt(2),parts[2]);
            //LOG.info(ft1Instance.toString());
            dataset.add(ft1Instance);
        }

        return dataset;
    }

    public static Instances CreateMockInstanceSetAllIn1(int nUsers, String relation,ArrayList<String> locationAttributes, ArrayList<String> motionAttributes,ArrayList<String> classAttributes,ArrayList<String> features){

        int numofAttributes = (nUsers * 2) + 1;
        FastVector fvWekaAttributes = new FastVector(numofAttributes);


        //Creating/adding the Classes

        for (int i = 0; i<nUsers; i++){
            fvWekaAttributes.addElement(ALStringtoNominalAttribute("Motion" + i, motionAttributes));
            fvWekaAttributes.addElement(ALStringtoNominalAttribute("Location" + i, locationAttributes));
        }

        fvWekaAttributes.addElement(ALStringtoNominalAttribute("Activity",classAttributes));


        //Creating the dataset and adding the features
        Instances dataset = new Instances(relation, fvWekaAttributes, numofAttributes);
        dataset.setClassIndex(numofAttributes - 1);

        LOG.info("No. classes: " + dataset.numClasses());
        for (int i = 0; i < dataset.numAttributes(); i++){
            LOG.info(dataset.attribute(i).toString() + "|" + dataset.attribute(i).name() + ":" + dataset.attribute(i).numValues());
        }
        //create the features (Instance) group

        for(int i=0;i<features.size();i++){
            String[] parts=features.get(i).split(",");
            LOG.info(i + "|" +features.get(i));
            Instance ft1Instance = new Instance(numofAttributes);
            for (int k = 0; k < numofAttributes; k++){
                if (!parts[k].equals("?")) {
                    ft1Instance.setValue((Attribute) fvWekaAttributes.elementAt(k), parts[k]);
                }
            }
            dataset.add(ft1Instance);
        }

        return dataset;
    }

    /**
     * @param InstancetoEval
     * @param cls: Clasifier WITH model
     * @return String with predicted distribution and ist classes: A,0.5|B,0.5
     */
    public static String GetPredictionDistributionOnline(Instances InstancetoEval,Classifier cls){
        String predictedDistribution="";
        try {
            if (InstancetoEval.classIndex() == -1)
                InstancetoEval.setClassIndex(InstancetoEval.numAttributes() - 1);

            double[] DistributionVector = cls.distributionForInstance(InstancetoEval.instance(0));

            for (int i = 0; i < InstancetoEval.numClasses() - 1; i++) {
                String nameClass = InstancetoEval.classAttribute().value(i);
                predictedDistribution = predictedDistribution + nameClass + ","
                        + DistributionVector[i] + "|";

            }

            predictedDistribution = predictedDistribution + InstancetoEval.classAttribute()
                    .value(InstancetoEval.numClasses() - 1) + "," + DistributionVector[
                    InstancetoEval.numClasses() - 1];

            return  predictedDistribution;
        } catch (Exception e){
            return "Some-Problem,1.00";
        }
    }

    /**
     * @param InstancetoEval
     * @param cls: Clasifier WITH model
     * @return String with predicted distribution and ist classes: A,0.5|B,0.5
     */
    public static String GetPredictionDistributionOnlineMotionLocation(Instances InstancetoEval,Classifier cls){
        String predictedDistribution="";
        try {
            if (InstancetoEval.classIndex() == -1) {
                InstancetoEval.setClassIndex(InstancetoEval.numAttributes()-1);
            }


            LOG.warning("Class Index:" + InstancetoEval.classIndex()
                    + " | " + InstancetoEval.attribute(InstancetoEval.classIndex()).name()
                    + " | " + InstancetoEval.attribute(InstancetoEval.classIndex()).toString());

            double[] DistributionVector = cls.distributionForInstance(InstancetoEval.instance(0));

            for (int i = 0; i < InstancetoEval.numClasses() - 1; i++) {
                String nameClass = InstancetoEval.classAttribute().value(i);
                predictedDistribution = predictedDistribution + nameClass + ","
                        + DistributionVector[i] + "|";

            }

            predictedDistribution = predictedDistribution + InstancetoEval.classAttribute()
                    .value(InstancetoEval.numClasses() - 1) + "," + DistributionVector[
                    InstancetoEval.numClasses() - 1];

            return  predictedDistribution;
        } catch (Exception e){
            return "Some-Problem,1.00";
        }
    }

    /**
     *
     * @param OriginalSet:original intances of dataset
     * @param eval: Single String having headers \n values
     * HashMap<String, Integer> attributesHashMap = createAttributesHashMap(wekaInstances);
     * @return Instace to eval to use with GetPredictionDistributionOnline
     */
    public static Instances CreateLocationInstanceToEval(Instances OriginalSet,String eval){
        try {
            HashMap<String, Integer> attributesHashMap = createAttributesHashMap(OriginalSet);

            /*
            LOG.info("AttributesHashMap created");
            for (String attr : attributesHashMap.keySet()){
                LOG.info(attr + " " + attributesHashMap.get(attr));
            }*/

            Instances datasettoEval = OriginalSet;
            datasettoEval.delete();

            if (datasettoEval.classIndex() == -1)
                datasettoEval.setClassIndex(datasettoEval.numAttributes() - 1);

            int numofAttributes = OriginalSet.numAttributes();
            LOG.info("Original set no. attr: " + numofAttributes);

            //Inserting the One only instance
            Instance ft1Instance = new Instance(numofAttributes);
            String[] parts = eval.split("\\n");
            String allAttributesFromEval = parts[0];
            LOG.info("Attr from ToEval: " + allAttributesFromEval);

            String allvaluesFromEval = parts[1];
            LOG.info("Values from ToEval: " + allvaluesFromEval);


            String[] attributesFromEval = allAttributesFromEval.split(",");
            String[] valuesFromEval = allvaluesFromEval.split(",");

            for (int i = 0; i < attributesFromEval.length; i++) {
                Integer index = getAttributeIndex(attributesHashMap, attributesFromEval[i]);
               // LOG.info(attributesFromEval[i] + " index " + index + " |i:" + i);
                if (index > -1) {
//            	 System.out.println(valuesFromEval[i]);
                    ft1Instance.setValue(datasettoEval.attribute(index),
                            Double.parseDouble(valuesFromEval[i]));
                }
            }

            //LOG.info(valuesFromEval[valuesFromEval.length-1]);
            //ft1Instance.setValue(datasettoEval.attribute(datasettoEval.numAttributes() - 1),valuesFromEval[valuesFromEval.length - 1]);

            //Checking compatibility
            if (OriginalSet.checkInstance(ft1Instance) == false) {
                LOG.info("Err:Instances Not compatible");
            }

            datasettoEval.add(ft1Instance);
            LOG.info("Add ft1instnce to datasetToEval");

            return datasettoEval;
        } catch (Exception e){
            return null;
        }
    }




    public static Instances CreateInstanceSetToEval(Instances OriginalSet, DeviceMotionLocationRecord recordToEval){

        try {
            HashMap<String, Integer> attributesHashMap = createAttributesHashMap(OriginalSet);

            /*
            LOG.info("AttributesHashMap created");
            for (String attr : attributesHashMap.keySet()){
                LOG.info(attr + " " + attributesHashMap.get(attr));
            }*/

            Instances datasettoEval = OriginalSet;
            datasettoEval.delete();

            if (datasettoEval.classIndex() == -1) {
                datasettoEval.setClassIndex(datasettoEval.numAttributes()-1);
            }

            LOG.warning("Class Index:" + datasettoEval.classIndex()
                    + " | " + datasettoEval.attribute(datasettoEval.classIndex()).name()
                    + " | " + datasettoEval.attribute(datasettoEval.classIndex()).toString());

            int numofAttributes = datasettoEval.numAttributes();
            LOG.info("Original set no. attr: " + numofAttributes);

            //Inserting the One only instance
            Instance ft1Instance = new Instance(numofAttributes);

            //Adding username
           /* WekaUtils wekaUtils = new WekaUtils();
            String username = wekaUtils.removeAccents(recordToEval.getUsername().replaceAll("\\s",""));
            ft1Instance.setValue(datasettoEval.attribute(0), username);
            LOG.warning("Username:" + ft1Instance.toString(0));*/

            //Adding event
            String activity = recordToEval.getEvent().replaceAll("\\s", "");
            ft1Instance.setValue(datasettoEval.attribute(datasettoEval.numAttributes()-2), activity);
            LOG.warning("Activity:" + activity +"|" + ft1Instance.toString(datasettoEval.numAttributes()-2));


            //Adding location attr
            String[] parts = recordToEval.getLocationFeatures().getValue().split("\\n");
            String allAttributesFromEval = parts[0];
            LOG.info("Location Attr from ToEval: " + allAttributesFromEval);

            String allvaluesFromEval = parts[1];
            LOG.info("Location Values from ToEval: " + allvaluesFromEval);


            String[] attributesFromEval = allAttributesFromEval.split(",");
            String[] valuesFromEval = allvaluesFromEval.split(",");

            for (int i = 0; i < attributesFromEval.length; i++) {
                Integer index = getAttributeIndex(attributesHashMap, attributesFromEval[i]);
                // LOG.info(attributesFromEval[i] + " index " + index + " |i:" + i);
                if (index > -1) {
                    ft1Instance.setValue(datasettoEval.attribute(index), Double.parseDouble(valuesFromEval[i]));
                }
            }


            //Adding Motion Attr
            String motionAttr = recordToEval.getMotionFeatures().getValue();
            String[] motionValues = motionAttr.split(",");
            if (!motionAttr.contains("?")){
                Integer index = getAttributeIndex(attributesHashMap, "mean_ACCX");
                LOG.info("Initial motion attr index " + index);

                int j = 0;
                for (int i = index; i < (index+126); i++){
                    ft1Instance.setValue(datasettoEval.attribute(i), Double.parseDouble(motionValues[j++]));
                }

            }

            LOG.warning(ft1Instance.toString());

            //Checking compatibility
            if (!OriginalSet.checkInstance(ft1Instance)) {
                LOG.info("Err:Instances Not compatible");
            }

            datasettoEval.add(ft1Instance);
            LOG.info("Add ft1instnce to datasetToEval");

            return datasettoEval;
        } catch (Exception e){
            return null;
        }

    }


    public static HashMap<String, Integer> createAttributesHashMap(Instances wekaInstances){
        HashMap<String, Integer> attributesHashMap = new HashMap<String, Integer>();
        for (int i = 0; i < wekaInstances.numAttributes(); i++){
            attributesHashMap.put(wekaInstances.attribute(i).name(), i);
        }
        return attributesHashMap;
    }

    public static Integer getAttributeIndex(HashMap<String, Integer> attributesHashMap, String attribute){
        Integer index = -1;
        if (attributesHashMap.containsKey(attribute)){
            index = attributesHashMap.get(attribute);
        }
        return index;
    }


    /**
     * Create a NUMERIC Attribute "@attribute NAME numeric" from a name
     * @param attributeName, strings with name of the Attribute
     **/
    public static Attribute StringtoNumericAttribute(String attributeName){
        Attribute attributeobj = new Attribute(attributeName);
        return attributeobj;
    }
    /**
     * @brief 	Create a NOMINAL Attribute "@attribute NAME {Value1,Value2...}" from a Name and ArrayList
     * Same method can be use for the Classes
     * @param attributeName, strings with name of the Attribute
     * @param valueList, Nominal values of the Attribute
     **/
    public static Attribute ALStringtoNominalAttribute(String attributeName,ArrayList<String> valueList){
        FastVector fvNominalVal = new FastVector(valueList.size());

        for(int i=0;i<valueList.size();i++){
            fvNominalVal.addElement(valueList.get(i));
        }

        Attribute attributeobj = new Attribute(attributeName, fvNominalVal);

        return attributeobj;
    }



}
