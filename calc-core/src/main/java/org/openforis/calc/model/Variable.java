package org.openforis.calc.model;

import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;


/**
 * @author G. Miceli
 * @author M. Togna
 */
public class Variable extends org.openforis.calc.persistence.jooq.tables.pojos.Variable implements Identifiable {
	
	private static final long serialVersionUID = 1L;

	public boolean isCategorical() {
		switch (getVariableTypeEnum()) {
		case NOMINAL:
		case ORDINAL:
		case MULTIPLE_RESPONSE:
		case BOOLEAN:
			return true;
		default:
			return false;
		}
	}
	
	public boolean isNumeric() {
		return !isCategorical();
	}
	
	public boolean isBoolean() {
		return getVariableTypeEnum() == VariableType.BOOLEAN;
	}
	
	public void setVariableTypeEnum(VariableType type) {
		super.setVariableType( type.toString() );
	}

	public VariableType getVariableTypeEnum() {
		return VariableType.get(getVariableType());
	}
	
	@Override
	public Integer getId() {
		return super.getVariableId();
	}

	@Override
	public void setId(Integer id) {
		super.setVariableId(id);
	}
	
	public Unit<? extends Quantity> getUnit() {
		String uom = getUom();
		return uom == null ? null : Unit.valueOf(uom);
	}
}
