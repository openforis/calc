package org.openforis.calc.schema;

import org.jooq.impl.SQLDataType;
import org.openforis.calc.metadata.MultiwayVariable;

/**
 * 
 * @author Mino Togna
 * @author S. Ricci
 */
public class CategoryDimensionTable extends DimensionTable {

	private static final long serialVersionUID = 1L;

	private MultiwayVariable variable;

	CategoryDimensionTable(RelationalSchema schema, MultiwayVariable variable) {
		super(variable.getDimensionTable(), schema);

		this.variable = variable;

		initFields();
	}

	public MultiwayVariable getVariable() {
		return variable;
	}

	@Override
	protected void initFields() {
		setIdField( createField(this.variable.getDimensionTableIdColumn(), SQLDataType.INTEGER, this) );
		setCaptionField( createField(this.variable.getDimensionTable() + "_label", SQLDataType.VARCHAR, this) );
	}
}
