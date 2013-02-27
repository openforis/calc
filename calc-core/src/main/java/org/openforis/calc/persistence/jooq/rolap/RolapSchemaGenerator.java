package org.openforis.calc.persistence.jooq.rolap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import mondrian.olap.MondrianDef.Cube;
import mondrian.olap.MondrianDef.Dimension;
import mondrian.olap.MondrianDef.Hierarchy;
import mondrian.olap.MondrianDef.Level;
import mondrian.olap.MondrianDef.Schema;

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

	private String adminRole;
	private String userRole;
	private String aoiDimensionName;
	private String aoiCaption;
	private String stratumCaption;
	private String sesuDimensionName;
	private String sesuCaption;
	private String dbSchema;
	
	private SurveyMetadata survey;
	private Collection<ObservationUnitMetadata> units;
	
	private Schema schema;
	private List<Dimension> sharedDimensions;
	private List<RolapTable<?>> dbTables;

	
	public RolapSchemaGenerator(SurveyMetadata surveyMetadata) {
		this.survey = surveyMetadata;
		this.units = surveyMetadata.getObservationMetadata();
		adminRole = "ROLE_ADMIN";
		userRole = "ROLE_USER";
		aoiDimensionName = "AOI";
		aoiCaption = "Areas of Interest";
		stratumCaption = "Stratum";
		sesuDimensionName = "SESU";
		sesuCaption = "Socioeconomic SU";
		dbSchema = surveyMetadata.getSurveyName();
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
	
	public String getAoiDimensionName() {
		return aoiDimensionName;
	}

	public void setAoiDimensionName(String aoiDimensionName) {
		this.aoiDimensionName = aoiDimensionName;
	}

	public String getDatabaseSchema() {
		return dbSchema;
	}

	public void setDatabaseSchema(String name) {
		this.dbSchema = name;
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
		dbTables = new ArrayList<RolapTable<?>>();
		
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
		initVariableDimensions();
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
		if ( table instanceof HierarchicalDimensionTable ) {
			level.parentColumn = ((HierarchicalDimensionTable) table).PARENT_ID.getName();
		}
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

	private static Hierarchy createHierarchy(String name, Level... levels) {
		Hierarchy hier = new Hierarchy();
		hier.name = name;
		hier.visible = true;
		hier.hasAll = false;
		hier.levels = levels;
		return hier;
	}
	// AOI DIMENSIONS
	
	private void initAoiDimensions() {
		List<AoiHierarchyMetadata> hiers = survey.getAoiHierarchyMetadata();
		AoiHierarchyMetadata hierMetadata = hiers.get(0);
		Hierarchy hier = createAoiDimensionHierarchy(hierMetadata);
		Dimension dim = createDimension(aoiDimensionName, aoiCaption, hier);
		// TODO Multiple AOI hierarchies
		sharedDimensions.add(dim);
	}

	private Hierarchy createAoiDimensionHierarchy(AoiHierarchyMetadata hierMetadata) {
		String name = toMdxName(hierMetadata.getAoiHierarchyName());
		List<AoiHierarchyLevelMetadata> levelMeta = hierMetadata.getLevelMetadata();
		Level[] levels = new Level[levelMeta.size()];
		for (int i = 0; i < levelMeta.size(); i++) {
			AoiHierarchyLevelMetadata level = levelMeta.get(i);
			AoiDimensionTable table = new AoiDimensionTable(dbSchema, level);
			String levelName = toMdxName(table.getName());
			levels[i] = createDimensionHierarchyLevel(table, levelName);
			dbTables.add(table);
		}
		return createHierarchy(name, levels); 
	}

	// SAMPLING DESIGN DIMENSIONS
	
	private void initSamplingDesignDimensions() {
		ClusterDimensionTable clusterTable = new ClusterDimensionTable(dbSchema);
		dbTables.add(clusterTable);
		initStratumDimension();
		initSesuDimension(clusterTable);
		initPlotDimensions(clusterTable);
	}

	private void initStratumDimension() {
		StratumDimensionTable stratumTable = new StratumDimensionTable(dbSchema);
		dbTables.add(stratumTable);
		
		String stratumMdxName = toMdxName(stratumTable.getName());
		Level level = createDimensionHierarchyLevel(stratumTable, stratumMdxName);
		Hierarchy hier = createHierarchy(stratumMdxName, level);
		Dimension dim = createDimension(stratumMdxName, stratumCaption, hier);
		sharedDimensions.add(dim);
	}
	
	private void initSesuDimension(DimensionTable table) {
		String mdxName = toMdxName(table.getName());
		Level level = createDimensionHierarchyLevel(table, mdxName);
		Hierarchy hier = createHierarchy(mdxName, level);
		Dimension dim = createDimension(sesuDimensionName, sesuCaption, hier);
		sharedDimensions.add(dim);
	}
	
	private void initPlotDimensions(DimensionTable clusterTable) {
		for ( ObservationUnitMetadata unit : units ) {
			if ( unit.isPlot() ) {
				Hierarchy hier = createPlotDimensionHierarchy(unit, clusterTable);
				String dimName = toMdxName(unit.getObsUnitName());
				Dimension dim = createDimension(dimName, unit.getObsUnitLabel(), hier);
				sharedDimensions.add(dim);
			}
		}
	}

	private Hierarchy createPlotDimensionHierarchy(ObservationUnitMetadata unit, DimensionTable clusterTable) {
		String name = toMdxName(unit.getObsUnitName());
		// TODO exclude cluster if not clustered design
		Level clusterLevel = createClusterDimensionHierarchyLevel(unit, clusterTable);
		Level plotLevel = createPlotDimensionHierarchyLevel(unit);
		return createHierarchy(name, clusterLevel, plotLevel);
	}

	private Level createClusterDimensionHierarchyLevel(ObservationUnitMetadata unit, DimensionTable clusterTable) {
		String tableName = clusterTable.getName();
		String levelName = toMdxName(tableName);
		Level level = createDimensionHierarchyLevel(clusterTable, levelName);
		return level;
	}

	private Level createPlotDimensionHierarchyLevel(ObservationUnitMetadata unit) {
		PlotDimensionTable table = new PlotDimensionTable(dbSchema, unit);
		String tableName = table.getName();
		String levelName = toMdxName(tableName);
		dbTables.add(table);
		Level level = createDimensionHierarchyLevel(table, levelName);
		return level;
	}

	// VARIABLE DIMENSIONS
	
	private void initVariableDimensions() {
		for ( ObservationUnitMetadata obsUnitMetadata : units ) {
			Collection<VariableMetadata> vars = obsUnitMetadata.getVariableMetadata();
			for ( VariableMetadata var : vars ) {
				if ( var.isCategorical() && var.isForAnalysis() ) {
					String name = getVariableDimensionName(var);
					Hierarchy hier = createVariableDimensionHierarchy(var);
					Dimension dim = createDimension(name, var.getVariableLabel(), hier);
					sharedDimensions.add(dim);
				}
			}
		}
	}

	private Hierarchy createVariableDimensionHierarchy(VariableMetadata var) {
		String name = getVariableDimensionName(var);
		DimensionTable table = new CategoryDimensionTable(dbSchema, var);
		String levelName = toMdxName(table.getName());
		Level level = createDimensionHierarchyLevel(table, levelName);
		dbTables.add(table);
		return createHierarchy(name, level);
	}

	////////// CUBES
	
	private void initCubes() {
		List<Cube> cubes = new ArrayList<Cube>();
		for ( ObservationUnitMetadata unit : units ) {
			if( unit.isPlot() || unit.hasNumericVariablesForAnalysis() ) {
				CubeGenerator cubeGen = CubeGenerator.createInstance(dbSchema, unit);
				Cube cube = cubeGen.createCube();
				cubes.add(cube);
				dbTables.addAll(cubeGen.getDatabaseTables());
			}
		}
		schema.cubes = cubes.toArray(new Cube[0]);
	}
}
