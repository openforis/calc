package org.openforis.calc.persistence.jooq.rolap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import mondrian.olap.MondrianDef;
import mondrian.olap.MondrianDef.Cube;
import mondrian.olap.MondrianDef.Dimension;
import mondrian.olap.MondrianDef.Hierarchy;
import mondrian.olap.MondrianDef.Level;
import mondrian.olap.MondrianDef.Schema;
import mondrian.olap.MondrianDef.View;

import org.openforis.calc.model.AoiHierarchyLevelMetadata;
import org.openforis.calc.model.AoiHierarchyMetadata;
import org.openforis.calc.model.ObservationUnitMetadata;
import org.openforis.calc.model.SurveyMetadata;
import org.openforis.calc.model.VariableMetadata;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public class RolapSchemaGenerator {
	
//	private static final String GRANT_ACCESS_ALL = "all";
//	private static final String GRANT_ACCESS_NONE = "none";
    private static final String TYPE_STANDARD_DIMENSION = "StandardDimension";

    // IN
	private String adminRole;
	private String userRole;
	private String aoiCaption;
	private String stratumCaption;
	private String sesuDimensionName;
	private String sesuCaption;
	private String databaseSchema;
	private SurveyMetadata survey;
	private Collection<ObservationUnitMetadata> units;

	// TEMP
	private List<Dimension> aoiDimensions;
	private List<Dimension> sharedDimensions;

	// OUT
	private Schema schema;
	private List<RolapTable> dbTables;

	
	public RolapSchemaGenerator(SurveyMetadata surveyMetadata) {
		this.survey = surveyMetadata;
		this.units = surveyMetadata.getObservationMetadata();
		adminRole = "ROLE_ADMIN";
		userRole = "ROLE_USER";
		aoiCaption = "Areas of Interest";
		stratumCaption = "Stratum";
		sesuDimensionName = "SESU";
		sesuCaption = "Socioeconomic SU";
		databaseSchema = surveyMetadata.getSurveyName();
	}
	
	public SurveyMetadata getSurveyMetadata() {
		return survey;
	}
	
	public String getAdminRole() {
		return adminRole;
	}

	public void setAdminRole(String adminRole) {
		this.adminRole = adminRole;
	}

	public String getUserRole() {
		return userRole;
	}

	public void setUserRole(String userRole) {
		this.userRole = userRole;
	}
	
	public List<Dimension> getAoiDimensions() {
		return Collections.unmodifiableList(aoiDimensions);
	}

	public String getDatabaseSchema() {
		return databaseSchema;
	}

	public void setDatabaseSchema(String name) {
		this.databaseSchema = name;
	}

	static String getVariableDimensionName(VariableMetadata var) {
		return toMdxName(var.getVariableName());
	}

	static String toMdxName(String name) {
		StringBuilder sb = new StringBuilder();
		String[] s = name.split("[_\\-]");

		for ( int i = 0 ; i < s.length ; i++ ) {
			String string = s[i];
			sb.append(string.substring(0, 1).toUpperCase());
			sb.append(string.substring(1, string.length()));
		}

		return sb.toString();
	}

	synchronized
	public RolapSchemaDefinition generateDefinition() {
		dbTables = new ArrayList<RolapTable>();
		aoiDimensions = new ArrayList<Dimension>();
		
		initSchema();
		initSharedDimensions();
		initCubes();
//		initRoles();
		
		return new RolapSchemaDefinition(schema, dbTables);
	}

	///////////// SCHEMA 
	
	private void initSchema() {
		schema = new Schema();
		String surveyName = survey.getSurveyName();
		schema.name = toMdxName(surveyName);
	}

	private void initSharedDimensions() {
		sharedDimensions = new ArrayList<Dimension>();
		initAoiDimensions();
		initSamplingDesignDimensions();
		// TODO species dimension
//		initSpeciesDimension();
		initUserDefinedDimensions();
		schema.dimensions = sharedDimensions.toArray(new Dimension[0]);
	}

	////////////// SHARED DIMENSIONS

	private static Level createDimensionHierarchyLevel(DimensionTable table, String levelName) {
		Level level = new Level();
		level.name = levelName;
		level.visible = true;
		level.table = table.getName();
		level.column = table.ID.getName(); 
		level.nameColumn = table.LABEL.getName();
		level.type = "String";
		level.uniqueMembers = false;
		level.levelType = "Regular";
		level.hideMemberIf = "Never";
		return level;
	}


	private static Dimension createDimension(String name, String caption, Hierarchy... hierarchies) {
		Dimension dim = new Dimension();
		dim.type = TYPE_STANDARD_DIMENSION;
		dim.visible = true;
		dim.highCardinality = false;
		dim.name = name;
		dim.caption = caption;
		dim.hierarchies = hierarchies;
		return dim;
	}

	private Hierarchy createHierarchy(String name, String table, Level... levels) {
		Hierarchy hier = new Hierarchy();
		hier.name = name;
		hier.visible = true;
		hier.hasAll = false;		
		hier.levels = levels;
		if ( table != null ) {
			hier.relation = new MondrianDef.Table(databaseSchema, table, null, null);
		}
		return hier;
	}
	
	// AOI DIMENSIONS
	
	private void initAoiDimensions() {
		List<AoiHierarchyMetadata> hiers = survey.getAoiHierarchyMetadata();
		for (AoiHierarchyMetadata hierMetadata : hiers) {
			Hierarchy hier = createAoiDimensionHierarchy(hierMetadata);
			String aoiDimensionName = toMdxName( hierMetadata.getAoiHierarchyName() ); //hier.getName();
			Dimension dim = createDimension(aoiDimensionName, aoiCaption, hier);		
			sharedDimensions.add(dim);
		}
	}

	private Hierarchy createAoiDimensionHierarchy(AoiHierarchyMetadata hierMetadata) {
		String name = toMdxName(hierMetadata.getAoiHierarchyName());
		List<AoiHierarchyLevelMetadata> levelMeta = hierMetadata.getLevelMetadata();
		Level[] levels = new Level[levelMeta.size()];
		AoiDimensionTable lastTable = null; 
		for (int i = 0; i < levelMeta.size(); i++) {
			AoiHierarchyLevelMetadata level = levelMeta.get(i);
			AoiDimensionTable table = new AoiDimensionTable(databaseSchema, level, lastTable);
			String tableName = table.getName();
			String levelName = toMdxName(tableName);
			levels[i] = createDimensionHierarchyLevel(table, levelName);
			levels[i].table = null;
			levels[i].column = table.getDenormalizedIdColumn(); 
			levels[i].nameColumn = table.getDenormalizedLabelColumn();
			dbTables.add(table);
			lastTable = table;
		}
		Hierarchy hier = createHierarchy(name, null, levels);
		hier.relation = createJoinView(lastTable, hierMetadata.getAoiHierarchyName());
		return hier;
	}

	private View createJoinView(HierarchicalDimensionTable leafTable, String viewName) {
		View view = new MondrianDef.View();
		MondrianDef.SQL mondrianSql = new MondrianDef.SQL();
		mondrianSql.dialect = "generic";
		mondrianSql.cdata = leafTable.getDenormalizedSelectSql();
		view.selects = new MondrianDef.SQL[] {mondrianSql};
		view.alias = viewName;
		return view;
	}

	// SAMPLING DESIGN DIMENSIONS
	
	private void initSamplingDesignDimensions() {
		ClusterDimensionTable clusterTable = new ClusterDimensionTable(databaseSchema, survey.getSurveyId());
		dbTables.add(clusterTable);
		initStratumDimension();
		initSesuDimension(clusterTable);
		initPlotDimensions(clusterTable);
	}

	private void initStratumDimension() {
		StratumDimensionTable stratumTable = new StratumDimensionTable(databaseSchema, survey.getSurveyId());
		dbTables.add(stratumTable);
		
		String tableName = stratumTable.getName();
		String stratumMdxName = toMdxName(tableName);
		Level level = createDimensionHierarchyLevel(stratumTable, stratumMdxName);
		Hierarchy hier = createHierarchy(stratumMdxName, tableName, level);
		Dimension dim = createDimension(stratumMdxName, stratumCaption, hier);
		sharedDimensions.add(dim);
	}
	
	private void initSesuDimension(DimensionTable table) {
		String mdxName = toMdxName(table.getName());
		Level level = createDimensionHierarchyLevel(table, mdxName);
		Hierarchy hier = createHierarchy(mdxName, table.getName(), level);
		Dimension dim = createDimension(sesuDimensionName, sesuCaption, hier);
		sharedDimensions.add(dim);
	}
	
	private void initPlotDimensions(ClusterDimensionTable clusterTable) {
		for ( ObservationUnitMetadata unit : units ) {
			if ( unit.isPlot() ) {
				Hierarchy hier = createPlotDimensionHierarchy(unit, clusterTable);
				String dimName = toMdxName(unit.getObsUnitName());
				Dimension dim = createDimension(dimName, unit.getObsUnitLabel(), hier);
				sharedDimensions.add(dim);
			}
		}
	}

	private Hierarchy createPlotDimensionHierarchy(ObservationUnitMetadata unit, ClusterDimensionTable clusterTable) {
		String name = toMdxName(unit.getObsUnitName());
		Level clusterLevel = createDimensionHierarchyLevel(clusterTable, toMdxName(clusterTable.getName()));
		clusterLevel.table = null;
		clusterLevel.column = clusterTable.getDenormalizedIdColumn(); 
		clusterLevel.nameColumn = clusterTable.getDenormalizedLabelColumn();
		
		PlotDimensionTable plotTable = new PlotDimensionTable(databaseSchema, unit, clusterTable);
		dbTables.add(plotTable);
		Level plotLevel = createDimensionHierarchyLevel(plotTable, toMdxName(plotTable.getName()));
		plotLevel.table = null;
		plotLevel.column = plotTable.getDenormalizedIdColumn(); 
		plotLevel.nameColumn = plotTable.getDenormalizedLabelColumn();

		// TODO exclude cluster if not clustered design
		Hierarchy hier = createHierarchy(name, null, clusterLevel, plotLevel);
		hier.relation = createJoinView(plotTable, unit.getObsUnitName());
		return hier;
	}

	// USER-DEFINED DIMENSIONS
	
	private void initUserDefinedDimensions() {
		for ( ObservationUnitMetadata obsUnitMetadata : units ) {
			Collection<VariableMetadata> vars = obsUnitMetadata.getVariableMetadata();
			for ( VariableMetadata var : vars ) {
				if ( var.isCategorical() && var.isForAnalysis() ) {
					String name = getVariableDimensionName(var);
					Hierarchy hier = createUserDefinedDimensionHierarchy(var);
					Dimension dim = createDimension(name, var.getVariableLabel(), hier);
					sharedDimensions.add(dim);
				}
			}
		}
	}

	private Hierarchy createUserDefinedDimensionHierarchy(VariableMetadata var) {
		String name = getVariableDimensionName(var);
		DimensionTable table = new CategoryDimensionTable(databaseSchema, var);
		String tableName = table.getName();
		String levelName = toMdxName(tableName);
		Level level = createDimensionHierarchyLevel(table, levelName);
		dbTables.add(table);
		return createHierarchy(name, tableName, level);
	}

	////////// CUBES
	
	private void initCubes() {
		List<Cube> cubes = new ArrayList<Cube>();
		for ( ObservationUnitMetadata unit : units ) {
			if( unit.isPlot() || unit.hasNumericVariablesForAnalysis() ) {
				RolapCubeGenerator cubeGen = RolapCubeGenerator.createInstance(this, unit);
				Cube cube = cubeGen.createCube();
				cubes.add(cube);
				dbTables.addAll(cubeGen.getDatabaseTables());
			}
		}
		schema.cubes = cubes.toArray(new Cube[0]);
	}
}
