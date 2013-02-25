/**
 * 
 */
package org.openforis.calc.persistence.jooq.olap;


/**
 * @author M. Togna
 *
 */
public class DimensionTable extends OlapTable<DimensionRecord> {

	private static final long serialVersionUID = 1L;
	
//	public final org.jooq.TableField<DimensionRecord, Integer> ID = createField("id", org.jooq.impl.SQLDataType.INTEGER, this);
	
	public final org.jooq.TableField<DimensionRecord, String> CODE = createField("code", org.jooq.impl.SQLDataType.VARCHAR, this);
	
	public final org.jooq.TableField<DimensionRecord, String> LABEL = createField("label", org.jooq.impl.SQLDataType.VARCHAR, this);

	/**
	 * @param name
	 * @param schema
	 */
	public DimensionTable(String name, String schema) {
		super( name, schema );
	}

}
