package org.openforis.calc.persistence;
import static org.openforis.calc.persistence.jooq.Tables.AOI;
import static org.openforis.calc.persistence.jooq.Tables.AOI_HIERARCHY_LEVEL;
import static org.openforis.calc.persistence.jooq.Tables.AOI_HIERARCHY;
import static org.openforis.calc.persistence.jooq.Tables.CLUSTER;
import static org.openforis.calc.persistence.jooq.Tables.GROUND_PLOT_VIEW;
import static org.openforis.calc.persistence.jooq.Tables.PLOT_CATEGORICAL_VALUE_VIEW;
import static org.openforis.calc.persistence.jooq.Tables.PLOT_SECTION_AOI;
import static org.openforis.calc.persistence.jooq.Tables.PLOT_SECTION_VIEW;
import static org.openforis.calc.persistence.jooq.Tables.STRATUM;
import static org.openforis.calc.persistence.jooq.Tables.PLOT_EXPANSION_FACTOR;
import static org.openforis.calc.persistence.jooq.Tables.AOI_STRATUM_VIEW;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Insert;
import org.jooq.JoinType;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SelectQuery;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.impl.Factory;
import org.openforis.calc.model.ObservationUnitMetadata;
import org.openforis.calc.model.SurveyMetadata;
import org.openforis.calc.model.VariableMetadata;
import org.openforis.calc.persistence.jooq.JooqTableGenerator;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.tables.Aoi;
import org.openforis.calc.persistence.jooq.tables.AoiHierarchy;
import org.openforis.calc.persistence.jooq.tables.AoiHierarchyLevel;
import org.openforis.calc.persistence.jooq.tables.AoiStratumView;
import org.openforis.calc.persistence.jooq.tables.Cluster;
import org.openforis.calc.persistence.jooq.tables.FactTable;
import org.openforis.calc.persistence.jooq.tables.GroundPlotView;
import org.openforis.calc.persistence.jooq.tables.PlotCategoricalValueView;
import org.openforis.calc.persistence.jooq.tables.PlotExpansionFactor;
import org.openforis.calc.persistence.jooq.tables.PlotSectionAoi;
import org.openforis.calc.persistence.jooq.tables.PlotSectionView;
import org.openforis.calc.persistence.jooq.tables.Stratum;
import org.openforis.calc.persistence.jooq.tables.records.FactRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author M. Togna
 */
@SuppressWarnings("rawtypes")
@Component
@Transactional
public class PlotFactTableDao extends JooqDaoSupport {

	private static final Stratum S = STRATUM;
	private static final AoiStratumView ASV = AOI_STRATUM_VIEW;
	private static final Aoi A = AOI;
	private static final AoiHierarchyLevel AHL = AOI_HIERARCHY_LEVEL;
	private static final AoiHierarchy AH = AOI_HIERARCHY;
	private static final Cluster C = CLUSTER;
	private static final PlotSectionView P = PLOT_SECTION_VIEW;

	private static final GroundPlotView V = GROUND_PLOT_VIEW;
	private static final PlotSectionAoi PSA = PLOT_SECTION_AOI;
	private static final PlotExpansionFactor PEF = PLOT_EXPANSION_FACTOR;
	
	private static final String COUNT_COLUMN_NAME = "cnt";
	private static final String EST_AREA_COLUMN_NAME = "est_area";

	private static final String[] PLOT_POINTS = new String[] { P.PLOT_GPS_READING.getName(), P.PLOT_ACTUAL_LOCATION.getName(), P.PLOT_LOCATION.getName() };
	private static final String[] PLOT_DIMENSIONS = new String[] { S.STRATUM_ID.getName(), C.CLUSTER_ID.getName(), P.PLOT_SECTION_ID.getName() };
	private static final String[] AGG_STRATUM_AOI_EXCLUDED_DIMENSIONS = new String[] { C.CLUSTER_ID.getName(), P.PLOT_SECTION_ID.getName() };
	private static final String[] PLOT_MEASURES = new String[] { P.PLOT_LOCATION_DEVIATION.getName(), COUNT_COLUMN_NAME, EST_AREA_COLUMN_NAME };
	
