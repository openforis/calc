package org.openforis.calc.r;

import java.util.List;

/**
 * 
 * @author G. Miceli
 * @author S. Ricci
 *
 */
public class RDataFrame extends RScript {
	
	private List<String> columnNames;
	private List<RVector> columns;
	
	public RDataFrame(List<String> columnNames, List<RVector> columns) {
		super();
		this.columnNames = columnNames;
		this.columns = columns;
		
		buildScript();
	}
	
	protected void buildScript() {
		append("data.frame(");
		if ( columns != null ) {
			for ( int i=0; i < columnNames.size(); i++) {
				String columnName = columnNames.get(i);
				RVector column = columns.get(i);
				append(columnName);
				append(" = ");
				append(column.toScript());
				if ( i < columnNames.size() - 1 ) {
					append(",");
				}
			}
		}
		append(")");
	}
	
	public List<String> getColumnNames() {
		return columnNames;
	}
	
	public List<RVector> getColumns() {
		return columns;
	}
	
	/*
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
	*/
	
}
