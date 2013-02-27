/**
 * 
 */
package org.openforis.calc.persistence.jooq.rolap;



/**
 * @author M. Togna
 * @author G. Miceli
 *
 */
public abstract class DimensionTable<R extends DimensionRecord<R>> extends RolapTable<R> {

	private static final long serialVersionUID = 1L;
	
	public final org.jooq.TableField<R, String> CODE = createField("code", org.jooq.impl.SQLDataType.VARCHAR, this);
	
	public final org.jooq.TableField<R, String> LABEL = createField("label", org.jooq.impl.SQLDataType.VARCHAR, this);

	/**
	 * @param name
	 * @param schema
	 * @param recordType 
	 */
	DimensionTable(String schema, String name, Class<R> recordType) {
		super( schema, name, recordType );
	}
}
