package org.openforis.calc.persistence;

import static org.openforis.calc.persistence.jooq.Tables.AOI;
import static org.openforis.calc.persistence.jooq.Tables.PLOT_CATEGORICAL_VALUE_VIEW;
import static org.openforis.calc.persistence.jooq.Tables.PLOT_SECTION_VIEW;

import java.util.Collection;

import org.jooq.Field;
import org.jooq.JoinType;
import org.jooq.SelectQuery;
import org.jooq.impl.Factory;
import org.openforis.calc.io.flat.FlatDataStream;
import org.openforis.calc.io.flat.FlatRecord;
import org.openforis.calc.model.VariableMetadata;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.tables.Aoi;
import org.openforis.calc.persistence.jooq.tables.PlotCategoricalValueView;
import org.openforis.calc.persistence.jooq.tables.PlotSectionView;
import org.openforis.calc.persistence.jooq.tables.records.PlotSectionViewRecord;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 * @author Mino Togna
 */
@Component
@Transactional
public class PlotSectionViewDao extends JooqDaoSupport<PlotSectionViewRecord, PlotSectionView> {

	private static final PlotSectionView V = PLOT_SECTION_VIEW;
	private static final Aoi A = AOI;

	public PlotSectionViewDao() {
		super(V, PlotSectionView.class, V.PLOT_OBS_UNIT_ID, V.CLUSTER_CODE, V.PLOT_NO, V.PLOT_SECTION, V.VISIT_TYPE);
	}

	public Integer getId(int obsUnitId, String clusterCode, int plotNo, String plotSection, String visitType) {
		return getIdByKey(obsUnitId, clusterCode, plotNo, plotSection, visitType);
	}

	@Override
	protected Field<?> pk() {
		return V.PLOT_SECTION_ID;
	}

	public Object[] extractKey(FlatRecord r, int obsUnitId) {
		return extractKey(r, obsUnitId, V.CLUSTER_CODE, V.PLOT_NO, V.PLOT_SECTION, V.VISIT_TYPE);
	}

	public FlatDataStream streamAll(String[] fields, int observationUnitId) {
		return stream(fields, V.PLOT_OBS_UNIT_ID, observationUnitId);
	}
	
	public FlatDataStream getCategoryDistributionStream(Collection<VariableMetadata> variables, int obsUnitId, boolean useShares){
		Factory create = getJooqFactory();
		SelectQuery query = create.selectQuery();
		
		query.addSelect( V.STRATUM_ID );
		query.addSelect( A.AOI_ID );
		
		query.addFrom(V);
		
		//TODO change in future
		query.addJoin( A, A.AOI_ID.eq(1) );
		
		query.addConditions(V.PLOT_OBS_UNIT_ID.eq(obsUnitId));
		
		query.addGroupBy(V.STRATUM_ID);
		query.addGroupBy(A.AOI_ID);
		
		int varIndex = 0;
		for ( VariableMetadata variable : variables ) {
			String varName = variable.getVariableName();
			if(variable.isCategorical()){
				PlotCategoricalValueView plotCatValueView = PLOT_CATEGORICAL_VALUE_VIEW.as("c_"+ (varIndex++) );
				
				query.addSelect(plotCatValueView.CATEGORY_CODE.as(varName));
				
				query.addJoin(
						plotCatValueView, 
						JoinType.LEFT_OUTER_JOIN, 
						V.PLOT_SECTION_ID.eq(plotCatValueView.PLOT_SECTION_ID).and(plotCatValueView.VARIABLE_NAME.eq(varName))
						);
				
				query.addGroupBy(plotCatValueView.CATEGORY_CODE);
			}
		}
		
		if(useShares) {
			query.addSelect( V.PLOT_SHARE.div(100).sum().as("plot_distribution") );
		} else {
			query.addSelect(V.PLOT_SECTION_ID.count().as("plot_distribution") );
			query.addConditions( V.PRIMARY_SECTION.eq(true) );
		}
		
		return stream( query.fetch() );
	}
	
}
