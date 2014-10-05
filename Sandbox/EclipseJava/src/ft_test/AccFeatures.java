/**
 * @author  Luis Gonzalez
 * @version 1, 05/10/14
 *
 * @brief Organized class containing methods to calculate the features od accelerometers
 * At most, raw data should be ArrayList of the accelerometers (3x) and timestamp
 * 
 */
package ft_test;

import java.util.ArrayList;
import java.util.List;


public class AccFeatures {

    public static double Mean(List<Double> samples) {
        double mean = 0.0;

        for (int i=0; i < samples.size(); i++) {
            double sample = samples.get(i);
            double delta = sample - mean;
            mean += delta / (i+1);
        }


        return mean;
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

    public static double Magnitud(Double a,Double b,Double c) {
    	return Math.sqrt((tX*tX)+(tY*tY)+(tZ*tZ));  
    }

    
}
