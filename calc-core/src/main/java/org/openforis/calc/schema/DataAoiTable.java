/**
 * 
 */
package org.openforis.calc.schema;

import static org.jooq.impl.SQLDataType.BIGINT;
import static org.jooq.impl.SQLDataType.VARCHAR;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.PrimarySamplingUnitTable;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.AoiHierarchy;
import org.openforis.calc.metadata.AoiLevel;
import org.openforis.calc.psql.Psql;

/**
 * @author Mino Togna
 *
 */
public class DataAoiTable extends AbstractTable {

	private static final long serialVersionUID = 1L;
	
	private UniqueKey<Record> primaryKey;
	protected Workspace workspace;
	
	protected Map<AoiLevel, Field<Long>> aoiIdFields;
	protected Map<AoiLevel, Field<String>> aoiCaptionFields;
	protected Map<AoiLevel, Field<String>> aoiCodeFields;
	protected Map<AoiLevel, Field<BigDecimal>> aoiAreaFields;
	private TableField<Record, Long> idField;
	private List<TableField<Record, ?>> keyFields;

	/**
	 * @param name
	 * @param schema
	 */
	public DataAoiTable(String name, DataSchema schema) {
		super(name, schema);
		
		this.workspace = schema.getWorkspace();
//		this.aoiIdFields = new HashMap<AoiLevel, Field<Long>>();
		
		createPrimaryKeyField();
		createAoiFields();
	}

	@SuppressWarnings("unchecked")
	protected void createPrimaryKeyField() {
		if( workspace.has2StagesSamplingDesign() ){
			this.keyFields = new ArrayList<TableField<Record,?>>();
			PrimarySamplingUnitTable<?> psuTable = workspace.getSamplingDesign().getPrimarySamplingUnitTable();
			for (Field<?> psuField : psuTable.getPsuFields()) {
				TableField<Record,?> field = super.copyField( psuField );
				this.keyFields.add( field );
			}
		
		} else {
			this.idField = createField("id", BIGINT, this);
			this.primaryKey = KeyFactory.newUniqueKey(this, idField);
		}
	}

	protected void setIdField(TableField<Record, Long> idField) {
		this.idField = idField;
	}

	protected void setPrimaryKey(UniqueKey<Record> primaryKey) {
		this.primaryKey = primaryKey;
	}

	public TableField<Record, Long> getIdField() {
		return idField;
	}

	@Override
	public UniqueKey<Record> getPrimaryKey() {
		return primaryKey;
	}

	protected void createAoiFields() {
		
		this.aoiIdFields = new HashMap<AoiLevel, Field<Long>>();
		this.aoiCaptionFields = new HashMap<AoiLevel, Field<String>>();
		this.aoiCodeFields = new HashMap<AoiLevel, Field<String>>();
		this.aoiAreaFields = new HashMap<AoiLevel, Field<BigDecimal>>();
		
		List<AoiHierarchy> aoiHierarchies = workspace.getAoiHierarchies();
		for (AoiHierarchy hierarchy : aoiHierarchies) {
			List<AoiLevel> levels = hierarchy.getLevels();
			for (AoiLevel level : levels) {
				Field<Long> aoiField = createField( level.getFkColumn(), BIGINT, this );
				aoiIdFields.put(level, aoiField);
				
				Field<String> aoiField1 = createField( level.getCaptionColumn(), VARCHAR, this );
				aoiCaptionFields.put(level, aoiField1);
				
				Field<String> aoiCodeField = createField( level.getCodeColumn(), VARCHAR, this );
				aoiCodeFields.put(level, aoiCodeField);
				
				Field<BigDecimal> area = createField( level.getAreaColumn(), Psql.DOUBLE_PRECISION, this );
				aoiAreaFields.put(level, area);
			}
		}
	}

//	protected void createAoiIdField(AoiLevel level, String fkColumn) {
//		Field<Integer> aoiField = createField(fkColumn, BIGINT, this);
//		aoiIdFields.put(level, aoiField);
//	}
	

	public Field<Long> getAoiIdField(AoiLevel level) {
		return aoiIdFields.get(level);
	}

	public Collection<Field<Long>> getAoiIdFields() {
		return aoiIdFields.values();
	}	
	
	public Field<String> getAoiCaptionField(AoiLevel level) {
		return aoiCaptionFields.get(level);
	}
	
	public Collection<Field<String>> getAoiCaptionFields() {
		return aoiCaptionFields.values();
	}
	
	public Field<String> getAoiCodeField(AoiLevel level) {
		return aoiCodeFields.get(level);
	}
	
	public Collection<Field<String>> getAoiCodeFields() {
		return aoiCodeFields.values();
	}

	
	public Field<BigDecimal> getAoiAreaField(AoiLevel level) {
		return aoiAreaFields.get(level);
	}
	
	public Collection<Field<BigDecimal>> getAoiAreaFields() {
		return aoiAreaFields.values();
	}

}
