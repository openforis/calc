/**
 * 
 */
package org.openforis.calc.persistence;

import java.util.Collection;

import org.jooq.Query;
import org.jooq.impl.Factory;
import org.openforis.calc.model.VariableMetadata;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.tables.Category;
import org.openforis.calc.persistence.jooq.tables.Variable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Mino Togna
 *
 */
@SuppressWarnings("rawtypes")
@Component 
@Transactional
public class OlapDimensionDao extends JooqDaoSupport {

	private static final String DIMENSION_NA_VALUE = "No Data";
	private static final String DIMENSION_NA_ID = "-1";
	private static Category C = Category.CATEGORY;
	private static Variable V = Variable.VARIABLE;
	
 	@SuppressWarnings("unchecked")
	public OlapDimensionDao() {
		super(null, null);
	}

 	@Transactional
	public void generateOlapDimensions(String surveyName, Collection<VariableMetadata> variables) {
		startBatch();
		Factory create = getBatchFactory();
		
		/**


CREATE TABLE "naforma1"."accessibility"  ( 
	"category_id"   	integer NOT NULL,
	"category_code" 	varchar(256) NULL,
	"category_label"	varchar(256) NULL 
	);

insert into naforma1.accessibility
select
    category_id,
    category_code,
    category_label
from
    category c
inner join
    variable v on v.variable_id = c.variable_id and v.variable_id = 3    

		 */
		for ( VariableMetadata variable : variables ) {
			if( variable.isCategorical() && variable.isForAnalysis() ) {
				String variableName = variable.getVariableName();
				Integer variableId = variable.getVariableId();
				String tableName = surveyName+"."+ variableName ;
				
				Query dropTable = getDropTableQuery(create, tableName);
				addQueryToBatch(dropTable);
				
				Query createTable = getCreateTableQuery(create, tableName);
				addQueryToBatch(createTable);
				
				Query insertValues = getInsertValuesQuery(create, variableId, tableName);
				addQueryToBatch(insertValues);
				
				Query insertNaValue = getInsertNaValueQuery(create, variableId, tableName);
				addQueryToBatch(insertNaValue);
			}
		}
		
		executeBatch();
	}

	private Query getDropTableQuery(Factory create, String tableName) {
		StringBuffer sb = new StringBuffer();
		sb.append("drop table if exists ");
		sb.append(tableName);
		Query dropTable = create.query( sb.toString() );
		return dropTable;
	}

	private Query getInsertValuesQuery(Factory create, Integer variableId, String tableName) {
		StringBuilder sb = new StringBuilder();
		sb.append("insert into ");
		sb.append(tableName);
		sb.append(" select ");
		sb.append("c.");
		sb.append(C.CATEGORY_ID.getName());
		sb.append(",");
		sb.append("c.");
		sb.append(C.CATEGORY_CODE.getName());
		sb.append(",");
		sb.append("c.");
		sb.append(C.CATEGORY_LABEL.getName());				
		sb.append(" from ");
		sb.append(C.getName());
		sb.append(" c");
		sb.append(" inner join ");
		sb.append(V.getName());
		sb.append(" v ");
		sb.append("on ");
		sb.append("c.");
		sb.append(C.VARIABLE_ID.getName());
		sb.append(" = ");
		sb.append("v.");
		sb.append(V.VARIABLE_ID.getName());
		sb.append(" and ");
		sb.append("v.");
		sb.append(V.VARIABLE_ID.getName());
		sb.append(" = ");
		sb.append(variableId);
		Query insertValues = create.query( sb.toString() );
		return insertValues;
	}

	private Query getInsertNaValueQuery(Factory create, Integer variableId, String tableName) {
		StringBuilder sb = new StringBuilder();
		sb.append("insert into ");
		sb.append(tableName);
		sb.append("(");
		sb.append(C.CATEGORY_ID.getName());
		sb.append(",");
		sb.append(C.CATEGORY_CODE.getName());
		sb.append(",");
		sb.append(C.CATEGORY_LABEL.getName());
		sb.append(")");
		sb.append(" values");
		sb.append("(");
		sb.append(DIMENSION_NA_ID);
		sb.append(",");
		sb.append("'");
		sb.append(DIMENSION_NA_VALUE);
		sb.append("'");
		sb.append(",");
		sb.append("'");
		sb.append(DIMENSION_NA_VALUE);
		sb.append("'");
		sb.append(")");
		
		Query insertValues = create.query( sb.toString() );
		return insertValues;
	}
	
	private Query getCreateTableQuery(Factory create, String tableName) {
		StringBuilder sb = new StringBuilder();
		sb.append("create table ");
		sb.append(tableName);
		sb.append(" ( ");
		sb.append("\""+C.CATEGORY_ID.getName()+"\"   	integer NOT NULL,");
		sb.append("\""+C.CATEGORY_CODE.getName()+"\" 	varchar(256) NULL,");
		sb.append("\""+C.CATEGORY_LABEL.getName()+"\"	varchar(256) NULL");
		sb.append(" );");
		String createTableSql = sb.toString();
		
		Query createTable = create.query(createTableSql);
		return createTable;
	}
	
}
