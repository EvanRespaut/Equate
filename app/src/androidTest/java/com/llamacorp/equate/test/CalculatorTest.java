package com.llamacorp.equate.test;

import java.math.BigDecimal;
import java.math.MathContext;

import junit.framework.TestCase;

import com.llamacorp.equate.Calculator;
import com.llamacorp.equate.Solver;

public class CalculatorTest extends TestCase {

	//THIS STUFF IS THE SAME AS CALC
	//we want the display precision to be a bit less than calculated
	//private MathContext mcOperate = new MathContext(intCalcPrecision);
	private MathContext mcDisp = new MathContext(intDisplayPrecision);

	//precision for all calculations
	public static final int intDisplayPrecision = 8;
	public static final int intCalcPrecision = intDisplayPrecision+2;




	protected void setUp() throws Exception {
		super.setUp();
	}

	
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testParseKeyPressed() {
		Calculator calc = Calculator.getTestCalculator();
		//try all ops before (should do nothing), try double decimal, try changing op, and extra equals
		loadStringToCalc("=++/-+-*--1..+4.34b+-2-==", calc);

		assertEquals("1.3", calc.toString());

		//should clear the expression
		loadStringToCalc("b", calc);
		assertEquals("", calc.toString());


		//test clear key
		loadStringToCalc(".01=5c", calc);
		assertEquals("", calc.toString());

		//order of operations
		loadStringToCalc("10+.5*4=", calc);

		assertEquals("12", calc.toString());

		//try adding to expression
		loadStringToCalc("-4=", calc);
		assertEquals("8", calc.toString());

		//try typing a number now
		loadStringToCalc("4", calc);
		assertEquals("4", calc.toString());

		//try adding just a . make sure it does't break
		loadStringToCalc("1+.+2=", calc);
		assertEquals("41.2", calc.toString());


		//try adding just a .E and E. make sure it does't break
		loadStringToCalc(".1E.*/0E.E.-3=", calc);
		assertEquals("-2.9", calc.toString());

		//try to break it some more
		loadStringToCalc("4.E=1+.+E=.=", calc);

		//this had problems before...
		loadStringToCalc("0-5-5-5=", calc);

		//this had problems before...
		loadStringToCalc("1+E+1=", calc);

		loadStringToCalc("b-1+4=", calc);
		assertEquals("3", calc.toString());		

		loadStringToCalc("3-1*-1=", calc);
		assertEquals("4", calc.toString());		

		loadStringToCalc("b-3*-1*-1=", calc);
		assertEquals("-3", calc.toString());		

		loadStringToCalc("3-(2-4)*-5=", calc);
		assertEquals("-7", calc.toString());		

		loadStringToCalc("2+-2*-3=", calc);
		assertEquals("8", calc.toString());		
		
		loadStringToCalc("(30+3%", calc);
		calc.setSelection(2,2);
		loadStringToCalc("=", calc);
		assertEquals("30.03", calc.toString());		
	}

	public void testNumberAccuracy(){
		Calculator calc = Calculator.getTestCalculator();
		loadStringToCalc("4", calc);

		//make sure 2.2 is represented properly
		loadStringToCalc("c2.2*3=", calc);
		assertEquals("6.6", calc.toString());

		//make sure 1/3= then *3 is 1	
		loadStringToCalc("1/3=*3=", calc);
		assertEquals("1", calc.toString());

		//make #E+# and #E-# are parsed correctly (save time with constructor)
		calc = Calculator.getTestCalculator();
		loadStringToCalc("10000000000*10000000000+10=", calc);
		//be sure the exponent at the end is a 20 (other part of the string might be rounded differently
		assertTrue(calc.toString().matches(".*E20$"));
	}


	public void testErrors(){
		Calculator calc = Calculator.getTestCalculator();
		//divide by zero error
		loadStringToCalc("1/0=", calc);
		assertEquals(Solver.strDivideZeroError, calc.toString());

		//make sure num clears the error
		loadStringToCalc("+1=", calc);
		assertEquals("1", calc.toString());

		//overflow
		loadStringToCalc("9E9999999999=", calc);		
	}


	public void testCleaning(){
		Calculator calc = Calculator.getTestCalculator();
		//make sure we're cleaning properly
		loadStringToCalc("6.10000=", calc);
		assertEquals("6.1", calc.toString());
		loadStringToCalc("0.00800=", calc);
		assertEquals("0.008", calc.toString());
		loadStringToCalc("6.000=", calc);
		assertEquals("6", calc.toString());
		loadStringToCalc("800=", calc);
		assertEquals("800", calc.toString());
		loadStringToCalc(".080800=", calc);
		assertEquals("0.0808", calc.toString());
	}

