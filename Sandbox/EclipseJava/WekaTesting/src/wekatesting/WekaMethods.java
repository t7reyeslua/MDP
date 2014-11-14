package wekatesting;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

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
	  * @brief 	Create a dataset as readed from an .arff file.
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
	
		//Creating/adding the motion attributes
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

			ft1Instance.setValue((Attribute)fvWekaAttributes.elementAt(parts.length-1),parts[parts.length-1]); 
			
			dataset.add(ft1Instance);
		}
				
		return dataset;
	}
	/**
	  * @brief 	Create a dataset as readed from an .arff file, only for Location.
	  * @param relation, name relation of the .arff , e.g. "Events"
	  * @param locationAttributes, Wifi networks attributes, watch that networks start with an alphabetic character
	  * @param classAttributes, name of appliances
	  * @param features; list of features, should be coherent in order with locationAttributes and then with motionAttributes,
	  * 		watch out that for a non existing Motion, shall be filled with "?"'s
	  **/  
	public static Instances CreateLocationInstanceSet(String relation,ArrayList<String> locationAttributes,ArrayList<String> classAttributes,ArrayList<String> features){

		int numofAttributes = locationAttributes.size()+1;
		FastVector fvWekaAttributes = new FastVector(numofAttributes);
	
		//Creating/adding the motion attributes
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

				ft1Instance.setValue((Attribute)fvWekaAttributes.elementAt(j),Double.parseDouble(parts[j])); 
			   }

			ft1Instance.setValue((Attribute)fvWekaAttributes.elementAt(parts.length-1),parts[parts.length-1]); 
			
			dataset.add(ft1Instance);
		}
				
		return dataset;
	}
	
	/**
	  * @brief 	Create .arff file
	  * @param dataset, a complete dataset (intances)
	  * @param namepath, as "/data/test.arff"
	 * @throws IOException 
	  */
	public static void Intances2Arff(Instances dataset,String namepath) throws IOException{
		
		 ArffSaver saver = new ArffSaver();
		 saver.setInstances(dataset);
		 saver.setFile(new File(namepath));
		 saver.writeBatch();
		 
	}
	
	
	/**
	  * @brief 	Create a NUMERIC Attribute "@attribute NAME numeric" from a name
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
	  * @see http://weka.wikispaces.com/Programmatic+Use
	  **/  
	public static Attribute ALStringtoNominalAttribute(String attributeName,ArrayList<String> valueList){
		FastVector fvNominalVal = new FastVector(valueList.size());
		
		for(int i=0;i<valueList.size();i++){
			fvNominalVal.addElement(valueList.get(i));
		}
		
		Attribute attributeobj = new Attribute(attributeName, fvNominalVal);
		
		return attributeobj;
	}
	
	
	/**
	  * @brief 	Serialization Method for J48
	  * @param modelpathnname, Fullpath including the name to save ("Name.model")
	  * @param arffpathnname, Fullpath including the name of the .arff (NAME.arff)
	  * 
	  **/  
	public static void CreateModelJ48(String modelpathnname,String arffpathnname)throws Exception{
		
		// create J48
		 Classifier cls = new J48();
		 
		 // train
		 Instances inst = new Instances(new BufferedReader(new FileReader(arffpathnname)));
		 inst.setClassIndex(inst.numAttributes() - 1);
		 cls.buildClassifier(inst);
		 weka.core.SerializationHelper.write(modelpathnname, cls);		
	}
	
	/**
	  * @brief 	Serialization Method for J48
	  * @param modelpathnname, Fullpath including the name to save ("C:\\some\\where\\to\\write\\test.model")
	  * @param arffpathnname, Fullpath including the name of the .arff ("C:\\some\\where\\to\\read\\test.arff")
	  * @param folds, number of folds (typically 10)
	  * 
	  * @return Prints the summary, of correct classifiers
	  **/  
	public static String FoldEvaluation(String modelpathnname,String arffpathnname, int folds)throws Exception{
		Instances data = new Instances(new BufferedReader(new FileReader(arffpathnname)));
		
		 Classifier cls = (Classifier) weka.core.SerializationHelper.read(modelpathnname);
		 if (data.classIndex() == -1)
			 data.setClassIndex(data.numAttributes() - 1);
		 Evaluation eval = new Evaluation(data);
		 Random rand = new Random(1);  // using seed = 1
		 eval.crossValidateModel(cls, data, folds, rand);
		 
		return eval.toSummaryString();		
	}
		
	
	/**
	  * @brief 	This methods takes an .arff file and returns the Prediction of the instance num value.
	  * 		If not present it uses the 1st one.
	  * 		The test file shall have the same attributes than the one used to create the model 
	  * 
	  * @param modelpath, Fullpath including the name to save ("C:\\some\\where\\to\\write\\test.model")
	  * @param testArff, Fullpath including the name of the .arff ("C:\\some\\where\\to\\read\\test.arff")
	  * @param instancenum if needed a the distribution of a specific instance in a large arff file 
	  * 
	  * @return array with the Prediction distribution array in probabilities 
	  * 
	  * @ref	http://stackoverflow.com/questions/20017957/how-to-reuse-saved-classifier-created-from-explorerin-weka-in-eclipse-java/20019031#20019031
	  * 		
	  * "predictionDistribution"
	  * 		http://stackoverflow.com/questions/11960580/weka-classification-likelihood-of-the-classes
	  * */
		public static double[] GetPredictionDistribution(String modelpath,String testArff)throws Exception{
			
			Classifier cls = (Classifier) weka.core.SerializationHelper.read(modelpath);
			DataSource source = new DataSource(testArff);//get instances from test file
			Instances instances = source.getDataSet();
			 if (instances.classIndex() == -1)
				 instances.setClassIndex(instances.numAttributes() - 1);
			double[] predictionDistribution = cls.distributionForInstance(instances.instance(0)); 
			 

			return  predictionDistribution;
		}
		public static double[] GetPredictionDistribution(String modelpath,String testArff,int instancenum)throws Exception{
			
			Classifier cls = (Classifier) weka.core.SerializationHelper.read(modelpath);
			DataSource source = new DataSource(testArff);//get instances from test file
			Instances instances = source.getDataSet();
			 if (instances.classIndex() == -1)
				 instances.setClassIndex(instances.numAttributes() - 1);
			double[] predictionDistribution = cls.distributionForInstance(instances.instance(instancenum)); 
			
			return  predictionDistribution;
		}
		
		/**
		  * @brief 	Similar to GetPredictionDistribution prints such distribution.
		  * 
		  * @param modelpath complete path including model name "C:\\some\\where\\tree.model"
		  * @param testArff complete path including arff name to test "C:\\some\\where\\totest.arff"
		  * @param instancenum if needed a the distribution of a specific instance in a large arff file 
		  * 
		  * @return array with the Prediction distribution array in probabilities 
		  * */
		public static void PrintPredictedDistribution(String modelpath,String testArff,int instanceNum)throws Exception{

			Classifier cls = (Classifier) weka.core.SerializationHelper.read(modelpath);

			DataSource source = new DataSource(testArff);//get instances from test file

			Instances instances = source.getDataSet();
			if (instances.classIndex() == -1)
				 instances.setClassIndex(instances.numAttributes() - 1);

			//perform your prediction
			double value=cls.classifyInstance(instances.instance(instanceNum));
			
			double[] predictionDistribution = cls.distributionForInstance(instances.instance(instanceNum));		
					
			//get the name of the class value
			String prediction=instances.classAttribute().value((int)value); 
			
			int predictionposition=-1;
			for(int i=0;i<instances.numClasses();i++){
				if(prediction == instances.classAttribute().value((int) i))
					predictionposition=i;
			}

			System.out.printf("Prediction of instance %d is %10s with  %6.3f%% \t", 
					instanceNum,prediction,predictionDistribution[predictionposition]);

		    for (int predictionDistributionIndex = 0; 
		            predictionDistributionIndex < predictionDistribution.length; 
		            predictionDistributionIndex++)
		       {
		           // Get this distribution index's class label.
		           String predictionDistributionIndexAsClassLabel = 
		        		   instances.classAttribute().value(
		                   predictionDistributionIndex);

		           // Get the probability.
		           double predictionProbability = 
		               predictionDistribution[predictionDistributionIndex];

		           System.out.printf("[%s : %6.3f]", 
		                             predictionDistributionIndexAsClassLabel, 
		                             predictionProbability );
		       }
		    System.out.printf("\n");
		}
		
		

		
}
