package org.openforis.calc.schema;

import org.jooq.impl.SQLDataType;
import org.openforis.calc.metadata.CategoryLevel;
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
		super( getLevel(variable).getTableName() , schema );

		this.variable = variable;

		initFields();
	}

	private static CategoryLevel getLevel( MultiwayVariable variable ) {
		CategoryLevel categoryLevel = variable.getCategoryLevel();
		return categoryLevel;
	}

	public MultiwayVariable getVariable() {
		return variable;
	}

	@Override
	protected void initFields() {
		CategoryLevel level = getLevel( this.variable );
		
		setIdField( createField(level.getIdColumn(), SQLDataType.INTEGER, this) );
		setCaptionField( createField(level.getCaptionColumn() , SQLDataType.VARCHAR, this) );
		setCodeField( createField(level.getCodeColumn() , SQLDataType.VARCHAR, this) );
	}
}