	public void testExponents(){
		Calculator calc = Calculator.getTestCalculator();
		//first try to break it
		loadStringToCalc("E6EE*/bE++--*2=", calc);
		assertEquals("0.06", calc.toString());

		//try some random math
		loadStringToCalc("30E2-2E10*=", calc);
		assertEquals("-1.9999997E10", calc.toString());

		//tests to make sure that after a #.#E# expression, we can put a .
		loadStringToCalc("3.2E2*.5=", calc);
		assertEquals("160", calc.toString());


		//test conversion of long numbers into and out of E
		calc.parseKeyPressed("6");
		for(int i=0;i<Calculator.intDisplayPrecision;i++)
			calc.parseKeyPressed("0");
		calc.parseKeyPressed("=");
		//not sure if we'll be keeping the . after the 6, both will pass for now
		assertTrue(calc.toString().matches("6\\.?E8"));

		//make sure the number one less than the precision is display as plain text
		String tester="6";
		calc.parseKeyPressed(tester);
		for(int i=0;i<Calculator.intDisplayPrecision-1;i++){
			calc.parseKeyPressed("0");
			tester=tester+"0";
		}
		calc.parseKeyPressed("=");
		assertTrue(calc.toString().equals(tester));


		//0E8 should reduce to 0 not "E8"
		loadStringToCalc("0E8==", calc);

		//catch the potentail problem of "532E+-"
		loadStringToCalc("--5232E+-0=", calc);
		assertEquals("-5232", calc.toString());

		//catch problem where this would hang
		loadStringToCalc("1E9999999+1=", calc);
		assertEquals("Number Too Large", calc.toString());

		//catch problem where this would hang
		loadStringToCalc("1E9999999+1E999999=", calc);
		assertEquals("Number Too Large", calc.toString());

		loadStringToCalc("2E-2E-2=", calc);
		assertEquals("-1.98", calc.toString());
	}


	public void testPower(){
		Calculator calc = Calculator.getTestCalculator();
		//test basic functionality
		loadStringToCalc("4^3=", calc);
		assertEquals("64", calc.toString());
		//test sqrt
		loadStringToCalc("64^.5=", calc);
		assertEquals("8", calc.toString());
		//test order of operations
		loadStringToCalc("2*3^(1+3)=", calc);
		assertEquals("162", calc.toString());
		//test lots of decimals
		loadStringToCalc("4.3^.3=", calc);
		BigDecimal bd = new BigDecimal(1.5489611908722423119058589800223, mcDisp);
		assertTrue(calc.toString().matches(bd.toString()));	
		//test large numbers
		loadStringToCalc("9.1^500=", calc);
		assertEquals("Number Too Large", calc.toString());
		//test mixed exponents and powers
		loadStringToCalc("2.1E2^1.1E2=", calc);
		assertEquals("2.7804969E255", calc.toString());

		loadStringToCalc("3-3^2=", calc);
		assertEquals("-6", calc.toString());

		loadStringToCalc("b-2*-4^2=", calc);
		assertEquals("32", calc.toString());

		loadStringToCalc("(-5)^2.0=", calc);
		assertEquals("25", calc.toString());

		loadStringToCalc("b-(6)^(.9+(1.1))=", calc);
		assertEquals("-36", calc.toString());

		loadStringToCalc("(-2+1)^2=", calc);
		assertEquals("1", calc.toString());

		loadStringToCalc("3*-((-2)+(1^0))^2=", calc);
		assertEquals("-3", calc.toString());

		loadStringToCalc("3*-2.E-3^-2.E-3=", calc);
		assertEquals("-3.0375203", calc.toString());

		loadStringToCalc("2+-1^2*-9^((.5)^1+.1+-.1^1)=", calc);
		assertEquals("5", calc.toString());

		//TODO do we really wanna fix this?
		//loadStringToCalc("2^3^2=", calc);
		//assertEquals("512", calc.toString());

	}


	public void testSelection(){
		Calculator calc = Calculator.getTestCalculator();

		loadStringToCalc("342+-23523*3532", calc);
		assertEquals("342+-23523*3532", calc.toString());

		//this is reversed intentionally, this can happen if user drags end before start
		calc.setSelection(10, 5);
		loadStringToCalc("15", calc);		
		assertEquals("342+-15*3532", calc.toString());

		calc.setSelection(5, 7);
		loadStringToCalc("-*1)", calc);		
		assertEquals("(342*1)*3532", calc.toString());

		calc.setSelection(7, 7);
		loadStringToCalc(".7", calc);		
		assertEquals("(342*1)*.7*3532", calc.toString());

		calc.setSelection(11, 11);
		loadStringToCalc("^", calc);		
		assertEquals("(342*1)*.7^3532", calc.toString());

		calc.setSelection(4, 11);
		loadStringToCalc("(", calc);		
		assertEquals("(342*(3532", calc.toString());

		loadStringToCalc("=", calc);		
		assertEquals("1207944", calc.toString());

		calc.setSelection(4, 4);
		loadStringToCalc("EE^*/-", calc);		
		assertEquals("1207E-944", calc.toString());

		calc.setSelection(0, 9);
		loadStringToCalc("-8^3=", calc);		
		assertEquals("-512", calc.toString());

		//TODO add this test to android test since this problem is fixed, it's just 
		//fixed using an android specific method
		calc.setSelection(2, 2);
		calc.setSolved(false);
		loadStringToCalc(".", calc);		
		assertEquals("-5.12", calc.toString());
	}

