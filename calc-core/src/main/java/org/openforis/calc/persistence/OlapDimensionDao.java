/**
 * 
 */
package org.openforis.calc.persistence;
import static org.openforis.calc.persistence.jooq.Tables.AOI;
import static org.openforis.calc.persistence.jooq.Tables.AOI_HIERARCHY;
import static org.openforis.calc.persistence.jooq.Tables.AOI_HIERARCHY_LEVEL;
import static org.openforis.calc.persistence.jooq.Tables.SURVEY;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jooq.Insert;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SelectQuery;
import org.jooq.impl.Factory;
import org.openforis.calc.model.ObservationUnitMetadata;
import org.openforis.calc.model.SurveyMetadata;
import org.openforis.calc.model.VariableMetadata;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.JooqTableGenerator;
import org.openforis.calc.persistence.jooq.olap.DimensionRecord;
import org.openforis.calc.persistence.jooq.olap.DimensionTable;
import org.openforis.calc.persistence.jooq.tables.AoiHierarchy;
import org.openforis.calc.persistence.jooq.tables.AoiHierarchyLevel;
import org.openforis.calc.persistence.jooq.tables.Category;
import org.openforis.calc.persistence.jooq.tables.Survey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author M. Togna
 *
 */
@SuppressWarnings("rawtypes")
@Component 
@Transactional
public class OlapDimensionDao extends JooqDaoSupport {

	private static final String DIMENSION_NA_VALUE = "No Data";
	private static final String DIMENSION_NA_ID = "-1";
	private static final Category C = Category.CATEGORY;
//	private static final Variable V = Variable.VARIABLE;
	
	private static final org.openforis.calc.persistence.jooq.tables.Aoi A = AOI;
	private static final org.openforis.calc.persistence.jooq.tables.AoiHierarchyLevel L = AOI_HIERARCHY_LEVEL;
	private static final org.openforis.calc.persistence.jooq.tables.AoiHierarchy H = AOI_HIERARCHY;
	private static final org.openforis.calc.persistence.jooq.tables.Survey S = SURVEY;
	
	@Autowired
	private JooqTableGenerator jooqTableGenerator;
	
 	@SuppressWarnings("unchecked")
	public OlapDimensionDao() {
		super(null, null);
	}

 	@Transactional
	public void createAoiOlapDimensionTables(int surveyId) {
		List<DimensionTable> aoiDimTables = getAoiDimensionTables(surveyId);
		creteAoiDimensionTables(aoiDimTables);
 	}
 	
 	@Transactional(propagation = Propagation.REQUIRES_NEW)
	private void creteAoiDimensionTables(List<DimensionTable> aoiDimTables) {
		for ( DimensionTable aoiDimTable : aoiDimTables ) {
			jooqTableGenerator.create(aoiDimTable);
		}
	}
 	
 	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void createVariableDimensionTables(Collection<VariableMetadata> variables) {

 		for ( VariableMetadata variable : variables ) {
 			if( variable.isCategorical() && variable.isForAnalysis() ) {
 				DimensionTable dimTable = getDimensionTable(variable);
 				jooqTableGenerator.create(dimTable);
 			}
		}
 		
	}

 	private DimensionTable getDimensionTable(VariableMetadata variable){
 		ObservationUnitMetadata obsUnitMetadata = variable.getObservationUnitMetadata();
 		SurveyMetadata surveyMetadata = obsUnitMetadata.getSurveyMetadata();
 		String surveyName = surveyMetadata.getSurveyName();
 		
 		String varName = variable.getVariableName();
 		
 		DimensionTable dimTable = new DimensionTable(varName, surveyName);
 		return dimTable;
 	}
 	
 	@Transactional
	public void populateDimensionTables(int surveyId, Collection<VariableMetadata> variables) {
 		for ( VariableMetadata variable : variables ) {
 			if( variable.isCategorical() && variable.isForAnalysis() ) {
 				DimensionTable dimTable = getDimensionTable(variable);
 				populateDimensionTable(dimTable, variable.getVariableId());
 			}
		}
 	}
 	
 	@Transactional
 	private void populateDimensionTable(DimensionTable dimTable, Integer variableId) {
 		Factory create = getJooqFactory();
		Category c = C.as("c");
		
 		SelectQuery select = create.selectQuery();
 		select.addSelect(c.CATEGORY_ID, c.CATEGORY_CODE, c.CATEGORY_LABEL);
 		select.addFrom(c);
 		select.addConditions(c.VARIABLE_ID.eq( variableId));
 		
 		create
 			.insertInto(dimTable, dimTable.ID, dimTable.CODE, dimTable.LABEL)
 			.select(select)
 			.execute();
 		
 		create
			.insertInto(dimTable, dimTable.ID, dimTable.CODE, dimTable.LABEL)
			.values( DIMENSION_NA_ID, DIMENSION_NA_VALUE, DIMENSION_NA_VALUE)
			.execute();
	}

	/**
	 * 
	 * @return
	 */
	@Transactional
	private List<DimensionTable> getAoiDimensionTables(int surveyId) {
		List<DimensionTable> dimTables = new ArrayList<DimensionTable>();
		
		Factory create = getJooqFactory();
		AoiHierarchyLevel l = L.as("l");
		AoiHierarchy h = H.as("h");
		Survey s = S.as("s");
//		select l.aoi_hierarchy_level_rank, l.aoi_hierarchy_level_name from  calc.aoi_hierarchy_level l;
		Result<Record> result = create
			.selectDistinct(l.AOI_HIERARCHY_LEVEL_ID, l.AOI_HIERARCHY_LEVEL_RANK, l.AOI_HIERARCHY_LEVEL_NAME, s.SURVEY_NAME)
			.from(l)
			.join(h)
			.on( l.AOI_HIERARCHY_ID.eq(h.AOI_HIERARCHY_ID) )
			.join(s)
			.on( h.SURVEY_ID.eq(s.SURVEY_ID) )
			.where( h.SURVEY_ID.eq(surveyId) )
			.fetch();
		for ( Record record : result ) {
			
			String name = record.getValue(l.AOI_HIERARCHY_LEVEL_NAME);
			String surveyName = record.getValue(s.SURVEY_NAME);
			DimensionTable dimTable = new DimensionTable(name, surveyName);
			
			dimTables.add(dimTable);
		}
			
		return dimTables;
	}
 	
 	@Transactional
	public void populateAoiDimensionTables(int surveyId) {
		List<DimensionTable> aoiDimTables = getAoiDimensionTables(surveyId);
		for ( DimensionTable aoiDimTable : aoiDimTables ) {
			populateAoiDimensionTable(surveyId, aoiDimTable);
		}
 	}
 	
	@Transactional
	private int populateAoiDimensionTable(int surveyId, DimensionTable dimensionTable) {
		Factory create = getJooqFactory();
		
		String dimTableName = dimensionTable.getName();
		
//		Factory create = getJooqFactory();
		AoiHierarchyLevel l = L.as("l");
		AoiHierarchy h = H.as("h");
		org.openforis.calc.persistence.jooq.tables.Aoi a = A.as("a");
		
		SelectQuery select = create.selectQuery();
			select.addSelect( a.AOI_ID , a.AOI_CODE , a.AOI_LABEL );
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
		
		Insert<DimensionRecord> insert = create
			.insertInto( dimensionTable , dimensionTable.ID, dimensionTable.CODE, dimensionTable.LABEL )
			.select( select );
		
		
		int r = insert.execute();
		
		return r;
	}
	
}
