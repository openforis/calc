package org.openforis.calc.psql;

import org.apache.commons.lang.StringUtils;
import org.jooq.Schema;

/**
 * 
 * @author G. Miceli
 *
 */
public class SetDefaultSchemaSearchPathStep extends PsqlPart {

	SetDefaultSchemaSearchPathStep(Psql psql, Schema... schemas) {
		super(psql);
		append("set search_path to ");
		append(StringUtils.join(schemas, ", "));
	}
}