	public void testNegateOperator(){
		Calculator calc = Calculator.getTestCalculator();

		loadStringToCalc("-1+2+23+63n=", calc);
		assertEquals("-39", calc.toString());

		loadStringToCalc("5-7n=", calc);
		assertEquals("12", calc.toString());
		
		loadStringToCalc("n=", calc);
		assertEquals("-12", calc.toString());
		
		loadStringToCalc("n=", calc);
		assertEquals("12", calc.toString());
		
		loadStringToCalc("(45-67)n1=", calc);
		assertEquals("-23", calc.toString());
		
		loadStringToCalc("43*n=", calc);
		assertEquals("43", calc.toString());
		
		loadStringToCalc("43E-30n=", calc);
		assertEquals("-4.3E-29", calc.toString());
		
		loadStringToCalc("(-45n)=", calc);
		assertEquals("45", calc.toString());
	}

	public void testInvertOperator(){
		Calculator calc = Calculator.getTestCalculator();

		loadStringToCalc("-4i=", calc);
		assertEquals("-0.25", calc.toString());
		
		loadStringToCalc("i", calc);
		assertEquals("-4", calc.toString());

		loadStringToCalc("15+100+32", calc);
		calc.setSelection(3,3);
		loadStringToCalc("i=", calc);
		assertEquals("47.01", calc.toString());

		loadStringToCalc("15+100+32", calc);
		calc.setSelection(6,6);
		loadStringToCalc("i=", calc);
		assertEquals("47.01", calc.toString());

		loadStringToCalc("15+100+32", calc);
		calc.setSelection(4,5);
		loadStringToCalc("i=", calc);
		assertEquals("47.01", calc.toString());

		loadStringToCalc("ci16=", calc);
		assertEquals("0.0625", calc.toString());

	}
	
	
	public void testPara(){
		Calculator calc = Calculator.getTestCalculator();

		loadStringToCalc("(2+3)*3+1*(3-1*(1))=", calc);
		assertEquals("17", calc.toString());

		loadStringToCalc("1+2*(2+3.3)=", calc);
		assertEquals("11.6", calc.toString());

		//test for adding multiplies between num and (
		loadStringToCalc("6(.1+.4)=", calc);
		assertEquals("3", calc.toString());

		//test for adding multiplies between . and (
		loadStringToCalc("2.(3)=", calc);
		assertEquals("6", calc.toString());

		//test for adding multiplies between E and (
		loadStringToCalc("5E(3*2)=", calc);
		assertEquals("5000000", calc.toString());

		//test for adding multiplies between E and (
		loadStringToCalc("5E(-3*2)=", calc);
		assertEquals("0.000005", calc.toString());

		//test for adding multiplies between E and (
		loadStringToCalc("(1-7)(2+1)=", calc);
		assertEquals("-18", calc.toString());

		//test for adding multiplies number and )
		loadStringToCalc("(2/4)10=", calc);
		assertEquals("5", calc.toString());

		//test auto add opening para
		loadStringToCalc("1+2+3)=", calc);
		assertEquals("6", calc.toString());

		//test auto add closing para; also test para followed by invalid op 
		loadStringToCalc("(*3=", calc);
		assertEquals("3", calc.toString());		

		//test auto add closing para; also test para followed by invalid op 
		loadStringToCalc("(3(.2=", calc);
		assertEquals("0.6", calc.toString());

		//test auto adding multiplies for paras
		loadStringToCalc("2((5)6)(3)7(3)(4).2+8.(0)=", calc);
		assertEquals("3024", calc.toString());
	}