	@Autowired
	private JooqTableGenerator jooqTableGenerator;

	@SuppressWarnings("unchecked")
	public PlotFactTableDao() {
		super(null, null);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	synchronized
	public void createPlotFactTable(ObservationUnitMetadata obsUnitMetadata) {
		int surveyId = obsUnitMetadata.getSurveyId();
		//1. Plot fact table
		FactTable plotFactTable = getPlotFactTable(obsUnitMetadata);
		String plotFactTableName = plotFactTable.getName();
		jooqTableGenerator.create(plotFactTable);
		
		//2. Aoi / Stratum aggregation table
		Result<Record> levelRecords = getAoiHierarchyLevelRecords(surveyId);
		FactTable prevLevelTable = plotFactTable;
		String prevLevel = "";
		for ( Record record : levelRecords ) {
			
			String levelName = record.getValue(AHL.AOI_HIERARCHY_LEVEL_NAME);
			String tableName = "agg_"+levelName+"_stratum_" + plotFactTableName;
			FactTable aoiStratumFact = getAggFactTable(prevLevelTable, tableName, prevLevel);
			
			prevLevelTable = aoiStratumFact;
			prevLevel = levelName;
			jooqTableGenerator.create(aoiStratumFact);
		}
				
	}

	@Transactional
	synchronized
	public void populatePlotFactTable(ObservationUnitMetadata obsUnitMetadata) {
		Factory create = getJooqFactory();
		int surveyId = obsUnitMetadata.getSurveyId();
		
		//1. Plot fact table
		FactTable plotFactTable = getPlotFactTable(obsUnitMetadata);
		String plotFactTableName = plotFactTable.getName();
		
		SelectQuery plotFactSelect = getPlotFactSelect(create, obsUnitMetadata);
		Insert<FactRecord> plotFactInsert = getInsertFromSelect(plotFactTable, plotFactSelect);
		
		if ( getLog().isDebugEnabled() ) {
			getLog().debug("Plot fact table insert:");
			getLog().debug(plotFactInsert.toString());
		}		
		plotFactInsert.execute();
		
		Result<Record> levelRecords = getAoiHierarchyLevelRecords(surveyId);
		FactTable prevLevelTable = plotFactTable;
		String prevLevel = "";
		for ( Record record : levelRecords ) {
			
			String levelName = record.getValue(AHL.AOI_HIERARCHY_LEVEL_NAME);
			Integer levelRank = record.getValue(AHL.AOI_HIERARCHY_LEVEL_RANK);
			String tableName = "agg_"+levelName+"_stratum_" + plotFactTableName;
			FactTable aoiStratumFact = getAggFactTable(prevLevelTable, tableName, prevLevel);
			// generate select
			SelectQuery aggAoiStratumSelect = getAggAoiStratumSelect(prevLevelTable, levelName, levelRank, prevLevel, surveyId );
			Insert<FactRecord> insert = getInsertFromSelect(aoiStratumFact, aggAoiStratumSelect);
			if ( getLog().isDebugEnabled() ) {
				getLog().debug(tableName + " insert:");
				getLog().debug(insert.toString());
			}	
			insert.execute();
			
			prevLevelTable = aoiStratumFact;
			prevLevel = levelName;
		}
		
		
		//2. agg plot fact at aoi/stratum level 
//		FactTable aoiStratumPlotFact = getAggAoiStratumPlotFactTable(plotFactTable);
//		
//		SelectQuery aoiStratumSelect = getAggAoiStratumPlotFactSelect(create, plotFactTable, aoiStratumPlotFact);
//		Insert<FactRecord> aoiStratumInsert = getAggAoiStratumPlotFactInsert(create, aoiStratumPlotFact, aoiStratumSelect);
//		
//		if ( getLog().isDebugEnabled() ) {
//			getLog().debug("Aoi Stratum aggregate plot fact table insert:");
//			getLog().debug(aoiStratumInsert.toString());		
//		}
//		
//		aoiStratumInsert.execute();
	}
	
	@SuppressWarnings("unchecked")
	private SelectQuery getAggAoiStratumSelect(FactTable table, String aoiLevelName, int aoiLevelRank, String prevLevel, int surveyId) {
		Factory create = getJooqFactory();
		PlotExpansionFactor e = PEF.as("e");
		AoiStratumView s = ASV.as("s");
		
		SelectQuery select = create.selectQuery();
		
		select.addSelect(s.STRATUM_ID);
		select.addSelect(Factory.coalesce(table.getField(COUNT_COLUMN_NAME).sum(), 0).as(COUNT_COLUMN_NAME));
		select.addSelect(Factory.coalesce(table.getField(COUNT_COLUMN_NAME).sum().mul(e.EXP_FACTOR), s.AREA).as(EST_AREA_COLUMN_NAME) );
		
		
		select.addFrom(table);
		
		select.addJoin(
				e, 
				e.AOI_ID.eq( (Field<Integer>) table.getField(aoiLevelName))
					.and(e.STRATUM_ID.eq( (Field<Integer>) table.getField(S.STRATUM_ID.getName())) )
			);
		select.addJoin(
				s, 
				JoinType.RIGHT_OUTER_JOIN, 
				e.STRATUM_ID.eq( s.STRATUM_ID )
				.and(e.AOI_ID.eq(s.AOI_ID))
			);
		
		select.addGroupBy(s.STRATUM_ID);
		select.addGroupBy(s.AREA);
		select.addGroupBy(e.EXP_FACTOR);
		
		Result<Record> levelRecords = getAoiHierarchyLevelRecords(surveyId, aoiLevelRank);
		Aoi childAoi = null;
		int i = 0;
		List<String> aoiNames = new ArrayList<String>();
		aoiNames.add(prevLevel);
		
		for ( Record record : levelRecords ) {
			
			Aoi a = A.as("a"+(i++));
			int aoiLevelId = record.getValue(AHL.AOI_HIERARCHY_LEVEL_ID);
			String levelName = record.getValue(AHL.AOI_HIERARCHY_LEVEL_NAME);
			aoiNames.add(levelName);
			
			select.addSelect( a.AOI_ID.as(levelName) );
			
			select.addJoin(a, 
					( childAoi == null ) 
					? s.AOI_ID.eq(a.AOI_ID).and(a.AOI_HIERARCHY_LEVEL_ID.eq(aoiLevelId))
					: childAoi.AOI_PARENT_ID.eq( a.AOI_ID )
				);
			
			select.addGroupBy(a.AOI_ID);
			
			childAoi = a;
		}
		List<TableField<FactRecord, Integer>> srcDimensions = table.getDimensionFields();
		List<TableField<FactRecord, Integer>> dimensions = new ArrayList<TableField<FactRecord,Integer>>(srcDimensions.size());
		for ( TableField<FactRecord, Integer> f : srcDimensions ) {
			String fieldName = f.getName();
			if(! (aoiNames.contains(fieldName) || ArrayUtils.contains( PLOT_DIMENSIONS, fieldName) ) ){
				select.addSelect(Factory.coalesce(f, -1).as(fieldName));
				dimensions.add(f);
			}
		}
		
		select.addGroupBy(dimensions);
		return select;
	}
	
	private Insert<FactRecord> getInsertFromSelect(FactTable table, SelectQuery select) {
		Factory create = getJooqFactory();
		
		List<Field<?>> selectFields = select.getFields();
		List<Field<?>> fields = new ArrayList<Field<?>>(selectFields.size());
		for ( Field<?> field : selectFields ) {
			String fieldName = field.getName();
			Field<?> destField = table.getField( fieldName );
			if( destField == null ){
				throw new IllegalArgumentException("Field " + fieldName + " not found in table " + table.getName());
			}
			fields.add( destField );
		}		

		Insert<FactRecord> insert = 
					create.insertInto(table, fields)
					.select(select);
		
		return insert;
	}

	@SuppressWarnings("unchecked")
	@Deprecated
	private SelectQuery getAggAoiStratumPlotFactSelect(Factory create, FactTable plotFactTable, FactTable aoiStratumPlotFact) {
		Table<FactRecord> p = plotFactTable.as("p");
		PlotSectionAoi psa = PSA.as("psa");
		PlotExpansionFactor pef = PEF.as("pef");
		
		SelectQuery select = create.selectQuery();
		List<TableField<FactRecord, Integer>> dims = aoiStratumPlotFact.getDimensionFields();
		List<Field<?>> stratumDims = new ArrayList<Field<?>>();
		for ( TableField<FactRecord, Integer> f : dims ) {
			if(!A.AOI_ID.getName().equals(f.getName())){
				stratumDims.add( p.getField(f.getName()) );
			}
		}
		select.addSelect(psa.AOI_ID);
		select.addSelect(stratumDims);
		select.addSelect(p.getField("cnt").sum().as("cnt"));
		select.addSelect(pef.EXP_FACTOR.mul( p.getField("cnt").sum() ).as("est_area") );
		
		select.addFrom(p);
		select.addJoin(
				psa, 
				JoinType.RIGHT_OUTER_JOIN, 
				psa.PLOT_SECTION_ID.eq( (Field<Integer>) p.getField(psa.PLOT_SECTION_ID.getName()) )
			);
		select.addJoin(
				pef,
				JoinType.JOIN,
				pef.AOI_ID.eq(psa.AOI_ID)
					.and(pef.STRATUM_ID.eq( (Field<Integer>) p.getField(pef.STRATUM_ID.getName())))
			);
		
		select.addGroupBy(psa.AOI_ID);
		select.addGroupBy(stratumDims);
		select.addGroupBy(pef.EXP_FACTOR);
		return select;
	}
	
	private FactTable getAggFactTable(FactTable factTable, String tableName, String... excludeLevel) {
		String[] excludedDimensions = new String[] { C.CLUSTER_ID.getName(), P.PLOT_SECTION_ID.getName() };
		excludedDimensions = ArrayUtils.addAll(excludedDimensions, excludeLevel);
		
		String[] excludedMeasures = new String[] { P.PLOT_LOCATION_DEVIATION.getName() };
		String[] excludedPoints = PLOT_POINTS;

		FactTable aggFactTable = factTable.aggregate(tableName, excludedDimensions, excludedMeasures, excludedPoints);
		return aggFactTable;
	}
	
	private FactTable getPlotFactTable(ObservationUnitMetadata obsUnitMetadata) {
		SurveyMetadata surveyMetadata = obsUnitMetadata.getSurveyMetadata();
		String schema = surveyMetadata.getSurveyName();
		int surveyId = surveyMetadata.getSurveyId();
		String table = obsUnitMetadata.getObsUnitName() + "_fact";
		
		Collection<VariableMetadata> vars = obsUnitMetadata.getVariableMetadata();
		String[] dimensions = ArrayUtils.clone(PLOT_DIMENSIONS);
		
		// add aoi dimensions
		Result<Record> result = getAoiHierarchyLevelRecords(surveyId);
		for ( Record record : result ) {
			String levelName = record.getValue(AHL.AOI_HIERARCHY_LEVEL_NAME);
			dimensions = ArrayUtils.add(dimensions, levelName);
		}		
		
		
		for ( VariableMetadata var : vars ) {
			if( var.isForAnalysis() && var.isCategorical() ) {
				dimensions = ArrayUtils.add(dimensions, var.getVariableName());
			}
		}
		
		return new FactTable(schema, table, PLOT_MEASURES, dimensions, PLOT_POINTS);
	}

	private Result<Record> getAoiHierarchyLevelRecords(int surveyId) {
//		Factory create = getJooqFactory();
//		
//		Result<Record> result = create
//			.select()
//			.from(AHL)
//			.join(AH)
//				.on(AHL.AOI_HIERARCHY_ID.eq(AH.AOI_HIERARCHY_ID))
//			.where( AH.SURVEY_ID.eq(surveyId) )
//			.orderBy(AHL.AOI_HIERARCHY_LEVEL_RANK.desc())
//			.fetch();
//		
//		return result;
		return getAoiHierarchyLevelRecords(surveyId, null);
	}

	private Result<Record> getAoiHierarchyLevelRecords(int surveyId, Integer fromLevel) {
		Factory create = getJooqFactory();
		
		Condition condition = fromLevel != null ? AH.SURVEY_ID.eq(surveyId).and(AHL.AOI_HIERARCHY_LEVEL_RANK.lessOrEqual(fromLevel)) :  AH.SURVEY_ID.eq(surveyId);
		
		Result<Record> result = create
			.select()
			.from(AHL)
			.join(AH)
				.on(AHL.AOI_HIERARCHY_ID.eq(AH.AOI_HIERARCHY_ID))
			.where(  condition )
			.orderBy(AHL.AOI_HIERARCHY_LEVEL_RANK.desc())
			.fetch();
		
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private SelectQuery getPlotFactSelect(Factory create, ObservationUnitMetadata obsUnitMetadata){
		int obsUnitId = obsUnitMetadata.getObsUnitId();
		Integer surveyId = obsUnitMetadata.getSurveyId();
		Collection<VariableMetadata> variables = obsUnitMetadata.getVariableMetadata();	
				
		GroundPlotView p = V.as("p");
		PlotSectionAoi pa = PSA.as("pa");
		Aoi a = A.as("a");
		AoiHierarchyLevel al = AHL.as("al");
		AoiHierarchyLevel al2 = AHL.as("al");
		
		SelectQuery select = create.selectQuery();
		
		select.addSelect(p.STRATUM_ID);
		
		select.addSelect(p.CLUSTER_ID);
		select.addSelect(p.PLOT_SECTION_ID);
		select.addSelect(p.PLOT_LOCATION);
		select.addSelect(p.PLOT_GPS_READING);
		select.addSelect(p.PLOT_ACTUAL_LOCATION);
		select.addSelect(p.PLOT_LOCATION_DEVIATION);
		select.addSelect(Factory.val(1).as("cnt"));
		//TODO join with plot numeric value to get the right area
		select.addSelect(Factory.val(706.8583470577034).as("est_area"));
		
		select.addFrom(p);
		
		select.addJoin(pa, p.PLOT_SECTION_ID.eq(pa.PLOT_SECTION_ID));
		select.addJoin(a, a.AOI_ID.eq(pa.AOI_ID));
		select.addJoin(al, a.AOI_HIERARCHY_LEVEL_ID.eq(al.AOI_HIERARCHY_LEVEL_ID));
		
		select.addConditions(p.PLOT_OBS_UNIT_ID.eq(obsUnitId));
		// Only primary sections planned plot
		select.addConditions(p.VISIT_TYPE.eq("P"));
		select.addConditions(p.PRIMARY_SECTION.isTrue());
		select.addConditions(
				al.AOI_HIERARCHY_LEVEL_RANK.eq(
						create
						.select(al2.AOI_HIERARCHY_LEVEL_RANK.max())
						.from(al2)
						)
				);
		
		Result<Record> levelRecords = getAoiHierarchyLevelRecords(surveyId);
		int i = 0;
		Aoi childAoi = a;
		for ( Record record : levelRecords ) {
			String levelName = record.getValue(AHL.AOI_HIERARCHY_LEVEL_NAME);
			if((i++==0)){
				select.addSelect(pa.AOI_ID.as(levelName));
			} else {
				Aoi parentAoi = A.as("a"+i);
				
				select.addSelect(parentAoi.AOI_ID.as(levelName ));
				
				select.addJoin(parentAoi, childAoi.AOI_PARENT_ID.eq(parentAoi.AOI_ID));
				childAoi = parentAoi;
			}
		}
		
		
		int varIndex = 0;		
		for ( VariableMetadata variable : variables ) {
			if ( variable.isCategorical() && variable.isForAnalysis() ) {
				String varName = variable.getVariableName();
				PlotCategoricalValueView plotCatValueView = PLOT_CATEGORICAL_VALUE_VIEW.as("c_" + (varIndex++));
	
				select.addSelect( Factory.coalesce(plotCatValueView.CATEGORY_ID, -1).as(varName) );
	
				select.addJoin(
						plotCatValueView, 
						JoinType.LEFT_OUTER_JOIN, 
						p.PLOT_SECTION_ID.eq(plotCatValueView.PLOT_SECTION_ID)
							.and(plotCatValueView.VARIABLE_NAME.eq(varName)
						)
				);
			}
		}
		
		return select;
	}
	
	
}
