package org.openforis.calc.persistence;

import static org.openforis.calc.persistence.jooq.Tables.AOI;
import static org.openforis.calc.persistence.jooq.Tables.AOI_HIERARCHY_LEVEL;
import static org.openforis.calc.persistence.jooq.Tables.GROUND_PLOT_VIEW;
import static org.openforis.calc.persistence.jooq.Tables.PLOT_CATEGORICAL_VALUE_VIEW;
import static org.openforis.calc.persistence.jooq.Tables.PLOT_SECTION_AOI;

import java.util.Collection;
import java.util.List;

import javax.sql.DataSource;

import org.jooq.Field;
import org.jooq.JoinType;
import org.jooq.SelectQuery;
import org.jooq.impl.Factory;
import org.openforis.calc.model.ObservationUnitMetadata;
import org.openforis.calc.model.VariableMetadata;
import org.openforis.calc.persistence.jooq.FactTableGenerator;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.tables.Aoi;
import org.openforis.calc.persistence.jooq.tables.AoiHierarchyLevel;
import org.openforis.calc.persistence.jooq.tables.GroundPlotView;
import org.openforis.calc.persistence.jooq.tables.PlotCategoricalValueView;
import org.openforis.calc.persistence.jooq.tables.PlotSectionAoi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Mino Togna
 */
@SuppressWarnings("rawtypes")
@Component
@Transactional
public class FactTableDao extends JooqDaoSupport {

//	private static final Stratum S = STRATUM;
	private static final Aoi A = AOI;
//	private static final Cluster C = CLUSTER;
//	private static final PlotSectionView P = PLOT_SECTION_VIEW;
	
	private static final GroundPlotView V = GROUND_PLOT_VIEW;
	private static final AoiHierarchyLevel AL = AOI_HIERARCHY_LEVEL;
	private static final PlotSectionAoi PSA = PLOT_SECTION_AOI;

	@Autowired
	private DataSource dataSource;

	@SuppressWarnings("unchecked")
	public FactTableDao() {
		super(null, null);
	}

	@Transactional
	synchronized
	public void createOrUpdateFactTable(ObservationUnitMetadata obsUnitMetadata) {
		FactTableGenerator factTableGenerator = generateFactTable(obsUnitMetadata);

		Factory create = getJooqFactory();

		SelectQuery plotFactSelect = getPlotFactSelect(create, obsUnitMetadata);

		StringBuilder sb = new StringBuilder();
		sb.append("insert into ");
		sb.append(factTableGenerator.getTableName());
		sb.append(" (");
		List<Field<?>> fields = plotFactSelect.getFields();
		int i = 0;
		for ( Field<?> field : fields ) {
			if ( (i++) != 0 ) {
				sb.append(",");
			}
			sb.append(field.getName());
		}
		sb.append(") ");
		sb.append(plotFactSelect.toString());

		String insertSql = sb.toString();
		if ( getLog().isDebugEnabled() ) {
			getLog().debug("Plot fact table insert:");
			getLog().debug(insertSql);
		}

		create.execute(insertSql);
	}

	private FactTableGenerator generateFactTable(ObservationUnitMetadata obsUnitMetadata) {
		FactTableGenerator factTableGenerator = new FactTableGenerator(obsUnitMetadata, dataSource);
		factTableGenerator.generate();
		return factTableGenerator;
	}
	
	
	@SuppressWarnings("unchecked")
	SelectQuery getPlotFactSelect(Factory create, ObservationUnitMetadata obsUnitMetadata){
		int obsUnitId = obsUnitMetadata.getObsUnitId();
		Collection<VariableMetadata> variables = obsUnitMetadata.getVariableMetadata();	
				
		GroundPlotView p = V.as("p");
		PlotSectionAoi pa = PSA.as("pa");
		Aoi a = A.as("a");
		AoiHierarchyLevel al = AL.as("al");
		AoiHierarchyLevel al2 = AL.as("al");
		
//		Factory create = getJooqFactory();
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
		
//		if ( getLog().isDebugEnabled() ) {
//			getLog().debug("Plot fact table select:");
//			getLog().debug(select.toString());
//		}
//		
		return select;
	}
	
	
}
