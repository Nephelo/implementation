
package libs.wavelets.wavelet_util;

import java.io.*;

import libs.wavelets.wavelet_util.plot;
import libs.wavelets.sort.qsort;

/**
<p>
class bell_curves
</p>
<p>
  Plot the Haar coefficients as a histogram, in Gnuplot format.  In
  another file generate a normal curve with the mean and standard
  deviation of the histogram.  Using two files allows the histogram
  and the normal curve to be plotted together using gnu plot, where
  the histogram and normal curve will be different colored lines.
  If the spectrum as 256 coefficient points, the files generated
  would be <tt>coef256</tt> and <tt>normal256</tt> for the coefficient
  histogram and the normal curve.  To plot these using gnuplot the
  following command would be used:
</p>
<pre>
   plot 'coef256' with boxes, 'normal256' with lines
</pre>
<p>
  This will result in a gnuplot graph where the histogram is one
  color and the normal curve is another.
</p>
<p>
  A normal curve is a probability distribution, where the values
  are plotted on the x-axis and the probability of that value
  occuring is plotted on the y-axis.  To plot the coefficient 
  histogram in the same terms, the percentage of the total points
  is represented for each histogram bin.  This is the same as the
  integral of the normal curve in the histogram range, if the 
  coefficient histogram fell in a perfect normal distribution.
  For example;
</p>

<p>
<img src="http://www.bearcave.com/misl/misl_tech/wavelets/close_images/coef256.jpg" border=0 align=center>
</p>

 */
public class bell_curves extends plot {

  String class_name() { return "bell_curves"; }

  /**
    A histogram "bin"
   */
  private class bin {
     public bin() {};        // suppress default initialization
     public double start;    // start of the bin
     public double percent;  // percentage of the total points in bin
  }

  /**
   Encapsulate the low and high values of a number range
   */
  private class low_high {
     public low_high() {}
     public low_high( double l, double h )
     {
       low = l;
       high = h;
     }
     public double low;
     public double high;
  }

   /**
      Bell curve info: mean, sigma (the standard deviation)
      
    */
   private class bell_info {
     public bell_info() {}
     public bell_info(double m, double s)
     {
       mean = m;
       sigma = s;
     }
     public double mean;
     public double sigma;
   } // bell_info


  /**
   <p>
   Calculate the mean and standard deviation.
   </p>
   <p>
   The stddev function is passed an array of numbers.
   It returns the mean, standard deviation in the
   bell_info object.
   </p>
   */
  public bell_info stddev( double v[] )
  {
    bell_info stats = null;
    if (v != null && v.length > 0) {
      int N = v.length;
    
      // calculate the mean (a.k.a average)
      double sum = 0.0;
      for (int i = 0; i < N; i++) {
	sum = sum + v[i];
      }
      double mean = sum / (double)N;

      // calculate the standard deviation sum
      double stdDevSum = 0;
      double x;
      for (int i = 0; i < N; i++) {
	x = v[i] - mean;
	stdDevSum = stdDevSum + (x * x);
      }
      double sigmaSquared = stdDevSum / (N-1);
      double sigma = Math.sqrt( sigmaSquared );

      stats = new bell_info(mean, sigma);
    }
    return stats;
  } // stddev


