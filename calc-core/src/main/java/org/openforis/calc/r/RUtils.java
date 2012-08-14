/**
 * 
 */
package org.openforis.calc.r;

import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPDouble;
import org.rosuda.REngine.REXPInteger;
import org.rosuda.REngine.REXPLogical;
import org.rosuda.REngine.REXPNull;
import org.rosuda.REngine.REXPString;
import org.rosuda.REngine.REXPVector;

/**
 * @author Mino Togna
 * 
 */
public class RUtils {
	/**
	 * Convert array to {@link REXPVector}.
	 * 
	 * @param array
	 *            array to convert
	 * @return {@link REXPVector}.
	 */
	public static REXPVector toVector(double... array) {
		return new REXPDouble(array);
	}

	public static REXPVector toVector(List<Double> list) {
		Double[] doubles = list.toArray(new Double[]{});
		double[] array = ArrayUtils.toPrimitive(doubles);
		return toVector(array);
	}
	
	/**
	 * Convert array to {@link REXPVector}.
	 * 
	 * @param array
	 *            array to convert
	 * @return {@link REXPVector}.
	 */
	public static REXPVector toVector(int... array) {
		return new REXPInteger(array);
	}

	/**
	 * Convert array to {@link REXPVector}.
	 * 
	 * @param array
	 *            array to convert
	 * @return {@link REXPVector}.
	 */
	public static REXPVector toVector(boolean... array) {
		return new REXPLogical(array);
	}

	/**
	 * Convert array to {@link REXPVector}.
	 * 
	 * @param array
	 *            array to convert
	 * @return {@link REXPVector}.
	 */
	public static REXPVector toVector(String... array) {
		return new REXPString(array);
	}

	/**
	 * Convert object to {@link REXPVector}.
	 * 
	 * @param array
	 *            object that is null or an array
	 * @return if object is null, returns {@link REXPNull}, otherwise if object is a primitive array returns an {@link REXPVector}, otherwise throws an
	 *         {@link IllegalArgumentException}.
	 */
	public static REXP toVector(Object array) {
		if (array == null) {
			return new REXPNull();
		}

		Class<?> arrayClass = array.getClass();

		if (arrayClass == double[].class) {
			return new REXPDouble((double[]) array);
		} else if (arrayClass == int[].class) {
			return new REXPInteger((int[]) array);
		} else if (arrayClass == boolean[].class) {
			return new REXPLogical((boolean[]) array);
		} else if (arrayClass == String[].class) {
			return new REXPString((String[]) array);
		} else {
			throw new IllegalArgumentException("Cannot convert " + arrayClass.getCanonicalName() + " to R object");
		}
	}
}
