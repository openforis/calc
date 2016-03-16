package org.openforis.calc.schema;

import org.jooq.Record;
import org.jooq.Schema;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;
import org.openforis.calc.collect.SpeciesCodeTable;
import org.openforis.calc.collect.SpeciesCodeView;
import org.openforis.calc.metadata.CategoryLevel;
import org.openforis.calc.metadata.MultiwayVariable;

/**
 * 
 * @author Mino Togna
 */
public class SpeciesCategoryDimensionTable extends DimensionTable {

	private static final long serialVersionUID = 1L;

	private MultiwayVariable variable;

	private TableField<Record, Integer> genusIdField;
	private TableField<Record, String> genusCaptionField;
	private TableField<Record, String> genusCodeField;

	public SpeciesCategoryDimensionTable(Schema schema, MultiwayVariable variable) {
		super(getTableName(variable), schema);

		this.variable = variable;

		initFields();
	}

	private static String getTableName(MultiwayVariable variable) {
		CategoryLevel level 	= variable.getCategoryLevel();
		SpeciesCodeTable table 	= new SpeciesCodeTable(level.getName(), level.getSchemaName());
		SpeciesCodeView view 	= new SpeciesCodeView(table);
		
		return view.getName();
	}

	public MultiwayVariable getVariable() {
		return variable;
	}

	@Override
	protected void initFields() {
		CategoryLevel level 	= variable.getCategoryLevel();

		SpeciesCodeTable table 	= new SpeciesCodeTable(level.getName(), level.getSchemaName());
		SpeciesCodeView view 	= new SpeciesCodeView(table);

		setIdField(	createField( view.getIdField().getName(), SQLDataType.INTEGER, this));
		setCaptionField( createField( view.getScientificNameField().getName() , SQLDataType.VARCHAR, this));
		setCodeField( createField( view.getCodeField().getName() , SQLDataType.VARCHAR, this));

		genusIdField 		= createField( view.getGenusIdField().getName(), SQLDataType.INTEGER, this);
		genusCaptionField	= createField( view.getGenusScientificNameField().getName(), SQLDataType.VARCHAR, this);
		genusCodeField 		= createField( view.getGenusCodeField().getName() , SQLDataType.VARCHAR, this);
	}
	
	public TableField<Record, Integer> getGenusIdField() {
		return genusIdField;
	}
	
	public TableField<Record, String> getGenusCaptionField() {
		return genusCaptionField;
	}
	
	public TableField<Record, String> getGenusCodeField() {
		return genusCodeField;
	}
	
}
