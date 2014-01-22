package org.openforis.calc.r;

import java.util.ArrayList;
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
	private boolean changed;
	
	public RDataFrame() {
		this((String[]) null, (RVector[]) null);
	}
	
	public RDataFrame(String[] columnNames, RVector[] columns) {
		super();
		this.columnNames = new ArrayList<String>();
		this.columns= new ArrayList<RVector>();
		
		if ( columnNames != null ) {
			for ( int i=0; i < columnNames.length; i++) {
				String columnName = columnNames[i];
				RVector column = columns[i];
				addColumn(columnName, column);
			}
		}
		this.changed = true;
	}

	public void addColumn(String name, RVector column) {
		if ( columnNames.contains(name) ) {
			throw new IllegalArgumentException("Column already defined for this dataframe: " + name);
		}
		columnNames.add(name);
		columns.add(column);
		changed = true;
	}
	
	public int getSize() {
		if ( columns == null || columns.isEmpty() ) {
			return 0;
		} else {
			RVector firstColumn = columns.get(0);
			return firstColumn.size();
		}
	}
	
	@Override
	protected String toScript() {
		if ( changed ) {
			buildScript();
		}
		return super.toScript();
	}
	
	protected void buildScript() {
		reset();
		append("data.frame(");
		if ( columnNames != null ) {
			for ( int i=0; i < columnNames.size(); i++) {
				String columnName = columnNames.get(i);
				RVector column = columns.get(i);
				append(columnName);
				append(" = ");
				append(column.toScript());
				if ( i < columns.size() - 1 ) {
					append(", ");
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
