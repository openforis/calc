package org.openforis.calc.schema;

import static org.jooq.impl.SQLDataType.INTEGER;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Schema;
import org.jooq.TableField;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.AoiHierarchy;
import org.openforis.calc.metadata.AoiLevel;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.psql.Psql;

/**
 * 
 * @author M. Togna
 */
public class ClusterCountsTable extends AbstractTable {

	private static final long serialVersionUID = 1L;
	private static final String TABLE_NAME = "_cluster_counts";

	public final TableField<Record, Integer> STRATUM;
//	public final TableField<Record, Integer> AOI_ID;
	public final TableField<Record, Integer> CLUSTER_ID;
	public final TableField<Record, BigDecimal> WEIGHT;
	public final TableField<Record, BigDecimal> BASE_UNIT_WEIGHT;

	private Map<AoiLevel, Field<Integer>> aoiIdFields; 
	
	public ClusterCountsTable(Schema schema, Entity clusterEntity) {
		super(TABLE_NAME, schema);

		STRATUM = createField("stratum", INTEGER, this);
//		AOI_ID = createField(level.getFkColumn(), INTEGER, this);
		CLUSTER_ID = createField(clusterEntity.getIdColumn(), INTEGER, this);
		WEIGHT = createField("weight", Psql.DOUBLE_PRECISION, this);
		BASE_UNIT_WEIGHT = createField("base_unit_weight", Psql.DOUBLE_PRECISION, this);
		
		this.aoiIdFields 				= new HashMap<AoiLevel, Field<Integer>>();
		
//		if ( isGeoreferenced() ) {
			Workspace workspace = clusterEntity.getWorkspace();
			List<AoiHierarchy> aoiHierarchies = workspace.getAoiHierarchies();
			for ( AoiHierarchy hierarchy : aoiHierarchies ) {
				List<AoiLevel> levels = hierarchy.getLevels();
				for ( AoiLevel level : levels ) {
//					if ( lowestLevel == null || level.getRank() <= lowestLevel.getRank() ) {
						String fkColumn = level.getFkColumn();
						Field<Integer> aoiField = createField(fkColumn, INTEGER, this);
						aoiIdFields.put(level, aoiField);
//					}
				}
			}
//		}
	}
	
	public Field<Integer> getAoiIdField(AoiLevel level) {
		return aoiIdFields == null ? null : aoiIdFields.get(level);
	}

}
