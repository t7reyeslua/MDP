/**
 * @author  Luis Gonzalez
 * @version 1, 30/04/14
 *
 * See test_MSD() method as an implementation example
 * Working code :)
 * 
 */


package ft_test;

import java.util.ArrayList;
import java.util.List;

public class MeanStandardDeviation {

    private Double mean;
    private Double stdDev;

    public MeanStandardDeviation(Double mean, Double stdDev) {
        this.mean = mean;
        this.stdDev = stdDev;
    }
    
    public static double getDevStdofSet(ArrayList<ArrayList<Double>> accelerometerset){
    	double stdDevSet=0.0;
    	
    	
    	
    	for(int j=0;j<3;j++){
    		ArrayList<Double> samples = new ArrayList<Double>();
	    	for(int i=0;i<accelerometerset.size();i++){
	    		samples.add(accelerometerset.get(i).get(j));
	    	}
	    	

	    	stdDevSet+=StandardDeviation(samples);
    	}
    	
        return stdDevSet;
    }
    
    public static double StandardDeviation(List<Double> samples) {
        double mean = 0.0;
        double m2 =0.0;
        for (int i=0; i < samples.size(); i++) {
            double sample = samples.get(i);
            double delta = sample - mean;
            mean += delta / (i+1);
            m2 += delta * (sample - mean);
        }
        double variance = m2 / (samples.size() - 1);
        double stdDev = Math.sqrt(variance);
        return stdDev;
    }
    
    public static double Mean(List<Double> samples) {
        double mean = 0.0;

        for (int i=0; i < samples.size(); i++) {
            double sample = samples.get(i);
            double delta = sample - mean;
            mean += delta / (i+1);
        }


        return mean;
    }  
    
    public static MeanStandardDeviation meanAndStandardDeviation(List<Double> samples) {
        double mean = 0.0;
        double m2 =0.0;
        for (int i=0; i < samples.size(); i++) {
            double sample = samples.get(i);
            double delta = sample - mean;
            mean += delta / (i+1);
            m2 += delta * (sample - mean);
        }
        double variance = m2 / (samples.size() - 1);
        double stdDev = Math.sqrt(variance);
        return new MeanStandardDeviation(mean, stdDev);
    }

    public Double getMean() {
        return mean;
    }

    public Double getStdDev() {
        return stdDev;
    }
    

     public static void test_MSD() {
         //Example of implementation
         ArrayList<Double> samples = new ArrayList<Double>();
         samples.add(3.1);
         samples.add(6.4);
         samples.add(9.7);

         MeanStandardDeviation msd = MeanStandardDeviation.meanAndStandardDeviation(samples);
         
         System.out.println("THIS IS STANDARD!!!!");
         System.out.println("Mean is " + msd.getMean());
         System.out.println("Std Dev is " + msd.getStdDev());

     }
    

}
