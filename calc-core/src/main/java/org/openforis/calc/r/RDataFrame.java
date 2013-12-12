package org.openforis.calc.r;

import java.util.ArrayList;
import java.util.List;

import org.openforis.calc.engine.DataRecord;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPDouble;
import org.rosuda.REngine.REXPInteger;
import org.rosuda.REngine.REXPList;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REXPString;
import org.rosuda.REngine.RList;

/**
 * 
 * @author G. Miceli
 * @author S. Ricci
 *
 */
public class RDataFrame {
	
	private List<String> columnNames;
	private List<DataRecord> rows;
	
	public RDataFrame(List<String> columnNames, List<DataRecord> rows) {
		super();
		this.columnNames = columnNames;
		this.rows = rows;
	}
	
	public List<String> getColumnNames() {
		return columnNames;
	}
	
	public List<DataRecord> getRows() {
		return rows;
	}
	
	public REXP toREXP() throws RException {
		List<REXP> rexpColumns = new ArrayList<REXP>();
		
		for (String columnName : columnNames) {
			RList columnValues = new RList();
			for (DataRecord row : rows) {
				Object value = row.getValue(columnName);
				if ( value == null ) {
					columnValues.add(null);
				} else if ( value instanceof Double ) {
					columnValues.add(new REXPDouble((Double) value));
				} else if ( value instanceof Integer ) {
					columnValues.add(new REXPInteger((Integer) value));
				} else if ( value instanceof String) {
					columnValues.add(new REXPString((String) value));
				} else {
					throw new UnsupportedOperationException("Unknow value type: " + value.getClass());
				}
			}
			rexpColumns.add(new REXPList(new REXPList(columnValues), columnName));
		}
		try {
			REXP result = REXP.createDataFrame(new RList(rexpColumns));
			return result;
		} catch(REXPMismatchException e) {
			throw new RException(e);
		}
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("data.frame(");
		if ( rows != null ) {
			for (String columnName : columnNames) {
				printColumnData(sb, columnName);
			}
		}
		sb.append(")");
		return sb.toString();
	}

	private void printColumnData(StringBuilder sb, String columnName) {
		sb.append(columnName);
		sb.append(" = ");
		sb.append("c(");
		for(int i = 0; i < rows.size(); i++) {
			DataRecord row = rows.get(i);
			Object value = row.getValue(columnName);
			sb.append(value);
			if ( i < rows.size() - 1 ) {
				sb.append(",");
			}
		}
		sb.append(")");
	}
	
	
}
