package org.openforis.calc.chain.pre;

import static org.openforis.calc.persistence.jooq.Tables.SRS;

import org.jooq.Field;
import org.jooq.Table;
import org.jooq.Update;
import org.jooq.impl.DSL;
import org.openforis.calc.engine.Task;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.psql.Psql;
import org.openforis.calc.psql.UpdateWithStep;
import org.openforis.calc.schema.OutputDataTable;
import org.openforis.calc.schema.OutputSchema;

/**
 * Update Point columns, taking X, Y and SRSID from other columns and converting them to the default SRS.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
public final class AssignLocationColumnsTask extends Task {

	public static final String LOCATION_COLUMN = "_location";
	private static final String TO_WGS84 = "ST_Transform(ST_SetSRID(ST_Point(%s::float,%s::float),srid),4326)";

	@Override
	protected void execute() throws Throwable {
		// TODO
		OutputSchema outputSchema = getOutputSchema();
		for ( OutputDataTable dataTable : outputSchema.getOutputDataTables() ) {

			Entity entity = dataTable.getEntity();
			if ( entity.isGeoreferenced() ) {
				if ( entity.getLocationColumn() == null ) {

					String xColumn = "\"" + entity.getXColumn() + "\"";
					String yColumn = "\"" + entity.getYColumn() + "\"";
					String srsColumn = entity.getSrsColumn();

					convertCoordinates(dataTable, xColumn, yColumn, srsColumn);
				} else {
					copyLocationColumn();
				}
			}
		}

	}

	private void convertCoordinates(OutputDataTable dataTable, String xColumn, String yColumn, String srsColumn) {
		String expr = String.format(TO_WGS84, xColumn, yColumn);

		Table<?> cursor = new Psql().select().from(SRS).asTable("srs");

		Update<?> update = new Psql().update(dataTable).set(dataTable.getLocationField(), DSL.field(expr, Psql.GEODETIC_COORDINATE));

		UpdateWithStep step = psql().updateWith(cursor, update, dataTable.getSrsIdField().eq((Field<String>) cursor.field(SRS.SRS_ID)));

		System.out.println(step.toString());
		step.execute();
	}

	private void copyLocationColumn() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not yet implemented");
	}

}
