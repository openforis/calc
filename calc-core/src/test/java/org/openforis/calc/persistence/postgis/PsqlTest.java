package org.openforis.calc.persistence.postgis;

import static org.openforis.calc.persistence.jooq.Tables.AOI;
import static org.openforis.calc.persistence.jooq.Tables.STRATUM;

import org.jooq.Field;
import org.jooq.Table;
import org.jooq.Update;
import org.junit.Assert;
import org.junit.Test;
import org.openforis.calc.psql.Psql;
import org.openforis.calc.psql.UpdateWithStep;

/**
 * 
 * @author G. Miceli
 *
 */
public class PsqlTest {

//	@Test
	public void testUpdateWith() {
		Field<Integer> fld = STRATUM.ID;
		Table<?> cursor = new Psql().select(fld).from(STRATUM).asTable("x");
		Update<?> update = new Psql().update(AOI).set(AOI.ID, cursor.field(fld));
		UpdateWithStep updateWith = new Psql().updateWith(cursor, update, AOI.ID.eq(cursor.field(fld)));
		String expectedSql = "with \"x\" as (select \"calc\".\"stratum\".\"id\" from \"calc\".\"stratum\") update \"calc\".\"aoi\" set \"id\" = \"x\".\"id\" from \"x\" where \"calc\".\"aoi\".\"id\" = \"x\".\"id\"";
		Assert.assertEquals(expectedSql, updateWith.toString());
	}
}
