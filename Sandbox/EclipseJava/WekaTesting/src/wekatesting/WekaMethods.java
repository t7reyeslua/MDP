package wekatesting;

import java.io.BufferedReader;
import java.io.FileReader;

import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class WekaMethods {
	

	
	/**
	  * @brief 	Serialization Method for J48
	  * 
	  **/  
	public static void CreateModelJ48(String modelpath,String testArff,int instanceNum)throws Exception{
		// create J48
		 Classifier cls = new J48();
		 
		 // train
		 Instances inst = new Instances(new BufferedReader(new FileReader(testArff)));
		 inst.setClassIndex(inst.numAttributes() - 1);
		 cls.buildClassifier(inst);
		 weka.core.SerializationHelper.write(modelpath, cls);		
	}
		
	/**
	  * @brief 	Serialization Method for NaiveBayes,default config
	  * 
	  **/  
	public static void CreateModelNaiveBayes(String modelpath,String testArff,int instanceNum)throws Exception{
		// create J48
		 Classifier cls = new NaiveBayes();
		 
		 // train
		 Instances inst = new Instances(new BufferedReader(new FileReader(testArff)));
		 inst.setClassIndex(inst.numAttributes() - 1);
		 cls.buildClassifier(inst);
		 weka.core.SerializationHelper.write(modelpath, cls);		
	}
		
//	/**
//	  * @brief 	Serialization Method for AttributeSelectedClassifier
//	  * NOT TESTED
//	  **/  
//	public static void CreateModelAttributeSelectionCfs(String modelpath,String testArff,int instanceNum)throws Exception{
//		// create J48
//		 Classifier cls = new AttributeSelectedClassifier();
//		 
//		 // train
//		 Instances inst = new Instances(new BufferedReader(new FileReader(testArff)));
//		 inst.setClassIndex(inst.numAttributes() - 1);
//		 cls.buildClassifier(inst);
//		 weka.core.SerializationHelper.write(modelpath, cls);		
//	}
//		
	
	/**
	  * @brief 	This methods takes an .arff file and returns the Prediction of the instance num value.
	  * 		If not present it uses the 1st one.
	  * 		The test file shall have the same attributes than the one used to create the model 
	  * 
	  * @param modelpath complete path including model name "C:\\some\\where\\tree.model"
	  * @param testArff complete path including arff name to test "C:\\some\\where\\totest.arff"
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
