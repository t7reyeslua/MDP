/**
 * @author  Luis Gonzalez
 * @version 1, 04/10/14
 *
 *@brief Modified Test to extract features of accelerometer sensors. 
 * 
 */
package ft_test;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;



public class MainTest {
	//private static final boolean debug = false;
	
	public static void main(String[] args){

		//if (debug) 
			//System.out.println("debug was enabled");
		
		testmonitor();
		//testMove();
		//testFurier();
		//testCompass();
		
		System.out.println("Ok Mr.");
    }
	
	
	public static void testFurier(){
		//String filepath = "WalkOne.txt"; 
		//String filepath = "Standing  1000.txt"; 
		//String filepath = "Walking  1061018013.txt";
		//String filepath = "Running  1061018014.txt";
		String filepath = "Standing  2188327506.txt";// 3 steps, stand, random.
		
        Reader_From_Files  readFile = new Reader_From_Files(filepath);
        //readFile.printFile();
        //readFile.s2ltest();
        
        ArrayList<Double> Xlist = new ArrayList<Double>();
        ArrayList<Double> Ylist = new ArrayList<Double>();
        ArrayList<Double> Zlist = new ArrayList<Double>();
        ArrayList<Double> Normlist = new ArrayList<Double>();
        ArrayList<Double> Filtered = new ArrayList<Double>();

        readFile.AccelString2list(Xlist, Ylist, Zlist, Normlist);
        
        double[] inreal=new double [Normlist.size()];
        double[] inimag=new double [Normlist.size()];
        double[] outreal=new double [Normlist.size()];
        double[] outimag=new double [Normlist.size()];
        
        Filtered = StepCounter.ButtFilterArray(Normlist);
        
        MeanStandardDeviation Nmsd = MeanStandardDeviation.meanAndStandardDeviation(Filtered);
        



       for(int i=0;i<Normlist.size();i++){
    	   
    	   inreal[i]=Normlist.get(i)-Nmsd.getMean();
    	   inimag[i]=0; 
       
       }
       
      Fft.computeDft(inreal, inimag, outreal, outimag);
      System.out.println(outimag.length);
       /******************Write to file****************************************************/        
		   	Writer writer = null;
		
		   	try {
		   	    writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("DFT.dat"), "utf-8"));
		   	    
		   	    
		           for(int i=0; i<Normlist.size(); i++){
		           	writer.write(String.valueOf(i+" "+inreal[i]+" "+inimag[i])+" "+outreal[i]+" "+outimag[i]+"\n");
		           	
		           }
		   	    
		   	    
		   	} catch (IOException ex) {
		   	  // report
		   	} finally {
		   	   try {writer.close();} catch (Exception ex) {}
		   	}
		   	
		   	Fft.transform(inreal, inimag);

		   	
		   	System.out.println("1st component = "+Fft.FirstComponentFFT(Normlist, 200));
       /******************Write to file****************************************************/        
		  
		
		   	try {
		   	    writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("FFT.dat"), "utf-8"));
		   	    
		   	    
		           for(int i=0; i<Normlist.size(); i++){
		           	writer.write(String.valueOf(i+" "+inreal[i]+" "+inimag[i])+"\n");
		           	
		           }
		   	    
		   	    
		   	} catch (IOException ex) {
		   	  // report
		   	} finally {
		   	   try {writer.close();} catch (Exception ex) {}
		   	}
		   	
  	

	}
	
	
	public static void testmonitor(){
		String filepath = "Standing  1000.txt"; // 3 steps, stand, random.
		//String filepath = "Walking  1061018013.txt";
		//String filepath = "Running  1061018014.txt";
		//String filepath = "Standing  2188327506.txt";
		
        Reader_From_Files  readFile = new Reader_From_Files(filepath);
        //readFile.printFile();
        //readFile.s2ltest();
        
        ArrayList<Double> Xlist = new ArrayList<Double>();
        ArrayList<Double> Ylist = new ArrayList<Double>();
        ArrayList<Double> Zlist = new ArrayList<Double>();
        ArrayList<Double> Normlist = new ArrayList<Double>();
        ArrayList<Double> Filtered = new ArrayList<Double>();
        
        
        readFile.AccelString2list(Xlist, Ylist, Zlist, Normlist);
        
        

        
        Filtered = StepCounter.ButtFilterArray(Normlist);
        
        Reader_From_Files.write2File(Filtered);
        Reader_From_Files.write2File(Normlist);
        //for(int i=0; i<Filtered.size(); i++){
          //  System.out.println(i+ " "+Filtered.get(i) );
        //}
        
        MeanStandardDeviation Nmsd = MeanStandardDeviation.meanAndStandardDeviation(Filtered);
        System.out.println("Mean of Filtered " + Nmsd.getMean());
        
		System.out.println("No Steps " + StepCounter.stepCounter(Filtered,3.0,1.5));
		
	}


	public static void testMove(){
		ArrayList<Double> cellVector = new ArrayList<Double>(Collections.nCopies(10, 0.0));
		ArrayList<Double> cellLenght = new ArrayList<Double>(Collections.nCopies(10, 4.0));

		
		int numSteps=4;
		
		System.out.println("Test Move");
		System.out.println("Initial belief:");
		cellVector.set(0,0.0);
		cellVector.set(1,0.0);
		cellVector.set(2,0.0);
		cellVector.set(3,0.0);
		cellVector.set(4,1.0);
		cellVector.set(5,0.0);
		cellVector.set(6,0.0);
		cellVector.set(7,0.0);
		cellVector.set(8,0.0);
		cellVector.set(9,0.0);
		
		
		
		Bayesian.PrintCells(cellVector);
		
		
		System.out.printf("Moving %d Steps \n", numSteps); 
		
		System.out.printf("Post is \n"); 
		Bayesian.PrintCells(Bayesian.MoveOneDirection(cellVector, cellLenght, numSteps,"fwd"));
		
		System.out.printf("Now both directions \n"); 
		Bayesian.PrintCells(Bayesian.MoveBothDirections(cellVector, cellLenght, numSteps));
	}
	
	public static void testSense(){
		ArrayList<Double> cellVector = new ArrayList<Double>(Collections.nCopies(10, 0.0));
		ArrayList<Double> cellLenght = new ArrayList<Double>(Collections.nCopies(10, 4.0));

		
		int numSteps=6;
		
		System.out.println("Test Move");
		System.out.println("Initial belief:");
		cellVector.set(0,0.0);
		cellVector.set(1,0.0);
		cellVector.set(2,0.5);
		cellVector.set(3,0.0);
		cellVector.set(4,0.0);
		cellVector.set(5,0.0);
		cellVector.set(6,0.5);
		cellVector.set(7,0.0);
		cellVector.set(8,0.0);
		cellVector.set(9,0.0);
		
		
		
		Bayesian.PrintCells(cellVector);
		
		
		System.out.printf("Moving %d Steps \n", numSteps); 
		
		System.out.printf("Post is \n"); 
		Bayesian.PrintCells(Bayesian.MoveOneDirection(cellVector, cellLenght, numSteps,"fwd"));
		
		System.out.printf("Now both directions \n"); 
		Bayesian.PrintCells(Bayesian.MoveBothDirections(cellVector, cellLenght, numSteps));
	}
	
	public static void testCompass(){
		int offset=350;
		System.out.printf("offset %d degrees \n", offset); 
		int dir=0;
		
		

		for(int i=0;i<=360;){
			dir=Compass.CompassCompensatedDirection(i,offset);
			System.out.printf("For a reading of %d degrees  direction is %d \n", i,dir); 
			i=i+45;
		}
		
	}
}
