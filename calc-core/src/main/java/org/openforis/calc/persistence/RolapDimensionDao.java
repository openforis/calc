/**
 * 
 */
package org.openforis.calc.persistence;

import static org.openforis.calc.persistence.jooq.Tables.AOI;
import static org.openforis.calc.persistence.jooq.Tables.AOI_HIERARCHY;
import static org.openforis.calc.persistence.jooq.Tables.AOI_HIERARCHY_LEVEL;
import static org.openforis.calc.persistence.jooq.Tables.CLUSTER;
import static org.openforis.calc.persistence.jooq.Tables.SAMPLE_PLOT_VIEW;
import static org.openforis.calc.persistence.jooq.Tables.SPECIMEN_VIEW;
import static org.openforis.calc.persistence.jooq.Tables.STRATUM;
import static org.openforis.calc.persistence.jooq.Tables.TAXON;

import org.jooq.Insert;
import org.jooq.Record;
import org.jooq.SelectQuery;
import org.jooq.impl.Factory;
import org.openforis.calc.model.AoiHierarchyLevelMetadata;
import org.openforis.calc.model.AoiHierarchyMetadata;
import org.openforis.calc.model.ObservationUnitMetadata;
import org.openforis.calc.model.TaxonomicChecklistMetadata;
import org.openforis.calc.model.VariableMetadata;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.rolap.AoiDimensionTable;
import org.openforis.calc.persistence.jooq.rolap.CategoryDimensionTable;
import org.openforis.calc.persistence.jooq.rolap.ClusterDimensionTable;
import org.openforis.calc.persistence.jooq.rolap.PlotDimensionTable;
import org.openforis.calc.persistence.jooq.rolap.SpecimenDimensionTable;
import org.openforis.calc.persistence.jooq.rolap.StratumDimensionTable;
import org.openforis.calc.persistence.jooq.rolap.TaxonDimensionTable;
import org.openforis.calc.persistence.jooq.tables.AoiHierarchy;
import org.openforis.calc.persistence.jooq.tables.AoiHierarchyLevel;
import org.openforis.calc.persistence.jooq.tables.Category;
import org.openforis.calc.persistence.jooq.tables.SamplePlotView;
import org.openforis.calc.persistence.jooq.tables.SpecimenView;
import org.openforis.calc.persistence.jooq.tables.Taxon;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author M. Togna
 * @author G. Miceli
 *
 */
@SuppressWarnings("rawtypes")
@Component 
@Transactional
public class RolapDimensionDao extends JooqDaoSupport {

	private static final String DIMENSION_NA_VALUE = "No Data";
	private static final String DIMENSION_NA_ID = "-1";
	
 	@SuppressWarnings("unchecked")
	public RolapDimensionDao() {
		super(null, null);
	}

	@Transactional
 	public void populate(CategoryDimensionTable table) {
 		VariableMetadata var = table.getVariableMetadata();
 		int variableId = var.getVariableId();
 		Factory create = getJooqFactory();
		Category c = Category.CATEGORY.as("c");
		
 		SelectQuery select = create.selectQuery();
 		select.addSelect(c.CATEGORY_ID, c.CATEGORY_CODE, c.CATEGORY_LABEL);
 		select.addFrom(c);
 		select.addConditions(c.VARIABLE_ID.eq( variableId));
 		
 		create
 			.insertInto(table, table.ID, table.CODE, table.LABEL)
 			.select(select)
 			.execute();
 		
 		create
			.insertInto(table, table.ID, table.CODE, table.LABEL)
			.values(DIMENSION_NA_ID, DIMENSION_NA_VALUE, DIMENSION_NA_VALUE)
			.execute();
	}
 	
