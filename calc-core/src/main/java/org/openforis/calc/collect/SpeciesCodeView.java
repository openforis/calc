/**
 * 
 */
package org.openforis.calc.collect;

import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;
import org.openforis.calc.schema.AbstractTable;

/**
 * @author M. Togna
 *
 */
public class SpeciesCodeView extends AbstractTable {

	private static final long serialVersionUID = 1L;
	private TableField<Record, Integer> idField;
	private TableField<Record, String> codeField;
	private TableField<Record, String> scientificNameField;
	private TableField<Record, String> rankField;
	
	private TableField<Record, Integer> genusIdField;
	private TableField<Record, String> genusCodeField;
	private TableField<Record, String> genusScientificNameField;
	private TableField<Record, String> genusRankField;

	private String speciesListName;
	
	public enum Rank {
		genus;
	}
	
	SpeciesCodeView(SpeciesCodeTable table) {
		super( table.getName() + "_view", table.getSchema() );
		initFields( table.getSpeciesListName() );
	}
	
	private void initFields(String speciesList) {
		this.speciesListName 		= speciesList;

		this.idField 				= createField(getName() + "_id", SQLDataType.INTEGER, this);
		this.codeField 				= createField(speciesList, SQLDataType.VARCHAR, this);
		this.scientificNameField 	= createField(speciesList + "_scientific_name", SQLDataType.VARCHAR, this);
		this.rankField 				= createField( "rank", SQLDataType.VARCHAR, this);
		
		this.genusIdField 				= createField( "genus_id", SQLDataType.INTEGER, this);
		this.genusCodeField 			= createField( "genus_code", SQLDataType.VARCHAR, this);
		this.genusScientificNameField 	= createField( "genus_scientific_name", SQLDataType.VARCHAR, this);
		this.genusRankField 			= createField( "genus_rank", SQLDataType.VARCHAR, this);
		
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

	public TableField<Record, Integer> getGenusIdField() {
		return genusIdField;
	}

	public TableField<Record, String> getGenusCodeField() {
		return genusCodeField;
	}

	public TableField<Record, String> getGenusScientificNameField() {
		return genusScientificNameField;
	}

	public TableField<Record, String> getGenusRankField() {
		return genusRankField;
	}

	public String getSpeciesListName() {
		return speciesListName;
	}
	
	
}
