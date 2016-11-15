package org.jooq.util;

import org.jooq.util.ColumnDefinition;
import org.jooq.util.DefaultGeneratorStrategy;
import org.jooq.util.Definition;
import org.jooq.util.SchemaDefinition;
import org.jooq.util.TableDefinition;

/**
 * @author G. Miceli
 */
public class CustomGeneratorStrategy extends DefaultGeneratorStrategy {

	@Override
	public String getJavaClassName(Definition definition, Mode mode) {
		String defaultName = super.getJavaClassName(definition, mode);
		if( mode != null ){
			switch (mode) {
			case POJO:
				if( ! (definition instanceof ColumnDefinition) ) {
					return defaultName + "Base";
				}
				return defaultName;
			case DEFAULT:
				if ( definition instanceof SchemaDefinition ) {
					return defaultName + "Schema";
				} else if ( definition instanceof TableDefinition ) {
					return defaultName + "Table";
				}	
			default:
				return defaultName;
			}
		}
		return defaultName;
	}
	
}
