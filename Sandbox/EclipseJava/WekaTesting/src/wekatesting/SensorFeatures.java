/**
 * @author  Luis Gonzalez
 * @version 1, 05/10/14
 *
 * @brief Organized class containing methods to calculate the features od accelerometers
 * At most, raw data should be ArrayList of the accelerometers (3x) and timestamp
 * 
 * FFT and 1st Fundamental frequency can return an error (wrong freq, 2nd best) if the signal is too low or the sample rate is small
 * May be useful to return gain or top frequencies
 * 
 */

package wekatesting;

import java.util.ArrayList;
import java.util.List;

public class SensorFeatures {

/**************************** Time Domain features ****************************/
	/**
	  * @brief 	 
	  * @return Mean of the Array 
	  * */
    public static double Mean(List<Double> samples) {
        double mean = 0.0;

        for (int i=0; i < samples.size(); i++) {
            double sample = samples.get(i);
            double delta = sample - mean;
            mean += delta / (i+1);
        }


        return mean;
    }  
    
    public static double Variance(List<Double>  data) {
        // Get the mean of the data set
        double mean = Mean(data);

        double sumOfSquaredDeviations = 0;

        // Loop through the data set
        for (int i = 0; i < data.size(); i++) {
          // sum the difference between the data element and the mean squared
          sumOfSquaredDeviations += Math.pow(data.get(i) - mean, 2);
        }

        // Divide the sum by the length of the data set - 1 to get our result
        return sumOfSquaredDeviations / (data.size() - 1);
      }
    
    public static double Covariance(List<Double>  a, List<Double>  b) {
        double amean = Mean(a);
        double bmean = Mean(b);

        double result = 0;

        for (int i = 0; i < a.size(); i++) {
          result += (a.get(i) - amean) * (b.get(i) - bmean);
        }

        result /= a.size() - 1;

        return result;
      }
    
	/**
	  * @brief 	 
	  * @return It returns the Array minus its Mean 
	  * 
	  * */
	public static ArrayList<Double> ZeroNormal(ArrayList<Double> samples) {
        double mean = Mean(samples);
        ArrayList<Double> ZNormalList = new ArrayList<Double>();

        for (int i=0; i < samples.size(); i++) {
        	ZNormalList.add(samples.get(i)-mean);
        }

        return ZNormalList;
    } 
	
