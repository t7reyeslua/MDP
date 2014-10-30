/**
 * @author  Luis Gonzalez
 * @version 1, 04/10/14
 *
 *Idea from:
 *	http://jdongprogramming.com/helpful-code-snippets/how-to-read-a-txt-or-csv-in-java/
 * 
 * See s2ltest() method as an implementation example
 * 
 * ATENTION, only Acc2list should be used instead of AccelString2list for the format of (# TS aX aY aZ)
 */

package wekatesting;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Scanner;

 
public class Reader_From_Files {
 

 
    private File file;
    private boolean fileExists; //reduces the chances of our program crashing
    private ArrayList<String> fileContent = new ArrayList<String>();
	

    public Reader_From_Files(String filepath){
 
        file = new File(filepath);
        fileExists = file.exists();
 
        if(fileExists){
        readFile();
        } else {
            System.out.println("File doesn't exists");    
        }
    }
 
    private void readFile(){    
 
            try{
                Scanner s= new Scanner(file);
 
                while(s.hasNextLine()){
                    fileContent.add(s.nextLine());
                }
 
                s.close(); 
 
            }catch(Exception e){
                e.printStackTrace();
            }
 
    }
 
    public ArrayList<String> getFileContent(){
        return fileContent;
    }
 
    public void printFile(){
        for(int i=0; i<fileContent.size(); i++){
            System.out.println(fileContent.get(i));
            
        }
    }
    
    public void AccelString2list(ArrayList<Double> Xlist,ArrayList<Double> Ylist,ArrayList<Double> Zlist,ArrayList<Double> Normlist){
    double tX,tY,tZ; 
    double normvector;	
    String tempstr;
    String[] tmpchararray;
    
        for(int i=0; i<fileContent.size(); i++){
            tempstr = fileContent.get(i);
            tmpchararray = tempstr.split("	");//carefull if is tab or space between " "
            
            //System.out.println(tempstr);
            
            tX=Double.parseDouble(tmpchararray[2]);
            tY=Double.parseDouble(tmpchararray[3]);
            tZ=Double.parseDouble(tmpchararray[4]);
           
            normvector= Math.sqrt((tX*tX)+(tY*tY)+(tZ*tZ));            
            //System.out.println(tX + "\t" + tY + "\t" +tZ + "\t" + normvector);

            
            Xlist.add(tX);
            Ylist.add(tY);
            Zlist.add(tZ);

            Normlist.add(normvector); 
           
        }
    }
    
    public void Acc2list(ArrayList<Integer> TimeStamp, ArrayList<Double> Xlist,ArrayList<Double> Ylist,ArrayList<Double> Zlist){
    	/*Format of txt num timestamp aX aY aZ
    	 * */
    	
    double tX,tY,tZ;
    int ts;	
    String tempstr;
    String[] tmpchararray;
    
        for(int i=0; i<fileContent.size(); i++){
            tempstr = fileContent.get(i);
            tmpchararray = tempstr.split("	");//carefull if is tab or space between " "
            
            //System.out.println(tempstr);
            
            ts=Integer.parseInt(tmpchararray[1]); //Timestamp, may have to use doubles
            tX=Double.parseDouble(tmpchararray[2]);
            tY=Double.parseDouble(tmpchararray[3]);
            tZ=Double.parseDouble(tmpchararray[4]);
                     
//            System.out.println(ts+"\t"+tX + "\t" + tY + "\t" +tZ);

            TimeStamp.add(ts);
            Xlist.add(tX);
            Ylist.add(tY);
            Zlist.add(tZ);

           
        }
    }
    
    /**
     * Limiter should be tab,space or coma value**/
    public ArrayList<String> Sensors2arraylists(){
    	
    ArrayList<String> SensorList = new ArrayList<String>();

    String tempstr;
    
        for(int i=0; i<fileContent.size(); i++){
            tempstr = fileContent.get(i);
            SensorList.add(tempstr);
            }

//            System.out.println(ts+"\t"+tX + "\t" + tY + "\t" +tZ);

        return SensorList;
        }
        
    
      
    public ArrayList<ArrayList<Double>> AccelString2AccelSet(){
    	

    	ArrayList<ArrayList<Double>> accelerometerset = new ArrayList<ArrayList<Double>> () ;
    	
    	
    String tempstr;
    String[] tmpchararray;
    
        for(int i=0; i<fileContent.size(); i++){
            tempstr = fileContent.get(i);
            tmpchararray = tempstr.split(" ");
            
            ArrayList<Double> accelerometer_set_line = new ArrayList<Double>(3);
            
            accelerometer_set_line.add(Double.parseDouble(tmpchararray[0]));
            accelerometer_set_line.add(Double.parseDouble(tmpchararray[1]));
            accelerometer_set_line.add(Double.parseDouble(tmpchararray[2])); 
           
            accelerometerset.add(accelerometer_set_line);
        }
        
        return accelerometerset;
              
    }
    public void MapString2list(){
    	
    	
/*
 * To Read a txt that has point and ID "x y ID" lines
 * return an arraylist of 2 doubles and a char (string)
 * 
 * */
        
    }
    
    public static ArrayList<ArrayList<Double>> getArrayListset() {
    	//Read file and return an Accelerometerset WITH ID
    	ArrayList<ArrayList<Double>> ArraySet = new ArrayList<ArrayList<Double>>();
    	
    	return ArraySet;
    }
   
    
    /*
     * idea from:
     * http://stackoverflow.com/questions/5694385/getting-the-filenames-of-all-files-in-a-folder
     * */
    public static ArrayList<String> getFileNames(String filepath) {
    	
    	ArrayList<String> FileNames = new ArrayList<String> ();
    	
    	File folder = new File(filepath);
    	File[] listOfFiles = folder.listFiles();

    	    for (int i = 0; i < listOfFiles.length; i++) {
//    	        System.out.println("File " + listOfFiles[i].getName());
    	    	
    	    	FileNames.add(listOfFiles[i].getName());
    	    }
    	
    	
    	return FileNames;
    }
    

    public static void write2File(ArrayList<String> ftList,String ResultsFileName){
    	
    	ResultsFileName=ResultsFileName+"_"+String.valueOf(System.currentTimeMillis())+".dat";
    	
    	Writer writer = null;

    	try {
    	    writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(ResultsFileName), "utf-8"));
    	    
            for(int i=0; i<ftList.size(); i++){
            	writer.write(ftList.get(i)+"\n");
            	
            }
    	    
    	    
    	} catch (IOException ex) {
    	  // report
    	} finally {
    	   try {writer.close();} catch (Exception ex) {}
    	}
    	
    }

 
}

