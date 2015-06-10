/**
 * 
 */
package org.jooq.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.TableField;
import org.json.simple.JSONArray;
import org.openforis.calc.metadata.SamplingDesign;
import org.openforis.calc.metadata.SamplingDesign.ColumnJoin;
import org.openforis.calc.metadata.SamplingDesign.TwoStagesSettings;

/**
 * @author M. Togna
 *
 */
public class PrimarySamplingUnitTable<R extends Record> extends DynamicTable<R> {

	private static final long serialVersionUID = 1L;

	private List<Field<?>> psuFields;
//	private List<Field<?>> suFields;
	private TableField<Record,BigDecimal> noTheoreticalBu = null ;
	/**
	 * @param name
	 * @param schema
	 */
	public PrimarySamplingUnitTable(String name, String schema) {
		super(name, schema);
		
		psuFields = new ArrayList<Field<?>>();
//		suFields = new ArrayList<Field<?>>();
	}
	
	public void initSamplingDesignFields( SamplingDesign samplingDesign , JSONArray info ){
		super.initFields( info );
	
		TwoStagesSettings settings = samplingDesign.getTwoStagesSettingsObject();
		List<ColumnJoin> psuIdColumns = settings.getPsuIdColumns();
//		List<ColumnJoin> suIdColumns = settings.getSamplingUnitPsuJoinColumns();
		
		noTheoreticalBu = (TableField<Record, BigDecimal>) field( settings.getNoBaseUnitColumn() );
		
		for (Field<?> field : getFields()) {
			String fieldName = field.getName();
			
			for (ColumnJoin columnJoin : psuIdColumns) {
				if( columnJoin.getColumn().equals(fieldName) ){
					psuFields.add( field );
					break;
				}
			}
			
//			for (ColumnJoin columnJoin : suIdColumns) {
//				if( columnJoin.getColumn().equals(fieldName) ){
//					suFields.add( field );
//					break;
//				}
//			}
		}
	}
	
	public List<Field<?>> getPsuFields() {
		return psuFields;
	}
	
	public TableField<Record, BigDecimal> getNoTheoreticalBu() {
		return noTheoreticalBu;
	}
	
//	public List<Field<?>> getSuFields() {
//		return suFields;
//	}
}
