package tudelft.mdp.utils;

import android.util.Log;

/*************************************************************************
 *  File: Gaussian.java
 *  Author: Robert Sedgewick, based on Marsaglia method, with minimal changes by
 *      Wayne Snyder
 *  Original URL: http://introcs.cs.princeton.edu/java/22library/Gaussian.java.html
 *
 *  Purpose: This contains six basic methods for computing values relative
 *    to the normal distribution; the main() method at bottom illustrates how to use them.
 *
 *       public static double phi(double x) :   This calculates the probability
 *               density function for a standardized normal variable (mean = 0
 *               and stdev = 1); phi(x) = y value on the normal curve for the value x;
 *
 *       public static double phi(double x, double mu, double sigma) : This calculates
 *               the probability density function for an arbitrary normal distribution;
 *
 *       public static double Phi(double z) : This calculates the cumulative density
 *               function for a standard normal variable; Phi(z) gives the area to the left
 *               of the value z in the curve, i.e., the probability P(X <= z) that X produces
 *               a value less than z.
 *
 *       public static double Phi(double z, double mu, double sigma) : Same thing for an arbitrary
 *               normal random variable
 *
 *       public static double PhiInverse(double y) : The inverse of Phi, i.e., given a
 *               probability y, PhiInverse(y) gives you the x value such that
 *               P(X <= x) = y.
 *
 *       private static double PhiInverse(double y, double mu, double sigma) : Same but
 *               for arbitrary normal random variable
 *
 *  The approximations calculated here are accurate to absolute error less than 8 * 10^(-16).
 *  Reference: Evaluating the Normal Distribution by George Marsaglia.
 *  http://www.jstatsoft.org/v11/a05/paper
 *
 *************************************************************************/
public class Gaussian {

    // return phi(x) = standard Gaussian pdf
    public static double phi(double x) {
        return Math.exp(-x * x / 2) / Math.sqrt(2 * Math.PI);
    }

    // return phi(x, mu, signma) = Gaussian pdf with mean mu and stddev sigma
    public static double phi(double x, double mu, double sigma) {
        return phi((x - mu) / sigma) / sigma;
    }

    // return Phi(z) = standard Gaussian cdf using Taylor approximation
    // this gives the probability that a random variable distributed according to the
    // standard normal distribution (mean = 0 and stdev = 1) produces a value less than z
    public static double Phi(double z) {
        if (z < -8.0) return 0.0;
        if (z >  8.0) return 1.0;
        double sum = 0.0, term = z;
        for (int i = 3; sum + term != sum; i += 2) {
            sum  = sum + term;
            term = term * z * z / i;
        }
        return 0.5 + sum * phi(z);
    }

    // return Phi(z, mu, sigma) = Gaussian cdf with mean mu and stddev sigma
    // This gives the probability that a random variable X distributed normally with
    // mean mu and stdev sigma produces a value less than z
    public static double Phi(double z, double mu, double sigma) {
        //Log.i("MDP-Gaussian", "Z=" + z + " MU=" + mu + " SIGMA="+ sigma);
        if (z == mu){
            Log.e("MDP-Gaussian", "Z equals mu");
            z += 0.1;
        }
        return Phi((z - mu) / sigma);
    }

    // Compute z for standard normal such that Phi(z) = y via bisection search
    public static double PhiInverse(double y) {
        return PhiInverseHelper(y, .00000001, -8, 8);
    }

    private static double PhiInverseHelper(double y, double delta, double lo, double hi) {
        double mid = lo + (hi - lo) / 2;
        if (hi - lo < delta) return mid;
        if (Phi(mid) > y) return PhiInverseHelper(y, delta, lo, mid);
        else              return PhiInverseHelper(y, delta, mid, hi);
    }

    // Same as previous for arbitrary normal random variables
    public static double PhiInverse(double y, double mu, double sigma) {
        return PhiInverseHelper2(y, mu, sigma, .00000001, (mu - 8*sigma), (mu + 8*sigma));
    }

    private static double PhiInverseHelper2(double y, double mu, double sigma, double delta, double lo, double hi) {
        double mid = lo + (hi - lo) / 2;
        if (hi - lo < delta) return mid;
        if (Phi(mid,mu,sigma) > y) return PhiInverseHelper2(y, mu, sigma, delta, lo, mid);
        else              return PhiInverseHelper2(y, mu, sigma, delta, mid, hi);
    }


}
