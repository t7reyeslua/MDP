package ft_test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Luis Gonzalez
 * @version 2, 05/10/14
 * 
 * @brief Class for counting/identify steps default a,b coefs are set for
 *        [a,b]=butter(2,5.333/(200/2),'low') stepdelayallowance variable is the
 *        watchdog for the stepCounter, if the "Up"/"Down" transition is longer
 *        than value, the funciton wont take it as a step. Usually a step takes
 *        around 100 units,double the time should be fine.
 * 
 *        Counting: the last "half step" might not be counted.
 * 
 * 
 */
public class XCounter {
	static int stepdelayallowance = 200;
	/**
	 * cutoff freq 5.333hz, sample freq 200hz (5 ms) We run at 160steps/min 2.6
	 * per second
	 * 
	 * Probably the best
	 */
	static double a0 = 0.001654805183328;
	static double a1 = 0.003309610366655;
	static double a2 = 0.001654805183328;
	static double b0 = 1.0;
	static double b1 = -1.881679572986208;
	static double b2 = 0.888298793719518;

	public static ArrayList<Double> ButtFilterArray(ArrayList<Double> x) {
		/**
		 * @author Luis Gonzalez
		 * @version 1, 19/05/14
		 * 
		 * @brief it returns the filtered signal with Butterworth 2nd order low
		 *        pass filter. Current filter works for a sampling rate of
		 *        200hz, the average rate of the timestamp Cutoff freq of
		 *        2.666Hz, this is the max double freq. of Running
		 *        (160Steps/min) To also count running steps Other value
		 *        constanst can be used for other freq. (see at the end of file)
		 *@extended Butterworth constanst have to be provided, 
		 */
		ArrayList<Double> y = new ArrayList<Double>();
		double x0, x1, x2;
		double y0 = 0, y1 = 0, y2 = 0;

		y.add(0.0);
		y.add(0.0);
		y.add(0.0);

		for (int i = 0; i < x.size() - 3; i++) {
			x0 = x.get(i);
			x1 = x.get(i + 1);
			x2 = x.get(i + 2);

			// y0=x.get(i);
			// y1=x.get(i+1);
			// y2=x.get(i+2);

			y0 = a0 * x0 + a1 * x1 + a2 * x2 - b1 * y1 - b2 * y2;
			x2 = x1;
			x1 = x0;
			y2 = y1;
			y1 = y0;

			y.add(i, y0);// y.add(i+1, y1);y.add(i+2, y2);
		}

		return y;
	}

	public static int stepCounter(ArrayList<Double> y, double upperThreshold,
			double lowerThreshold) {
		/**
		 * @author Luis Gonzalez
		 * @version 2, 06/10/14
		 * 
		 * @brief it returns the Number of times the value goes outside of the
		 *        Threshold:mean +/- Upper/Lower Threshold as number of steps,
		 *        the signal (y)should be filtered. Good values are
		 *        upperThreshold=3,lowerThreshold=1.5 (this values are for Smartphone)
		 */
		double mean = 0.0;
		int steps = 0;
		int stepdelay = 0;
		boolean up = false;
		boolean down = false;

		for (int i = 0; i < y.size(); i++) {
			double sample = y.get(i);
			double delta = sample - mean;
			mean += delta / (i + 1);
		}

		for (int i = 100; i < y.size() - 3; i++) {
			if (y.get(i) > mean + upperThreshold) {
				up = true;
				stepdelay = i;

			}
			if (y.get(i) < mean - lowerThreshold && up == true) {
				down = true;
			}
			if (up == true && down == true) {
				if (i - stepdelay < stepdelayallowance) {
					steps = steps + 1;
					// System.out.println("Step "+steps+" in "+i);
				}
				up = false;
				down = false;

			}

		}

		return steps;
	}

    public static int ZeroXing(List<Double> signal) {
    	/**
		 * @author Luis Gonzalez
		 * @version 2, 06/10/14
		 * 
		 * @brief it returns the Number of Zero crossing of a signal
		 */
    	
        int numZC=0;

        
        for (int i=0; i<signal.size()-2; i++){
                if( (signal.get(i)>=0 && signal.get(i+1)<0) ||( signal.get(i)<0 && signal.get(i+1)>=0)){
                        numZC++;
                }
        }                       

        return numZC;

    }  
    
    
	/**
	 * cutoff freq 8hz, sample freq 200hz (5 ms) [a,b]=butter(2,8/(200/2),'low')
	 */

	// static double a0=0.0134;
	// static double a1=0.0267;
	// static double a2=0.0134;
	// static double b0=1.0;
	// static double b1=-1.6475;
	// static double b2=0.7009;

	/**
	 * cutoff freq 5.333hz, sample freq 200hz (5 ms) We run at 160steps/min 2.6
	 * per second
	 * 
	 * [a,b]=butter(2,5.333/(200/2),'low')
	 */
	// static double a0=0.006262642476193;
	// static double a1=0.012525284952386;
	// static double a2=0.006262642476193;
	// static double b0=1.0;
	// static double b1=-1.763992597090927;
	// static double b2=0.789043166995699;

}