	/**
	  * @brief 	 
	  * @return It returns Standard deviation 
	  * 
	  * */
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
    	return Math.sqrt((a*a)+(b*b)+(c*c));  
    }

    public static int ZeroXing(ArrayList<Double> signal) {
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
    
/**************************** Frequency Domain features ****************************/
/**
 * @author Luis Gonzalez
 * @brief Based on the code of:
 * Free FFT and convolution (Java)
 * Copyright (c) 2014 Nayuki Minase
 * http://nayuki.eigenstate.org/page/free-small-fft-in-multiple-languages
 * 
 * Included modified codes to calculate the freq in Hz and finding fundamental freq.
 * 
 */
	/**
	  * @brief 	Strictly for TimeStamp List arrays in progressive order
	  * @return Sample frequency 
	  * */
    public static double GetSampleFreq(ArrayList<Double> timestamp) {

//    	double divizor=0.0;
//    	int ttime=timestamp.get(0)
//    	double nSeconds=Math.floor(ttime/divizor)*3600;
//    	ttime=ttime%divizor;
//    	divizor=divizor/100;
//%minutes
//        nSeconds=nSeconds+floor(MMSSmmm/divizor)*60;
//        SSmmm=mod(MMSSmmm,divizor);
//        divizor=divizor/100;
//%seconds
//        nSeconds=nSeconds+floor(SSmmm/divizor);
//        mmm=mod(SSmmm,divizor);
//%milliseconds
//        nSeconds=nSeconds+(mmm*.001);

    	
    	if(timestamp.size() < 10 ){
    		System.out.println("Err-GetSampleFreq, Not enough values");    
    	}else if(timestamp.get(0) > timestamp.get(timestamp.size() - 1)){
    		System.out.println("Err-GetSampleFreq, Wrong timestamp list");     		    		
    	}
    		
    	return Math.round(timestamp.size()/(((timestamp.get(timestamp.size() - 1)-timestamp.get(0)))/1000)); // For TimeStamp in MILISECONDS

    }
    
    public static double FirstComponentFFT(ArrayList<Double> signal,double sampleFrequency) {
		/**
		 * @author Luis Gonzalez
		 * @brief Using computeDft returns the First frequency components of the  signal
		 * 
		 */
		
        double[] real=new double [signal.size()];
        double[] imag=new double [signal.size()];
        //double[] absFT=new double [signal.size()/2];
        double signalMean= Mean(signal);
        

        
        for(int i=0;i<signal.size();i++){
     	   real[i]=signal.get(i)-signalMean;
     	   imag[i]=0; 
        }
        
        transform(real, imag); //the imag is the one that will carry the FT;
        
        double highestFreq=0.0;
        int p=0;
        for(int i=0;i<signal.size()/2;i++){
//        	absFT[i]=Math.abs(imag[i]);
        	 
//        	System.out.println(i+"\t"+Math.abs(imag[i]));
        	 
      	   if(Math.abs(imag[i])>highestFreq){
      		 highestFreq=Math.abs(imag[i]);
      		 p=i;
      		 
      	   }
         }
//        System.out.println(p);
		return (sampleFrequency/signal.size())*p;
	}
	
	/* 
	 * Computes the discrete Fourier transform (DFT) of the given complex vector, storing the result back into the vector.
	 * The vector can have any length. This is a wrapper function.
	 */
	public static void transform(double[] real, double[] imag) {
		if (real.length != imag.length)
			throw new IllegalArgumentException("Mismatched lengths");
		
		int n = real.length;
		if (n == 0)
			return;
		else if ((n & (n - 1)) == 0)  // Is power of 2
			transformRadix2(real, imag);
		else  // More complicated algorithm for arbitrary sizes
			transformBluestein(real, imag);
	}
	

	
	public static void computeDft(double[] inreal, double[] inimag, double[] outreal, double[] outimag) {
		int n = inreal.length;
		for (int k = 0; k < n; k++) {  // For each output element
			double sumreal = 0;
			double sumimag = 0;
			for (int t = 0; t < n; t++) {  // For each input element
				double angle = 2 * Math.PI * t * k / n;
				sumreal +=  inreal[t] * Math.cos(angle) + inimag[t] * Math.sin(angle);
				sumimag += -inreal[t] * Math.sin(angle) + inimag[t] * Math.cos(angle);
			}
			outreal[k] = sumreal;
			outimag[k] = sumimag;
		}
	}
	
	/* 
	 * Computes the inverse discrete Fourier transform (IDFT) of the given complex vector, storing the result back into the vector.
	 * The vector can have any length. This is a wrapper function. This transform does not perform scaling, so the inverse is not a true inverse.
	 */
	public static void inverseTransform(double[] real, double[] imag) {
		transform(imag, real);
	}
	
	
	/* 
	 * Computes the discrete Fourier transform (DFT) of the given complex vector, storing the result back into the vector.
	 * The vector's length must be a power of 2. Uses the Cooley-Tukey decimation-in-time radix-2 algorithm.
	 */
	public static void transformRadix2(double[] real, double[] imag) {
		// Initialization
		if (real.length != imag.length)
			throw new IllegalArgumentException("Mismatched lengths");
		int n = real.length;
		int levels = 31 - Integer.numberOfLeadingZeros(n);  // Equal to floor(log2(n))
		if (1 << levels != n)
			throw new IllegalArgumentException("Length is not a power of 2");
		double[] cosTable = new double[n / 2];
		double[] sinTable = new double[n / 2];
		for (int i = 0; i < n / 2; i++) {
			cosTable[i] = Math.cos(2 * Math.PI * i / n);
			sinTable[i] = Math.sin(2 * Math.PI * i / n);
		}
		
		// Bit-reversed addressing permutation
		for (int i = 0; i < n; i++) {
			int j = Integer.reverse(i) >>> (32 - levels);
			if (j > i) {
				double temp = real[i];
				real[i] = real[j];
				real[j] = temp;
				temp = imag[i];
				imag[i] = imag[j];
				imag[j] = temp;
			}
		}
		
		// Cooley-Tukey decimation-in-time radix-2 FFT
		for (int size = 2; size <= n; size *= 2) {
			int halfsize = size / 2;
			int tablestep = n / size;
			for (int i = 0; i < n; i += size) {
				for (int j = i, k = 0; j < i + halfsize; j++, k += tablestep) {
					double tpre =  real[j+halfsize] * cosTable[k] + imag[j+halfsize] * sinTable[k];
					double tpim = -real[j+halfsize] * sinTable[k] + imag[j+halfsize] * cosTable[k];
					real[j + halfsize] = real[j] - tpre;
					imag[j + halfsize] = imag[j] - tpim;
					real[j] += tpre;
					imag[j] += tpim;
				}
			}
			if (size == n)  // Prevent overflow in 'size *= 2'
				break;
		}
	}
	
	
	/* 
	 * Computes the discrete Fourier transform (DFT) of the given complex vector, storing the result back into the vector.
	 * The vector can have any length. This requires the convolution function, which in turn requires the radix-2 FFT function.
	 * Uses Bluestein's chirp z-transform algorithm.
	 */
	public static void transformBluestein(double[] real, double[] imag) {
		// Find a power-of-2 convolution length m such that m >= n * 2 + 1
		if (real.length != imag.length)
			throw new IllegalArgumentException("Mismatched lengths");
		int n = real.length;
		if (n >= 0x20000000)
			throw new IllegalArgumentException("Array too large");
		int m = Integer.highestOneBit(n * 2 + 1) << 1;
		
		// Trignometric tables
		double[] cosTable = new double[n];
		double[] sinTable = new double[n];
		for (int i = 0; i < n; i++) {
			int j = (int)((long)i * i % (n * 2));  // This is more accurate than j = i * i
			cosTable[i] = Math.cos(Math.PI * j / n);
			sinTable[i] = Math.sin(Math.PI * j / n);
		}
		
		// Temporary vectors and preprocessing
		double[] areal = new double[m];
		double[] aimag = new double[m];
		for (int i = 0; i < n; i++) {
			areal[i] =  real[i] * cosTable[i] + imag[i] * sinTable[i];
			aimag[i] = -real[i] * sinTable[i] + imag[i] * cosTable[i];
		}
		double[] breal = new double[m];
		double[] bimag = new double[m];
		breal[0] = cosTable[0];
		bimag[0] = sinTable[0];
		for (int i = 1; i < n; i++) {
			breal[i] = breal[m - i] = cosTable[i];
			bimag[i] = bimag[m - i] = sinTable[i];
		}
		
		// Convolution
		double[] creal = new double[m];
		double[] cimag = new double[m];
		convolve(areal, aimag, breal, bimag, creal, cimag);
		
		// Postprocessing
		for (int i = 0; i < n; i++) {
			real[i] =  creal[i] * cosTable[i] + cimag[i] * sinTable[i];
			imag[i] = -creal[i] * sinTable[i] + cimag[i] * cosTable[i];
		}
	}
	
	
	/* 
	 * Computes the circular convolution of the given real vectors. Each vector's length must be the same.
	 */
	public static void convolve(double[] x, double[] y, double[] out) {
		if (x.length != y.length || x.length != out.length)
			throw new IllegalArgumentException("Mismatched lengths");
		int n = x.length;
		convolve(x, new double[n], y, new double[n], out, new double[n]);
	}
	
	
	/* 
	 * Computes the circular convolution of the given complex vectors. Each vector's length must be the same.
	 */
	public static void convolve(double[] xreal, double[] ximag, double[] yreal, double[] yimag, double[] outreal, double[] outimag) {
		if (xreal.length != ximag.length || xreal.length != yreal.length || yreal.length != yimag.length || xreal.length != outreal.length || outreal.length != outimag.length)
			throw new IllegalArgumentException("Mismatched lengths");
		
		int n = xreal.length;
		xreal = xreal.clone();
		ximag = ximag.clone();
		yreal = yreal.clone();
		yimag = yimag.clone();
		
		transform(xreal, ximag);
		transform(yreal, yimag);
		for (int i = 0; i < n; i++) {
			double temp = xreal[i] * yreal[i] - ximag[i] * yimag[i];
			ximag[i] = ximag[i] * yreal[i] + xreal[i] * yimag[i];
			xreal[i] = temp;
		}
		inverseTransform(xreal, ximag);
		for (int i = 0; i < n; i++) {  // Scaling (because this FFT implementation omits it)
			outreal[i] = xreal[i] / n;
			outimag[i] = ximag[i] / n;
		}
	}
    
	
	
}
