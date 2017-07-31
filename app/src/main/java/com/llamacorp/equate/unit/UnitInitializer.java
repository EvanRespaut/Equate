package com.llamacorp.equate.unit;

import java.util.ArrayList;
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
		unitsOfSpeed.addUnit(new UnitScalar("mi/min", "Miles per minute", 1 / 26.8224));
		unitsOfSpeed.addUnit(new UnitScalar("min/mi", "Minute miles", 1 / 26.8224, true));
		unitsOfSpeed.addUnit(new UnitScalar("ft/s", "Feet per second", 1 / 0.3048));
		unitsOfSpeed.addUnit(new UnitScalar("mph", "Miles per hour", 1 / 0.44704));
		unitsOfSpeed.addUnit(new UnitScalar("knot", "Knots", 1 / 0.514444));

		unitsOfSpeed.addUnit(new UnitScalar("m/s", "Meters per second", 1));
		unitsOfSpeed.addUnit(new UnitScalar("km/s", "Kilometers per second", 1 / 1000.0));
		unitsOfSpeed.addUnit(new UnitScalar("km/m", "Kilometers per minute", 60 / 1000.0));
		unitsOfSpeed.addUnit(new UnitScalar("kph", "Kilometers per hour", 3600 / 1000.0));

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

		//array of values from 1914 $10 bill; starts with 1913; uses the CPI index
		//data can be found: http://data.bls.gov/timeseries/CUUR0000SA0
		double[] cpiTable = {9.9, 10, 10.1, 10.9, 12.8, 15.1, 17.3, 20, 17.9, 16.8,
				  17.1, 17.1, 17.5, 17.7, 17.4, 17.1, 17.1, 16.7, 15.2, 13.7, 13, 13.4,
				  13.7, 13.9, 14.4, 14.1, 13.9, 14, 14.7, 16.3, 17.3, 17.6, 18, 19.5,
				  22.3, 24.1, 23.8, 24.1, 26, 26.5, 26.7, 26.9, 26.8, 27.2, 28.1, 28.9,
				  29.1, 29.6, 29.9, 30.2, 30.6, 31, 31.5, 32.4, 33.4, 34.8, 36.7, 38.8,
				  40.5, 41.8, 44.4, 49.3, 53.8, 56.9, 60.6, 65.2, 72.6, 82.4, 90.9, 96.5,
				  99.6, 103.9, 107.6, 109.6, 113.6, 118.3, 124, 130.7, 136.2, 140.3,
				  144.5, 148.2, 152.4, 156.9, 160.5, 163, 166.6, 172.2, 177.1, 179.9,
				  184, 188.9, 195.3, 201.6, 207.342, 215.303, 214.537, 218.056, 224.939,
				  229.594, 232.957, 236.911};

		ArrayList<Double> al = new ArrayList<>();
		for (double val : cpiTable) {
			//convert values such that 1 is current 2014 dollar
			double normalizedValue = val / cpiTable[cpiTable.length - 1];
			al.add(normalizedValue);
		}


		UnitType uc = new UnitType(name,
				  "https://finance.yahoo.com/webservice/v1/symbols/allcurrencies/quote");
		uc.addUnit(new UnitCurrency("USD", "Dollars", 1));
		uc.addUnit(new UnitCurrency("EUR", "Euros", 0.929));
		uc.addUnit(new UnitCurrency("CAD", "Canadian Dollars", 1.26));
		uc.addUnit(new UnitCurrency("GBP", "British Pounds", 0.67));
		uc.addUnit(new UnitCurrency("BTC", "Bitcoins", 0.004,
				  "https://blockchain.info/tobtc?currency=USD&value=1"));

		uc.addUnit(new UnitHistCurrency("USD", "Dollars", al, 1913, 1975));
		uc.addUnit(new UnitCurrency("CHF", "Swiss Francs", 0.967));
		uc.addUnit(new UnitCurrency("JPY", "Japanese Yen", 119.7));
		uc.addUnit(new UnitCurrency("HKD", "Hong Kong Dollars", 7.75));


		uc.addUnit(new UnitCurrency("AUD", "Australian Dollars", 1.390144));
		uc.addUnit(new UnitCurrency("CNY", "Chinese Yuans", 6.198));
		uc.addUnit(new UnitCurrency("RUB", "Russian Rubles", 66.499496));

		uc.addUnit(new UnitCurrency("AFN", "Afghan Afghani", 64.349998));
		uc.addUnit(new UnitCurrency("ALL", "Albanian Lek", 122.059502));
		uc.addUnit(new UnitCurrency("DZD", "Algerian Dinar", 105.470001));
		uc.addUnit(new UnitCurrency("AOA", "Angolan Kwanza", 135.294998));
		uc.addUnit(new UnitCurrency("ARS", "Argentine Peso", 9.3998));
		uc.addUnit(new UnitCurrency("AMD", "Armenian Dram", 481.73999));
		uc.addUnit(new UnitCurrency("AWG", "Aruban Florin", 1.79));
		uc.addUnit(new UnitCurrency("AZN", "Azerbaijani Manat", 1.0568));
		uc.addUnit(new UnitCurrency("BSD", "Bahamian Dollars", 1));
		uc.addUnit(new UnitCurrency("BHD", "Bahraini Dinar", 0.3774));
		uc.addUnit(new UnitCurrency("BDT", "Bangladeshi Taka", 77.822952));
		uc.addUnit(new UnitCurrency("BBD", "Barbados Dollars", 2));
		uc.addUnit(new UnitCurrency("BYR", "Belarusian Ruble", 17495));
		uc.addUnit(new UnitCurrency("BZD", "Belize Dollars", 1.995));
		uc.addUnit(new UnitCurrency("BMD", "Bermudian Dollars", 1));
		uc.addUnit(new UnitCurrency("BTN", "Bhutanese Ngultrum", 65.764999));
		uc.addUnit(new UnitCurrency("BOB", "Bolivian Boliviano", 6.9));
		uc.addUnit(new UnitCurrency("BAM", "Bosnia-Herzegovina Convertible Mark", 1.7315));
		uc.addUnit(new UnitCurrency("BWP", "Botswana Pula", 10.24795));
		uc.addUnit(new UnitCurrency("BRL", "Brazilian Real", 3.9415));
		uc.addUnit(new UnitCurrency("BND", "Brunei Dollars", 1.40215));
		uc.addUnit(new UnitCurrency("BGN", "Bulgarian Lev", 1.7321));
		uc.addUnit(new UnitCurrency("BIF", "Burundian Francs", 1549));
		uc.addUnit(new UnitCurrency("KHR", "Cambodian Riel", 4094.949951));
		uc.addUnit(new UnitCurrency("CVE", "Cape Verde Escudo", 97.0495));
		uc.addUnit(new UnitCurrency("KYD", "Cayman Islands Dollars", 0.82));
		uc.addUnit(new UnitCurrency("XAF", "Central African CFA", 580.724243));
		uc.addUnit(new UnitCurrency("XPF", "CFP Francs", 105.645401));
		uc.addUnit(new UnitCurrency("CLP", "Chilean Peso", 678.554993));
		uc.addUnit(new UnitCurrency("CLF", "Chilean Unidad de Fomento", 0.0246));
		uc.addUnit(new UnitCurrency("CNH", "Chinese Offshore Yuans", 6.38755));
		uc.addUnit(new UnitCurrency("COP", "Colombian Peso", 2984.830078));
		uc.addUnit(new UnitCurrency("KMF", "Comorian Francs", 435.543213));
		uc.addUnit(new UnitCurrency("CDF", "Congolese Francs", 927.5));
		uc.addUnit(new UnitCurrency("XCP", "Copper (lb)", 0.419463));
		uc.addUnit(new UnitCurrency("CRC", "Costa Rican Colon", 532.299988));
		uc.addUnit(new UnitCurrency("HRK", "Croatian Kuna", 6.75905));
		uc.addUnit(new UnitCurrency("CUP", "Cuban Peso", 1));
		uc.addUnit(new UnitCurrency("CYP", "Cypriot Pound", 0.51955));
		uc.addUnit(new UnitCurrency("CZK", "Czech Koruna", 23.975));
		uc.addUnit(new UnitCurrency("DKK", "Danish Krone", 6.60755));
		uc.addUnit(new UnitCurrency("DEM", "Deutsche Mark (obsolete)", 1.71745));
		uc.addUnit(new UnitCurrency("DJF", "Djiboutian Francs", 176.994995));
		uc.addUnit(new UnitCurrency("DOP", "Dominican Peso", 45.209999));
		uc.addUnit(new UnitCurrency("XCD", "East Caribbean Dollars", 2.7));
		uc.addUnit(new UnitCurrency("ECS", "Ecuadorian Sucre (obsolete)", 25000));
		uc.addUnit(new UnitCurrency("EGP", "Egyptian Pound", 7.8305));
		uc.addUnit(new UnitCurrency("SVC", "El Salvador Colon (obsolete)", 8.7325));
		uc.addUnit(new UnitCurrency("ERN", "Eritrean Nakfa", 15.28));
		uc.addUnit(new UnitCurrency("ETB", "Ethiopian Birr", 20.898001));
		uc.addUnit(new UnitCurrency("FKP", "Falkland Islands Pound", 0.6485));
		uc.addUnit(new UnitCurrency("FJD", "Fiji Dollars", 2.15795));
		uc.addUnit(new UnitCurrency("FRF", "French Franc (obsolete)", 5.7601));
		uc.addUnit(new UnitCurrency("GMD", "Gambian Dalasi", 38.93));
		uc.addUnit(new UnitCurrency("GEL", "Georgian Lari", 2.45));
		uc.addUnit(new UnitCurrency("GHS", "Ghanaian Cedi", 3.9));
		uc.addUnit(new UnitCurrency("GIP", "Gibraltar Pound", 0.6425));
		uc.addUnit(new UnitCurrency("XAU", "Gold (oz)", 0.000879));
		uc.addUnit(new UnitCurrency("GTQ", "Guatemalan Quetzal", 7.6545));
		uc.addUnit(new UnitCurrency("GNF", "Guinean Francs", 7249.950195));
		uc.addUnit(new UnitCurrency("GYD", "Guyanese Dollars", 207.210007));
		uc.addUnit(new UnitCurrency("HTG", "Haitian Gourde", 51.737499));
		uc.addUnit(new UnitCurrency("HNL", "Honduran Lempira", 21.949699));
		uc.addUnit(new UnitCurrency("HUF", "Hungarian Forint", 275.369995));
		uc.addUnit(new UnitCurrency("ISK", "Icelandic Krona", 127.324997));
		uc.addUnit(new UnitCurrency("INR", "Indian Rupee", 65.870453));
		uc.addUnit(new UnitCurrency("IDR", "Indonesian Rupiah", 14420.5));
		uc.addUnit(new UnitCurrency("IRR", "Iranian Rial", 29605));
		uc.addUnit(new UnitCurrency("IQD", "Iraqi Dinar", 1188));
		uc.addUnit(new UnitCurrency("IEP", "Irish Pound", 0.699154));
		uc.addUnit(new UnitCurrency("ILS", "Israeli New Shekel", 3.9344));
		uc.addUnit(new UnitCurrency("ITL", "Italian Lira (obsolete)", 1700.272217));
		uc.addUnit(new UnitCurrency("JMD", "Jamaican Dollars", 118.5));
		uc.addUnit(new UnitCurrency("JOD", "Jordanian Dinar", 0.709));
		uc.addUnit(new UnitCurrency("KZT", "Kazakhstani Tenge", 275.904938));
		uc.addUnit(new UnitCurrency("KES", "Kenyan Shilling", 105.149498));
		uc.addUnit(new UnitCurrency("KWD", "Kuwaiti Dinar", 0.3013));
		uc.addUnit(new UnitCurrency("KGS", "Kyrgyzstanian Som", 70.493797));
		uc.addUnit(new UnitCurrency("LAK", "Lao Kip", 8129.950195));
		uc.addUnit(new UnitCurrency("LVL", "Latvian Lats (obsolete)", 0.62055));
		uc.addUnit(new UnitCurrency("LBP", "Lebanese Pound", 1506.5));
		uc.addUnit(new UnitCurrency("LSL", "Lesotho Loti", 13.31525));
		uc.addUnit(new UnitCurrency("LRD", "Liberian Dollars", 84.660004));
		uc.addUnit(new UnitCurrency("LYD", "Libyan Dinar", 1.36495));
		uc.addUnit(new UnitCurrency("LTL", "Lithuanian Litas", 3.0487));
		uc.addUnit(new UnitCurrency("MOP", "Macau Pataca", 7.9826));
		uc.addUnit(new UnitCurrency("MKD", "Macedonian Denar", 53.91));
		uc.addUnit(new UnitCurrency("MGA", "Malagasy Ariary", 3098.600098));
		uc.addUnit(new UnitCurrency("MWK", "Malawian Kwacha", 557.494995));
		uc.addUnit(new UnitCurrency("MYR", "Malaysian Ringgit", 4.2265));
		uc.addUnit(new UnitCurrency("MVR", "Maldivian Rufiyaa", 15.34));
		uc.addUnit(new UnitCurrency("MRO", "Mauritanian Ouguiya", 290));
		uc.addUnit(new UnitCurrency("MUR", "Mauritian Rupee", 35.450001));
		uc.addUnit(new UnitCurrency("MXN", "Mexican Peso", 16.6224));
		uc.addUnit(new UnitCurrency("MXV", "Mexican Unidad de Inversion", 2.81));
		uc.addUnit(new UnitCurrency("MDL", "Moldovan Leu", 19.9));
		uc.addUnit(new UnitCurrency("MNT", "Mongolian Tugrik", 1990.5));
		uc.addUnit(new UnitCurrency("MAD", "Moroccan Dirham", 9.67155));
		uc.addUnit(new UnitCurrency("MZN", "Mozambican Metical", 43.169998));
		uc.addUnit(new UnitCurrency("MMK", "Myanmar Kyat", 1282.400024));
		uc.addUnit(new UnitCurrency("NAD", "Namibian Dollars", 13.31525));
		uc.addUnit(new UnitCurrency("NPR", "Nepalese Rupee", 105.223999));
		uc.addUnit(new UnitCurrency("ANG", "Netherlands Antillean Guilder", 1.79));
		uc.addUnit(new UnitCurrency("TWD", "New Taiwan Dollars", 32.395));
		uc.addUnit(new UnitCurrency("NZD", "New Zealand Dollars", 1.563441));
		uc.addUnit(new UnitCurrency("NIO", "Nicaraguan Cordoba Oro", 27.553801));
		uc.addUnit(new UnitCurrency("NGN", "Nigerian Naira", 199.044998));
		uc.addUnit(new UnitCurrency("KPW", "North Korean Won", 900));
		uc.addUnit(new UnitCurrency("NOK", "Norwegian Krone", 8.16405));
		uc.addUnit(new UnitCurrency("OMR", "Omani Rial", 0.38505));
		uc.addUnit(new UnitCurrency("PKR", "Pakistan Rupee", 104.355003));
		uc.addUnit(new UnitCurrency("XPD", "Palladium (oz)", 0.001638));
		uc.addUnit(new UnitCurrency("PAB", "Panamanian Balboa", 1));
		uc.addUnit(new UnitCurrency("PGK", "Papua New Guinea Kina", 2.83015));
		uc.addUnit(new UnitCurrency("PYG", "Paraguay Guarani", 5474.685059));
		uc.addUnit(new UnitCurrency("PEN", "Peruvian Nuevo Sol", 3.19));
		uc.addUnit(new UnitCurrency("PHP", "Philippine Peso", 46.512501));
		uc.addUnit(new UnitCurrency("XPT", "Platinum (oz)", 0.001016));
		uc.addUnit(new UnitCurrency("PLN", "Polish Zloty", 3.7231));
		uc.addUnit(new UnitCurrency("QAR", "Qatari Riyal", 3.6438));
		uc.addUnit(new UnitCurrency("RON", "Romanian Leu", 3.82225));
		uc.addUnit(new UnitCurrency("RWF", "Rwandan Francs", 732.080017));
		uc.addUnit(new UnitCurrency("WST", "Samoan Tala", 2.658703));
		uc.addUnit(new UnitCurrency("STD", "Sao Tome Dobra", 21495));
		uc.addUnit(new UnitCurrency("SAR", "Saudi Riyal", 3.75005));
		uc.addUnit(new UnitCurrency("RSD", "Serbian Dinar", 106.220001));
		uc.addUnit(new UnitCurrency("SCR", "Seychelles Rupee", 13.09995));
		uc.addUnit(new UnitCurrency("SLL", "Sierra Leonean Leone", 4308));
		uc.addUnit(new UnitCurrency("XAG", "Silver (oz)", 0.065989));
		uc.addUnit(new UnitCurrency("SGD", "Singapore Dollars", 1.363));
		uc.addUnit(new UnitCurrency("SIT", "Slovenian Tolar (obsolete)", 216.486755));
		uc.addUnit(new UnitCurrency("SBD", "Solomon Islands Dollars", 7.971566));
		uc.addUnit(new UnitCurrency("SOS", "Somali Shilling", 639.950012));
		uc.addUnit(new UnitCurrency("ZAR", "South African Rand", 13.3139));
		uc.addUnit(new UnitCurrency("KRW", "South-Korean Won", 1174.295044));
		uc.addUnit(new UnitCurrency("XDR", "Special Drawing Rights", 0.70875));
		uc.addUnit(new UnitCurrency("LKR", "Sri Lankan Rupee", 140.490005));
		uc.addUnit(new UnitCurrency("SHP", "St Helena Pounds", 0.6425));
		uc.addUnit(new UnitCurrency("SDG", "Sudanese Pounds", 6.09));
		uc.addUnit(new UnitCurrency("SRD", "Suriname Dollars", 3.3));
		uc.addUnit(new UnitCurrency("SZL", "Swazi Lilangeni", 13.31525));
		uc.addUnit(new UnitCurrency("SEK", "Swedish Krona", 8.25805));
		uc.addUnit(new UnitCurrency("SYP", "Syrian Pounds", 188.785995));
		uc.addUnit(new UnitCurrency("TJS", "Tajikistan Somoni", 6.4005));
		uc.addUnit(new UnitCurrency("TZS", "Tanzanian Shilling", 2169.199951));
		uc.addUnit(new UnitCurrency("THB", "Thai Baht", 35.647999));
		uc.addUnit(new UnitCurrency("TOP", "Tonga Pa'anga", 2.17491));
		uc.addUnit(new UnitCurrency("TTD", "Trinidad/Tobago Dollars", 6.3382));
		uc.addUnit(new UnitCurrency("TND", "Tunisian Dinar", 1.95155));
		uc.addUnit(new UnitCurrency("TRY", "Turkish Lira", 3.00635));
		uc.addUnit(new UnitCurrency("TMT", "Turkmenistan Manat", 3.5));
		uc.addUnit(new UnitCurrency("UGX", "Uganda Shilling", 3650));
		uc.addUnit(new UnitCurrency("UAH", "Ukrainian Hryvnia", 21.799999));
		uc.addUnit(new UnitCurrency("AED", "United Arab Emirates Dirham", 3.67275));
		uc.addUnit(new UnitCurrency("UYU", "Uruguayan Peso", 28.815001));
		uc.addUnit(new UnitCurrency("UZS", "Uzbekistani Som", 2610.889893));
		uc.addUnit(new UnitCurrency("VUV", "Vanuatu Vatu", 114.485001));
		uc.addUnit(new UnitCurrency("VEF", "Venezuelan Bolivar", 6.35));
		uc.addUnit(new UnitCurrency("VND", "Vietnamese Dong", 22475));
		uc.addUnit(new UnitCurrency("XOF", "West African CFA", 580.724243));
		uc.addUnit(new UnitCurrency("YER", "Yemeni Rial", 214.889999));
		uc.addUnit(new UnitCurrency("ZMW", "Zambian Kwacha", 10.003));
		uc.addUnit(new UnitCurrency("ZWL", "Zimbabwean Dollars (obsolete)", 322.355011));


		return uc;
	}
}
