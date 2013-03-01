package org.openforis.calc.persistence;
import org.jooq.Insert;
import org.jooq.Record;
import org.jooq.SelectQuery;
import org.openforis.calc.model.ObservationUnitMetadata;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.rolap.FactTable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author M. Togna
 * @author G. Miceli
 */
@SuppressWarnings("rawtypes")
@Component
@Transactional
public abstract class RolapAggregateDao extends JooqDaoSupport {

	@SuppressWarnings("unchecked")
	public RolapAggregateDao() {
		super(null, null);
	}

	@SuppressWarnings("unchecked")
	@Transactional
	synchronized
	public void populate(FactTable table) {
		ObservationUnitMetadata unit = table.getObservationUnitMetadata();
		
		SelectQuery select = createFactSelect(unit);
		Insert<Record> insert = createInsertFromSelect(table, select);
		
		getLog().debug("Inserting fact data:");
		getLog().debug(insert);
		
		insert.execute();
		
		getLog().debug("Complete");
		
//		Result<Record> levelRecords = getAoiHierarchyLevelRecords(surveyId);
//		FactTable prevLevelTable = plotFactTable;
//		List<String> prevLevels = new ArrayList<String>();
//		for ( Record record : levelRecords ) {
//			
//			String levelName = record.getValue(AHL.AOI_HIERARCHY_LEVEL_NAME);
//			Integer levelRank = record.getValue(AHL.AOI_HIERARCHY_LEVEL_RANK);
//			String tableName = "agg_"+levelName+"_stratum_" + plotFactTableName;
//			FactTable aoiStratumFact = getAggFactTable(prevLevelTable, tableName, prevLevels);
//			// generate select
//			SelectQuery aggAoiStratumSelect = getAggAoiStratumSelect(plotFactTable, levelName, levelRank, prevLevels, surveyId );
//			Insert<Record> insert = getInsertFromSelect(aoiStratumFact, aggAoiStratumSelect);
//			if ( getLog().isDebugEnabled() ) {
//				getLog().debug(tableName + " insert:");
//				getLog().debug(insert.toString());
//			}	
//			insert.execute();
//			
//			prevLevelTable = aoiStratumFact;
//			prevLevels.add( levelName );
//		}
		
		
		//2. agg plot fact at aoi/stratum level 
//		FactTable aoiStratumPlotFact = getAggAoiStratumPlotFactTable(plotFactTable);
//		
//		SelectQuery aoiStratumSelect = getAggAoiStratumPlotFactSelect(create, plotFactTable, aoiStratumPlotFact);
//		Insert<Record> aoiStratumInsert = getAggAoiStratumPlotFactInsert(create, aoiStratumPlotFact, aoiStratumSelect);
//		
//		if ( getLog().isDebugEnabled() ) {
//			getLog().debug("Aoi Stratum aggregate plot fact table insert:");
//			getLog().debug(aoiStratumInsert.toString());		
//		}
//		
//		aoiStratumInsert.execute();
	}

