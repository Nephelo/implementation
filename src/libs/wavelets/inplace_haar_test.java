package libs.wavelets;
/**
   Test code for the inplace_haar.java wavelets code.

   To compile make sure that ".." is in the CLASSPATH.  For
   example, on UNIX (or with the Cygnus Bash shell)

      setenv CLASSPATH ..:$CLASSPATH

   Compile commmand:

      javac simple_haar_test.java
      jvc   simple_haar_test.java

   To run enter:

       jview simple_haar_test
       java  simple_haar_test

 */

// package test;

import libs.wavelets.wavelet_util.*;

class inplace_haar_test {

  /**
    Print an array of doubles
   */
  private void pr_vals( double[] vals ) {
    if (vals != null) {
      for (int i = 0; i < vals.length; i++) {
	System.out.print( vals[i] );
	if (i < vals.length-1)
	  System.out.print(", ");
      }
      System.out.println();
    }
  } // pr_vals



  /**
     Test the simple_haar wavelet code, using the <i>vals</i>
     argument.
   */
  private void wavelet_test( double[] vals )
  {
    libs.wavelets.wavelets.inplace_haar haar = new libs.wavelets.wavelets.inplace_haar();

    System.out.println("test data: ");
    pr_vals( vals );
    System.out.println();

    haar.wavelet_calc( vals );
    System.out.println("wavelet coefficients, ordered by " + 
		       "increasing frequency:");
    haar.pr_ordered();
    System.out.println();

    System.out.println("after calculating inverse Haar transform:");
    haar.inverse();
    haar.pr();
    System.out.println();
  } // wavelet_test


  public static void main( String[] args ) {
    inplace_haar_test mainRef = new inplace_haar_test();
    if (mainRef != null) {
      double vals[] =  { (double)3.0, (double)1.0, (double)0.0,
			 (double)4.0, (double)8.0, (double)6.0,
			 (double)9.0, (double)9.0 };

      mainRef.wavelet_test(vals);
      
      System.out.println();
      double vals2[] = { 32.0, 10.0, 20.0, 38.0,
			 37.0, 28.0, 38.0, 34.0,
			 18.0, 24.0, 18.0, 9.0, 
			 23.0, 24.0, 28.0, 34.0 };

      mainRef.wavelet_test(vals2);
      
      // Calculate the wavelet spectrum
      libs.wavelets.wavelets.inplace_haar haar = new libs.wavelets.wavelets.inplace_haar();
      haar.wavelet_spectrum( vals2 );
      
      // output spectrum to "foofile"
      gnuplot3D pts = new gnuplot3D( vals2, "foofile" );
    }
  } // main

} // inplace_haar_test
