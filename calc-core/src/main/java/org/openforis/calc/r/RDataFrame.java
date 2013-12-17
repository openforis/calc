package org.openforis.calc.r;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openforis.calc.engine.DataRecord;

/**
 * 
 * @author G. Miceli
 * @author S. Ricci
 *
 */
public class RDataFrame extends RScript {
	
	private List<RNamedVector> columns;
	private boolean changed;
	
	public RDataFrame() {
		this((RNamedVector) null);
	}
	
	public RDataFrame(RNamedVector... columns) {
		super();
		this.columns= new ArrayList<RNamedVector>(Arrays.asList(columns));
		this.changed = true;
	}

	public void addColumn(RNamedVector column) {
		columns.add(column);
		changed = true;
	}
	
	public List<DataRecord> toRecords() {
		List<DataRecord> results = new ArrayList<DataRecord>();
		int count = 0;
		while ( count < getRowsCount() ) {
			DataRecord record = new DataRecord();
			for ( int columnIndex = 0;  columnIndex < columns.size(); columnIndex++ ) {
				RNamedVector column = columns.get(columnIndex);
				String colName = column.getName();
				if ( count >= column.size() ) {
					System.out.println(String.format("Trying to access invalid position %d for column %s", count, colName));
				}
				Object value = column.getValue(count);
				record.add(colName, value);
			}
			results.add(record);
			count ++;
		}
		return results;
	}
	
	public int getRowsCount() {
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
		if ( columns != null ) {
			for ( int i=0; i < columns.size(); i++) {
				RNamedVector column = columns.get(i);
				append(column.toScript());
				if ( i < columns.size() - 1 ) {
					append(",");
				}
			}
		}
		append(")");
	}
	
	public List<String> getColumnNames() {
		List<String> result = new ArrayList<String>();
		for (RNamedVector col : columns) {
			result.add(col.getName());
		}
		return result;
	}
	
	public List<RNamedVector> getColumns() {
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
