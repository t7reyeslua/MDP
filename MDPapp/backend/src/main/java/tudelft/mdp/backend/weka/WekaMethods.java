package tudelft.mdp.backend.weka;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

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
    public static Instances CreateInstanceSet(String relation,ArrayList<String> motionAttributes,ArrayList<String> locationAttributes,ArrayList<String> classAttributes,ArrayList<String> features){

        int numofAttributes = motionAttributes.size()+locationAttributes.size()+1;
        FastVector fvWekaAttributes = new FastVector(numofAttributes);

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

    /**
     * @param InstancetoEval
     * @param cls: Clasifier WITH model
     * @return String with predicted distribution and ist classes: A,0.5|B,0.5
     * @throws Exception
     */
    public static String GetPredictionDistributionOnline(Instances InstancetoEval,Classifier cls) throws Exception{
        String predictedDistribution="";
        if (InstancetoEval.classIndex() == -1)
            InstancetoEval.setClassIndex(InstancetoEval.numAttributes() - 1);



        double[] DistributionVector = cls.distributionForInstance(InstancetoEval.instance(0));

        for(int i=0;i<InstancetoEval.numClasses()-1;i++){
            String nameClass = InstancetoEval.classAttribute().value(i);
            predictedDistribution=predictedDistribution+nameClass+ ","+DistributionVector[i]+"|";

        }

        predictedDistribution=predictedDistribution+InstancetoEval.classAttribute().value(InstancetoEval.numClasses()-1)+ ","+DistributionVector[InstancetoEval.numClasses()-1];


        return  predictedDistribution;
    }

    /**
     *
     * @param OriginalSet:original intances of dataset
     * @param eval: Single String having headers \n values
     * HashMap<String, Integer> attributesHashMap = createAttributesHashMap(wekaInstances);
     * @return Instace to eval to use with GetPredictionDistributionOnline
     * @throws Exception
     */
    public static Instances CreateLocationInstanceToEval(Instances OriginalSet,String eval) throws Exception{
        HashMap<String, Integer> attributesHashMap = createAttributesHashMap(OriginalSet);

        Instances datasettoEval= OriginalSet;
        datasettoEval.delete();

        if (datasettoEval.classIndex() == -1)
            datasettoEval.setClassIndex(datasettoEval.numAttributes() - 1);

        int numofAttributes = OriginalSet.numAttributes();

        //Inserting the One only instance
        Instance ft1Instance = new Instance(numofAttributes);
        String[] parts = eval.split("\\n");
        String allAttributesFromEval = parts[0];


        String allvaluesFromEval = parts[1];


        String[] attributesFromEval = allAttributesFromEval.split(",");
        String[] valuesFromEval = allvaluesFromEval.split(",");

        for (int i=0;i<attributesFromEval.length;i++){
            Integer index = getAttributeIndex(attributesHashMap, attributesFromEval[i]);

            if (index > -1) {
//            	 System.out.println(valuesFromEval[i]);
                ft1Instance.setValue(datasettoEval.attribute(index),Double.parseDouble(valuesFromEval[i]));
            }
        }

//        System.out.println(valuesFromEval[valuesFromEval.length-1]);
        ft1Instance.setValue(datasettoEval.attribute(datasettoEval.numAttributes()-1),valuesFromEval[valuesFromEval.length-1]);

        //Checking compatibility
        if(OriginalSet.checkInstance(ft1Instance)==false)
            System.out.println("Err:Instances Not compatible");

        datasettoEval.add(ft1Instance);

        return datasettoEval;
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