	public void testPercent(){
		Calculator calc = Calculator.getTestCalculator();

		loadStringToCalc("200+5%=", calc);
		assertEquals("210", calc.toString());

		loadStringToCalc("200-5%=", calc);
		assertEquals("190", calc.toString());

		loadStringToCalc("200+-5%=", calc);
		assertEquals("190", calc.toString());

		loadStringToCalc("5%=", calc);
		assertEquals("0.05", calc.toString());

		loadStringToCalc("b-5%=", calc);
		assertEquals("-0.05", calc.toString());

		loadStringToCalc("200*5%=", calc);
		assertEquals("10", calc.toString());

		loadStringToCalc("200/5%=", calc);
		assertEquals("4000", calc.toString());

		loadStringToCalc("200*-5%=", calc);
		assertEquals("-10", calc.toString());

		loadStringToCalc("200/-5%=", calc);
		assertEquals("-4000", calc.toString());

		loadStringToCalc("1+200+5%=", calc);
		assertEquals("211.05", calc.toString());

		loadStringToCalc("1+200+5%*/^+-4%=", calc);
		assertEquals("202.608", calc.toString());

		loadStringToCalc("5%*%6=", calc);
		assertEquals("0.3", calc.toString());
		
		//test 88 percent of 5
		loadStringToCalc("88%5=", calc);
		assertEquals("4.4", calc.toString());
		
		//TODO, simplified broken version  is 1+2%3
		loadStringToCalc(".1+2.%3.%47.+200%.5=", calc);
		assertEquals("1.1282", calc.toString());
	}


	public void testUnits(){
		Calculator calc = Calculator.getTestCalculator();

		clickConvKey(Const.TEMP, Const.F, calc);
		loadStringToCalc("212", calc);
		clickConvKey(Const.TEMP, Const.C, calc);
		assertEquals("100", calc.toString());
		clickConvKey(Const.TEMP, Const.F, calc);
		assertEquals("212", calc.toString());
		clickConvKey(Const.TEMP, Const.K, calc);
		assertEquals("373.15", calc.toString());
		clickConvKey(Const.TEMP, Const.C, calc);
		assertEquals("100", calc.toString());

		//1E900 yard to mm to yard should not hang
		loadStringToCalc("c1E900", calc);
		clickConvKey(Const.LENGTH, Const.YARD, calc);
		clickConvKey(Const.LENGTH, Const.MM, calc);
		assertEquals("9.144E902", calc.toString());

		//501 in F to K, check 533.70556, back to F should be 501
		loadStringToCalc("c501", calc);
		clickConvKey(Const.TEMP, Const.F, calc);
		clickConvKey(Const.TEMP, Const.K, calc);
		clickConvKey(Const.TEMP, Const.F, calc);
		assertEquals("501", calc.toString());

		loadStringToCalc("c456+3+6", calc);
		calc.setSelection(5,5);
		loadStringToCalc("b", calc);
		clickConvKey(Const.TEMP, Const.F, calc);
		clickConvKey(Const.TEMP, Const.K, calc);
		assertEquals(Solver.strSyntaxError, calc.toString());
	}


	private void clickConvKey(int unitTypePos, int convKeyPos, Calculator calc){
		calc.setUnitTypePos(unitTypePos);
		
		boolean requestConvert = calc.getCurrUnitType().selectUnit(convKeyPos);

		//this is normally performed in the convert key fragment, have to do this manually here
		if(requestConvert){
			calc.convertFromTo(calc.getCurrUnitType().getPrevUnit(), calc.getCurrUnitType().getCurrUnit());
		}
	}

	/**
	 * Helper function to type in keys to calc and return result
	 * @param str is input key presses
	 * @return calc.toString()
	 */
	private void loadStringToCalc(String str, Calculator calc){

		for(int i=0; i<str.length();i++){
			calc.parseKeyPressed(String.valueOf(str.charAt(i)));
			//be sure to update where keys are going
			//	calc.setSelection(calc.toString().length(), calc.toString().length());
		}
	}


	public Calculator bruteCalc;
	public String [] allKeyArray={"0","1","2","3","4","5","6","7","8","9",".","+","-","*","/","b","E","="};
	public String [] someKeyArray={"0","9",".","+","-","*","/","b","E","="};

	//these configure the testing
	public String [] testKeyArray=someKeyArray;
	public boolean clearAfterEach = true;
	public int numRuns=4;


	//run the brute force test
	public void testBrute(){
		bruteCalc = Calculator.getTestCalculator();
		bruteForceTest(numRuns,"");
	}


	//this will cycle through all combinations of keys
	public void bruteForceTest(int numTimes, String startSting){
		if(numTimes==0)
			return;
		numTimes--;
		for(int i=0;i<testKeyArray.length;i++){
			if(numTimes==0){
				for(int s=0;s<startSting.length();s++){
					String str=String.valueOf(startSting.charAt(s));
					//System.out.print(str);
					//System.out.println("Expression= " + bruteCalc.toString());

					bruteCalc.parseKeyPressed(str);					
				}
				try {
					bruteCalc.parseKeyPressed("=");
				} catch (Exception e){
					//System.out.println("Error input: \"");
					//System.out.print(startSting);
					//System.out.println("=" + "\"");
					throw new IllegalStateException();
				}
				if(clearAfterEach)
					bruteCalc.parseKeyPressed("c");
				//System.out.println("=");
				break;
			}
			else
				bruteForceTest(numTimes,startSting + testKeyArray[i]);
		}
	}

}

















