package wekatesting;

import java.util.ArrayList;

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
	//Hardcoded coefs
//	static double a0 = 0.001654805183328;
//	static double a1 = 0.003309610366655;
//	static double a2 = 0.001654805183328;
//	static double b0 = 1.0;
//	static double b1 = -1.881679572986208;
//	static double b2 = 0.888298793719518;

	public static ArrayList<Double> ButtFilterArray(ArrayList<Double> x,int order, double Cutfreq,double SamplingFreq) {
		/**
		 * @author Luis Gonzalez
		 * @version 1, 19/05/14
		 * 
		 * @brief it returns the filtered signal with Butterworth 2nd order low
		 *        pass filter. the average rate of the timestamp Cutoff freq of
		 *        2.666Hz, this is the max double freq. of Running
		 *        (160Steps/min) To also count running steps Other value
		 *        constanst can be used for other freq. (see at the end of file)
		 */
		
		/* Get coeficients first*/
		
		double [] a=new double[3];
		double [] b=new double[3];
		
		SetButtCoefs(order,Cutfreq, SamplingFreq, true, a, b);
		
		
		
		
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

			y0 = a[0] * x0 + a[1] * x1 + a[2] * x2 - b[1] * y1 - b[2] * y2;
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

   /**
     *Methods to calculate the butteworth coefs, from:
     * https://code.google.com/p/jstk/source/browse/trunk/jstk/src/de/fau/cs/jstk/sampled/filters/Butterworth.java?r=176
     * computeScale
     * computeB
     * computeA
     * 
     */
	
		/**
         * Generate a Butterworth low/high pass filter at the given cutoff frequency 
         * 
         * @param source
         * @param order 
         * @param SampleRate in Hz
         * @param lowp true for lowpass, false for high pass
         */
        public static void SetButtCoefs(int order, double Cutfreq,double SampleRate, boolean lowp,double [] bc,double [] ac) {

                int n = order;
                
                double ff = 2. * Cutfreq / SampleRate;
                
                double scale = computeScale(n, ff, lowp);
                
                double [] b = computeB(n, lowp);
                for (int i = 0; i < b.length; ++i)
                	b[i] *= scale;
                
                double [] a = computeA(n, ff);
                
                for (int i = 0; i < b.length; ++i){
                	ac[i]=a[i];
                	bc[i]=b[i];               	
                }
        }
        
	/*    
     * Compute the scale factor for the b coefficients for given low/high pass
     * filter.
     * 
     * @param n
     * @param f
     * @param lowp
     * @return
     */
    private static double computeScale(int n, double f, boolean lowp) {
            double omega = Math.PI * f;
        double fomega = Math.sin(omega);
        double parg0 = Math.PI / (double)(2*n);
        
        double sf = 1.;
        for (int k = 0; k < n/2; ++k )
            sf *= 1.0 + fomega * Math.sin((double)(2*k+1)*parg0);

        fomega = Math.sin(omega / 2.0);

        if (n % 2 == 1) 
            sf *= fomega + (lowp ? Math.cos(omega / 2.0) : Math.sin(omega / 2.));
        sf = Math.pow( fomega, n ) / sf;

        return sf;
    }
    
    /*
    * Compute the B coefficients for low/high pass. The cutoff frequency is not
    * required.
    * 
    * @param n
    * @param lowp
    * @return
    */
   private static double [] computeB(int n, boolean lowp) {
           double [] ccof = new double [n + 1];
           
           ccof[0] = 1;
           ccof[1] = n;
           
           for (int i = 2; i < n/2 + 1; ++i) {
                   ccof[i] = (n - i + 1) * ccof[i - 1] / i;
                   ccof[n - i] = ccof[i];
           }
           
           ccof[n - 1] = n;
           ccof[n] = 1;

           if (!lowp) {
                   for (int i = 1; i < n + 1; i += 2)
                           ccof[i] = -ccof[i];
           }
           
           return ccof;

   }

   /**
    * Compute the A coefficients for a low/high pass for the given frequency
    * @param n
    * @param f frequency in radians (2 * hz / samplef)
    * @return
    */
   private static double [] computeA(int n, double f) {
           double parg;    // pole angle
           double sparg;   // sine of the pole angle
           double cparg;   // cosine of the pole angle
           double a;               // workspace variable
           double [] rcof = new double [2 * n]; // binomial coefficients

           double theta = Math.PI * f;
           double st = Math.sin(theta);
           double ct = Math.cos(theta);

           for (int k = 0; k < n; ++k) {
                   parg = Math.PI * (double) (2*k + 1) / (double) (2*n);
                   sparg = Math.sin(parg);
                   cparg = Math.cos(parg);
                   a = 1. + st * sparg;
                   rcof[2 * k] = -ct / a;
                   rcof[2 * k + 1] = -st * cparg / a;
           }

           // compute the binomial
           double [] temp = binomialMult(rcof);
           
           // we only need the n+1 coefficients
           double [] dcof = new double [n + 1];
           dcof[0] = 1.0;
           dcof[1] = temp[0];
           dcof[2] = temp[2];
           for (int k = 3; k < n + 1; ++k)
                   dcof[k] = temp[2*k - 2];


           return dcof;
   }
   /**
    *  Multiply a series of binomials and returns the coefficients of the 
    *  resulting polynomial. The multiplication has the following form:<b/>
    *  
    *  (x+p[0])*(x+p[1])*...*(x+p[n-1]) <b/>
    *  
    *  The p[i] coefficients are assumed to be complex and are passed to the
    *  function as an array of doubles of length 2n.<b/>
    *  
    *  The resulting polynomial has the following form:<b/>
    *  
    *  x^n + a[0]*x^n-1 + a[1]*x^n-2 + ... +a[n-2]*x + a[n-1] <b/>
    *  
    *  The a[i] coefficients can in general be complex but should in most cases
    *  turn out to be real. The a[i] coefficients are returned by the function 
    *  as an array of doubles of length 2n.
    *  
    * @param p array of doubles where p[2i], p[2i+1] (i=0...n-1) is assumed to be the real, imaginary part of the i-th binomial.
    * @return coefficients a: x^n + a[0]*x^n-1 + a[1]*x^n-2 + ... +a[n-2]*x + a[n-1]
    */
   private static double [] binomialMult(double [] p) {
           int n = p.length / 2;
           double [] a = new double [2 * n];

           for (int i = 0; i < n; ++i) {
                   for (int j = i; j > 0; --j) {
                           a[2 * j] += p[2 * i] * a[2 * (j - 1)] - p[2 * i + 1]
                                           * a[2 * (j - 1) + 1];
                           a[2 * j + 1] += p[2 * i] * a[2 * (j - 1) + 1] + p[2 * i + 1]
                                           * a[2 * (j - 1)];
                   }

                   a[0] += p[2 * i];
                   a[1] += p[2 * i + 1];
           }

           return a;
   }
}
