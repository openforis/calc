package org.openforis.calc.persistence;
import static org.openforis.calc.persistence.jooq.Tables.AOI;
import static org.openforis.calc.persistence.jooq.Tables.AOI_HIERARCHY_LEVEL;
import static org.openforis.calc.persistence.jooq.Tables.CLUSTER;
import static org.openforis.calc.persistence.jooq.Tables.GROUND_PLOT_VIEW;
import static org.openforis.calc.persistence.jooq.Tables.PLOT_CATEGORICAL_VALUE_VIEW;
import static org.openforis.calc.persistence.jooq.Tables.PLOT_SECTION_AOI;
import static org.openforis.calc.persistence.jooq.Tables.PLOT_SECTION_VIEW;
import static org.openforis.calc.persistence.jooq.Tables.STRATUM;
import static org.openforis.calc.persistence.jooq.Tables.PLOT_EXPANSION_FACTOR;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.jooq.Field;
import org.jooq.Insert;
import org.jooq.JoinType;
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
import org.openforis.calc.persistence.jooq.tables.AoiHierarchyLevel;
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
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Mino Togna
 */
@SuppressWarnings("rawtypes")
@Component
@Transactional
public class PlotFactTableDao extends JooqDaoSupport {

	private static final Stratum S = STRATUM;
	private static final Aoi A = AOI;
	private static final Cluster C = CLUSTER;
	private static final PlotSectionView P = PLOT_SECTION_VIEW;

	private static final GroundPlotView V = GROUND_PLOT_VIEW;
	private static final AoiHierarchyLevel AL = AOI_HIERARCHY_LEVEL;
	private static final PlotSectionAoi PSA = PLOT_SECTION_AOI;
	private static final PlotExpansionFactor PEF = PLOT_EXPANSION_FACTOR;
	
	private static final String COUNT_COLUMN_NAME = "cnt";
	private static final String EST_AREA_COLUMN_NAME = "est_area";

	private static final String[] POINTS = new String[] { P.PLOT_GPS_READING.getName(), P.PLOT_ACTUAL_LOCATION.getName(), P.PLOT_LOCATION.getName() };
	private static final String[] DIMENSIONS = new String[] { A.AOI_ID.getName(), S.STRATUM_ID.getName(), C.CLUSTER_ID.getName(), P.PLOT_SECTION_ID.getName() };
	private static final String[] MEASURES = new String[] { P.PLOT_LOCATION_DEVIATION.getName(), COUNT_COLUMN_NAME, EST_AREA_COLUMN_NAME };
	
	@Autowired
	private JooqTableGenerator factTableGenerator;

	@SuppressWarnings("unchecked")
	public PlotFactTableDao() {
		super(null, null);
	}

	@Transactional
	synchronized
	public void createOrUpdatePlotFactTable(ObservationUnitMetadata obsUnitMetadata) {
		
		Factory create = getJooqFactory();
		
		//1. Plot fact table
		FactTable plotFactTable = getPlotFactTable(obsUnitMetadata);
		factTableGenerator.generate(plotFactTable);
		
		SelectQuery plotFactSelect = getPlotFactSelect(create, obsUnitMetadata);
		Insert<FactRecord> plotFactInsert = getPlotFactInsert(plotFactTable, create, plotFactSelect);
		
		if ( getLog().isDebugEnabled() ) {
			getLog().debug("Plot fact table insert:");
			getLog().debug(plotFactInsert.toString());
		}
		
		plotFactInsert.execute();
		
		
		//2. agg plot fact at aoi/stratum level 
		FactTable aoiStratumPlotFact = getAggAoiStratumPlotFactTable(plotFactTable);
		factTableGenerator.generate(aoiStratumPlotFact);
		
		SelectQuery aoiStratumSelect = getAggAoiStratumPlotFactSelect(create, plotFactTable, aoiStratumPlotFact);
		Insert<FactRecord> aoiStratumInsert = getAggAoiStratumPlotFactInsert(create, aoiStratumPlotFact, aoiStratumSelect);
		
		if ( getLog().isDebugEnabled() ) {
			getLog().debug("Aoi Stratum aggregate plot fact table insert:");
			getLog().debug(aoiStratumInsert.toString());		
		}
		
		aoiStratumInsert.execute();
		
		//3. Aggg at  aoi level
		
	}

