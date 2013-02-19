/**
 * 
 */
package org.openforis.calc.persistence.jooq;

import static org.openforis.calc.persistence.jooq.Tables.AOI;
import static org.openforis.calc.persistence.jooq.Tables.CLUSTER;
import static org.openforis.calc.persistence.jooq.Tables.PLOT_SECTION_VIEW;
import static org.openforis.calc.persistence.jooq.Tables.STRATUM;

import java.sql.Types;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.ddlutils.Platform;
import org.apache.ddlutils.PlatformFactory;
import org.apache.ddlutils.PlatformInfo;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;
import org.openforis.calc.model.ObservationUnitMetadata;
import org.openforis.calc.model.SurveyMetadata;
import org.openforis.calc.model.VariableMetadata;
import org.openforis.calc.persistence.jooq.tables.Aoi;
import org.openforis.calc.persistence.jooq.tables.Cluster;
import org.openforis.calc.persistence.jooq.tables.PlotSectionView;
import org.openforis.calc.persistence.jooq.tables.Stratum;

/**
 * @author Mino Togna
 * 
 */
public class FactTableGenerator {

	private static final int GEOMETRY_POINT_TYPE_CODE = 1111;
	private static final String GEOMETRY_POINT_TYPE = "geometry(Point,4326)";

	private static final Stratum S = STRATUM;
	private static final Aoi A = AOI;
	private static final Cluster C = CLUSTER;
	private static final PlotSectionView P = PLOT_SECTION_VIEW;
	private static Map<String, Integer> PLOT_COLUMNS;

	private ObservationUnitMetadata obsUnitMetadata;
	private DataSource dataSource;
	private Platform platform;

	private Database database;
	private Table table;

	public FactTableGenerator(ObservationUnitMetadata obsUnitMetadata, DataSource dataSource) {
		this.obsUnitMetadata = obsUnitMetadata;
		this.dataSource = dataSource;

	}

	public void generate() {
		initPlatform();
		initDatabase();
		initTable();
		initColumns();

		drop();
		create();
	}

	public String getTableName() {
		return table.getName();
	}

	private void drop() {
		platform.dropTable(database, table, true);

	}

	private void create() {
		platform.createTables(database, false, false);
	}

	private void initPlatform() {
		platform = PlatformFactory.createNewPlatformInstance(dataSource);
		PlatformInfo platformInfo = platform.getPlatformInfo();
		platformInfo.addNativeTypeMapping(GEOMETRY_POINT_TYPE_CODE, GEOMETRY_POINT_TYPE);
	}

	private void initColumns() {
		addColumn("id", Types.INTEGER, true);

		// TODO for specimen
		if ( obsUnitMetadata.isTypePlot() ) {
			for ( String c : PLOT_COLUMNS.keySet() ) {
				addColumn(c, PLOT_COLUMNS.get(c));
			}
		}

		Collection<VariableMetadata> vars = obsUnitMetadata.getVariableMetadata();
		for ( VariableMetadata var : vars ) {
			if ( var.isForAnalysis() ) {
				String varName = var.getVariableName();
				if ( var.isNumeric() ) {
					addColumn(varName, Types.NUMERIC);
				} else if ( var.isCategorical() ) {
					addColumn(varName, Types.INTEGER);
				}
			}
		}
	}

	private void addColumn(String name, int typeCode, boolean autoIncrement) {
		Column col = new Column();
		col.setName(name);
		col.setTypeCode(typeCode);
		if ( autoIncrement ) {
			col.setAutoIncrement(autoIncrement);
		}
		table.addColumn(col);
	}

	private void addColumn(String name, int typeCode) {
		addColumn(name, typeCode, false);
	}

	private void initTable() {
		table = new Table();

		SurveyMetadata surveyMetadata = obsUnitMetadata.getSurveyMetadata();
		String surveyName = surveyMetadata.getSurveyName();
		String obsUnitName = obsUnitMetadata.getObsUnitName();

		table.setName(surveyName + "." + obsUnitName + "_fact");

		database.addTable(table);
	}

	private void initDatabase() {
		database = new Database();
	}

	static {
		PLOT_COLUMNS = new LinkedHashMap<String, Integer>();
		
		// plot dimensions
		PLOT_COLUMNS.put(A.AOI_ID.getName(), Types.INTEGER);
		PLOT_COLUMNS.put(S.STRATUM_ID.getName(), Types.INTEGER);
		PLOT_COLUMNS.put(C.CLUSTER_ID.getName(), Types.INTEGER);
		PLOT_COLUMNS.put(P.PLOT_SECTION_ID.getName(), Types.INTEGER);

		// plot locations
		PLOT_COLUMNS.put(P.PLOT_GPS_READING.getName(), GEOMETRY_POINT_TYPE_CODE);
		PLOT_COLUMNS.put(P.PLOT_ACTUAL_LOCATION.getName(), GEOMETRY_POINT_TYPE_CODE);
		PLOT_COLUMNS.put(P.PLOT_LOCATION.getName(), GEOMETRY_POINT_TYPE_CODE);
		
		// plot measures
		PLOT_COLUMNS.put(P.PLOT_LOCATION_DEVIATION.getName(), Types.NUMERIC);
		PLOT_COLUMNS.put("cnt", Types.NUMERIC);
		PLOT_COLUMNS.put("est_area", Types.NUMERIC);
	}

}
