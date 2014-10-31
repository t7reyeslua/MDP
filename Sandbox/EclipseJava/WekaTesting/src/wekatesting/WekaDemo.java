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


public class WekaDemo { 
	
	public static void main(String[] args) throws Exception {
		
		String folderpath = "C:/Users/LG/Dropbox/MDP_LAG/WatchTest1"; //Watch out for escape char "\", use "\\" in case
		String FileName = "Testa";
		GetARFF(folderpath,FileName);
		

//		PDistributionTest();
//		ButtTest();

		System.out.println("Program Done!");
		
	}
	
	/**
	 * @author Luis Gonzalez
	 * @brief returns .arff given the root folder, must contain a folder "Motion" and one "Location"
	 */
	private static void GetARFF(String folderpath,String ResultsFileName) {
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
		getftofMotionlog(folderpath,MotionNames,MotionValues,MotionAttributes);
		
		ClassNames=GetNameClass(LocationNames);
		
		System.out.println("\n\n Begin Merging \n");
		//**Writting part**//
		ResultsFileName=ResultsFileName+"_"+String.valueOf(System.currentTimeMillis())+".arff";
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
            for(int a=0; a<(ClassNames.size()-1); a++)
            	writer.write(NameDecomposition(ClassNames.get(a))+",");
            
        	writer.write(NameDecomposition(LocationNames.get(LocationNames.size()-1))+"}");
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
            		for(int z=0;z<125;z++){
            			
            			writer.write("99.99,");
            		}
            	}
                //Writting Class
        		writer.write(NameDecomposition(LocationNames.get(m))+"\n");
            }
    	    
    	} catch (IOException ex) {
    	  // report
    	} finally {
    	   try {writer.close();} catch (Exception ex) {}
    	}
    	
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
			AttributeNetworks.add("@attribute "+NetworkNames.get(m)+" numeric");
		}
		
//		Reader_From_Files.write2File(AttributeNetworks,"ftLocNets");
		

		ftNames.addAll(FullFileNames);		
		ftValues.addAll(ftValuesLocation);
		ftAttributes.addAll(AttributeNetworks);		
	}


	public static void getftofMotionlog(String Mainfolderpath,ArrayList<String> ftNames,ArrayList<String> ftValues,ArrayList<String> ftAttributes){
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

			
			ftString=SensorRaw.getFeatures(400);
			
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
	
	
	
	public static void PDistributionTest()throws Exception{
		
		String rootPathmodel="C:\\Users\\LG\\Documents\\GitHub\\MDP\\Sandbox\\EclipseJava\\WekaTesting\\J48modelLocationWide.model"; 
		String rootPathtest ="LocationWidetest.arff";
					
		for(int i =0;i<50;i++)
		WekaMethods.PrintPredictedDistribution(rootPathmodel,rootPathtest,i);
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
}


