package machineLearning;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.List;

import de.uos.fmt.musitech.utility.math.Rational;


public class ErrorFraction implements Serializable {
	private int numer;
	private double numerDbl;
	private double numerAlt;
	private int denom;
	private BigInteger numerBig;
	private BigInteger denomBig;


	public ErrorFraction(int argNumer, int argDenom) {
		numer = argNumer;
		denom = argDenom;
	}


	public ErrorFraction(double argNumer) {
		numerDbl = argNumer;
		denom = 1;
	}


//	public ErrorFraction(double argNumer, int argDenom) {
//		numerAlt = argNumer;
//		denom = argDenom;
//	}


	public ErrorFraction(BigInteger argNum, BigInteger argDenom) {
		numerBig = argNum;
		denomBig = argDenom;
	}


	public int getNumer() {
		return numer;
	}
	
	
	public double getNumerDbl() {
		return numerDbl;
	}
	


	public int getDenom() {
		return denom;
	}
	
	
	public BigInteger getNumerBig() {
		return numerBig;
	}


	public BigInteger getDenomBig() {
		return denomBig;
	}


	public double getNumerAlt() {
		return numerAlt;
	}


	// TESTED
	public double toDouble() {
		return (double) getNumer() / getDenom();
	}


	// TESTED
	public double toDoubleBig() {
		BigDecimal num = new BigDecimal(getNumerBig());
		BigDecimal den = new BigDecimal(getDenomBig());
		
		return num.divide(den, 10, RoundingMode.HALF_UP).doubleValue();
	}


	/**
	 * Converts the given ErrorFraction to a Rational.
	 * 
	 * @return
	 */
	// TESTED
	public Rational toRational() {
		return new Rational(getNumer(), getDenom());
	}


	/**
	 * Multiplies the ErrorFraction's numerator by the given int.
	 * 
	 * @param weight
	 * @return
	 */
	// TESTED
	public ErrorFraction multiplyNumerator(int multiplier) {
		int numerator = getNumer() * multiplier;
		int denominator = getDenom();
		ErrorFraction multiplied = new ErrorFraction(numerator, denominator);
		return multiplied;
	}


	/**
	 * Multiplies the ErrorFraction with the given ErrorFraction.
	 * 
	 * @param weight
	 * @return
	 */
	// TESTED
	public ErrorFraction multiply(ErrorFraction multiplier) {
		int numerator = getNumer() * multiplier.getNumer();
		int denominator = getDenom() * multiplier.getDenom();
		return new ErrorFraction(numerator, denominator);
	}


	/**
	 * Compares two ErrorFractions for equality. Returns true only if the numerators and denominators are equal;
	 * only mathematical equality does not suffice. 
	 * @param argErrorFraction
	 * @return
	 */
	// TESTED
	public boolean isEqual(ErrorFraction argErrorFraction) {
		if (getNumer() == argErrorFraction.getNumer() && getDenom() == argErrorFraction.getDenom()) {
			return true;
		}
		else { 
			return false;
		}
	}


	/**
	 * Returns the sum of the ErrorFractions given in the list.  
	 * 
	 * @param aList
	 * @return
	 */
	// TESTED
	public static ErrorFraction sum(List<ErrorFraction> aList) {
		Rational sumAsRational = new Rational(0, 1);
		for (ErrorFraction e : aList) {
			sumAsRational = sumAsRational.add(e.toRational());
		}
		sumAsRational.reduce();
		return new ErrorFraction(sumAsRational.getNumer(), sumAsRational.getDenom());
	}


	/**
	 * Calculates the weighted average (as an ErrorFraction) of all ErrorFractions contained in the List given
	 * as argument.
	 *  
	 * @param 
	 * @return 
	 */
	// TESTED 
	public static ErrorFraction getWeightedAverage(List<ErrorFraction> aList) {
		int sumOfNumerators = 0;
		int sumOfDenominators = 0;

		for (ErrorFraction e: aList) {
			sumOfNumerators += e.getNumer();
			sumOfDenominators += e.getDenom();
		}
		return new ErrorFraction(sumOfNumerators, sumOfDenominators);
	}


	/**
	 * 
	 * @param arg
	 * @return A BigInteger ErrorFraction.
	 */
	// TESTED
	public static ErrorFraction getWeightedAverageBig(List<ErrorFraction> arg) {
		BigInteger num = BigInteger.valueOf(0);
		BigInteger den = BigInteger.valueOf(0);
		for (ErrorFraction e : arg) {
			num = num.add(BigInteger.valueOf(e.getNumer()));
			den = den.add(BigInteger.valueOf(e.getDenom()));
		}
		return new ErrorFraction(num, den);
	}	


	/**
	 * Calculates the weighted average (as a double) )of all ErrorFractions contained in the List given as
	 * argument.
	 *  
	 * @param 
	 * @return 
	 */
	// TESTED 
	public static double getWeightedAverageAsDouble(List<ErrorFraction> aList) {
		int sumOfNumerators = 0;
		int sumOfDenominators = 0;

		for (ErrorFraction e: aList) {
			sumOfNumerators += e.getNumer();
			sumOfDenominators += e.getDenom();
		}
		return (double) sumOfNumerators / sumOfDenominators;
	}


	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ErrorFraction)) {
			return false;
		}
		else {
			return isEqual((ErrorFraction) obj);
		}
	}


	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(numer);
		sb.append('/');
		sb.append(denom);
		return sb.toString();
	}

}