/**
 * 
 */
package org.openforis.calc.transformation;

import static org.openforis.calc.persistence.jooq.Tables.AOI;
import static org.openforis.calc.persistence.jooq.Tables.CLUSTER;
import static org.openforis.calc.persistence.jooq.Tables.PLOT_SECTION_VIEW;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.openforis.calc.model.VariableMetadata;
import org.openforis.calc.persistence.jooq.tables.Aoi;
import org.openforis.calc.persistence.jooq.tables.Cluster;
import org.openforis.calc.persistence.jooq.tables.PlotSectionView;

/**
 * @author M. Togna
 * 
 */
public class FactTableBuilder {
	

	private String schema;
	private String tableName;
	private Collection<String> columns;
	private DataType dataType;
	
	public FactTableBuilder(String schema, String tableName, Collection<String> columns , DataType dataType) {
		this.schema = schema;
		this.tableName = tableName;
		this.columns = columns;
		this.dataType = dataType;
	}

	public String getDropTableSql() {
		StringBuilder sb = new StringBuilder();
		sb.append("drop table if exists ");
		sb.append(schema);
		sb.append(".");
		sb.append(tableName);
		sb.append(" cascade");
		return sb.toString();
	}

	public String getCreateTableSql() {
		StringBuilder sb = new StringBuilder();
		sb.append("create table ");
		sb.append(schema);
		sb.append(".");
		sb.append(tableName);
		sb.append(" ( ");
		appendColumns(sb);
		sb.append(" ) ");
		return sb.toString();
	}

	private void appendColumns(StringBuilder sb) {
		int i = 0;
		for ( String col : columns ) {
//			String columnName = null;
			String type = dataType.getDataType(col);

//			
//			
//			if ( DATA_TYPES.containsKey(col) ) {
//				columnName = col;
//				dataType = DATA_TYPES.get(col);
//			} else {
//				for ( VariableMetadata var : variables ) {
//					if ( var.getVariableName().equals(col) ) {
//						columnName = col;
//						dataType = TYPE_INTEGER;
//					}
//				}
//			}
//			if ( columnName == null ) {
//				columnName = col;
//				dataType = TYPE_DOUBLE_PRECISION;
//			}
//
			if ( (i++) != 0 ) {
				sb.append(" , ");
			}

			sb.append(col);
			sb.append(" ");
			sb.append(type);
		}

	}

}