  /**
    <p>
    normal_interval
    </p>

    <p>
    Numerically integreate the normal curve with mean
    <i>info.mean</i> and standard deviation <i>info.sigma</i>
    over the range <i>low</i> to <i>high</i>.
    </P>

    <p>
    There normal curve equation that is integrated is:
     </p>
     <pre>
       f(y) = (1/(s * sqrt(2 * pi)) e<sup>-(1/(2 * s<sup>2</sup>)(y-u)<sup>2</sup></sup>
     </pre>

     <p>
     Where <i>u</i> is the mean and <i>s</i> is the standard deviation.
     </p>

     <p>
     The area under the section of this curve from <i>low</i> to
     <i>high</i> is returned as the function result.
     </p>

     <p>
     The normal curve equation results in a curve expressed as
     a probability distribution, where probabilities are expressed
     as values greater than zero and less than one.  The total area
     under a normal curve is one.
     </p>

     <p>
     The integral is calculated in a dumb fashion (e.g., we're not
     using anything fancy like simpson's rule).  The area in
     the interval <b>x</b><sub>i</sub> to <b>x</b><sub>i+1</sub>
     is 
     </P>

     <pre>
     area = (<b>x</b><sub>i+1</sub> - <b>x</b><sub>i</sub>) * g(<b>x</b><sub>i</sub>)
     </pre>

     <p>
     Where the function g(<b>x</b><sub>i</sub>) is the point on the
     normal curve probability distribution at <b>x</b><sub>i</sub>.
     </p>

     @param info       This object encapsulates the mean and standard deviation
     @param low        Start of the integral
     @param high       End of the integral
     @param num_points Number of points to calculate (should be even)

   */
  private double normal_interval(bell_info info,
				 double low, 
				 double high, 
				 int num_points )
  {
    double integral = 0;

    if (info != null) {
      double s = info.sigma;
      // calculate 1/(s * sqrt(2 * pi)), where <i>s</i> is the stddev
      double sigmaSqrt = 1.0 / (s * (Math.sqrt(2 * Math.PI)));
      double oneOverTwoSigmaSqrd = 1.0 / (2 * s * s);

      double range = high - low;
      double step = range / num_points;
      double x = low;
      double f_of_x;
      double area;
      double t;
      for (int i = 0; i < num_points-1; i++) {
	t = x - info.mean;
	f_of_x = sigmaSqrt * Math.exp( -(oneOverTwoSigmaSqrd * t * t) );
	area = step * f_of_x; // area of one rectangle in the interval
	integral = integral + area;  // sum of the rectangles
	x = x + step;
      } // for
    }

    return integral;
  } // normal_interval



  /**

   <p>
   Output a gnuplot formatted histogram of the area under a normal
   curve through the range <i>m.low</i> to <i>m.high</i> based on the
   mean and standard deviation of the values in the array <i>v</i>.
   The number of bins used in the histogram is <i>num_bins</i>
   </p>

   @param prStr     PrintWriter object for output file
   @param num_bins  Number of histogram bins
   @param m         An object encapsulating the high and low values of v
   @param v         An array of doubles from which the mean and standard deviation is calculated.

   */
  private void normal_curve(PrintWriter prStr,
			    int num_bins,
			    low_high m,
			    double v[] )
  {
    // calculate the mean and standard deviation
    bell_info info = stddev( v );

    int N = v.length;
    int points_per_bin = N/num_bins;
    
    double range = m.high - m.low;
    double step = range / (double)num_bins;
    double start = m.low;
    double end = start + step;
    double area;
    double total_area = 0;

    prStr.println("#");
    prStr.println("# histogram of normal curve");
    prStr.println("# mean = " + info.mean + ", std. dev. = " + info.sigma );
    prStr.println("#");

    for (int i = 0; i < num_bins; i++) {
      area = normal_interval( info, start, end, points_per_bin );
      total_area = total_area + area;
      prStr.println(" " + start + "  " + area );
      start = end;
      end = start + step;
    } // for

    prStr.println("#");
    prStr.println("# Total area under curve = " + total_area );
    prStr.println("#");

  } // normal_curve



  /**
    <p>
    Write out a histogram for the Haar coefficient frequency
    spectrum in gnuplot format.
    </p>

    @param prStr     PrintWriter object for output file
    @param num_bins  Number of histogram bins 
    @param m         An object encapsulating the high and low values from v
    @param v         The array of doubles to histogram
   */
  private void histogram_coef(PrintWriter prStr, 
			      int num_bins, 
			      low_high m,
			      double v[] )
  {
    if (prStr != null && v != null) {
      prStr.println("#");
      prStr.println("# Histogram of Haar coefficients");
      prStr.println("#");
      int len = v.length;
      double range = m.high - m.low;
      double step = range / (double)num_bins;
      double start = m.low;
      double end = start + step;
      int count = 0;
      int i = 0;
      double area = 0;
      
      while (i < len && end <= m.high ) {
	if (v[i] >= start && v[i] < end) {
	  count++;
	  i++;
	}
	else {
	  double percent = (double)count / (double)len;
	  area = area + percent;
	  prStr.println(" " + start + "  " + percent );
	  start = end;
	  end = end + step;
	  count = 0;
	}
      } // for
      prStr.println("#");
      prStr.println("# Total area under curve = " + area );
      prStr.println("#");
    }
  } // histogram_coef


