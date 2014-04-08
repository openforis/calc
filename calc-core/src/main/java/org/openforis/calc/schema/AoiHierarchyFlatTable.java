/**
 * 
 */
package org.openforis.calc.schema;

import static org.jooq.impl.SQLDataType.INTEGER;
import static org.jooq.impl.SQLDataType.VARCHAR;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SelectQuery;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.AoiHierarchy;
import org.openforis.calc.metadata.AoiLevel;
import org.openforis.calc.persistence.jooq.Tables;
import org.openforis.calc.persistence.jooq.tables.AoiTable;
import org.openforis.calc.psql.Psql;

/**
 * @author Mino Togna
 *
 */
public class AoiHierarchyFlatTable extends AbstractTable {

	private static final long serialVersionUID = 1L;
//	private UniqueKey<Record> primaryKey;
	protected Map<AoiLevel, Field<Integer>> aoiIdFields;
	protected Map<AoiLevel, Field<String>> aoiCaptionFields;
	protected Map<AoiLevel, Field<BigDecimal>> aoiAreaFields;
	
	protected Workspace workspace;
	private AoiHierarchy hierarchy;

	/**
	 * @param name
	 * @param schema
	 */
	public AoiHierarchyFlatTable(AoiHierarchy hierarchy, DataSchema schema) {
		super(hierarchy.getName(), schema);
		
		this.hierarchy = hierarchy;
		this.workspace = schema.getWorkspace();
		
		this.aoiIdFields = new HashMap<AoiLevel, Field<Integer>>();
		this.aoiCaptionFields = new HashMap<AoiLevel, Field<String>>();
		this.aoiAreaFields = new HashMap<AoiLevel, Field<BigDecimal>>();
		
		createAoiFields();
	}

	public List<AoiLevel> getAoiLevels(){
		return this.hierarchy.getLevels() ;
	}

	public Field<Integer> getAoiIdField(AoiLevel level) {
		return aoiIdFields.get(level);
	}

	public Field<String> getAoiCaptionField(AoiLevel level) {
		return aoiCaptionFields.get(level);
	}

	public Field<BigDecimal> getAoiAreaField(AoiLevel level) {
		return aoiAreaFields.get(level);
	}
	
	protected void createAoiFields() {
		List<AoiLevel> levels = hierarchy.getLevels();
		for (AoiLevel level : levels) {
			Field<Integer> aoiField = createField( level.getFkColumn(), INTEGER, this );
			aoiIdFields.put(level, aoiField);
			
			Field<String> aoiField1 = createField( level.getCaptionColumn(), VARCHAR, this );
			aoiCaptionFields.put(level, aoiField1);
			
			Field<BigDecimal> area = createField( level.getAreaColumn(), Psql.DOUBLE_PRECISION, this );
			aoiAreaFields.put(level, area);
		}
	}
	
	public SelectQuery<Record> getSelectQuery(){
		SelectQuery<Record> select = new Psql().selectQuery();
//		List<AoiLevel> levels = hierarchy.getLevels();
		List<AoiLevel> aoiLevels = hierarchy.getLevels();
		
//		AoiTable childAoiTable = null;
		for ( int i = aoiLevels.size() - 1 ; i >= 0 ; i-- ) {
			AoiLevel aoiLevel = aoiLevels.get(i);
			AoiTable aoiTable = Tables.AOI.as( aoiLevel.getNormalizedName() );
	//		AoiDimensionTable aoiDimTable = outputSchema.getAoiDimensionTable(aoiLevel);
	
	//		String aoiLevelName = aoiLevel.getName();
	
			String aliasIdColumn = aoiLevel.getFkColumn();// aoiLevelName + "_id";
			String aliasCaptionColumn = aoiLevel.getCaptionColumn();// aoiLevelName + "_caption";
			String aliasCodeColumn = aoiLevel.getCodeColumn();
			String aliasAreaColumn = aoiLevel.getAreaColumn();
			
			select.addSelect( aoiTable.ID.as(aliasIdColumn) );
			select.addSelect( aoiTable.CAPTION.as(aliasCaptionColumn) );
			select.addSelect( aoiTable.CODE.as(aliasCodeColumn) );
			select.addSelect( aoiTable.LAND_AREA.as(aliasAreaColumn) );
			
			// first level
			if ( aoiLevel == this.hierarchy.getLeafLevel() ) {
				select.addFrom( aoiTable );
				select.addConditions( aoiTable.AOI_LEVEL_ID.eq(aoiLevel.getId()) );
			} else {
				AoiLevel childLevel = aoiLevels.get(i + 1);
				AoiTable childAoiTable = Tables.AOI.as( childLevel.getNormalizedName() );// aoiTable.as( aliasIdColumn ); //.get) outputSchema.getAoiDimensionTable(childLevel);
	
				select.addJoin( aoiTable, childAoiTable.PARENT_AOI_ID.eq(aoiTable.ID));
				
//				aoiTable = childAoiTable;
			}
	
		}
		
		return select;
	}
	
	public AoiHierarchy getAoiHierarchy() {
		return hierarchy;
	}

//	public 
	
}
