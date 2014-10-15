package tudelft.mdp.utils;

import java.util.ArrayList;

import org.apache.commons.math3.stat.regression.SimpleRegression;


public class SLinearRegression {

	/** 
	 * @brief this method considers a one dimention array, 
	 * inteded for linear regression of a sorted list,X values will be coninues (1,2,3...)
	 *
	 * @return regression [m,b,R^2,err_m,err_b] y=mx+b R^2
	 */
	public static double[] SimpleLinearRegression(ArrayList<Double> data){
	  double[] result=new double[5];
	  
	  SimpleRegression regression = new SimpleRegression();
	  for (int i=0; i < data.size(); i++) {
	    try {
	      regression.addData((double)i,(double)data.get(i));
	    }
	 catch (    NullPointerException e) {
	      ;
	    }
	  }
	  result[0]=regression.getSlope();
	  result[1]=regression.getIntercept();
	  result[2]=regression.getRSquare();
	  result[3]=regression.getSlopeStdErr();
	  result[4]=regression.getInterceptStdErr();
	  return result;
	}
	
	
	/** 
	 * @brief Shall compare 2 regression lines parameters (m,b from y=mx+b) 
	 * and return the multiplication factor to be implemented
	 *
	 * @return factors [m_factor,b_factor] y=mx+b R^2
	 */
	public static double[] CalibratrionFactor(double m_calibrated, double b_calibrated,double m_noncalibrated, double b_noncalibrated){
		double[] factors=new double[2];

		factors[0]=m_calibrated/m_noncalibrated;
		factors[1]=b_calibrated-b_noncalibrated;
				
		return factors;
	}

	/** 
	 * @brief test to compare two scans
	 */
	public static void TestSLR(){
		
		
	}
}
