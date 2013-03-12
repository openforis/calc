package org.openforis.calc.persistence.jooq.rolap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
import org.openforis.calc.model.TaxonomicChecklistMetadata;
import org.openforis.calc.model.VariableMetadata;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public class RolapSchemaGenerator {

    // IN
	private String adminRole;
	private String userRole;
	private String stratumCaption;
	private String sesuDimensionName;
	private String sesuCaption;
	private String databaseSchema;
	private SurveyMetadata survey;
	private Collection<ObservationUnitMetadata> units;

	// INTERNAL
//	private Collection<Dimension> aoiDimensions;
	private Collection<Dimension> sharedDimensions;
	private Collection<Cube> cubes;
	private MondrianDefFactory mdf; 
	
	// OUT
	private Schema schema;
	private List<RolapTable> dbTables;


	
	public RolapSchemaGenerator(SurveyMetadata surveyMetadata, String databaseSchema) {
		this.survey = surveyMetadata;
		this.units = surveyMetadata.getObservationMetadata();
		this.adminRole = "ROLE_ADMIN";
		this.userRole = "ROLE_USER";
		this.stratumCaption = "Stratum";
		this.sesuDimensionName = "SESU";
		this.sesuCaption = "Socioeconomic SU";
		this.databaseSchema = databaseSchema;
		this.mdf = new MondrianDefFactory(databaseSchema);
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
	
//	public List<Dimension> getAoiDimensions() {
//		return Collections.unmodifiableList(aoiDimensions);
//	}

	public String getDatabaseSchema() {
		return databaseSchema;
	}

	synchronized
	public RolapSchemaDefinition generateDefinition() {
		dbTables = new ArrayList<RolapTable>();
//		aoiDimensions = new ArrayList<Dimension>();
				
		initSharedDimensions();
		initCubes();
//		initRoles();

		initSchema();		

		return new RolapSchemaDefinition(schema, dbTables);
	}


	///////////// SCHEMA
	
	private void initSchema() {
		String surveyName = survey.getSurveyName();
		schema = mdf.createSchema(surveyName, sharedDimensions, cubes);
	}
	
	private void initSharedDimensions() {
		sharedDimensions = new ArrayList<Dimension>();
		initAoiDimensions();
		initSamplingDesignDimensions();
		// TODO species dimension
		initSpeciesDimensions();
		initUserDefinedDimensions();
	}

	MondrianDefFactory getMondrianDefFactory() {
		return mdf;
	}

	////////////// SHARED DIMENSIONS

	// AOI DIMENSIONS
	
	private void initAoiDimensions() {
		List<AoiHierarchyMetadata> hiers = survey.getAoiHierarchyMetadata();
		for (AoiHierarchyMetadata hierMetadata : hiers) {
			Hierarchy hier = createAoiDimensionHierarchy(hierMetadata);
			String name = hierMetadata.getAoiHierarchyName();
			String caption = hierMetadata.getAoiHierarchyLabel();
			Dimension dim = mdf.createDimension(name, caption, hier);		
			sharedDimensions.add(dim);
		}
	}

	private Hierarchy createAoiDimensionHierarchy(AoiHierarchyMetadata hier) {
		List<AoiHierarchyLevelMetadata> levelMeta = hier.getLevelMetadata();
		List<Level> levels = new ArrayList<Level>();
		AoiDimensionTable lastTable = null; 
		for (int i = 0; i < levelMeta.size(); i++) {
			AoiHierarchyLevelMetadata level = levelMeta.get(i);
			AoiDimensionTable table = new AoiDimensionTable(databaseSchema, level, lastTable);
			String tableName = table.getName();
			Level l = mdf.createLevel(tableName, table.getDenormalizedIdColumn(), table.getDenormalizedLabelColumn());
			levels.add(l);
			dbTables.add(table);
			lastTable = table;
		}
		String name = hier.getAoiHierarchyName();
		View joinView = mdf.createJoinView(lastTable, name);
		return mdf.createHierarchy(name, false, joinView, levels);
	}

	// SAMPLING DESIGN DIMENSIONS
	
	private void initSamplingDesignDimensions() {
		ClusterDimensionTable clusterTable = new ClusterDimensionTable(databaseSchema, survey.getSurveyId());
		dbTables.add(clusterTable);
		initStratumDimension();
		initSesuDimension(clusterTable);
		initPlotDimensions(clusterTable);
		initSpecimenDimensions(clusterTable);
	}

	private void initStratumDimension() {
		StratumDimensionTable stratumTable = new StratumDimensionTable(databaseSchema, survey.getSurveyId());
		dbTables.add(stratumTable);
		
		String tableName = stratumTable.getName();
		Level level = mdf.createLevel(tableName, stratumTable, stratumTable.ID, stratumTable.LABEL);
		Hierarchy hier = mdf.createHierarchy(tableName, true, tableName, level);
		Dimension dim = mdf.createDimension(tableName, stratumCaption, hier);
		sharedDimensions.add(dim);
	}
	
	private void initSesuDimension(DimensionTable table) {
		Level level = mdf.createLevel(table.getName(), table, table.ID, table.LABEL);
		Hierarchy hier = mdf.createHierarchy(table.getName(), true, table.getName(), level);
		Dimension dim = mdf.createDimension(sesuDimensionName, sesuCaption, hier);
		sharedDimensions.add(dim);
	}
	
	private void initPlotDimensions(ClusterDimensionTable clusterTable) {
		for ( ObservationUnitMetadata unit : units ) {
			if ( unit.isPlot() ) {
				Hierarchy hier = createPlotDimensionHierarchy(unit, clusterTable);
				Dimension dim = mdf.createDimension(unit.getDimensionTableName(), unit.getObsUnitLabel(), hier);
				sharedDimensions.add(dim);
			}
		}
	}

	private Hierarchy createPlotDimensionHierarchy(ObservationUnitMetadata unit, ClusterDimensionTable clusterTable) {
		String clusterLevelName = clusterTable.getName();
		Level clusterLevel = mdf.createLevel(clusterLevelName, clusterTable.getDenormalizedIdColumn(), clusterTable.getDenormalizedLabelColumn());
		PlotDimensionTable plotTable = new PlotDimensionTable(databaseSchema, unit, clusterTable);
		dbTables.add(plotTable);
		String plotLevelName = plotTable.getName();
		Level plotLevel = mdf.createLevel(plotLevelName, plotTable.getDenormalizedIdColumn(), plotTable.getDenormalizedLabelColumn());
		// TODO exclude cluster if not clustered design
		View joinView = mdf.createJoinView(plotTable, unit.getObsUnitName());
		Hierarchy hier = mdf.createHierarchy(unit.getObsUnitName(), true, joinView, clusterLevel, plotLevel);
		
		return hier;
	}
	
	private void initSpecimenDimensions(ClusterDimensionTable clusterTable) {
		for ( ObservationUnitMetadata unit : units ) {
			if ( unit.isSpecimen()  ) {
				Hierarchy hier = createSpecimenDimensionHierarchy(unit, clusterTable);
				Dimension dim = mdf.createDimension(unit.getDimensionTableName(), unit.getObsUnitLabel(), hier);
				sharedDimensions.add(dim);
			}
		}
	}
	
	private Hierarchy createSpecimenDimensionHierarchy(ObservationUnitMetadata unit, ClusterDimensionTable clusterTable) {
		String clusterLevelName = clusterTable.getName();
		Level clusterLevel = mdf.createLevel(clusterLevelName, clusterTable.getDenormalizedIdColumn(), clusterTable.getDenormalizedLabelColumn());
		PlotDimensionTable plotTable = new PlotDimensionTable(databaseSchema, unit.getObsUnitParent(), clusterTable);
//		dbTables.add(plotTable);
		String plotLevelName = plotTable.getName();
		Level plotLevel = mdf.createLevel(plotLevelName, plotTable.getDenormalizedIdColumn(), plotTable.getDenormalizedLabelColumn());
		
		SpecimenDimensionTable specimenTable = new SpecimenDimensionTable(databaseSchema, unit, plotTable);
		dbTables.add(specimenTable);
		
		String specimenLevelName = specimenTable.getName();
		Level specimenLevel = mdf.createLevel(specimenLevelName , specimenTable.getDenormalizedIdColumn(), specimenTable.getDenormalizedLabelColumn());
		
		// TODO exclude cluster if not clustered design
		View joinView = mdf.createJoinView(specimenTable, unit.getObsUnitName());
		Hierarchy hier = mdf.createHierarchy(unit.getObsUnitName(), true, joinView, clusterLevel, plotLevel, specimenLevel);
		
		return hier;
	}
	
	// SPECIES DIMENSIONS
	private void initSpeciesDimensions() {
		List<TaxonomicChecklistMetadata> checklists = getSurveyMetadata().getTaxonomicChecklists();
		for ( TaxonomicChecklistMetadata checklist : checklists ) {
			//TODO Create hierarchical taxon check lists 
			TaxonDimensionTable table = new TaxonDimensionTable(databaseSchema, checklist, null);
			dbTables.add(table);
			
			String tableName = table.getName();
			Level level = mdf.createLevel(tableName, table.ID.getName(), table.LABEL.getName());
			Hierarchy hier = mdf.createHierarchy(tableName, true, tableName, level);
			Dimension dim = mdf.createDimension(tableName, MondrianDefFactory.toMdxName(tableName), hier);
			sharedDimensions.add(dim);
		}
		
	}
	// USER-DEFINED DIMENSIONS
	
	private void initUserDefinedDimensions() {
		for ( ObservationUnitMetadata obsUnitMetadata : units ) {
			Collection<VariableMetadata> vars = obsUnitMetadata.getVariableMetadata();
			for ( VariableMetadata var : vars ) {
				if ( var.isCategorical() && var.isForAnalysis() ) {
					Hierarchy hier = createUserDefinedDimensionHierarchy(var);
					String name = var.getVariableName();
					String caption = var.getVariableLabel();
					Dimension dim = mdf.createDimension(name, caption, hier);
					sharedDimensions.add(dim);
				}
			}
		}
	}

	private Hierarchy createUserDefinedDimensionHierarchy(VariableMetadata var) {
		DimensionTable table = new CategoryDimensionTable(databaseSchema, var);
		String tableName = table.getName();
		Level level = mdf.createLevel(tableName, table, table.ID, table.LABEL);
		dbTables.add(table);
		return mdf.createHierarchy(var.getVariableName(), true, tableName, level);
	}

	////////// CUBES
	
	private void initCubes() {
		cubes = new ArrayList<Cube>();
		for ( ObservationUnitMetadata unit : units ) {
			if( unit.isPlot() || unit.hasNumericVariablesForAnalysis() ) {
				RolapCubeGenerator cubeGen = RolapCubeGenerator.createInstance(this, unit);
				Cube cube = cubeGen.createCube();
				cubes.add(cube);
				dbTables.addAll(cubeGen.getDatabaseTables());
			}
		}
	}
}
