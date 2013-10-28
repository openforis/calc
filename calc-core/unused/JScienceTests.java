package sandbox;

import java.util.Set;

import javax.measure.quantity.Area;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Quantity;
import javax.measure.quantity.Volume;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import org.jscience.economics.money.Currency;
import org.jscience.physics.amount.Amount;


public class JScienceTests {
	public static void main(String[] args) {
		Amount<Volume> vol = Amount.valueOf(12.5, SI.CUBIC_METRE);
		Amount<? extends Quantity> area = Amount.valueOf(1200, Unit.valueOf("ha"));
		Amount<Area> kmarea = area.to(SI.KILO(SI.SQUARE_METRE));
		System.out.println(kmarea);
		Amount<? extends Quantity> totvol = vol.divide(area);
		System.out.println("vol = "+vol + "\n\tdim = " + vol.getUnit().getDimension());
		System.out.println("area = "+area + "\n\tdim = " + area.getUnit().getDimension());
		System.out.println("totvol = "+totvol + "\n\tdim = " + totvol.getUnit().getDimension());
//		Unit<Volume> unit = vol.getUnit();
//		Dimension dim = unit.getDimension();
//		System.out.println(dim.toString());
		
		System.out.println(Currency.USD.getDimension());
		Amount<Dimensionless> n = Amount.valueOf(12000, Unit.ONE);
		Amount<Dimensionless> kn = n.to(SI.KILO(Unit.ONE));
		System.out.println(kn);
		Set<Unit<?>> units = SI.getInstance().getUnits();
		System.out.println("SI units:");
		for (Unit<?> unit : units) {
			if ( unit.getDimension().toString().equals("[L]²")) {
				System.out.println(unit+"\t"+unit.getDimension());
			}
		}
		Unit<? extends Quantity> kg = Unit.valueOf("%");
		System.out.println(kg);
		
//		System.out.println("Non SI units:");
//		units = NonSI.getInstance().getUnits();
//		for (Unit<?> unit : units) {
//			if ( unit.getDimension().toString().equals("[L]²")) {
//				System.out.println(unit+"\t"+unit.getDimension());
//			}
//		}
	}
}
