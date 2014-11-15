package wekatesting;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;


public class WekaDemo { 
	
	public static void main(String[] args) throws Exception {
		
		
		
//		EvalLocationModel();
		DatasetQuickLocationWekatest();
//		DatasetQuickWekatest();
//		GetLocARFFExp("C:\\Users\\LG\\Desktop\\LocationN5","C:\\Users\\LG\\Desktop\\LocationN5\\Location336_II",false);
//		DatasetQuickLocationWekatest();
//		EvalSensorWindowSize();
//		String pathmodel="C:\\Users\\LG\\Desktop\\watchSession";
//		String pathmodel="C:\\Users\\LG\\Dropbox\\MDP-Neo\\RUN_3_NOV";
//		String pathmodel="C:\\Users\\LG\\Desktop\\DatasetLG";
//		String arfffileName=pathmodel+"\\ResMotLocTOTAL";
//		GetLocMotARFFRealExp(pathmodel,arfffileName,false);
//		GetLocMotARFF(pathmodel,arfffileName,300,true);
				
		
//		String pathmodel="C:\\Users\\LG\\Documents\\GitHub\\MDP\\Sandbox\\EclipseJava\\WekaTesting\\j48.model";
//		String pathtest="C:\\Users\\LG\\Documents\\GitHub\\MDP\\Sandbox\\EclipseJava\\WekaTesting\\Testa_1414769470674.arff";
		  
		
//		WekaMethods.CreateModelJ48(pathmodel, pathtest);
//		WekaMethods.FoldEvaluation(pathmodel, pathtest,10);
		
//		for(int i =0;i<40;i++)
//			WekaMethods.PrintPredictedDistribution(pathmodel+model,pathtest,i);
		


//		PDistributionTest();
//		ButtTest();

		System.out.println("Program Done!");
		
	}
	/**
	 * @author Luis Gonzalez
	 * @throws Exception 
	 * @brief creates arff test for the dataset created on the Real Simulation
	 * 	 */
	private static void GetLocMotARFFRealExp(String folderpath,String ResultsFileName,boolean nametimestamp) {
		System.out.println("Getting ARRRRRFF! EXP");
		ArrayList<String> MotionNames = new ArrayList<String> ();
		ArrayList<String> MotionValues = new ArrayList<String> ();
		ArrayList<String> MotionAttributes = new ArrayList<String> ();
		
		ArrayList<String> LocationNames = new ArrayList<String> ();
		ArrayList<String> LocationValues = new ArrayList<String> ();
		ArrayList<String> LocationAttributes = new ArrayList<String> ();

		ArrayList<String> ClassNames = new ArrayList<String> ();

		

		//Files need to be in separate folders "Location" & "Motion"
		
		getftofLocationlogExp(folderpath,LocationNames,LocationValues,LocationAttributes);
		getftofMotionlogExp(folderpath,MotionNames,MotionValues,MotionAttributes); 
		
		ClassNames=GetNameClass(LocationNames);
		
		System.out.println("\n\n Begin Merging \n");
		//**Writting part**//
		if(nametimestamp==true)
			ResultsFileName=ResultsFileName+"_"+String.valueOf(System.currentTimeMillis());
		ResultsFileName=ResultsFileName+".arff";
    	
    	Writer writer = null;

    	try {
    	    writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(ResultsFileName), "utf-8"));
    	    
    	    writer.write("@relation Events"+"\n\n");  
    	    
    	    //first Location then Motion Attributes
            for(int i=0; i<LocationAttributes.size(); i++)
            	writer.write(LocationAttributes.get(i)+"\n");
            
            for(int i=0; i<MotionAttributes.size(); i++)
            	writer.write(MotionAttributes.get(i)+"\n");
                        	
