package org.openforis.calc.chain.pre;

import static org.openforis.calc.persistence.postgis.Psql.quote;

import java.util.List;

import org.openforis.calc.engine.SqlTask;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.persistence.postgis.Psql;

/**
 * Update Point columns, taking X, Y and SRSID from other columns and converting them to the default SRS.
 * 
 * @author G. Miceli
 */
public final class CreateLocationColumnsTask extends SqlTask {
	public static final String LOCATION_COLUMN = "_location";	
	private static final String TO_WGS84 = "ST_Transform(ST_SetSRID(ST_Point(%s::float,%s::float),srid),4326)";
	private static final String SRS_TABLE = "calc.srs";
	private static final String SRS_ID_COLUMN = "srs_id";
	
	@Override
	protected void execute() throws Throwable {
		Workspace ws = getWorkspace();
		List<Entity> entities = ws.getEntities();
		for (Entity entity : entities) {
			if (entity.isGeoreferenced()) {
				String dataTable = quote(entity.getDataTable());
				createLocationColumn(dataTable);
				if ( entity.getLocationColumn() == null ) {
					String xColumn = quote(entity.getXColumn());
					String yColumn = quote(entity.getYColumn());
					String srsColumn = quote(entity.getSrsColumn());
					convertCoordinates(dataTable, xColumn, yColumn, srsColumn);
				} else {
					copyLocationColumn(dataTable);
				}
			}
		}
	}

	private void convertCoordinates(String dataTable, String xColumn, String yColumn, String srsColumn) {
		String expr = String.format(TO_WGS84, xColumn, yColumn);
		psql()
			.update(dataTable+" f")
			.set(LOCATION_COLUMN + "=" + expr)
			.from(SRS_TABLE)
			.where(SRS_TABLE+"."+SRS_ID_COLUMN+" = f."+srsColumn)
			.execute();
	}

	private void createLocationColumn(String dataTable) {
		psql()
			.alterTable(dataTable)
			.addColumn(LOCATION_COLUMN, Psql.POINT4326)
			.execute();
	}

	private void copyLocationColumn(String dataTable) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not yet implemented");
	}

		
}