  /**
   <p>
   plot_freq
   </p>

   <p>
   Generate histograms for a set of coefficients
   (passed in the argument <i>v</i>).  Generate
   a seperate histogram for a normal curve.  Both
   histograms have the same number of bins and the
   same scale.
   </p>

   <p>
   The histograms are written to separate files in gnuplot
   format.  Different files are needed (as far as I can tell)
   to allow different colored lines for the coefficient histogram
   and the normal plot.  The file name reflects the number of
   points in the coefficient spectrum.
   </p>

   */
  private void plot_freq( double v[] ) 
     throws IOException
  {
    if (v != null) {
      String file_name = "coef" + v.length;
      PrintWriter prStr = OpenFile( file_name );
      if (prStr != null) {
	final int num_bins = 32;
	qsort.sort( v );
	low_high m = new low_high(v[0], v[v.length-1]);
	histogram_coef( prStr, num_bins, m, v );
	prStr.close();

	file_name = "normal" + v.length;
	prStr = OpenFile( file_name );
	if (prStr != null) {
	  normal_curve( prStr, num_bins, m, v );
	  prStr.close();
	}
	else {
	  IOException ioerr = new IOException();
	  throw ioerr;
	}
      }
      else {
	IOException ioerr = new IOException();
	throw ioerr;
      }
    }
  }  // plot_freq


  /**
   <p>
   This function is passed an ordered set of Haar wavelet
   coefficients.  For each frequency of coefficients
   a graph will be generated, in gnuplot format, that
   plots the ordered Haar coefficients as a histogram.  A
   gaussian (normal) curve with the same mean and standard
   deviation will also be plotted for comparision.
   </p>
   <p>
   The histogram for the coefficients is generated by counting the
   number of coefficients that fall within a particular bin range and
   then dividing by the total number of coefficients.  This results in
   a histogram where all bins add up to one (or to put it another way,
   a curve whose area is one).
   </p>
   <p>
   The standard formula for a normal curve results in a curve showing
   the probability profile.  To convert this curve to the same
   scale as the coefficient histogram, the area under the curve is
   integrated over the range of each bin (corresponding to the 
   coefficient histogram bins).  The area under the normal curve
   is one, resulting in the same scale.
   </p>
   <p>
   The size of the coefficient array must be a power of two.  When the
   Haar coefficients are ordered (see inplace_haar) the coefficient
   frequencies are the component powers of two.  For example, if the
   array length is 512, there will be 256 coefficients from the highest
   frequence from index 256 to 511.  The next frequency set will
   be 128 in length, from 128 to 255, the next will be 64 in length
   from 64 to 127, etc...
   </p>

   <p>
   As the number of coefficients decreases, the histograms become
   less meaningful.  This function only graphs the coefficient
   spectrum down to 64 coefficients.
   </p>

   */
  public void plot_curves( double coef[] )
  {
    if (coef != null) {
      final int min_coef = 64;
      int len = coef.length;
      int end = len;
      int start = len >> 1;
      while (start >= min_coef) {
	double v[] = new double[ start ];
	int ix = 0;
	for (int i = start; i < end; i++) {
	  v[ix] = coef[i];
	  ix++;
	}
	try {
	  plot_freq( v );
	}
	catch (Exception e) {
	  break; // exit the while loop
	}
	end = start;
	start = end >> 1;
      } // for
    }
  } // plot_curves

} // curve_plot
