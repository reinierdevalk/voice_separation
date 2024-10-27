package machineLearning;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.uos.fmt.musitech.utility.math.Rational;

public class ErrorFractionTest {
	
	private double delta;
	
	@Before
	public void setUp() throws Exception {
		delta = 1e-9;
	}


	@After
	public void tearDown() throws Exception {
	}


	@Test
	public void testToDouble() {
		ErrorFraction e1 = new ErrorFraction(8, 16);
		ErrorFraction e2 = new ErrorFraction(1, 5);
		ErrorFraction e3 = new ErrorFraction(4, 32);

		double expected1 = 0.5;
		double expected2 = 0.2;
		double expected3 = 0.125;

		double actual1 = e1.toDouble();
		double actual2 = e2.toDouble();
		double actual3 = e3.toDouble();

		assertEquals(expected1, actual1, delta);
		assertEquals(expected2, actual2, delta);
		assertEquals(expected3, actual3, delta);
	}


	@Test
	public void testToDoubleBig() {
		ErrorFraction e1 = new ErrorFraction(BigInteger.valueOf(300000), BigInteger.valueOf(600000));
		ErrorFraction e2 = new ErrorFraction(BigInteger.valueOf(3000000), BigInteger.valueOf(6000000));
		ErrorFraction e3 = new ErrorFraction(BigInteger.valueOf(30000000), BigInteger.valueOf(60000000));

		double expected1 = 0.5;
		double expected2 = 0.5;
		double expected3 = 0.5;

		double actual1 = e1.toDoubleBig();
		double actual2 = e2.toDoubleBig();
		double actual3 = e3.toDoubleBig();

		assertEquals(expected1, actual1, delta);
		assertEquals(expected2, actual2, delta);
		assertEquals(expected3, actual3, delta);
	}


	@Test
	public void testToRational() {
		ErrorFraction e = new ErrorFraction(8, 16);

		Rational expected = new Rational(8, 16);
		Rational actual = e.toRational();

		assertEquals(expected, actual);
		assertEquals(expected.getNumer(), actual.getNumer());
		assertEquals(expected.getDenom(), actual.getDenom());
	}


	@Test
	public void testMultiplyNumerator() {
		ErrorFraction e1 = new ErrorFraction(8, 16); // new ErrorFraction(56290126, 59696271); 
		ErrorFraction e2 = new ErrorFraction(3, 41);
		int multiplier1 = 5; // 1478;
		int multiplier2 = 24;

		ErrorFraction expected1 = new ErrorFraction(40, 16);; //new ErrorFraction(83196806228, 59696271);   
		ErrorFraction expected2 = new ErrorFraction(72, 41);

		ErrorFraction actual1 = e1.multiplyNumerator(multiplier1);
		ErrorFraction actual2 = e2.multiplyNumerator(multiplier2);

		assertEquals(expected1, actual1);
		assertEquals(expected1.getNumer(), actual1.getNumer());
		assertEquals(expected1.getDenom(), actual1.getDenom());
		assertEquals(expected2, actual2);
		assertEquals(expected2.getNumer(), actual2.getNumer());
		assertEquals(expected2.getDenom(), actual2.getDenom());
	}


	@Test
	public void testMultiply() {
		ErrorFraction e1 = new ErrorFraction(8, 16);
		ErrorFraction e2 = new ErrorFraction(3, 41);
		ErrorFraction multiplier1 = new ErrorFraction(3, 2);
		ErrorFraction multiplier2 = new ErrorFraction(2, 5);

		ErrorFraction expected1 = new ErrorFraction(24, 32);
		ErrorFraction expected2 = new ErrorFraction(6, 205);

		ErrorFraction actual1 = e1.multiply(multiplier1);
		ErrorFraction actual2 = e2.multiply(multiplier2);

		assertEquals(expected1, actual1);
		assertEquals(expected1.getNumer(), actual1.getNumer());
		assertEquals(expected1.getDenom(), actual1.getDenom());
		assertEquals(expected2, actual2);
		assertEquals(expected2.getNumer(), actual2.getNumer());
		assertEquals(expected2.getDenom(), actual2.getDenom());
	}


