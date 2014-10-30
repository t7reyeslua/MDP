package wekatesting;

import java.util.ArrayList;
import java.util.HashMap;


public class WekaDemo { 
	
	public static void main(String[] args) throws Exception {
		
//		PDistributionTest();
//		ButtTest();
//		getftofMotionlog();
//		getftofLocationlog();
		
		
		System.out.println("Program Done!");
	}
	

	private static void getftofLocationlog() {
		System.out.println("Start ft Location Log");
		String folderpath = "C:\\Users\\LG\\Dropbox\\MDP_LAG\\WatchTest1\\Location";
		String filepath;
		ArrayList<String> FileNames = new ArrayList<String> ();
		ArrayList<String> NetworkNames = new ArrayList<String> ();

		
		ArrayList<String> ftLocationArray = new ArrayList<String> ();
		
		
		FileNames=Reader_From_Files.getFileNames(folderpath);
		
		/*getting "Master list" of networks*/
		for(int p=0;p<FileNames.size();p++){
			filepath=folderpath+"\\"+FileNames.get(p);
						
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
			System.out.println(ftString);
			ftString=ftString+fileNameparts[2];
			ftLocationArray.add(ftString);
			
						
		}		
		Reader_From_Files.write2File(ftLocationArray,"ftLocation");
		
		
	}


	public static void getftofMotionlog(){
		System.out.println("Start readlog()");
		String folderpath = "C:\\Users\\LG\\Dropbox\\MDP_LAG\\WatchTest1\\Motion\\";
		String filepath;
		ArrayList<String> FileNames = new ArrayList<String> ();
		ArrayList<String> ftMotionArray = new ArrayList<String> ();
		
		FileNames=Reader_From_Files.getFileNames(folderpath);
		
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
			
			ftString=ftString+fileNameparts[2];
			ftMotionArray.add(ftString);
			
//			System.out.println(ftString);

			
		}
		
		Reader_From_Files.write2File(ftMotionArray,"ftMotion");
		
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

}


