package com.llamacorp.equate.unit;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;

class UnitInitializer {


	private static class UnitInitializerError extends RuntimeException {
	}

	/**
	 * Method to generate a default unit type array in the form of a linked
	 * hash map using the keys supplied as a parameter.
	 * @param keys for the returned Unit Type Hash map
	 * @param tabNames list of names for Unit Type tabs
	 * @return a linked hash map of Unit Types
	 * @throws UnitInitializerError if the supplied keys parameter is not of the
	 * same length as the default unit type array.
	 */
	public static LinkedHashMap<String, UnitType> getUnitTypeMap(ArrayList<String> keys,
																					 ArrayList<String> tabNames)
			  throws UnitInitializerError {
		LinkedHashMap<String, UnitType> unitTypeArray = new LinkedHashMap<>();
		ArrayList<UnitType> units = getDefaultUnitArray(tabNames);
		if (units.size() != keys.size())
			throw new UnitInitializerError();
		else {
			for (int i = 0; i < keys.size(); i++) {
				unitTypeArray.put(keys.get(i), units.get(i));
			}
			return unitTypeArray;
		}
	}


	private static ArrayList<UnitType> getDefaultUnitArray(ArrayList<String> tabNames) {
		ArrayList<UnitType> unitTypeArray = new ArrayList<>();

		int nameInd = 0;
		unitTypeArray.add(getCurrUnitType(tabNames.get(nameInd++)));

		UnitType unitsOfTemp = new UnitType(tabNames.get(nameInd++));
		unitsOfTemp.addUnit(new UnitTemperature());
		unitsOfTemp.addUnit(new UnitTemperature());
		unitsOfTemp.addUnit(new UnitTemperature());
		unitsOfTemp.addUnit(new UnitTemperature());
		unitsOfTemp.addUnit(new UnitTemperature("\u00B0F", "Fahrenheit", UnitTemperature.FAHRENHEIT));

		unitsOfTemp.addUnit(new UnitTemperature());
		unitsOfTemp.addUnit(new UnitTemperature());
		unitsOfTemp.addUnit(new UnitTemperature());
		unitsOfTemp.addUnit(new UnitTemperature("K", "Kelvin", UnitTemperature.KELVIN));
		unitsOfTemp.addUnit(new UnitTemperature("\u00B0C", "Celsius", UnitTemperature.CELSIUS));
		unitTypeArray.add(unitsOfTemp);


		UnitType unitsOfWeight = new UnitType(tabNames.get(nameInd++));
		unitsOfWeight.addUnit(new UnitScalar("oz", "Ounces", 1 / 0.028349523125)); //exact
		unitsOfWeight.addUnit(new UnitScalar("lb", "Pounds", 1 / 0.45359237)); //exact
		unitsOfWeight.addUnit(new UnitScalar("ton US", "Short tons", 1 / 907.18474)); //exact
		unitsOfWeight.addUnit(new UnitScalar("ton UK", "Long tons", 1 / 1016.0469088)); //exact
		unitsOfWeight.addUnit(new UnitScalar("st", "Stones", 1 / 6.35029318)); //exact

		unitsOfWeight.addUnit(new UnitScalar("\u00B5g", "Micrograms", 1 / 1e-9));
		unitsOfWeight.addUnit(new UnitScalar("mg", "Milligrams", 1 / 1e-6));
		unitsOfWeight.addUnit(new UnitScalar("g", "Grams", 1 / 0.001));
		unitsOfWeight.addUnit(new UnitScalar("kg", "Kilograms", 1));
		unitsOfWeight.addUnit(new UnitScalar("ton", "Metric tons", 1 / 1e3));

		unitsOfWeight.addUnit(new UnitScalar("oz t", "Troy ounces", 1 / 0.0311034768)); //exact
		unitsOfWeight.addUnit(new UnitScalar("gr", "Grains", 1 / 6.479891E-5)); //exact
		unitsOfWeight.addUnit(new UnitScalar("dwt", "Pennyweights", 20 / 0.0311034768)); //exact, 1/20 troy oz
		unitsOfWeight.addUnit(new UnitScalar("CD", "Carats", 5000)); // =200mg
		unitsOfWeight.addUnit(new UnitScalar("llama", "Avg llama weight", 1 / 165.0)); //avg of 130 and 200kg
		unitTypeArray.add(unitsOfWeight);


		UnitType unitsOfLength = new UnitType(tabNames.get(nameInd++));
		unitsOfLength.addUnit(new UnitScalar("in", "Inches", 1 / 0.0254));//exact
		unitsOfLength.addUnit(new UnitScalar("ft", "Feet", 1 / 0.3048));//exact: in*12
		unitsOfLength.addUnit(new UnitScalar("yd", "Yards", 1 / 0.9144));//exact: in*12*3
		unitsOfLength.addUnit(new UnitScalar("mi", "Miles", 1 / 1609.344));//exact: in*12*5280
		unitsOfLength.addUnit(new UnitScalar("km", "Kilometers", 1 / 1000.0));

		unitsOfLength.addUnit(new UnitScalar("\u00B5m", "Micrometers", 1E6));
		unitsOfLength.addUnit(new UnitScalar("mm", "Millimeters", 1000));
		unitsOfLength.addUnit(new UnitScalar("cm", "Centimeters", 100));
		unitsOfLength.addUnit(new UnitScalar("m", "Meters", 1));

		unitsOfLength.addUnit(new UnitScalar("nm", "Nanometers", 1E9));
		unitsOfLength.addUnit(new UnitScalar("pm", "Picometers", 1E12));
		unitsOfLength.addUnit(new UnitScalar("\u212B", "\u212Bngstr\u00F6ms", 1E10));
		unitsOfLength.addUnit(new UnitScalar("mil", "Thousandths of an inch", 1 / 2.54E-5));
		unitsOfLength.addUnit(new UnitScalar("fur", "Furlongs", 0.00497096954));
		unitsOfLength.addUnit(new UnitScalar("pc", "Parsecs", 3.24078E-17));
		unitsOfLength.addUnit(new UnitScalar("nmi", "Nautical miles", 1 / 1852.0));
		unitsOfLength.addUnit(new UnitScalar("ly", "Light years", 1 / 9.4607E15));
		unitsOfLength.addUnit(new UnitScalar("au", "Astronomical units", 1 / 1.495978707E11)); //exact
		unitsOfLength.addUnit(new UnitScalar("llama", "Avg llama height", 1 / 1.75));
		unitTypeArray.add(unitsOfLength);


		UnitType unitsOfArea = new UnitType(tabNames.get(nameInd++));
		unitsOfArea.addUnit(new UnitScalar("in\u00B2", "Square inches", 1 / 0.00064516));//exact: 0.0254^2
		unitsOfArea.addUnit(new UnitScalar("ft\u00B2", "Square feet", 1 / 0.09290304));//0.3048^2
		unitsOfArea.addUnit(new UnitScalar("yd\u00B2", "Square yards", 1 / 0.83612736));//0.3048^2*9
		unitsOfArea.addUnit(new UnitScalar("acre", "Acres", 1 / 4046.8564224));//0.3048^2*9*4840
		unitsOfArea.addUnit(new UnitScalar("mi\u00B2", "Square miles", 1 / 2589988.110336));//1609.344^2

		unitsOfArea.addUnit(new UnitScalar("mm\u00B2", "Square millimeters", 1 / 0.000001));
		unitsOfArea.addUnit(new UnitScalar("cm\u00B2", "Square centimeters", 1 / 0.0001));
		unitsOfArea.addUnit(new UnitScalar("m\u00B2", "Square meters", 1));
		unitsOfArea.addUnit(new UnitScalar("km\u00B2", "Square kilometers", 1 / 1000000.0));

		unitsOfArea.addUnit(new UnitScalar("ha", "Hectares", 1 / 10000.0));
		unitsOfArea.addUnit(new UnitScalar("a", "Ares", 0.01));
		unitsOfArea.addUnit(new UnitScalar("cir mil", "Circular mils", 1 / 5.067E-10));
		unitTypeArray.add(unitsOfArea);


		UnitType unitsOfVolume = new UnitType(tabNames.get(nameInd++));
		unitsOfVolume.addUnit(new UnitScalar("tbsp", "Tablespoons", 1 / 0.00001478676478125));//exact: gal/256
		unitsOfVolume.addUnit(new UnitScalar("cup", "Cups", 1 / 0.0002365882365));//exact: gal/16
		unitsOfVolume.addUnit(new UnitScalar("pt", "Pints (US)", 1 / 0.000473176473));//exact: gal/8
		unitsOfVolume.addUnit(new UnitScalar("qt", "Quarts (US)", 1 / 0.000946352946));//exact: gal/4
		unitsOfVolume.addUnit(new UnitScalar("gal", "Gallons (US)", 1 / 0.003785411784));//exact: according to wiki

		unitsOfVolume.addUnit(new UnitScalar("tsp", "Teaspoons", 1 / 0.00000492892159375));//exact: gal/768
		unitsOfVolume.addUnit(new UnitScalar("fl oz", "Fluid ounces (US)", 1 / 0.0000295735295625));//exact: gal/128
		unitsOfVolume.addUnit(new UnitScalar("mL", "Milliliters", 1E6));
		unitsOfVolume.addUnit(new UnitScalar("L", "Liters", 1000));

		unitsOfVolume.addUnit(new UnitScalar("cL", "Centiliters", 1E5));
		unitsOfVolume.addUnit(new UnitScalar("dL", "Deciliters", 1E4));
		unitsOfVolume.addUnit(new UnitScalar("gal UK", "Gallons (UK)", 1000 / 4.54609));//exact: 4.54609L/gal uk
		unitsOfVolume.addUnit(new UnitScalar("qt UK", "Quarts (UK)", 1000 / 1.1365225));//exact: gal uk/4
		unitsOfVolume.addUnit(new UnitScalar("pt UK", "Pints (UK)", 1000 / 0.56826125));//exact: gal uk/8
		unitsOfVolume.addUnit(new UnitScalar("fl oz UK", "Fluid ounces (UK)", 1000 / 0.0284130625));//exact: gal uk/160
		unitsOfVolume.addUnit(new UnitScalar("shot", "Shots (US)", 1 / 0.00004436029434375));//exact for 1.5 fl oz
		unitsOfVolume.addUnit(new UnitScalar("keg", "Half barrel keg", 1 / 0.058673882652)); //exact
		unitsOfVolume.addUnit(new UnitScalar("m\u00B3", "Cubic meters", 1));
		unitsOfVolume.addUnit(new UnitScalar("in\u00B3", "Cubic inches", 1 / 0.000016387064));//exact: gal/231
		unitsOfVolume.addUnit(new UnitScalar("ft\u00B3", "Cubic feet", 1 / 0.028316846592));//exact: gal/231*12^3
		unitsOfVolume.addUnit(new UnitScalar("yd\u00B3", "Cubic yards", 1 / 0.764554857984));//exact: 3^3 ft^3
		unitsOfVolume.addUnit(new UnitScalar("cm\u00B3", "Cubic centimeters", 1E6));


		unitTypeArray.add(unitsOfVolume);


		UnitType unitsOfSpeed = new UnitType(tabNames.get(nameInd++));
		unitsOfSpeed.addUnit(new UnitScalar("min/mi", "Minute miles", 1 / 26.8224, true));
		unitsOfSpeed.addUnit(new UnitScalar("mi/min", "Miles per minute", 1 / 26.8224));
		unitsOfSpeed.addUnit(new UnitScalar("ft/s", "Feet per second", 1 / 0.3048));
		unitsOfSpeed.addUnit(new UnitScalar("mph", "Miles per hour", 1 / 0.44704));
		unitsOfSpeed.addUnit(new UnitScalar("knot", "Knots", 1 / 0.514444));

		unitsOfSpeed.addUnit(new UnitScalar("min/km", "Minutes per kilometer", 60 / 1000.0, true));
		unitsOfSpeed.addUnit(new UnitScalar("km/min", "Kilometers per minute", 60 / 1000.0));
		unitsOfSpeed.addUnit(new UnitScalar("m/s", "Meters per second", 1));
		unitsOfSpeed.addUnit(new UnitScalar("kph", "Kilometers per hour", 3600 / 1000.0));

		unitsOfSpeed.addUnit(new UnitScalar("km/s", "Kilometers per second", 1 / 1000.0));
		unitsOfSpeed.addUnit(new UnitScalar("mi/s", "Miles per second", 1 / 0.44704 / 3600));
		unitsOfSpeed.addUnit(new UnitScalar("c sound", "Speed of sound (sea level)", 1 / 340.3));
		unitsOfSpeed.addUnit(new UnitScalar("c light", "Speed of light (vacuum)", 1 / 299792458.0));
		unitTypeArray.add(unitsOfSpeed);


		UnitType unitsOfTime = new UnitType(tabNames.get(nameInd++));
		unitsOfTime.addUnit(new UnitScalar("sec", "Seconds", 1));
		unitsOfTime.addUnit(new UnitScalar("min", "Minutes", 1 / 60.0));
		unitsOfTime.addUnit(new UnitScalar("hour", "Hours", 1 / 3600.0));
		unitsOfTime.addUnit(new UnitScalar("day", "Days", 1 / 86400.0));
		unitsOfTime.addUnit(new UnitScalar("week", "Weeks", 1 / 604800.0));

		unitsOfTime.addUnit(new UnitScalar("mo", "Months", 1 / 2629743.84)); //Exact - average month -- below divided by 12
		unitsOfTime.addUnit(new UnitScalar("year", "Years", 1 / 31556926.08)); //Exact - Nasa says 365.2422 days per year avg
		unitsOfTime.addUnit(new UnitScalar("ms", "Milliseconds", 1000));
		unitsOfTime.addUnit(new UnitScalar("\u00B5s", "Microseconds", 1E6));
		unitsOfTime.addUnit(new UnitScalar("ns", "Nanoseconds", 1E9));

		unitTypeArray.add(unitsOfTime);


		UnitType unitsOfFuel = new UnitType(tabNames.get(nameInd++));
		unitsOfFuel.addUnit(new UnitScalar());
		unitsOfFuel.addUnit(new UnitScalar());
		unitsOfFuel.addUnit(new UnitScalar());
		unitsOfFuel.addUnit(new UnitScalar("mpg US", "Miles per gallon (US)", 3.785411784 / 1.609344)); //exact
		unitsOfFuel.addUnit(new UnitScalar("mpg UK", "Miles per gallon (UK)", 4.54609 / 1.609344)); //exact

		unitsOfFuel.addUnit(new UnitScalar());
		unitsOfFuel.addUnit(new UnitScalar());
		unitsOfFuel.addUnit(new UnitScalar("L/100km", "Liter per 100 Kilometers", .01, true));
		unitsOfFuel.addUnit(new UnitScalar("km/L", "Meters per liter", 1));
		unitsOfFuel.addUnit(new UnitScalar("mi/L", "Miles per liter", 1 / 1.609344)); //exact
		unitTypeArray.add(unitsOfFuel);


		UnitType unitsOfPower = new UnitType(tabNames.get(nameInd++));
		unitsOfPower.addUnit(new UnitScalar("MW", "Megawatts", 1E-6));
		unitsOfPower.addUnit(new UnitScalar("kW", "Kilowatts", 1E-3));
		unitsOfPower.addUnit(new UnitScalar("W", "Watts", 1));
		unitsOfPower.addUnit(new UnitScalar("hp", "Imperial horsepower", 0.00134102208959503)); //exact, see below
		unitsOfPower.addUnit(new UnitScalar("PS", "Metric horsepower", 1 / 735.49875)); //exact from wiki?

		unitsOfPower.addUnit(new UnitScalar());
		unitsOfPower.addUnit(new UnitScalar("Btu/hr", "Btus/Hour", 3.412141632)); //approx
		unitsOfPower.addUnit(new UnitScalar("Btu/min", "Btus/Minute", 0.0568690272)); //approx
		unitsOfPower.addUnit(new UnitScalar("ft-lb/min", "Foot-Pounds/Minute", 0.737562149277265 * 60)); //exact, see below
		unitsOfPower.addUnit(new UnitScalar("ft-lb/sec", "Foot-Pounds/Second", 0.737562149277265)); //exact, see below
		unitTypeArray.add(unitsOfPower);


		UnitType unitsOfEnergy = new UnitType(tabNames.get(nameInd++));
		unitsOfEnergy.addUnit(new UnitScalar("cal", "Calories", 0.239005736)); //approx
		unitsOfEnergy.addUnit(new UnitScalar("kCal", "Kilocalories", 0.239005736 / 1E3)); //approx, but exact comp to cal
		unitsOfEnergy.addUnit(new UnitScalar("BTU", "British thermal units", 0.00094781712)); //approx
		unitsOfEnergy.addUnit(new UnitScalar("ft-lb", "Foot-pounds", 0.737562149277265)); //exact - assumes g=9.80665m^2/s
		unitsOfEnergy.addUnit(new UnitScalar("in-lb", "Inch-pounds", 12 / 0.737562149277265)); //exact from ft-lb

		unitsOfEnergy.addUnit(new UnitScalar("kJ", "Kilojoules", 0.001));
		unitsOfEnergy.addUnit(new UnitScalar("J", "Joules", 1));
		unitsOfEnergy.addUnit(new UnitScalar("Wh", "Watt-Hours", 1 / 3.6E3)); //exact
		unitsOfEnergy.addUnit(new UnitScalar("kWh", "Kilowatt-Hours", 1 / 3.6E6)); //exact

		unitsOfEnergy.addUnit(new UnitScalar("Nm", "Newton-Meters", 1));
		unitsOfEnergy.addUnit(new UnitScalar("MJ", "Megajoules", 1E-6));
		unitsOfEnergy.addUnit(new UnitScalar("eV", "Electronvolts", 6.241509E18));
		unitsOfEnergy.addUnit(new UnitScalar("Ha", "Hartrees", 2.29371044869059200E17));
		unitTypeArray.add(unitsOfEnergy);

		UnitType unitsOfForce = new UnitType(tabNames.get(nameInd++));
		unitsOfForce.addUnit(new UnitScalar());
		unitsOfForce.addUnit(new UnitScalar());
		unitsOfForce.addUnit(new UnitScalar("dyn", "Dynes", 1E5));
		unitsOfForce.addUnit(new UnitScalar("kgf", "Kilogram-Force", 1 / 9.80665)); //exact
		unitsOfForce.addUnit(new UnitScalar("N", "Newtons", 1));

		unitsOfForce.addUnit(new UnitScalar());
		unitsOfForce.addUnit(new UnitScalar());
		unitsOfForce.addUnit(new UnitScalar("pdl", "Poundals", 1 / 0.138254954376)); //exact
		unitsOfForce.addUnit(new UnitScalar("lbf", "Pound-Force", 1 / 4.4482216152605)); //exact
		unitsOfForce.addUnit(new UnitScalar("ozf", "Ounce-Force", 16 / 4.4482216152605));  //exact
		unitTypeArray.add(unitsOfForce);


		UnitType unitsOfTorque = new UnitType(tabNames.get(nameInd++));
		unitsOfTorque.addUnit(new UnitScalar("Nm", "Newton meters", 1));
		unitsOfTorque.addUnit(new UnitScalar("Ncm", "Newton centimeters", 100));
		unitsOfTorque.addUnit(new UnitScalar("kgf m", "Kilogram-Force Meters", 1 / 9.80665)); //exact
		unitsOfTorque.addUnit(new UnitScalar("kgf cm", "Kilogram-Force Centimeters", 100 / 9.80665)); //exact
		unitsOfTorque.addUnit(new UnitScalar("dyn m", "Dyne meters", 1E5));

		unitsOfTorque.addUnit(new UnitScalar("lbf in", "Pound-Force Inches", 12 / 1.3558179483314004));  //exact
		unitsOfTorque.addUnit(new UnitScalar("lbf ft", "Pound-Force Feet", 1 / 1.3558179483314004));  //exact
		unitsOfTorque.addUnit(new UnitScalar("ozf in", "Ounce-Force Inches", 192 / 1.3558179483314004));  //exact
		unitsOfTorque.addUnit(new UnitScalar("ozf ft", "Ounce-Force Feet", 16 / 1.3558179483314004));  //exact
		unitsOfTorque.addUnit(new UnitScalar("dyn cm", "Dyne centimeters", 1));
		unitTypeArray.add(unitsOfTorque);

		UnitType unitsOfPressure = new UnitType(tabNames.get(nameInd++));
		unitsOfPressure.addUnit(new UnitScalar("N/m\u00B2", "Newton/Square Meter", 1));
		unitsOfPressure.addUnit(new UnitScalar("lb/ft\u00B2", "Pounds/Square Foot", 144 / 6894.757293168));  //approx
		unitsOfPressure.addUnit(new UnitScalar("psi", "Pounds/Square Inch", 1 / 6894.757293168)); //approx
		unitsOfPressure.addUnit(new UnitScalar("atm", "Atmospheres", 1 / 101325.0));  //exact
		unitsOfPressure.addUnit(new UnitScalar("bar", "Bars", 0.00001));  //exact

		unitsOfPressure.addUnit(new UnitScalar("kg/m\u00B2", "Kilogram/Square Meter", 1 / 9.80665)); //approx?
		unitsOfPressure.addUnit(new UnitScalar("kPa", "Kilopascals", 0.001)); //exact
		unitsOfPressure.addUnit(new UnitScalar("Pa", "Pascals", 1)); //exact
		unitsOfPressure.addUnit(new UnitScalar("inHg", "Inches of mercury", 1 / 3386.388640341)); //exact using cmHg - 1/(1333.22387415*2.54)

		unitsOfPressure.addUnit(new UnitScalar("cmHg", "Centimeters of mercury", 1 / 1333.22387415)); //exact
		unitsOfPressure.addUnit(new UnitScalar("cmH\u2082O", "Centimeters of water", 1 / 98.0665)); //exact
		unitsOfPressure.addUnit(new UnitScalar("mmHg", "Millimeters of mercury", 1 / 133.322387415)); //exact
		unitsOfPressure.addUnit(new UnitScalar("N/cm\u00B2", "Newton/Square Centimeters", 1E-4));
		unitsOfPressure.addUnit(new UnitScalar("N/mm\u00B2", "Newton/Square Millimeters", 1E-6));
		unitsOfPressure.addUnit(new UnitScalar("kg/cm\u00B2", "Kilogram/Square Centimeter", 1 / 98066.5)); //approx?
		unitsOfPressure.addUnit(new UnitScalar("Torr", "Torr", 760 / 101325.0)); //exact
		unitsOfPressure.addUnit(new UnitScalar("mTorr", "Millitorr", 760 / 101.325)); //exact
		unitTypeArray.add(unitsOfPressure);

		UnitType unitsOfDigital = new UnitType(tabNames.get(nameInd++));
		unitsOfDigital.addUnit(new UnitScalar("byte", "Bytes", 1. / 8));
		unitsOfDigital.addUnit(new UnitScalar("kB", "Kilobytes", 1. / (8. * Math.pow(2, 10))));  //approx
		unitsOfDigital.addUnit(new UnitScalar("MB", "Megabytes", 1. / (8. * Math.pow(2, 20))));  //exact
		unitsOfDigital.addUnit(new UnitScalar("GB", "Gigabytes", 1. / (8. * Math.pow(2, 30))));  //exact
		unitsOfDigital.addUnit(new UnitScalar("TB", "Terabytes", 1. / (8. * Math.pow(2, 40))));  //exact

		unitsOfDigital.addUnit(new UnitScalar("bit", "Bits", 1));
		unitsOfDigital.addUnit(new UnitScalar("kbit", "Kilobits", 1. / Math.pow(2, 10)));  //approx
		unitsOfDigital.addUnit(new UnitScalar("Mbit", "Megabits", 1. / Math.pow(2, 20)));  //exact
		unitsOfDigital.addUnit(new UnitScalar("Gbit", "Gigabits", 1. / Math.pow(2, 30)));  //exact


		unitsOfDigital.addUnit(new UnitScalar("nibble", "Nibbles", 1. / 4.));  //exact
		unitsOfDigital.addUnit(new UnitScalar("Tbit", "Terabits", 1. / Math.pow(2, 40)));  //exact
		unitsOfDigital.addUnit(new UnitScalar("PB", "Petabytes", 1. / (8. * Math.pow(2, 50))));  //approx
		unitsOfDigital.addUnit(new UnitScalar("EB", "Exabytes", 1. / (8. * Math.pow(2, 60))));  //approx
		unitsOfDigital.addUnit(new UnitScalar("ZB", "Zettabytes", 1. / (8. * Math.pow(2, 70))));  //exact
		unitsOfDigital.addUnit(new UnitScalar("YB", "Yottabytes", 1. / (8. * Math.pow(2, 80))));  //exact
		unitsOfDigital.addUnit(new UnitScalar("Pbit", "Petabits", 1. / Math.pow(2, 50)));  //approx
		unitsOfDigital.addUnit(new UnitScalar("Ebit", "Exabits", 1. / Math.pow(2, 60)));  //exact
		unitsOfDigital.addUnit(new UnitScalar("Zbit", "Zettabits", 1. / Math.pow(2, 70)));  //exact
		unitsOfDigital.addUnit(new UnitScalar("Ybit", "Yottabits", 1. / Math.pow(2, 80)));  //exact
		unitTypeArray.add(unitsOfDigital);

		return unitTypeArray;
	}


