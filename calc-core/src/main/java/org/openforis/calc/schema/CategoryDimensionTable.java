package org.openforis.calc.schema;

import org.jooq.Schema;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.SchemaImpl;
import org.openforis.calc.metadata.CategoricalVariable;
import org.openforis.calc.metadata.CategoryLevel;

/**
 * 
 * @author Mino Togna
 * @author S. Ricci
 */
public class CategoryDimensionTable extends DimensionTable {

	private static final long serialVersionUID = 1L;

	private CategoricalVariable<?> variable;

	public CategoryDimensionTable(Schema schema, CategoricalVariable<?> variable) {
		super( getLevel(variable).getTableName() , schema );

		this.variable = variable;

		initFields();
	}

	public CategoryDimensionTable( CategoricalVariable<?> variable ){
		this(  new SchemaImpl( getLevel(variable).getSchemaName() ) , variable );
	}
	
	private static CategoryLevel getLevel( CategoricalVariable<?> variable ) {
		CategoryLevel categoryLevel = variable.getCategoryLevel();
		return categoryLevel;
	}

	public CategoricalVariable<?> getVariable() {
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