	@Transactional
	public int populate(AoiDimensionTable table) {
		Factory create = getJooqFactory();
		AoiHierarchyLevelMetadata level = table.getAoiHierarchyLevelMetadata();
		AoiHierarchyMetadata hier = level.getAoiHierachyMetadata();
		int surveyId = hier.getSurveyId();
		String dimTableName = table.getName();
		
		AoiHierarchyLevel l = AOI_HIERARCHY_LEVEL.as("l");
		AoiHierarchy h = AOI_HIERARCHY.as("h");
		org.openforis.calc.persistence.jooq.tables.Aoi a = AOI.as("a");
		
		SelectQuery select = create.selectQuery();
			select.addSelect( a.AOI_ID , a.AOI_CODE , a.AOI_LABEL , a.AOI_PARENT_ID );
			select.addFrom( a );
			select.addJoin( 
					l, 
					a.AOI_HIERARCHY_LEVEL_ID.eq(l.AOI_HIERARCHY_LEVEL_ID) 
				);
			
			select.addJoin( 
					h ,
					l.AOI_HIERARCHY_ID.eq( h.AOI_HIERARCHY_ID ) 
				);
			select.addConditions( 
					l.AOI_HIERARCHY_LEVEL_NAME.eq(dimTableName) 
					.and( h.SURVEY_ID.eq(surveyId) ) 
				);
		
		Insert<Record> insert = create
			.insertInto( table , table.ID, table.CODE, table.LABEL, table.PARENT_ID )
			.select( select );
		
		int r = insert.execute();
		
		return r;
	}

	
	@Transactional
	public void populate(ClusterDimensionTable table) {
		int surveyId = table.getSurveyId();
 		Factory create = getJooqFactory();
 		SelectQuery select = create.selectQuery();
 		select.addSelect(CLUSTER.CLUSTER_ID, CLUSTER.CLUSTER_CODE);
 		select.addFrom(CLUSTER);
 		select.addConditions(CLUSTER.SURVEY_ID.eq(surveyId));
		Insert<Record> insert = create
				.insertInto(table, table.ID, table.LABEL)
				.select(select);
		insert.execute();
	}

	@Transactional
	public void populate(PlotDimensionTable table) {
		ObservationUnitMetadata unit = table.getObservationUnitMetadata();
		int unitId = unit.getObsUnitId();
 		Factory create = getJooqFactory();
 		SelectQuery select = create.selectQuery();
 		SamplePlotView s = SAMPLE_PLOT_VIEW.as("s");
 		select.addSelect(s.SAMPLE_PLOT_ID, s.CLUSTER_ID, s.PLOT_NO);
 		select.addFrom(s);
 		select.addConditions(s.PLOT_OBS_UNIT_ID.eq(unitId));
 		select.addConditions(s.GROUND_PLOT.isTrue());
		Insert<Record> insert = create
				.insertInto(table, table.ID, table.PARENT_ID, table.LABEL)
				.select(select);
		insert.execute();
	}

	@Transactional
	public void populate(StratumDimensionTable table) {
		int surveyId = table.getSurveyId();
 		Factory create = getJooqFactory();
 		SelectQuery select = create.selectQuery();
 		select.addSelect(STRATUM.STRATUM_ID, STRATUM.STRATUM_NO);
 		select.addFrom(STRATUM);
 		select.addConditions(STRATUM.SURVEY_ID.eq(surveyId));
		Insert<Record> insert = create
				.insertInto(table, table.ID, table.LABEL)
				.select(select);
		insert.execute();
	}
	
	@Transactional
	public void populate(SpecimenDimensionTable table){
		ObservationUnitMetadata unit = table.getObservationUnitMetadata();
		int unitId = unit.getObsUnitId();
		
		SpecimenView s = SPECIMEN_VIEW.as("s");
		
		Factory create = getJooqFactory();
		SelectQuery select = create.selectQuery();
		select.addSelect(s.SPECIMEN_ID, s.SAMPLE_PLOT_ID, s.SPECIMEN_NO);
		select.addFrom(s);
		select.addConditions( s.SPECIMEN_OBS_UNIT_ID.eq(unitId) );
		
		Insert<Record> insert = create
				.insertInto(table, table.ID, table.PARENT_ID, table.LABEL)
				.select(select);
		insert.execute();
	}
	
	@Transactional
	public void populate(TaxonDimensionTable table){
		TaxonomicChecklistMetadata checkList = table.getTaxonomicChecklistMetadata();
		Integer checklistId = checkList.getChecklistId();
		
		Taxon t = TAXON.as("t");
		
		Factory create = getJooqFactory();
		SelectQuery select = create.selectQuery();
		select.addSelect( t.TAXON_ID, t.TAXON_PARENT_ID , t.SCIENTIFIC_NAME );
		select.addFrom( t );
		select.addConditions( t.CHECKLIST_ID.eq(checklistId) );
		
		Insert<Record> insert = create
				.insertInto(table, table.ID, table.PARENT_ID, table.LABEL)
				.select(select);
		insert.execute();
	}
	
}
