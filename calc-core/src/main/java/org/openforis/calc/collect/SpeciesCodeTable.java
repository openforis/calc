/**
 * 
 */
package org.openforis.calc.collect;

import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.SchemaImpl;
import org.openforis.calc.schema.AbstractTable;

/**
 * @author S. Ricci
 *
 */
public class SpeciesCodeTable extends AbstractTable {

	private static final long serialVersionUID = 1L;
	private TableField<Record, Integer> idField;
	private TableField<Record, String> codeField;
	private TableField<Record, String> parentCodeField;
	private TableField<Record, String> scientificNameField;
	private TableField<Record, String> rankField;
	private String speciesListName;
	
	public enum Rank {
		genus;
	}
	
	public SpeciesCodeTable(String speciesList, String schema) {
		super(speciesList + "_code", new SchemaImpl(schema));
		initFields(speciesList);
	}
	
	private SpeciesCodeTable( String alias, SpeciesCodeTable aliased ) {
		super(alias, aliased.getSchema(), aliased, null, "");
		initFields( aliased.getSpeciesListName() );
	}
	
	private void initFields(String speciesList) {
		this.speciesListName 		= speciesList;
		this.idField 				= createField(speciesList + "_id", SQLDataType.INTEGER, this);
		this.codeField 				= createField(speciesList, SQLDataType.VARCHAR, this);
		this.parentCodeField 		= createField(speciesList+"_parent_code", SQLDataType.VARCHAR, this);
		this.scientificNameField 	= createField(speciesList + "_scientific_name", SQLDataType.VARCHAR, this);
		this.rankField 				= createField( "rank", SQLDataType.VARCHAR, this);
	}
	
	public TableField<Record, Integer> getIdField() {
		return idField;
	}

	public TableField<Record, String> getCodeField() {
		return codeField;
	}

	public TableField<Record, String> getScientificNameField() {
		return scientificNameField;
	}

	public TableField<Record, String> getRankField() {
		return rankField;
	}
	
	public TableField<Record, String> getParentCodeField() {
		return parentCodeField;
	}
	
	public String getSpeciesListName() {
		return speciesListName;
	}
	
	public SpeciesCodeTable as( String alias ){
		return new SpeciesCodeTable( alias, this );
		
	}
	
}