	/**
	 * Helper method for getDefaultUnitArray, just keeps the file a little
	 * more organized
	 *
	 * @return UnitArray of default unitCurrency
	 */
	private static UnitType getCurrUnitType(String name) {

		// array of values from 1914 $10 bill; starts with 1913; uses the CPI index
		// data can be found: http://data.bls.gov/timeseries/CUUR0000SA0
		// to update, simply add in average CPI for the year to the end of the
		// array. Code will increment years according to array size
		double[] cpiTable = {9.88, 10.02, 10.11, 10.88, 12.83, 15.04, 17.33, 20.04,
				  17.85, 16.75, 17.05, 17.13, 17.54, 17.70, 17.36, 17.16, 17.16, 16.70,
				  15.21, 13.64, 12.93, 13.38, 13.73, 13.87, 14.38, 14.09, 13.91, 14.01,
				  14.73, 16.33, 17.31, 17.59, 17.99, 19.52, 22.33, 24.04, 23.81, 24.07,
				  25.96, 26.55, 26.77, 26.85, 26.78, 27.18, 28.09, 28.86, 29.15, 29.58,
				  29.89, 30.25, 30.63, 31.02, 31.51, 32.46, 33.36, 34.78, 36.68, 38.83,
				  40.49, 41.82, 44.40, 49.31, 53.82, 56.91, 60.61, 65.23, 72.58, 82.41,
				  90.93, 96.50, 99.60, 103.88, 107.57, 109.61, 113.63, 118.26, 123.97,
				  130.66, 136.19, 140.32, 144.46, 148.23, 152.38, 156.85, 160.52, 163.01,
				  166.58, 172.20, 177.07, 179.88, 183.96, 188.88, 195.29, 201.59, 207.34,
				  215.30, 214.54, 218.06, 224.94, 229.59, 232.96, 236.74, 237.02, 240.01,
				  245.12, 250.09};

		ArrayList<Double> al = new ArrayList<>();
		for (double val : cpiTable) {
			//convert values such that 1 is current 2014 dollar
			double normalizedValue = val / cpiTable[cpiTable.length - 1];
			al.add(normalizedValue);
		}

		// the time that the below cryptocurrency prices were last updated
		GregorianCalendar cryTime = new GregorianCalendar(2018, 9, 12, 2, 30);

		UnitType uc = new UnitType(name);
		uc.addUnit(new UnitCurrency("USD", "Dollars", 1));
		uc.addUnit(new UnitCurrency("EUR", "Euros", 0.8954807734551));
		uc.addUnit(new UnitCurrency("CAD", "Canadian Dollars", 1.3418667651443));
		uc.addUnit(new UnitCurrency("GBP", "British Pounds", 0.78938188670712));
		uc.addUnit(new UnitCurrency("BTC", "Bitcoins", 1 / 3197.67, cryTime));

		uc.addUnit(new UnitHistCurrency("USD", "Dollars", al, 1913, 1975));
		uc.addUnit(new UnitCurrency("CHF", "Swiss Francs", 1.0091002473378));
		uc.addUnit(new UnitCurrency("JPY", "Japanese Yen", 110.39322146201));
		uc.addUnit(new UnitCurrency("HKD", "Hong Kong Dollars", 6.3929));


		uc.addUnit(new UnitCurrency("AUD", "Australian Dollars", 1.4532825691398));
		uc.addUnit(new UnitCurrency("CNY", "Chinese Yuans", 6.9083926614883));
		uc.addUnit(new UnitCurrency("RUB", "Russian Rubles", 64.355880022623));
		uc.addUnit(new UnitCurrency("ETH", "Ethereum", 1 / 259.493, cryTime));


		uc.addUnit(new UnitCurrency("AFN", "Afghan Afghani", 80.473422099979));
		uc.addUnit(new UnitCurrency("ALL", "Albanian Lek", 109.7283782248));
		uc.addUnit(new UnitCurrency("DZD", "Algerian Dinar", 119.68218613174));
		uc.addUnit(new UnitCurrency("AOA", "Angolan Kwanza", 327.28813559322));
		uc.addUnit(new UnitCurrency("ARS", "Argentine Peso", 44.81720370108));
		uc.addUnit(new UnitCurrency("AMD", "Armenian Dram", 479.41173770965));
		uc.addUnit(new UnitCurrency("AWG", "Aruban Florin", 1.8088145754297));
		uc.addUnit(new UnitCurrency("AZN", "Azerbaijani Manat", 1.6991023493749));
		uc.addUnit(new UnitCurrency("BSD", "Bahamian Dollars", 1));
		uc.addUnit(new UnitCurrency("BHD", "Bahraini Dinar", 0.37738985278396));
		uc.addUnit(new UnitCurrency("BDT", "Bangladeshi Taka", 84.500927967698));
		uc.addUnit(new UnitCurrency("BBD", "Barbados Dollars", 1.9999050876587));
		//uc.addUnit(new UnitCurrency("BYR", "Belarusian Ruble", 0));
		uc.addUnit(new UnitCurrency("BZD", "Belize Dollars", 2.0143959941581));
		//uc.addUnit(new UnitCurrency("BMD", "Bermudian Dollars", 0));
		//uc.addUnit(new UnitCurrency("BTN", "Bhutanese Ngultrum", 0));
		uc.addUnit(new UnitCurrency("BOB", "Bolivian Boliviano", 6.8955419956927));
		uc.addUnit(new UnitCurrency("BAM", "Bosnia-Herzegovina Convertible Mark", 1.7526662128432));
		uc.addUnit(new UnitCurrency("BWP", "Botswana Pula", 10.775800660244));
		uc.addUnit(new UnitCurrency("BRL", "Brazilian Real", 4.0331342936427));
		uc.addUnit(new UnitCurrency("BND", "Brunei Dollars", 1.3787062868936));
		uc.addUnit(new UnitCurrency("BGN", "Bulgarian Lev", 1.7508833630221));
		uc.addUnit(new UnitCurrency("BIF", "Burundian Francs", 1832.0683111954));
		uc.addUnit(new UnitCurrency("KHR", "Cambodian Riel", 4073.8396624472));
		uc.addUnit(new UnitCurrency("CVE", "Cape Verde Escudo", 99.280205655527));
		//uc.addUnit(new UnitCurrency("KYD", "Cayman Islands Dollars", 0));
		uc.addUnit(new UnitCurrency("XAF", "Central African CFA", 589.14306156733));
		uc.addUnit(new UnitCurrency("XPF", "CFP Francs", 106.93025445081));
		uc.addUnit(new UnitCurrency("CLP", "Chilean Peso", 697.20474254312));
		//uc.addUnit(new UnitCurrency("CLF", "Chilean Unidad de Fomento", 0));
		//uc.addUnit(new UnitCurrency("CNH", "Chinese Offshore Yuans", 0));
		uc.addUnit(new UnitCurrency("COP", "Colombian Peso", 3344.2454906206));
		uc.addUnit(new UnitCurrency("KMF", "Comorian Francs", 440.61608670849));
		uc.addUnit(new UnitCurrency("CDF", "Congolese Francs", 1645.5048998722));
		//uc.addUnit(new UnitCurrency("XCP", "Copper (lb)", 0));
		uc.addUnit(new UnitCurrency("CRC", "Costa Rican Colon", 588.75754559271));
		uc.addUnit(new UnitCurrency("HRK", "Croatian Kuna", 6.6477671757559));
		uc.addUnit(new UnitCurrency("CUP", "Cuban Peso", 1));
		//uc.addUnit(new UnitCurrency("CYP", "Cypriot Pound", 0));
		uc.addUnit(new UnitCurrency("CZK", "Czech Koruna", 23.089359209452));
		uc.addUnit(new UnitCurrency("DKK", "Danish Krone", 6.6855616675777));
		//uc.addUnit(new UnitCurrency("DEM", "Deutsche Mark (obsolete)", 0));
		uc.addUnit(new UnitCurrency("DJF", "Djiboutian Francs", 177.91495830838));
		uc.addUnit(new UnitCurrency("DOP", "Dominican Peso", 50.48366013072));
		uc.addUnit(new UnitCurrency("XCD", "East Caribbean Dollars", 2.7061873729942));
		//uc.addUnit(new UnitCurrency("ECS", "Ecuadorian Sucre (obsolete)", 0));
		uc.addUnit(new UnitCurrency("EGP", "Egyptian Pound", 16.952152720178));
		uc.addUnit(new UnitCurrency("SVC", "El Salvador Colon (obsolete)", 8.7494336202991));
		uc.addUnit(new UnitCurrency("ERN", "Eritrean Nakfa", 15.068279360125));
		uc.addUnit(new UnitCurrency("ETB", "Ethiopian Birr", 28.842419716206));
		//uc.addUnit(new UnitCurrency("FKP", "Falkland Islands Pound", 0));
		uc.addUnit(new UnitCurrency("FJD", "Fiji Dollars", 2.1634642316957));
		//uc.addUnit(new UnitCurrency("FRF", "French Franc (obsolete)", 0));
		uc.addUnit(new UnitCurrency("GMD", "Gambian Dalasi", 50.417754569189));
		uc.addUnit(new UnitCurrency("GEL", "Georgian Lari", 2.749145785877));
		uc.addUnit(new UnitCurrency("GHS", "Ghanaian Cedi", 5.161721464849));
		uc.addUnit(new UnitCurrency("GIP", "Gibraltar Pound", 0.78967815809919));
		//uc.addUnit(new UnitCurrency("XAU", "Gold (oz)", 0));
		uc.addUnit(new UnitCurrency("GTQ", "Guatemalan Quetzal", 7.6703078450843));
		uc.addUnit(new UnitCurrency("GNF", "Guinean Francs", 9130.0236406618));
		uc.addUnit(new UnitCurrency("GYD", "Guyanese Dollars", 209.74311627654));
		uc.addUnit(new UnitCurrency("HTG", "Haitian Gourde", 89.605568445476));
		uc.addUnit(new UnitCurrency("HNL", "Honduran Lempira", 24.443037974683));
		uc.addUnit(new UnitCurrency("HUF", "Hungarian Forint", 292.34140864397));
		uc.addUnit(new UnitCurrency("ISK", "Icelandic Krona", 123.94194075444));
		uc.addUnit(new UnitCurrency("INR", "Indian Rupee", 69.728203257801));
		uc.addUnit(new UnitCurrency("IDR", "Indonesian Rupiah", 14560.060685514));
		uc.addUnit(new UnitCurrency("IRR", "Iranian Rial", 41997.419523911));
		uc.addUnit(new UnitCurrency("IQD", "Iraqi Dinar", 1192.3433158382));
		//uc.addUnit(new UnitCurrency("IEP", "Irish Pound", 0));
		uc.addUnit(new UnitCurrency("ILS", "Israeli New Shekel", 3.6113906081893));
		//uc.addUnit(new UnitCurrency("ITL", "Italian Lira (obsolete)", 0));
		uc.addUnit(new UnitCurrency("JMD", "Jamaican Dollars", 134.56445993031));
		uc.addUnit(new UnitCurrency("JOD", "Jordanian Dinar", 0.7087860698337));
		uc.addUnit(new UnitCurrency("KZT", "Kazakhstani Tenge", 379.02161705336));
		uc.addUnit(new UnitCurrency("KES", "Kenyan Shilling", 101.23728635839));
		uc.addUnit(new UnitCurrency("KWD", "Kuwaiti Dinar", 0.30405262313668));
		uc.addUnit(new UnitCurrency("KGS", "Kyrgyzstanian Som", 69.755580497636));
		uc.addUnit(new UnitCurrency("LAK", "Lao Kip", 8678.6516853934));
		//uc.addUnit(new UnitCurrency("LVL", "Latvian Lats (obsolete)", 0));
		uc.addUnit(new UnitCurrency("LBP", "Lebanese Pound", 1511.2123246731));
		uc.addUnit(new UnitCurrency("LSL", "Lesotho Loti", 14.383612662942));
		uc.addUnit(new UnitCurrency("LRD", "Liberian Dollars", 182.16981132076));
		uc.addUnit(new UnitCurrency("LYD", "Libyan Dinar", 1.4012045569987));
		//uc.addUnit(new UnitCurrency("LTL", "Lithuanian Litas", 0));
		uc.addUnit(new UnitCurrency("MOP", "Macau Pataca", 8.0845719070548));
		uc.addUnit(new UnitCurrency("MKD", "Macedonian Denar", 55.213232876322));
		uc.addUnit(new UnitCurrency("MGA", "Malagasy Ariary", 3681.6015252622));
		uc.addUnit(new UnitCurrency("MWK", "Malawian Kwacha", 740.84020717439));
		uc.addUnit(new UnitCurrency("MYR", "Malaysian Ringgit", 4.1850485475111));
		uc.addUnit(new UnitCurrency("MVR", "Maldivian Rufiyaa", 15.448));
		//uc.addUnit(new UnitCurrency("MRO", "Mauritanian Ouguiya", 0));
		uc.addUnit(new UnitCurrency("MUR", "Mauritian Rupee", 35.249318739213));
		uc.addUnit(new UnitCurrency("MXN", "Mexican Peso", 18.987862638036));
		//uc.addUnit(new UnitCurrency("MXV", "Mexican Unidad de Inversion", 0));
		uc.addUnit(new UnitCurrency("MDL", "Moldovan Leu", 17.975545035487));
		uc.addUnit(new UnitCurrency("MNT", "Mongolian Tugrik", 2643.3949349761));
		uc.addUnit(new UnitCurrency("MAD", "Moroccan Dirham", 9.6838515546635));
		uc.addUnit(new UnitCurrency("MZN", "Mozambican Metical", 63.415435139571));
		uc.addUnit(new UnitCurrency("MMK", "Myanmar Kyat", 1531.3243457573));
		uc.addUnit(new UnitCurrency("NAD", "Namibian Dollars", 14.378257632167));
		uc.addUnit(new UnitCurrency("NPR", "Nepalese Rupee", 111.50570232424));
		uc.addUnit(new UnitCurrency("ANG", "Netherlands Antillean Guilder", 1.8738476467734));
		uc.addUnit(new UnitCurrency("TWD", "New Taiwan Dollars", 31.471191528794));
		uc.addUnit(new UnitCurrency("NZD", "New Zealand Dollars", 1.5380528471442));
		uc.addUnit(new UnitCurrency("NIO", "Nicaraguan Cordoba Oro", 32.909042553191));
		uc.addUnit(new UnitCurrency("NGN", "Nigerian Naira", 306.58787830646));
		//uc.addUnit(new UnitCurrency("KPW", "North Korean Won", 0));
		uc.addUnit(new UnitCurrency("NOK", "Norwegian Krone", 8.7416552208258));
		uc.addUnit(new UnitCurrency("OMR", "Omani Rial", 0.38472266496653));
		uc.addUnit(new UnitCurrency("PKR", "Pakistan Rupee", 152.10077286578));
		//uc.addUnit(new UnitCurrency("XPD", "Palladium (oz)", 0));
		uc.addUnit(new UnitCurrency("PAB", "Panamanian Balboa", 1));
		uc.addUnit(new UnitCurrency("PGK", "Papua New Guinea Kina", 3.3830987051548));
		uc.addUnit(new UnitCurrency("PYG", "Paraguay Guarani", 6331.1546840958));
		uc.addUnit(new UnitCurrency("PEN", "Peruvian Nuevo Sol", 3.345763889373));
		uc.addUnit(new UnitCurrency("PHP", "Philippine Peso", 52.466845073656));
		//uc.addUnit(new UnitCurrency("XPT", "Platinum (oz)", 0));
		uc.addUnit(new UnitCurrency("PLN", "Polish Zloty", 3.8599309486948));
		uc.addUnit(new UnitCurrency("QAR", "Qatari Riyal", 3.660663507109));
		uc.addUnit(new UnitCurrency("RON", "Romanian Leu", 4.2635265042429));
		uc.addUnit(new UnitCurrency("RWF", "Rwandan Francs", 906.36000938746));
		uc.addUnit(new UnitCurrency("WST", "Samoan Tala", 2.6479259513198));
		//uc.addUnit(new UnitCurrency("STD", "Sao Tome Dobra", 0));
		uc.addUnit(new UnitCurrency("SAR", "Saudi Riyal", 3.750007631652));
		uc.addUnit(new UnitCurrency("RSD", "Serbian Dinar", 105.67391156924));
		uc.addUnit(new UnitCurrency("SCR", "Seychelles Rupee", 13.651466949452));
		uc.addUnit(new UnitCurrency("SLL", "Sierra Leonean Leone", 9002.3310023309));
		//uc.addUnit(new UnitCurrency("XAG", "Silver (oz)", 0));
		uc.addUnit(new UnitCurrency("SGD", "Singapore Dollars", 1.3788077451144));
		//uc.addUnit(new UnitCurrency("SIT", "Slovenian Tolar (obsolete)", 0));
		uc.addUnit(new UnitCurrency("SBD", "Solomon Islands Dollars", 8.0157741801577));
		uc.addUnit(new UnitCurrency("SOS", "Somali Shilling", 578.14371257487));
		uc.addUnit(new UnitCurrency("ZAR", "South African Rand", 14.37352800168));
		uc.addUnit(new UnitCurrency("KRW", "South-Korean Won", 1191.9566099885));
		//uc.addUnit(new UnitCurrency("XDR", "Special Drawing Rights", 0));
		uc.addUnit(new UnitCurrency("LKR", "Sri Lankan Rupee", 176.54050100567));
		//uc.addUnit(new UnitCurrency("SHP", "St Helena Pounds", 0));
		uc.addUnit(new UnitCurrency("SDG", "Sudanese Pounds", 45.064177362893));
		uc.addUnit(new UnitCurrency("SRD", "Suriname Dollars", 7.4700193423599));
		uc.addUnit(new UnitCurrency("SZL", "Swazi Lilangeni", 14.367559523809));
		uc.addUnit(new UnitCurrency("SEK", "Swedish Krona", 9.6425180023162));
		uc.addUnit(new UnitCurrency("SYP", "Syrian Pounds", 514.93333333332));
		uc.addUnit(new UnitCurrency("TJS", "Tajikistan Somoni", 9.4330085097736));
		uc.addUnit(new UnitCurrency("TZS", "Tanzanian Shilling", 2298.8095238095));
		uc.addUnit(new UnitCurrency("THB", "Thai Baht", 31.957816337837));
		uc.addUnit(new UnitCurrency("TOP", "Tonga Pa'anga", 2.2760490334748));
		uc.addUnit(new UnitCurrency("TTD", "Trinidad/Tobago Dollars", 6.7706872370266));
		uc.addUnit(new UnitCurrency("TND", "Tunisian Dinar", 2.992406632574));
		uc.addUnit(new UnitCurrency("TRY", "Turkish Lira", 6.10173061166));
		uc.addUnit(new UnitCurrency("TMT", "Turkmenistan Manat", 3.4926410928028));
		uc.addUnit(new UnitCurrency("UGX", "Uganda Shilling", 3764.1325536062));
		uc.addUnit(new UnitCurrency("UAH", "Ukrainian Hryvnia", 26.313646612543));
		uc.addUnit(new UnitCurrency("AED", "United Arab Emirates Dirham", 3.6724970529716));
		uc.addUnit(new UnitCurrency("UYU", "Uruguayan Peso", 35.286000026318));
		uc.addUnit(new UnitCurrency("UZS", "Uzbekistani Som", 8454.5870495206));
		uc.addUnit(new UnitCurrency("VUV", "Vanuatu Vatu", 117.20433370763));
		//uc.addUnit(new UnitCurrency("VEF", "Venezuelan Bolivar", 0));
		uc.addUnit(new UnitCurrency("VND", "Vietnamese Dong", 23512.643621727));
		uc.addUnit(new UnitCurrency("XOF", "West African CFA", 587.88782511163));
		uc.addUnit(new UnitCurrency("YER", "Yemeni Rial", 249.54768674076));
		uc.addUnit(new UnitCurrency("ZMW", "Zambian Kwacha", 13.617771509168));
		//uc.addUnit(new UnitCurrency("ZWL", "Zimbabwean Dollars (obsolete)", 0));

		uc.addUnit(new UnitCurrency("XRP", "Ripple", 1 / 0.177605, cryTime));
		uc.addUnit(new UnitCurrency("BCH", "Bitcoin Cash", 1 / 263.36, cryTime));
		uc.addUnit(new UnitCurrency("LTC", "Litecoin", 1 / 45.246, cryTime));
		uc.addUnit(new UnitCurrency("XEM", "NEM", 1 / 0.252438, cryTime));
		uc.addUnit(new UnitCurrency("ETC", "Ethereum Classic", 1 / 15.2, cryTime));
		uc.addUnit(new UnitCurrency("DASH", "Dash", 1 / 187.539, cryTime));
		uc.addUnit(new UnitCurrency("MIOTA", "IOTA", 1 / 0.425763, cryTime));
		uc.addUnit(new UnitCurrency("ANS", "NEO", 1 / 14.4828, cryTime));
		uc.addUnit(new UnitCurrency("XMR", "Monero", 1 / 47.4512, cryTime));
		uc.addUnit(new UnitCurrency("STRAT", "Stratis", 1 / 6.57858, cryTime));
		uc.addUnit(new UnitCurrency("QTUM", "Qtum", 1 / 9.9334, cryTime));
		uc.addUnit(new UnitCurrency("BCC", "BitConnect", 1 / 78.4569, cryTime));
		uc.addUnit(new UnitCurrency("WAVES", "Waves", 1 / 4.90476, cryTime));
		uc.addUnit(new UnitCurrency("EOS", "EOS", 1 / 1.70987, cryTime));
		uc.addUnit(new UnitCurrency("ZEC", "Zcash", 1 / 206.468, cryTime));
		uc.addUnit(new UnitCurrency("BTS", "BitShares", 1 / 0.148933, cryTime));
		uc.addUnit(new UnitCurrency("USDT", "Tether", 1 / 0.999844, cryTime));
		uc.addUnit(new UnitCurrency("VERI", "Veritaseum", 1 / 157.784, cryTime));
		uc.addUnit(new UnitCurrency("STEEM", "Steem", 1 / 1.3176, cryTime));
		uc.addUnit(new UnitCurrency("OMG", "OmiseGo", 1 / 2.74397, cryTime));
		uc.addUnit(new UnitCurrency("GNT", "Golem", 1 / 0.315259, cryTime));
		uc.addUnit(new UnitCurrency("SC", "Siacoin", 1 / 0.00919203, cryTime));
		uc.addUnit(new UnitCurrency("ICN", "Iconomi", 1 / 2.87782, cryTime));
		uc.addUnit(new UnitCurrency("BCN", "Bytecoin", 1 / 0.00134475, cryTime));
		uc.addUnit(new UnitCurrency("XLM", "Stellar Lumens", 1 / 0.0221617, cryTime));
		uc.addUnit(new UnitCurrency("LSK", "Lisk", 1 / 2.13442, cryTime));
		uc.addUnit(new UnitCurrency("GNO", "Gnosis", 1 / 207.614, cryTime));
		uc.addUnit(new UnitCurrency("SNT", "Status", 1 / 0.0612608, cryTime));
		uc.addUnit(new UnitCurrency("PAY", "TenX", 1 / 2.01811, cryTime));
		uc.addUnit(new UnitCurrency("DOGE", "Dogecoin", 1 / 0.00191011, cryTime));
//		uc.addUnit(new UnitCurrency("REP", "Augur", 1 / 18.6763, cryTime));
//		uc.addUnit(new UnitCurrency("GBYTE", "Byteball", 1 / 545.792, cryTime));
//		uc.addUnit(new UnitCurrency("FCT", "Factom", 1 / 20.0677, cryTime));
//		uc.addUnit(new UnitCurrency("GAME", "GameCredits", 1 / 2.69226, cryTime));
//		uc.addUnit(new UnitCurrency("DCR", "Decred", 1 / 30.2754, cryTime));
//		uc.addUnit(new UnitCurrency("PPT", "Populous", 1 / 4.44577, cryTime));
//		uc.addUnit(new UnitCurrency("MAID", "MaidSafeCoin", 1 / 0.359581, cryTime));
//		uc.addUnit(new UnitCurrency("BAT", "Basic Attention Token", 1 / 0.161562, cryTime));
//		uc.addUnit(new UnitCurrency("DGB", "DigiByte", 1 / 0.018204, cryTime));
//		uc.addUnit(new UnitCurrency("DGD", "DigixDAO", 1 / 69.343, cryTime));
//		uc.addUnit(new UnitCurrency("ARDR", "Ardor", 1 / 0.131982, cryTime));
//		uc.addUnit(new UnitCurrency("NXT", "Nxt", 1 / 0.129459, cryTime));
//		uc.addUnit(new UnitCurrency("MCAP", "MCAP", 1 / 1.98548, cryTime));
//		uc.addUnit(new UnitCurrency("KMD", "Komodo", 1 / 1.03886, cryTime));
//		uc.addUnit(new UnitCurrency("PIVX", "PIVX", 1 / 1.93232, cryTime));
//		uc.addUnit(new UnitCurrency("BNT", "Bancor", 1 / 2.34959, cryTime));
//		uc.addUnit(new UnitCurrency("LKK", "Lykke", 1 / 0.359779, cryTime));
//		uc.addUnit(new UnitCurrency("ARK", "Ark", 1 / 0.945247, cryTime));

		uc.addUnit(new UnitCurrency("SAT", "Satoshi", 1E8 / 3197.67, cryTime, "BTC", 1E8));
		uc.addUnit(new UnitCurrency("mBTC", "Millibitcoin", 1E3 / 3197.67, cryTime, "BTC", 1E3));

		uc.addUnit(new UnitCurrency("GWEI", "Gwei (Ethereum)", 1E9 / 259.493, cryTime, "ETH", 1E9));
		return uc;
	}
}
