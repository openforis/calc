/**
 * 
 */
package org.openforis.calc.persistence;

import static org.openforis.calc.persistence.jooq.Tables.*;

import org.jooq.Insert;
import org.jooq.SelectQuery;
import org.jooq.impl.Factory;
import org.openforis.calc.model.AoiHierarchyLevelMetadata;
import org.openforis.calc.model.AoiHierarchyMetadata;
import org.openforis.calc.model.VariableMetadata;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.rolap.AoiDimensionRecord;
import org.openforis.calc.persistence.jooq.rolap.AoiDimensionTable;
import org.openforis.calc.persistence.jooq.rolap.CategoryDimensionTable;
import org.openforis.calc.persistence.jooq.tables.AoiHierarchy;
import org.openforis.calc.persistence.jooq.tables.AoiHierarchyLevel;
import org.openforis.calc.persistence.jooq.tables.Category;
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
			.values( DIMENSION_NA_ID, DIMENSION_NA_VALUE, DIMENSION_NA_VALUE)
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
		
		Insert<AoiDimensionRecord> insert = create
			.insertInto( table , table.ID, table.CODE, table.LABEL, table.PARENT_ID )
			.select( select );
		
		int r = insert.execute();
		
		return r;
	}
	
}
