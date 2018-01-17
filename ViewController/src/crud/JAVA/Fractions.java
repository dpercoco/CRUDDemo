package crud.JAVA;

import java.math.BigDecimal;
import java.math.RoundingMode;

import java.text.DecimalFormat;

public class Fractions {
    
	public static String intFraction(Double decimal) {
		
            DecimalFormat df = new DecimalFormat("###");
            double fractionPart = decimal % 1;
            double integerPart = decimal - fractionPart;
            String integerPartS = df.format(integerPart) + " ";
            if (integerPart==0) integerPartS="";
            String output = integerPartS + toString(fractionPart);
            if (output.trim().equals("0")) output = "";
            if (output==null) return "";
            return output;
		
	}
	
	public static String toString(Double decimal) {
		
	    int LIMIT = 12;
        int denominators[] = new int[LIMIT + 1];
        int numerator, denominator, temp;
        int MAX_GOODNESS = 100;
        String fraction="";
        double originalDecimal = new BigDecimal(decimal).setScale(2, RoundingMode.HALF_UP).doubleValue();
  
        // Compute all the denominators
        // System.out.println("All computed denominators:");
        int i = 0;
        while (i < LIMIT + 1) {
            denominators[i] = new Double(decimal).intValue();
            //System.out.print(denominators[i] + "  ");
            decimal = 1.0 / (decimal - denominators[i]);
            i = i + 1;
        }
        
        // Compute the i-th approximation
        int last = 0;
        while (last < LIMIT) {
        
            // Initialize variables used in computation
            numerator = 1;
            denominator = 1;
            temp = 0;
            
            // Do the computation
            int current = last;
            while (current >= 0) {
                denominator = numerator;
                numerator = (numerator * denominators[current]) + temp;
                temp = denominator;
                current = current - 1;
            }
            last = last + 1;
                        
            // Display results
            fraction = (int)numerator + "/" +  (int)denominator;            
            double value = (double)numerator/denominator;
            double compareValue = new BigDecimal(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
            
            if(compareValue==originalDecimal){
            	if (denominator==1)fraction = (int)numerator + "" ;
            	fraction += " ";
            	if (numerator==0)fraction = "" ;
            	break;
            }
            
            /**
            int goodness = denominators[last];
            double error = 100 * Math.abs(value - originalDecimal) / originalDecimal;
            if (Math.abs(goodness) > MAX_GOODNESS) break;
            **/
           
        }
        //System.out.println(fraction);
		return fraction;        
    }
}
