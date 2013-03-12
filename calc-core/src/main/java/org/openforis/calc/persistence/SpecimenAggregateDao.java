package org.openforis.calc.persistence;

import java.math.BigDecimal;
import java.util.List;

import org.jooq.Field;
import org.jooq.Insert;
import org.jooq.SelectQuery;
import org.jooq.impl.Factory;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.rolap.SpecimenFactTable;
import org.openforis.calc.persistence.jooq.rolap.SpecimenPlotAggregateTable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author M. Togna
 * 
 */
@Component
@SuppressWarnings("rawtypes")
public class SpecimenAggregateDao extends JooqDaoSupport {

	@SuppressWarnings("unchecked")
	SpecimenAggregateDao() {
		super(null, null);
	}

	@Transactional
	synchronized 
	public void populate(SpecimenPlotAggregateTable aggTable) {

		SelectQuery select = createPlotAggregateSelect(aggTable);
		@SuppressWarnings("unchecked")
		Insert insert = createInsertFromSelect(aggTable, select);

		getLog().debug("Inserting specimen plot aggregate data:");
		getLog().debug(insert);

		insert.execute();

		getLog().debug("Complete");
	}

	@Transactional
	private SelectQuery createPlotAggregateSelect(SpecimenPlotAggregateTable aggTable) {
		SpecimenFactTable fact = aggTable.getFactTable();
		List<Field<Integer>> userDefinedDimensions = fact.getUserDefinedDimensionFields();

		Factory create = getJooqFactory();
		SelectQuery select = create.selectQuery();

		select.addSelect(fact.STRATUM_ID);
		select.addSelect(fact.CLUSTER_ID);
		select.addSelect(fact.PLOT_ID);
		select.addSelect(fact.SPECIMEN_TAXON_ID);
		select.addSelect(fact.getAoiFields());
		select.addSelect(userDefinedDimensions);
		select.addSelect(fact.COUNT.sum().as(aggTable.AGG_COUNT.getName()));
		select.addSelect(fact.COUNT.sum().as(aggTable.COUNT.getName()));
		
		for ( Field<BigDecimal> measure : fact.getUserDefinedMeasureFields() ) {
			select.addSelect(measure.sum().as(measure.getName()));
		}

		select.addFrom(fact);

		select.addGroupBy(fact.STRATUM_ID);
		select.addGroupBy(fact.CLUSTER_ID);
		select.addGroupBy(fact.PLOT_ID);
		select.addGroupBy(fact.SPECIMEN_TAXON_ID);
		select.addGroupBy(fact.getAoiFields());
		select.addGroupBy(userDefinedDimensions);
		
		return select;
	}

}
