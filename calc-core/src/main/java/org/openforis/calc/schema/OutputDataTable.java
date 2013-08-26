package org.openforis.calc.schema;

import static org.jooq.impl.SQLDataType.INTEGER;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.TableField;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.AoiHierarchy;
import org.openforis.calc.metadata.AoiHierarchyLevel;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.persistence.postgis.GeodeticCoordinate;
import org.openforis.calc.persistence.postgis.Psql;

/**
 * 
 * @author G. Miceli
 * @author M. Togna
 * 
 */
public class OutputDataTable extends DataTable {

	private static final long serialVersionUID = 1L;

	private static final String LOCATION_COLUMN_NAME = "_location";

	public final TableField<Record, Integer> STRATUM_ID;

	private InputDataTable inputDataTable;

	private Map<AoiHierarchyLevel, Field<Integer>> aoiLevelFieldMap;

	/**
	 * The point location field
	 */
	private Field<GeodeticCoordinate> locationField;

	public OutputDataTable(Entity entity, OutputSchema schema, InputDataTable inputDataTable) {
		super(entity, schema);

		this.inputDataTable = inputDataTable;
		createVariableFields(false);
		this.STRATUM_ID = createField("_stratum_id", INTEGER, this);

		this.aoiLevelFieldMap = new HashMap<AoiHierarchyLevel, Field<Integer>>();
		// Add aoi columns if entity is geo referenced
		if ( entity.isGeoreferenced() ) {
			createAoiFields();
			createLocationField();
		}
	}

	public InputDataTable getInputDataTable() {
		return inputDataTable;
	}

	public Field<Integer> getAoiField(AoiHierarchyLevel aoiLevel) {
		return aoiLevelFieldMap.get(aoiLevel);
	}

	public Field<GeodeticCoordinate> getLocationField() {
		return locationField;
	}

	private void createAoiFields() {
		Entity entity = getEntity();
		Workspace workspace = entity.getWorkspace();
		List<AoiHierarchy> aoiHierarchies = workspace.getAoiHierarchies();
		for ( AoiHierarchy hierarchy : aoiHierarchies ) {
			List<AoiHierarchyLevel> levels = hierarchy.getLevels();
			for ( AoiHierarchyLevel level : levels ) {
				String fkColumn = level.getFkColumn();

				Field<Integer> aoiField = createField(fkColumn, INTEGER, this);
				aoiLevelFieldMap.put(level, aoiField);
			}
		}
	}

	private void createLocationField() {
		locationField = createField(LOCATION_COLUMN_NAME, Psql.GEODETIC_COORDINATE_DATA_TYPE, this);
	}

}
