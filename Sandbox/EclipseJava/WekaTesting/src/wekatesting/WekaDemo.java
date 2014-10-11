package wekatesting;
import java.io.FileOutputStream;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.core.OptionHandler;
import weka.core.Utils;
import weka.filters.Filter;
import weka.core.converters.ConverterUtils.DataSource;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.Vector;


/*from:
 * http://stackoverflow.com/questions/20017957/how-to-reuse-saved-classifier-created-from-explorerin-weka-in-eclipse-java/20019031#20019031
 * */
/*
 * test from "predictionDistribution"
 * http://stackoverflow.com/questions/11960580/weka-classification-likelihood-of-the-classes
 * */
public class WekaDemo { 
	
	public static void main(String[] args) throws Exception{
	
		String rootPathmodel="C:\\Users\\LG\\Documents\\GitHub\\MDP\\Sandbox\\EclipseJava\\WekaTesting\\J48modelLocationWide.model"; 
		String rootPathtest ="LocationWidetest.arff";
		
    for(int i =0;i<50;i++)
		PrintPredictedDistribution(rootPathmodel,rootPathtest,i);
	
	
	}
	
 /**
  * @brief 	This methods takes an .arff file and returns the Prediction of the instancenum value.
  * 		If not present it uses the 1st one.
  * 		The test file shall have the same attributes than the one used to create the model 
  * 
  * @param modelpath complete path including model name "C:\\some\\where\\tree.model"
  * @param testArff complete path including arff name to test "C:\\some\\where\\totest.arff"
  * @param instancenum if needed a the distribution of a specific instance in a large arff file 
  * 
  * @return array with the Prediction distribution array in probabilities 
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