	protected abstract SelectQuery createFactSelect(ObservationUnitMetadata unit);
	
//	@SuppressWarnings("unchecked")
//	private SelectQuery getAggAoiStratumSelect(FactTable table, String aoiLevelName, int aoiLevelRank, List<String> prevLevels, int surveyId) {
//		Factory create = getJooqFactory();
//		PlotExpansionFactor e = PEF.as("e");
//		AoiStratumView s = ASV.as("s");
//		
//		SelectQuery select = create.selectQuery();
//		
//		select.addSelect(s.STRATUM_ID);
//		select.addSelect(Factory.coalesce(table.getField(COUNT_COLUMN_NAME).sum(), 0).as(COUNT_COLUMN_NAME));
//		select.addSelect(Factory.coalesce(table.getField(COUNT_COLUMN_NAME).sum().mul(e.EXP_FACTOR), s.AREA).as(EST_AREA_COLUMN_NAME) );
//		
//		
//		select.addFrom(table);
//		
//		select.addJoin(
//				e, 
//				e.AOI_ID.eq( (Field<Integer>) table.getField(aoiLevelName))
//					.and(e.STRATUM_ID.eq( (Field<Integer>) table.getField(S.STRATUM_ID.getName())) )
//			);
//		select.addJoin(
//				s, 
//				JoinType.RIGHT_OUTER_JOIN, 
//				e.STRATUM_ID.eq( s.STRATUM_ID )
//				.and(e.AOI_ID.eq(s.AOI_ID))
//			);
//		
//		select.addGroupBy(s.STRATUM_ID);
//		select.addGroupBy(s.AREA);
//		select.addGroupBy(e.EXP_FACTOR);
//		
//		Result<Record> levelRecords = getAoiHierarchyLevelRecords(surveyId, aoiLevelRank);
//		Aoi childAoi = null;
//		int i = 0;
//		List<String> aoiNames = new ArrayList<String>();
//		aoiNames.addAll(prevLevels);
//		
//		for ( Record record : levelRecords ) {
//			
//			Aoi a = A.as("a"+(i++));
//			int aoiLevelId = record.getValue(AHL.AOI_HIERARCHY_LEVEL_ID);
//			String levelName = record.getValue(AHL.AOI_HIERARCHY_LEVEL_NAME);
//			aoiNames.add(levelName);
//			
//			select.addSelect( a.AOI_ID.as(levelName) );
//			
//			select.addJoin(a, 
//					( childAoi == null ) 
//					? s.AOI_ID.eq(a.AOI_ID).and(a.AOI_HIERARCHY_LEVEL_ID.eq(aoiLevelId))
//					: childAoi.AOI_PARENT_ID.eq( a.AOI_ID )
//				);
//			
//			select.addGroupBy(a.AOI_ID);
//			
//			childAoi = a;
//		}
//		List<TableField<Record, Integer>> srcDimensions = table.getDimensionFields();
//		List<TableField<Record, Integer>> dimensions = new ArrayList<TableField<Record,Integer>>(srcDimensions.size());
//		for ( TableField<Record, Integer> f : srcDimensions ) {
//			String fieldName = f.getName();
//			if(! (aoiNames.contains(fieldName) || ArrayUtils.contains( PLOT_DIMENSIONS, fieldName) ) ){
//				select.addSelect(Factory.coalesce(f, -1).as(fieldName));
//				dimensions.add(f);
//			}
//		}
//		
//		select.addGroupBy(dimensions);
//		return select;
//	}

//	@SuppressWarnings("unchecked")
//	@Deprecated
//	private SelectQuery getAggAoiStratumPlotFactSelect(Factory create, FactTable plotFactTable, FactTable aoiStratumPlotFact) {
//		Table<Record> p = plotFactTable.as("p");
//		PlotSectionAoi psa = PSA.as("psa");
//		PlotExpansionFactor pef = PEF.as("pef");
//		
//		SelectQuery select = create.selectQuery();
//		List<TableField<Record, Integer>> dims = aoiStratumPlotFact.getDimensionFields();
//		List<Field<?>> stratumDims = new ArrayList<Field<?>>();
//		for ( TableField<Record, Integer> f : dims ) {
//			if(!A.AOI_ID.getName().equals(f.getName())){
//				stratumDims.add( p.getField(f.getName()) );
//			}
//		}
//		select.addSelect(psa.AOI_ID);
//		select.addSelect(stratumDims);
//		select.addSelect(p.getField("cnt").sum().as("cnt"));
//		select.addSelect(pef.EXP_FACTOR.mul( p.getField("cnt").sum() ).as("est_area") );
//		
//		select.addFrom(p);
//		select.addJoin(
//				psa, 
//				JoinType.RIGHT_OUTER_JOIN, 
//				psa.PLOT_SECTION_ID.eq( (Field<Integer>) p.getField(psa.PLOT_SECTION_ID.getName()) )
//			);
//		select.addJoin(
//				pef,
//				JoinType.JOIN,
//				pef.AOI_ID.eq(psa.AOI_ID)
//					.and(pef.STRATUM_ID.eq( (Field<Integer>) p.getField(pef.STRATUM_ID.getName())))
//			);
//		
//		select.addGroupBy(psa.AOI_ID);
//		select.addGroupBy(stratumDims);
//		select.addGroupBy(pef.EXP_FACTOR);
//		return select;
//	}
//	
//	private FactTable getAggFactTable(FactTable factTable, String tableName, List<String> excludeLevel) {
//		List<String> excludedDimensions = Arrays.asList(C.CLUSTER_ID.getName(), P.PLOT_SECTION_ID.getName());
//		excludedDimensions.addAll(excludeLevel);
//		
////		List<String> excludedMeasures = Arrays.asList(P.PLOT_LOCATION_DEVIATION.getName())
////		String[] excludedPoints = PLOT_POINTS;
//
//		AggregateTable aggFactTable = factTable.createAggregateTable(tableName, excludedDimensions);
//		return aggFactTable;
//	}

//	@Deprecated
//	private FactTable getPlotFactTable(ObservationUnitMetadata obsUnitMetadata) {
//		SurveyMetadata surveyMetadata = obsUnitMetadata.getSurveyMetadata();
//		String schema = surveyMetadata.getSurveyName();
//		int surveyId = surveyMetadata.getSurveyId();
//		String table = obsUnitMetadata.getObsUnitName() + "_fact";
//		
//		Collection<VariableMetadata> vars = obsUnitMetadata.getVariableMetadata();
//		String[] dimensions = ArrayUtils.clone(PLOT_DIMENSIONS);
//		
//		// add aoi dimensions
//		Result<Record> result = getAoiHierarchyLevelRecords(surveyId);
//		for ( Record record : result ) {
//			String levelName = record.getValue(AHL.AOI_HIERARCHY_LEVEL_NAME);
//			dimensions = ArrayUtils.add(dimensions, levelName);
//		}		
//		
//		
//		for ( VariableMetadata var : vars ) {
//			if( var.isForAnalysis() && var.isCategorical() ) {
//				dimensions = ArrayUtils.add(dimensions, var.getVariableName());
//			}
//		}
//		
//		return new FactTable(schema, table, PLOT_MEASURES, dimensions, PLOT_POINTS);
//	}

//	private Result<Record> getAoiHierarchyLevelRecords(int surveyId) {
////		Factory create = getJooqFactory();
////		
////		Result<Record> result = create
////			.select()
////			.from(AHL)
////			.join(AH)
////				.on(AHL.AOI_HIERARCHY_ID.eq(AH.AOI_HIERARCHY_ID))
////			.where( AH.SURVEY_ID.eq(surveyId) )
////			.orderBy(AHL.AOI_HIERARCHY_LEVEL_RANK.desc())
////			.fetch();
////		
////		return result;
//		return getAoiHierarchyLevelRecords(surveyId, null);
//	}

//	private Result<Record> getAoiHierarchyLevelRecords(int surveyId, Integer fromLevel) {
//		Factory create = getJooqFactory();
//		
//		Condition condition = fromLevel != null ? AH.SURVEY_ID.eq(surveyId).and(AHL.AOI_HIERARCHY_LEVEL_RANK.lessOrEqual(fromLevel)) :  AH.SURVEY_ID.eq(surveyId);
//		
//		Result<Record> result = create
//			.select()
//			.from(AHL)
//			.join(AH)
//				.on(AHL.AOI_HIERARCHY_ID.eq(AH.AOI_HIERARCHY_ID))
//			.where(  condition )
//			.orderBy(AHL.AOI_HIERARCHY_LEVEL_RANK.desc())
//			.fetch();
//		
//		return result;
//	}
	
}