            writer.write("@attribute activity {");
            for(int a=0; a<(ClassNames.size()-1); a++){
            	//TODO Change names to start with a letter
            	writer.write("A"+NameDecomposition(ClassNames.get(a))+",");
            }
        	//TODO Change names to start with a letter
        	writer.write("A"+NameDecomposition(ClassNames.get(ClassNames.size()-1))+"}");
            writer.write("\n\n");
            writer.write("@data\n");
            
            
            //Writting values
            for(int m=0; m<LocationValues.size(); m++){
            	//first location
            	writer.write(LocationValues.get(m));
        		writer.write(",");
            	
        		
            	//searching for its Motion counterpart (if any)
            	int fFlag=0;
            	for(int n=0; n<MotionValues.size(); n++){
            		

            		if(LocationNames.get(m).equals(MotionNames.get(n))){
//            			System.out.println(LocationNames.get(m)+" equals "+MotionNames.get(n)+"for m= "+m+" n= "+n);
            			writer.write(MotionValues.get(n)+",");
            			fFlag=1;
            		}	
            	}
            	if(fFlag==0){//No location found, fill with inf.
            		System.out.println(LocationNames.get(m)+" has no equal "+"for m= "+m);
            		for(int z=0;z<126;z++){
            			
            			writer.write("?,");
            		}
            	}
                //Writting Class
            	//TODO Change names to start with a letter
        		writer.write("A"+NameDecomposition(LocationNames.get(m))+"\n");
            }
    	    
    	} catch (IOException ex) {
    	  // report
    	} finally {
    	   try {writer.close();} catch (Exception ex) {}
    	}
    	
    }
	
	/**
	 * @brief Use to build the arff having he Location files with the format:
	 * log_LOCATION_Microwave-LivingRoom-04E85AC22D3580_20141113151155
	 * @param folderpath should be in a folder named "\Location"
	 * @param ResultsFileName
	 */
	private static void GetLocARFFExp(String folderpath,String ResultsFileName,boolean timetag) {
		System.out.println("Getting Location GetLocARFFExp!");

		ArrayList<String> LocationNames = new ArrayList<String> ();
		ArrayList<String> LocationValues = new ArrayList<String> ();
		ArrayList<String> LocationAttributes = new ArrayList<String> ();

		ArrayList<String> ClassNames = new ArrayList<String> ();

				
		getftofLocationlogExp(folderpath,LocationNames,LocationValues,LocationAttributes);
		

		
		ClassNames=GetNameClass(LocationNames);
		
		System.out.println("\n\nGettingNames\n\n");
		//change name of locations
		for(int i=0;i<ClassNames.size();i++){
			String[] splitname = ClassNames.get(i).split("-");
			
			ClassNames.set(i, splitname[1]);
		}
		ClassNames=RemoveDuplicates(ClassNames);
		for(int i=0;i<ClassNames.size();i++){
			
			System.out.println(ClassNames.get(i));
		}	
		
		System.out.println("\n\n Begin Merging \n");
		//**Writting part**//
		if(timetag==true)
			ResultsFileName=ResultsFileName+"_Location_"+String.valueOf(System.currentTimeMillis())+".arff";
		else
			ResultsFileName=ResultsFileName+"_Location"+".arff";
		
    	Writer writer = null;

    	try {
    	    writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(ResultsFileName), "utf-8"));
    	    
    	    writer.write("@relation Location"+"\n\n");  
    	    
    	    //just Location Attributes
            for(int i=0; i<LocationAttributes.size(); i++)
            	writer.write(LocationAttributes.get(i)+"\n");

            writer.write("@attribute location {");
            for(int a=0; a<(ClassNames.size()-1); a++){
            	writer.write(ClassNames.get(a)+",");
            }

        	writer.write(ClassNames.get(ClassNames.size()-1)+"}");
            writer.write("\n\n");
            writer.write("@data\n");
            
            
            //Writting values
            for(int m=0; m<LocationValues.size(); m++){
            	//first location
            	writer.write(LocationValues.get(m));
        		writer.write(",");
            	

                //Writting Class
            	//TODO Change names to start with a letter
        		writer.write(NameDecompositionLocation(LocationNames.get(m))+"\n");
            }
    	    
    	} catch (IOException ex) {
    	  // report
    	} finally {
    	   try {writer.close();} catch (Exception ex) {}
    	}
    	
    }
	/**
	 * @author Luis Gonzalez
	 * @throws Exception 
	 * @brief prints the evaluation (Correctly Classified Instances) Location test. 
	 * 	 */
	private static void EvalLocationModel() throws Exception {
		
		String tempArff="C:\\Users\\LG\\Desktop\\Arff_20141115002306.arff";
		String tempmodel="C:\\Users\\LG\\Desktop\\Classifier_J48_20141115024709.model";

		ArrayList<String> Results = new ArrayList<String> ();
		
		
		
		//create .model
		WekaMethods.CreateModelJ48(tempmodel,tempArff);
				
		//eval
		String[] parts=WekaMethods.FoldEvaluation(tempmodel, tempArff,10).split("\n");
		System.out.println(parts[1]);
		System.out.println(WekaMethods.FoldEvaluation(tempmodel, tempArff,10));
		
		
	}
	/**
	 * @author Luis Gonzalez
	 * @throws Exception 
	 * @brief prints the evaluation (Correctly Classified Instances) of a Set, by changing gradually the number of scans 
	 * (lines) consider to compute the attributes of the .arff. 
	 * 	 */
	private static void EvalSensorWindowSize() throws Exception {
		int maxScans=400;
		int minScans=50;
		int Step=10;
		String scanspath="C:\\Users\\LG\\Dropbox\\MDP_LAG\\WatchTest1";
		String tempArff="C:\\Users\\LG\\Documents\\GitHub\\MDP\\Sandbox\\EclipseJava\\WekaTesting\\temp.arff";
		String tempmodel="C:\\Users\\LG\\Documents\\GitHub\\MDP\\Sandbox\\EclipseJava\\WekaTesting\\temp.model";
		ArrayList<String> Results = new ArrayList<String> ();
		
		for(int i=maxScans;i>minScans;i=i-Step){
		
		System.out.println("For "+i+" scans:");	
		//Create .arff
		GetLocMotARFF(scanspath,tempArff,i,false);
		
		//create .model
		WekaMethods.CreateModelJ48(tempmodel,tempArff);
				
		//eval
					
		String[] parts=WekaMethods.FoldEvaluation(tempmodel, tempArff,10).split("\n");
		System.out.println("With "+i+" Scans"+parts[1]);
		Results.add("With "+i+" Scans"+parts[1]);
		
		}
		System.out.println("\n\n Now the results are\n");
		for(int r=0;r<Results.size();r++)
			System.out.println(Results.get(r));
		
		
	}
	/**
	 * @author Luis Gonzalez
	 * @brief returns .arff given the root folder, must contain a folder "Motion" and one "Location"
	 */
	private static void GetLocMotARFF(String folderpath,String ResultsFileName,int numofScans,boolean nametimestamp) {
		System.out.println("Getting ARRRRRFF!");
		ArrayList<String> MotionNames = new ArrayList<String> ();
		ArrayList<String> MotionValues = new ArrayList<String> ();
		ArrayList<String> MotionAttributes = new ArrayList<String> ();
		
		ArrayList<String> LocationNames = new ArrayList<String> ();
		ArrayList<String> LocationValues = new ArrayList<String> ();
		ArrayList<String> LocationAttributes = new ArrayList<String> ();

		ArrayList<String> ClassNames = new ArrayList<String> ();

		
		//TODO work with one folder
		//Files need to be in separate folders "Location" & "Motion"
		
		getftofLocationlog(folderpath,LocationNames,LocationValues,LocationAttributes);
		getftofMotionlog(folderpath,MotionNames,MotionValues,MotionAttributes, numofScans);
		
		ClassNames=GetNameClass(LocationNames);
		
		System.out.println("\n\n Begin Merging \n");
		//**Writting part**//
		if(nametimestamp==true)
			ResultsFileName=ResultsFileName+"_"+String.valueOf(System.currentTimeMillis());
		ResultsFileName=ResultsFileName+".arff";
//		ResultsFileName=ResultsFileName+".arff";
    	
    	Writer writer = null;

    	try {
    	    writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(ResultsFileName), "utf-8"));
    	    
    	    writer.write("@relation Events"+"\n\n");  
    	    
    	    //first Location then Motion Attributes
            for(int i=0; i<LocationAttributes.size(); i++)
            	writer.write(LocationAttributes.get(i)+"\n");
            
            for(int i=0; i<MotionAttributes.size(); i++)
            	writer.write(MotionAttributes.get(i)+"\n");
                        	
            writer.write("@attribute activity {");
            for(int a=0; a<(ClassNames.size()-1); a++){
            	//TODO Change names to start with a letter
            	writer.write("A"+NameDecomposition(ClassNames.get(a))+",");
            }
        	//TODO Change names to start with a letter
        	writer.write("A"+NameDecomposition(LocationNames.get(LocationNames.size()-1))+"}");
            writer.write("\n\n");
            writer.write("@data\n");
            
            
            //Writting values
            for(int m=0; m<LocationValues.size(); m++){
            	//first location
            	writer.write(LocationValues.get(m));
        		writer.write(",");
            	
        		

        		
            	//searching for its Motion counterpart (if any)
            	int fFlag=0;
            	for(int n=0; n<MotionValues.size(); n++){
            		if(LocationNames.get(m).equals(MotionNames.get(n))){
//            			System.out.println(LocationNames.get(m)+" equals "+MotionNames.get(n)+"for m= "+m+" n= "+n);
            			writer.write(MotionValues.get(n)+",");
            			fFlag=1;
            		}	
            	}
            	if(fFlag==0){//No location found, fill with inf.
            		System.out.println(LocationNames.get(m)+" has no equal");
            		for(int z=0;z<126;z++){
            			
            			writer.write("?,");
            		}
            	}
                //Writting Class
            	//TODO Change names to start with a letter
        		writer.write("A"+NameDecomposition(LocationNames.get(m))+"\n");
            }
    	    
    	} catch (IOException ex) {
    	  // report
    	} finally {
    	   try {writer.close();} catch (Exception ex) {}
    	}
    	
    }
	
	private static void GetLocARFF(String folderpath,String ResultsFileName,int numofScans) {
		System.out.println("Getting Location ARRRRRFF!");
		ArrayList<String> MotionNames = new ArrayList<String> ();
		ArrayList<String> MotionValues = new ArrayList<String> ();
		ArrayList<String> MotionAttributes = new ArrayList<String> ();
		
		ArrayList<String> LocationNames = new ArrayList<String> ();
		ArrayList<String> LocationValues = new ArrayList<String> ();
		ArrayList<String> LocationAttributes = new ArrayList<String> ();

		ArrayList<String> ClassNames = new ArrayList<String> ();

		
		//TODO work with one folder
		//Files need to be in separate folders "Location" & "Motion"
		
		getftofLocationlog(folderpath,LocationNames,LocationValues,LocationAttributes);
		getftofMotionlog(folderpath,MotionNames,MotionValues,MotionAttributes,numofScans);
		
		ClassNames=GetNameClass(LocationNames);
		
		System.out.println("\n\n Begin Merging \n");
		//**Writting part**//
		ResultsFileName=ResultsFileName+"_Location_"+String.valueOf(System.currentTimeMillis())+".arff";
		ResultsFileName=ResultsFileName+".arff";
    	
    	Writer writer = null;

    	try {
    	    writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(ResultsFileName), "utf-8"));
    	    
    	    writer.write("@relation Events"+"\n\n");  
    	    
    	    //just Location Attributes
            for(int i=0; i<LocationAttributes.size(); i++)
            	writer.write(LocationAttributes.get(i)+"\n");

            writer.write("@attribute activity {");
            for(int a=0; a<(ClassNames.size()-1); a++){
            	//TODO Change names to start with a letter
            	writer.write("A"+NameDecomposition(ClassNames.get(a))+",");
            }
        	//TODO Change names to start with a letter
        	writer.write("A"+NameDecomposition(LocationNames.get(LocationNames.size()-1))+"}");
            writer.write("\n\n");
            writer.write("@data\n");
            
            
            //Writting values
            for(int m=0; m<LocationValues.size(); m++){
            	//first location
            	writer.write(LocationValues.get(m));
        		writer.write(",");
            	

                //Writting Class
            	//TODO Change names to start with a letter
        		writer.write("A"+NameDecomposition(LocationNames.get(m))+"\n");
            }
    	    
    	} catch (IOException ex) {
    	  // report
    	} finally {
    	   try {writer.close();} catch (Exception ex) {}
    	}
    	
    }
	
	private static void GetMotARFF(String folderpath,String ResultsFileName,int numofScans) {
	System.out.println("Getting Motion ARRRRFF!");
	ArrayList<String> MotionNames = new ArrayList<String> ();
	ArrayList<String> MotionValues = new ArrayList<String> ();
	ArrayList<String> MotionAttributes = new ArrayList<String> ();
	
	ArrayList<String> LocationNames = new ArrayList<String> ();
	ArrayList<String> LocationValues = new ArrayList<String> ();
	ArrayList<String> LocationAttributes = new ArrayList<String> ();

	ArrayList<String> ClassNames = new ArrayList<String> ();

	
	//TODO work with one folder
	//Files need to be in separate folders "Location" & "Motion"
	
	getftofLocationlog(folderpath,LocationNames,LocationValues,LocationAttributes);
	getftofMotionlog(folderpath,MotionNames,MotionValues,MotionAttributes,numofScans);
	
	ClassNames=GetNameClass(LocationNames);
	
	System.out.println("\n\n Begin Merging \n");
	//**Writting part**//
	ResultsFileName=ResultsFileName+"_Motion_"+String.valueOf(System.currentTimeMillis())+".arff";
	ResultsFileName=ResultsFileName+".arff";
	
	Writer writer = null;

	try {
	    writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(ResultsFileName), "utf-8"));
	    
	    writer.write("@relation Events"+"\n\n");  
	    
	    //Motion Attributes
        
        for(int i=0; i<MotionAttributes.size(); i++)
        	writer.write(MotionAttributes.get(i)+"\n");
                    	
        writer.write("@attribute activity {");
        for(int a=0; a<(ClassNames.size()-1); a++){
        	//TODO Change names to start with a letter
        	writer.write("A"+NameDecomposition(ClassNames.get(a))+",");
        }
    	//TODO Change names to start with a letter
    	writer.write("A"+NameDecomposition(LocationNames.get(LocationNames.size()-1))+"}");
        writer.write("\n\n");
        writer.write("@data\n");
        
        
        //Writting values
        for(int m=0; m<MotionValues.size(); m++){
        	//just Motion
        	writer.write(MotionValues.get(m));
    		writer.write(",");
        	

            //Writting Class
        	//TODO Change names to start with a letter
    		writer.write("A"+NameDecomposition(LocationNames.get(m))+"\n");
        }
	    
	} catch (IOException ex) {
	  // report
	} finally {
	   try {writer.close();} catch (Exception ex) {}
	}
	
}
	
	private static void getftofLocationlogExp(String Mainfolderpath,ArrayList<String> ftNames,ArrayList<String> ftValues,ArrayList<String> ftAttributes) {
		System.out.println("Start ft Location Log");
		String folderpath = Mainfolderpath+"\\Location\\";
		String filepath;
		ArrayList<String> FileNames = new ArrayList<String> ();
		ArrayList<String> FullFileNames = new ArrayList<String> ();
		ArrayList<String> NetworkNames = new ArrayList<String> ();
		ArrayList<String> AttributeNetworks = new ArrayList<String> ();
		ArrayList<String> ftValuesLocation = new ArrayList<String> ();
		
		
		FileNames=Reader_From_Files.getFileNames(folderpath);
		Collections.sort(FileNames);
		
		/*getting "Master list" of networks*/
		for(int p=0;p<FileNames.size();p++){
			filepath=folderpath+FileNames.get(p);
						
			Reader_From_Files  readFile = new Reader_From_Files(filepath);
			ArrayList<String> NetworkStringList = new ArrayList<String>();

			NetworkStringList=readFile.getFileContent();
			String[] NetArray = NetworkStringList.get(0).split(",");
			ArrayList<String> newNetworkNames = new ArrayList<String> ();
						
//				System.out.println("\nFile "+(p+1)+" has "+NetArray.length+" Nets");


			for(int n=0;n<NetArray.length;n++){
				newNetworkNames.add(NetArray[n]);
//					System.out.println(newNetworkNames.get(n));
			}
			
			for(int m=0;m<NetworkNames.size();m++){

				for(int n=0;n<newNetworkNames.size();n++){
					if(NetworkNames.get(m).equals(newNetworkNames.get(n))){
//							System.out.println("removing  "+NetworkNames.get(m)+" "+newNetworkNames.get(n) +" from "+m+" & "+n);
						newNetworkNames.remove(NetworkNames.get(m));
					}
				}
			}
	
			NetworkNames.addAll(newNetworkNames);
						
//				System.out.println("Total merged  are  "+NetworkNames.size());
//				for(int n=0;n<NetworkNames.size();n++)
//					System.out.println(NetworkNames.get(n));			
		}
		
		
		
		/*******builing Array of values with "Master list" of networks**/
		for(int p=0;p<FileNames.size();p++){
			System.out.println(p+ " File "+FileNames.get(p));
			filepath=folderpath+"\\"+FileNames.get(p);
			
			
			Reader_From_Files  readFile = new Reader_From_Files(filepath);
			ArrayList<String> NetworkStringList = new ArrayList<String>();
			String ftString="";

			NetworkStringList=readFile.getFileContent();
			String[] NetArray = NetworkStringList.get(0).split(",");//Gets headers (Network names)
			String[] NetValueArray = NetworkStringList.get(1).split(",");//get values
			
//				ArrayList<String> tempNetworkNames = new ArrayList<String> ();
//				ArrayList<String> tempNetworkValuesNames = new ArrayList<String> ();
//				
//				for(int n=0;n<NetArray.length;n++){
//					tempNetworkNames.add(NetArray[n]);				
//					tempNetworkValuesNames.add(NetValueArray[n]);	
//					
//				}
			
						for(int m=0;m<NetworkNames.size();m++){
				int Nflag=0;
				for(int n=0;n<NetArray.length;n++){
					
					if(NetworkNames.get(m).equals(NetArray[n])){
						double value = Double.parseDouble(NetValueArray[n]);
						ftString=ftString+String.format("%.2f",value)+",";
//							ftString=ftString+NetValueArray[n]+",";
						Nflag=1;
					}

				}	
				if(Nflag==0)
					
					ftString=ftString+"?"+",";
			}
					
			
			String[] fileNameparts = FileNames.get(p).split("_");
//			String[] roundtime = fileNameparts[5].split(".txt");
//			int roudnum =Integer.parseInt(roundtime[0]);		
//			roudnum=roudnum/10;
			FullFileNames.add(fileNameparts[2]+"_"+fileNameparts[3]);
//				System.out.println(ftString);
			

			ftValuesLocation.add(deletelastchar(ftString,','));
			
						
		}		
		
		//NetworkNames carry on the attributes list
		//ftValuesLocation has its values
		
		//******Creating Attributes array*******//
		
		
		for(int m=0;m<NetworkNames.size();m++){
			//TODO Change Networknames to start with a letter
			AttributeNetworks.add("@attribute N"+NetworkNames.get(m)+" numeric");
		}
		
//			Reader_From_Files.write2File(AttributeNetworks,"ftLocNets");
		

		ftNames.addAll(FullFileNames);		
		ftValues.addAll(ftValuesLocation);
		ftAttributes.addAll(AttributeNetworks);		
	}

	private static void getftofMotionlogExp(String Mainfolderpath,ArrayList<String> ftNames,ArrayList<String> ftValues,ArrayList<String> ftAttributes){
			System.out.println("Start readlog()");
			String folderpath = Mainfolderpath+"\\Motion\\";
			String filepath;
			ArrayList<String> FileNames = new ArrayList<String> ();
			ArrayList<String> ftMotionArray = new ArrayList<String> ();
			ArrayList<String> fullFileNames = new ArrayList<String> ();
			
			FileNames=Reader_From_Files.getFileNames(folderpath);
			Collections.sort(FileNames);
			
			for(int p=0;p<FileNames.size();p++){
				System.out.println(p+ " File "+FileNames.get(p));
				filepath=folderpath+"\\"+FileNames.get(p);
				
				
				Reader_From_Files  readFile = new Reader_From_Files(filepath);
				ArrayList<String> SensorStringList = new ArrayList<String>();
			
				HashMap <String, ArrayList<Double>> SensorsArrays;
				String ftString;

				SensorStringList = readFile.getFileContent();
				
//				SensorStringList = readFile.Sensors2arraylists();
				
//				WekaSensorsRawDataObject SensorRaw =new WekaSensorsRawDataObject();
//				
//				SensorRaw.setSensorReadings(SensorStringList);
//				SensorRaw.buildSensorArrays();
//				SensorsArrays = new HashMap<String, ArrayList<Double>>(SensorRaw.getSensorsArrays());
//
//				

//				ftString=SensorRaw.getFeatures(numofScans);
				
				ftString=SensorStringList.get(0);
				String[] fileNameparts = FileNames.get(p).split("_");
//				String[] roundtime = fileNameparts[5].split(".txt");
//				int roudnum =Integer.parseInt(roundtime[0]);		
//				roudnum=roudnum/10;
				fullFileNames.add(fileNameparts[2]+"_"+fileNameparts[3]);
				ftMotionArray.add(ftString);
				
//				System.out.println(ftString);

				
			}
			
//			Reader_From_Files.write2File(ftMotionArray,"ftMotNoNames");
			
			WekaSensorsRawDataObject SensorAt =new WekaSensorsRawDataObject();
			
			
			ftNames.addAll(fullFileNames);
			ftAttributes.addAll(SensorAt.getAttributes());
			ftValues.addAll(ftMotionArray);
			
		}
			
	private static void getftofLocationlog(String Mainfolderpath,ArrayList<String> ftNames,ArrayList<String> ftValues,ArrayList<String> ftAttributes) {
		System.out.println("Start ft Location Log");
		String folderpath = Mainfolderpath+"\\Location\\";
		String filepath;
		ArrayList<String> FileNames = new ArrayList<String> ();
		ArrayList<String> FullFileNames = new ArrayList<String> ();
		ArrayList<String> NetworkNames = new ArrayList<String> ();
		ArrayList<String> AttributeNetworks = new ArrayList<String> ();
		ArrayList<String> ftValuesLocation = new ArrayList<String> ();
		
		
		FileNames=Reader_From_Files.getFileNames(folderpath);
		Collections.sort(FileNames);
		
		/*getting "Master list" of networks*/
		for(int p=0;p<FileNames.size();p++){
			filepath=folderpath+FileNames.get(p);
						
			Reader_From_Files  readFile = new Reader_From_Files(filepath);
			ArrayList<String> NetworkStringList = new ArrayList<String>();

			NetworkStringList=readFile.getFileContent();
			String[] NetArray = NetworkStringList.get(0).split(",");
			ArrayList<String> newNetworkNames = new ArrayList<String> ();
						
//			System.out.println("\nFile "+(p+1)+" has "+NetArray.length+" Nets");


			for(int n=0;n<NetArray.length;n++){
				newNetworkNames.add(NetArray[n]);
//				System.out.println(newNetworkNames.get(n));
			}
			
			for(int m=0;m<NetworkNames.size();m++){

				for(int n=0;n<newNetworkNames.size();n++){
					if(NetworkNames.get(m).equals(newNetworkNames.get(n))){
//						System.out.println("removing  "+NetworkNames.get(m)+" "+newNetworkNames.get(n) +" from "+m+" & "+n);
						newNetworkNames.remove(NetworkNames.get(m));
					}
				}
			}
	
			NetworkNames.addAll(newNetworkNames);
						
//			System.out.println("Total merged  are  "+NetworkNames.size());
//			for(int n=0;n<NetworkNames.size();n++)
//				System.out.println(NetworkNames.get(n));			
		}
		
		
		
		/*******builing Array of values with "Master list" of networks**/
		for(int p=0;p<FileNames.size();p++){
			System.out.println(p+ " File "+FileNames.get(p));
			filepath=folderpath+"\\"+FileNames.get(p);
			
			
			Reader_From_Files  readFile = new Reader_From_Files(filepath);
			ArrayList<String> NetworkStringList = new ArrayList<String>();
			String ftString="";

			NetworkStringList=readFile.getFileContent();
			String[] NetArray = NetworkStringList.get(0).split(",");//Gets headers (Network names)
			String[] NetValueArray = NetworkStringList.get(1).split(",");//get values
			
//			ArrayList<String> tempNetworkNames = new ArrayList<String> ();
//			ArrayList<String> tempNetworkValuesNames = new ArrayList<String> ();
//			
//			for(int n=0;n<NetArray.length;n++){
//				tempNetworkNames.add(NetArray[n]);				
//				tempNetworkValuesNames.add(NetValueArray[n]);	
//				
//			}
			
			
			for(int m=0;m<NetworkNames.size();m++){
				int Nflag=0;
				for(int n=0;n<NetArray.length;n++){
					
					if(NetworkNames.get(m).equals(NetArray[n])){
						double value = Double.parseDouble(NetValueArray[n]);
						ftString=ftString+String.format("%.2f",value)+",";
//						ftString=ftString+NetValueArray[n]+",";
						Nflag=1;
					}

				}	
				if(Nflag==0)
					ftString=ftString+"0.00"+",";
			}
					
			
			String[] fileNameparts = FileNames.get(p).split("_");
			String[] roundtime = fileNameparts[5].split(".txt");
			int roudnum =Integer.parseInt(roundtime[0]);		
			roudnum=roudnum/10;
			FullFileNames.add(fileNameparts[2]+"_"+fileNameparts[3]+"_"+fileNameparts[4]+"_"+roudnum);
			
//			System.out.println(ftString);
			

			ftValuesLocation.add(deletelastchar(ftString,','));
			
						
		}		
		
		//NetworkNames carry on the attributes list
		//ftValuesLocation has its values
		
		//******Creating Attributes array*******//
		
		
		for(int m=0;m<NetworkNames.size();m++){
			//TODO Change Networknames to start with a letter
			AttributeNetworks.add("@attribute N"+NetworkNames.get(m)+" numeric");
		}
		
//		Reader_From_Files.write2File(AttributeNetworks,"ftLocNets");
		

		ftNames.addAll(FullFileNames);		
		ftValues.addAll(ftValuesLocation);
		ftAttributes.addAll(AttributeNetworks);		
	}

	private static void getftofMotionlog(String Mainfolderpath,ArrayList<String> ftNames,ArrayList<String> ftValues,ArrayList<String> ftAttributes,int numofScans){
		System.out.println("Start readlog()");
		String folderpath = Mainfolderpath+"\\Motion\\";
		String filepath;
		ArrayList<String> FileNames = new ArrayList<String> ();
		ArrayList<String> ftMotionArray = new ArrayList<String> ();
		ArrayList<String> fullFileNames = new ArrayList<String> ();
		
		FileNames=Reader_From_Files.getFileNames(folderpath);
		Collections.sort(FileNames);
		
		for(int p=0;p<FileNames.size();p++){
			System.out.println(p+ " File "+FileNames.get(p));
			filepath=folderpath+"\\"+FileNames.get(p);
			
			
			Reader_From_Files  readFile = new Reader_From_Files(filepath);
			ArrayList<String> SensorStringList = new ArrayList<String>();
			HashMap <String, ArrayList<Double>> SensorsArrays;
			String ftString;


			SensorStringList = readFile.Sensors2arraylists();
			
			WekaSensorsRawDataObject SensorRaw =new WekaSensorsRawDataObject();
			
			SensorRaw.setSensorReadings(SensorStringList);
			SensorRaw.buildSensorArrays();
			SensorsArrays = new HashMap<String, ArrayList<Double>>(SensorRaw.getSensorsArrays());

			
			ftString=SensorRaw.getFeatures(numofScans);
			
			String[] fileNameparts = FileNames.get(p).split("_");
			String[] roundtime = fileNameparts[5].split(".txt");
			int roudnum =Integer.parseInt(roundtime[0]);		
			roudnum=roudnum/10;
			fullFileNames.add(fileNameparts[2]+"_"+fileNameparts[3]+"_"+fileNameparts[4]+"_"+roudnum);
			
			ftMotionArray.add(ftString);
			
//			System.out.println(ftString);

			
		}
		
//		Reader_From_Files.write2File(ftMotionArray,"ftMotNoNames");
		
		WekaSensorsRawDataObject SensorAt =new WekaSensorsRawDataObject();
		
		
		ftNames.addAll(fullFileNames);
		ftAttributes.addAll(SensorAt.getAttributes());
		ftValues.addAll(ftMotionArray);
		
	}
		

	
	private static void ButtTest() {

		double [] a=new double[3];
		double [] b=new double[3];
		
		XCounter.SetButtCoefs(2, 5.33333, 200, true, a, b);
		System.out.println("a[0]="+a[0]);
		System.out.println("a[1]="+a[1]);
		System.out.println("a[2]="+a[2]);
		System.out.println("b[0]="+b[0]);
		System.out.println("b[1]="+b[1]);
		System.out.println("b[2]="+b[2]);
	}
	
	/**
	 * @author Luis Gonzalez
	 * @brief Based on the code of:
	 * http://stackoverflow.com/questions/7438612/how-to-remove-the-last-character-from-a-string
	 * Same as deletalastchar from WekaSensorRawdataObject
	 * @return deletes the last character of a string, to take away the last coma of the ft string if needed
	 */
	public static String deletelastchar(String str,char x) {
	    if (str.length() > 0 && str.charAt(str.length()-1)==x) {
	      str = str.substring(0, str.length()-1);
	    }
	    return str;
	}

	public static String NameDecomposition(String fullName){
		String className;
		String[] fNameparts=fullName.split("_");
		className=fNameparts[0];
		return className;
	}
	public static String NameDecompositionLocation(String fullName){
		String className;
		String[] fNameparts=fullName.split("-");
		className=fNameparts[1];
		return className;
	}
	public static ArrayList<String> RemoveDuplicates(ArrayList<String> ALwduplicates){

		HashSet<String> listToSet = new HashSet<String>(ALwduplicates);
		ArrayList<String> listWithoutDuplicates = new ArrayList<String>(listToSet);
		return listWithoutDuplicates;
		
	}
	public static ArrayList<String> GetNameClass(ArrayList<String> FullListNames){
		ArrayList<String> ClassFullNames = new ArrayList<String>();
		ArrayList<String> ClassNames = new ArrayList<String>();
		
		for(int m=0;m<FullListNames.size();m++)
			ClassFullNames.add(NameDecomposition(FullListNames.get(m)));
			
		ClassNames=RemoveDuplicates(ClassFullNames);

		return ClassNames;
		
	}
	
	//This will test the methods of creating a dataset from arraylist and generate the .arff
	private static void DatasetQuickWekatest() throws IOException {
		ArrayList<String> motionAttributes = new ArrayList<String>();
		ArrayList<String> locationAttributes = new ArrayList<String>();
		ArrayList<String> classAttributes = new ArrayList<String>();
		ArrayList<String> features = new ArrayList<String>();

		motionAttributes.add("Mot1");
		motionAttributes.add("Mot2");
		locationAttributes.add("Loc1");
		locationAttributes.add("Loc2");
		classAttributes.add("A");
		classAttributes.add("B");
		classAttributes.add("C");
		features.add("1,2,3,4,A");
		features.add("11,22,33,44,B");
		features.add("111,222,333,444,C");
					
		Instances dataset;
		dataset = WekaMethods.CreateInstanceSet("event", motionAttributes, locationAttributes, classAttributes, features);
		WekaMethods.Intances2Arff(dataset, "C:\\Users\\LG\\Desktop\\DatasetQuickWekatest.arff");
	}
	private static void DatasetQuickLocationWekatest() throws Exception {
		ArrayList<String> locationAttributes = new ArrayList<String>();
		ArrayList<String> classAttributes = new ArrayList<String>();
		ArrayList<String> features = new ArrayList<String>();

		String OriginalArff = "C:\\Users\\LG\\Desktop\\Original.arff";
		String OriginalModel = "C:\\Users\\LG\\Desktop\\OModelj48.arff";
		String arfftoEval = "C:\\Users\\LG\\Desktop\\toeval.arff";
		
		
		//Original Dataset
		locationAttributes.add("Loc1");
		locationAttributes.add("Loc2");
		locationAttributes.add("Loc3");
		locationAttributes.add("Loc4");
		classAttributes.add("A");
		classAttributes.add("B");
		classAttributes.add("C");
		features.add("1,2,3,4,A");
		features.add("11,22,33,44,B");
		features.add("111,222,333,444,C");
					
		Instances dataset;
		dataset = WekaMethods.CreateLocationInstanceSet("event",locationAttributes, classAttributes, features);
		WekaMethods.Intances2Arff(dataset, OriginalArff);
		
				
		//Instance to Eval
		String toeval="Loc1,Loc3,Loc5\n3,5,0,A";
		WekaMethods.CreateLocationInstanceToEval(dataset,toeval);
		WekaMethods.Intances2Arff(dataset, arfftoEval);

		WekaMethods.CreateModelJ48(OriginalModel,OriginalArff );
		WekaMethods.PrintPredictedDistributionFile(OriginalModel, arfftoEval,0);
		


		
	}
	
	
}