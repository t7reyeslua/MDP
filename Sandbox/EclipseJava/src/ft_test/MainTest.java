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



public class MainTest {
	private static final boolean debug = true;
	
	public static void main(String[] args){

		if (debug) 
			System.out.println("debug was enabled");
		
		//testmonitor();
		//testMove();
		//	testFurier();
		//testCompass();
		testfeatures();

		
		
		System.out.println("Ok Mr.");
    }
	

	public static void testfeatures(){

//		String filepath = "log_washhand_20140928_154655.txt"; 
		String filepath = "log_walkFFT_20141005_202210.txt"; 
		
        Reader_From_Files  readFile = new Reader_From_Files(filepath);

        
        ArrayList<Double> Xlist = new ArrayList<Double>();
        ArrayList<Double> Ylist = new ArrayList<Double>();
        ArrayList<Double> Zlist = new ArrayList<Double>();
        ArrayList<Double> FilterX = new ArrayList<Double>();
        ArrayList<Double> FilterY = new ArrayList<Double>();
        ArrayList<Double> FilterZ = new ArrayList<Double>();
        ArrayList<Integer> TimeStamp = new ArrayList<Integer>();

        
        readFile.Acc2list(TimeStamp,Xlist, Ylist, Zlist);
      
//        int SampleSize=TimeStamp.size();
        double SampleFreq = AccFeatures.GetSampleFreq(TimeStamp);
        
		double MeanX=AccFeatures.Mean(Xlist);
        double MeanY=AccFeatures.Mean(Ylist);
        double MeanZ=AccFeatures.Mean(Zlist);
        
        double StdDevX=AccFeatures.StandardDeviation(Xlist);
        double StdDevY=AccFeatures.StandardDeviation(Ylist);
        double StdDevZ=AccFeatures.StandardDeviation(Zlist);
        
        double FundFreqX=AccFeatures.FirstComponentFFT(Xlist,SampleFreq);
        double FundFreqY=AccFeatures.FirstComponentFFT(Ylist,SampleFreq);
        double FundFreqZ=AccFeatures.FirstComponentFFT(Zlist,SampleFreq);
        
        int StepsX=0;
        int StepsY=0;
        int StepsZ=0;
        
        int NZeroX=0;
        int NZeroY=0;
        int NZeroZ=0;
        
        if (debug) {
	        System.out.println("Sample freq. = " + AccFeatures.GetSampleFreq(TimeStamp));
	        System.out.println("Mean" + MeanX+" "+ MeanY+" "+MeanZ);
	        System.out.println("Std Dev" + StdDevX+" "+ StdDevY+" "+ StdDevZ);
	        System.out.println("Variance" + StdDevX*StdDevX+" "+ StdDevY*StdDevY+" "+ StdDevZ*StdDevZ+" ");
	        System.out.println("Mags (mean)" + AccFeatures.Magnitud(MeanX,MeanY,MeanZ));
	        System.out.println("Mags (std)" + AccFeatures.Magnitud(StdDevX,StdDevY,StdDevZ));
	        System.out.println("Mags (var)" + AccFeatures.Magnitud(StdDevX*StdDevX,StdDevY*StdDevY,StdDevZ*StdDevZ));
        }
        //Freq Domain
        
        
        if (debug) {
	        System.out.println("1st component X= "+FundFreqX);
	        System.out.println("1st component Y= "+FundFreqY);
	        System.out.println("1st component Z= "+FundFreqZ);
        }
        
                
    
              

        FilterX=XCounter.ButtFilterArray(AccFeatures.ZeroNormal(Xlist));
        FilterY=XCounter.ButtFilterArray(AccFeatures.ZeroNormal(Ylist));
        FilterZ=XCounter.ButtFilterArray(AccFeatures.ZeroNormal(Zlist));

        StepsX=XCounter.stepCounter(FilterX, 3, 1.5);
        StepsY=XCounter.stepCounter(FilterY, 3, 1.5);
        StepsZ=XCounter.stepCounter(FilterZ, 3, 1.5);
        
        NZeroX=XCounter.ZeroXing(AccFeatures.ZeroNormal(AccFeatures.ZeroNormal(Xlist)));
        NZeroY=XCounter.ZeroXing(AccFeatures.ZeroNormal(AccFeatures.ZeroNormal(Ylist)));
        NZeroZ=XCounter.ZeroXing(AccFeatures.ZeroNormal(AccFeatures.ZeroNormal(Zlist)));
       
        
        if (debug) {
	        System.out.println("Steps X= "+StepsX);
	        System.out.println("Steps Y= "+StepsY);
	        System.out.println("Steps Z= "+StepsZ);
        }
        
        if (debug) {
	        System.out.println("Zero Crossing X= "+NZeroX);
	        System.out.println("Zero Crossing Y= "+NZeroY);
	        System.out.println("Zero Crossing Z= "+NZeroZ);

        }
        
  	
        
        
       }
       

	public static void testFurier(){
		//String filepath = "WalkOne.txt"; 
		String filepath = "Standing  1000.txt"; 
		//String filepath = "Walking  1061018013.txt";
		//String filepath = "Running  1061018014.txt";
//		String filepath = "Standing  2188327506.txt";// 3 steps, stand, random.
		
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
        
        Filtered = XCounter.ButtFilterArray(Normlist);
        
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
	
	




	

}