	@Test
	public void testIsEqual() {
		ErrorFraction e1 = new ErrorFraction(3, 4);
		ErrorFraction e2 = new ErrorFraction(3, 4);
		ErrorFraction e3 = new ErrorFraction(6, 8);
		ErrorFraction e4 = new ErrorFraction(2, 4);

		boolean expected1 = true;
		boolean expected2 = false;
		boolean expected3 = false;

		boolean actual1 = e1.isEqual(e2);
		boolean actual2 = e1.isEqual(e3);
		boolean actual3 = e1.isEqual(e4);

		assertEquals(expected1, actual1);
		assertEquals(expected2, actual2);
		assertEquals(expected3, actual3);
	}


	@Test
	public void testSum() {
		List<ErrorFraction> list1 = Arrays.asList(new ErrorFraction[]{new ErrorFraction(3, 4), 
			new ErrorFraction(1, 2), new ErrorFraction(1, 7)});
		List<ErrorFraction> list2 = Arrays.asList(new ErrorFraction[]{new ErrorFraction(1, 3), 
			new ErrorFraction(2, 4), new ErrorFraction(5, 3)});

		List<ErrorFraction> expected = 
			Arrays.asList(new ErrorFraction[]{new ErrorFraction(39, 28), new ErrorFraction(5, 2)});

		List<ErrorFraction> actual = new ArrayList<ErrorFraction>();
		actual.add(ErrorFraction.sum(list1));
		actual.add(ErrorFraction.sum(list2));

		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).getNumer(), actual.get(i).getNumer());
			assertEquals(expected.get(i).getDenom(), actual.get(i).getDenom());
		}
	}


	@Test
	public void testGetWeightedAverage() {
		ErrorFraction e1 = new ErrorFraction(7, 8); 
		ErrorFraction e2 = new ErrorFraction(4, 5);
		ErrorFraction e3 = new ErrorFraction(-5, 6);
		ErrorFraction e4 = new ErrorFraction(2, 11);
		List<ErrorFraction> toBeAveraged = Arrays.asList(new ErrorFraction[]{e1, e2, e3, e4});

		ErrorFraction expected = new ErrorFraction(8, 30);

		ErrorFraction actual = ErrorFraction.getWeightedAverage(toBeAveraged);

		assertEquals(expected, actual);	  
	}


	@Test
	public void testGetWeightedAverageBig() {
		List<ErrorFraction> list = Arrays.asList(new ErrorFraction[]{
			new ErrorFraction(1, 2), new ErrorFraction(3, 4),
			new ErrorFraction(5, 6), new ErrorFraction(7, 8),
		});

		ErrorFraction expected = new ErrorFraction(BigInteger.valueOf(16), BigInteger.valueOf(20));
		ErrorFraction actual = ErrorFraction.getWeightedAverageBig(list);

		assertEquals(expected, actual);
		assertEquals(expected.getNumer(), actual.getNumer());
		assertEquals(expected.getDenom(), actual.getDenom());
	}


	@Test
	public void testGetWeightedAverageAsDouble() {
		ErrorFraction e1 = new ErrorFraction(7, 8); 
		ErrorFraction e2 = new ErrorFraction(4, 5);
		ErrorFraction e3 = new ErrorFraction(-5, 6);
		ErrorFraction e4 = new ErrorFraction(2, 11);
		List<ErrorFraction> toBeAveraged = Arrays.asList(new ErrorFraction[]{e1, e2, e3, e4});

		double expected = (double) 8 / 30;
		double actual = ErrorFraction.getWeightedAverageAsDouble(toBeAveraged);

		assertEquals(expected, actual, delta);	 
	}

}