	private Insert<FactRecord> getAggAoiStratumPlotFactInsert(Factory create, FactTable aoiStratumPlotFact, SelectQuery aoiStratumSelect) {
		List<Field<?>> selectFields = aoiStratumSelect.getFields();
		List<Field<?>> fields = new ArrayList<Field<?>>(selectFields.size());
		for ( Field<?> field : selectFields ) {
			String fieldName = field.getName();
			fields.add( aoiStratumPlotFact.getField(fieldName ) );
		}		

		Insert<FactRecord> insertAoiStratumAgg = create.insertInto(aoiStratumPlotFact, fields)
			.select(aoiStratumSelect);
		return insertAoiStratumAgg;
	}

	@SuppressWarnings("unchecked")
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

	private Insert<FactRecord> getPlotFactInsert(FactTable plotFactTable, Factory create, SelectQuery plotFactSelect) {
		List<Field<?>> selectFields = plotFactSelect.getFields();
		List<Field<?>> fields = new ArrayList<Field<?>>(selectFields.size());
		for ( Field<?> field : selectFields ) {
			String fieldName = field.getName();
			fields.add( plotFactTable.getField(fieldName ) );
		}		
		
		Insert<FactRecord> insert = create
			.insertInto(plotFactTable, fields)
			.select(plotFactSelect);
		
		return insert;
	}
	
	private FactTable getAggAoiStratumPlotFactTable(FactTable plotFactTable){
		String[] exludedDimensions = new String[]{C.CLUSTER_ID.getName(), P.PLOT_SECTION_ID.getName()};
		String[] excludedMeasures = new String[]{P.PLOT_LOCATION_DEVIATION.getName()};
		String[] excludedPoints = POINTS;
		String tableName = "agg_aoi_stratum_" + plotFactTable.getName();
		FactTable aggStratumPlotFact = plotFactTable.aggregate(tableName, exludedDimensions, excludedMeasures, excludedPoints);
		return aggStratumPlotFact;
	}
	
	private FactTable getPlotFactTable(ObservationUnitMetadata obsUnitMetadata) {
		SurveyMetadata surveyMetadata = obsUnitMetadata.getSurveyMetadata();
		String schema = surveyMetadata.getSurveyName();
		String table = obsUnitMetadata.getObsUnitName() + "_fact";
		
		Collection<VariableMetadata> vars = obsUnitMetadata.getVariableMetadata();
		String[] dimensions = ArrayUtils.clone(DIMENSIONS);
		for ( VariableMetadata var : vars ) {
			if( var.isForAnalysis() && var.isCategorical() ) {
				dimensions = ArrayUtils.add(dimensions, var.getVariableName());
			}
		}
		
		return new FactTable(schema, table, MEASURES, dimensions, POINTS);
	}

	
	@SuppressWarnings("unchecked")
	private SelectQuery getPlotFactSelect(Factory create, ObservationUnitMetadata obsUnitMetadata){
		int obsUnitId = obsUnitMetadata.getObsUnitId();
		Collection<VariableMetadata> variables = obsUnitMetadata.getVariableMetadata();	
				
		GroundPlotView p = V.as("p");
		PlotSectionAoi pa = PSA.as("pa");
		Aoi a = A.as("a");
		AoiHierarchyLevel al = AL.as("al");
		AoiHierarchyLevel al2 = AL.as("al");
		
		SelectQuery select = create.selectQuery();
		
		select.addSelect(p.STRATUM_ID);
		select.addSelect(pa.AOI_ID);
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
