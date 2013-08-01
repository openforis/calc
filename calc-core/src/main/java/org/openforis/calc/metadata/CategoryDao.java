package org.openforis.calc.metadata;

import java.sql.Connection;

import javax.sql.DataSource;

import org.openforis.calc.persistence.jpa.AbstractJpaDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;

/**
 * 
 * @author S. Ricci
 *
 */
@Component
public class CategoryDao extends AbstractJpaDao<Category> {
	
	private static final String CATEGORY_TABLE_NAME = "category";
	private static final String CATEGORY_VARIABLE_ID_COL_NAME = "variable_id";
	private static final String CATEGORY_CODE_COL_NAME = "code";
	private static final String CATEGORY_NAME_COL_NAME = "name";
	private static final String CATEGORY_DESCR_COL_NAME = "description";
	private static final String CATEGORY_SORT_ORDER_COL_NAME = "sort_order";
	private static final String CATEGORY_ORIGINAL_ID_COL_NAME = "original_id";
	
	@Autowired
	private DataSource dataSource;

//	@Transactional
//	public void copyCodesIntoCategories(String inputSchema, String outputSchema,
//			int variableId, String codeTableName,
//			String codeColumnName, String codeTableLabelColumnName,
//			String codeTableDescriptionColumnName) {
//		
//		String codeTableIdColumnName = codeTableName + "_id";
//		
//		String insertQueryTemplate = 
//				"INSERT INTO %s.%s (%s, %s, %s, %s, %s, %s)" +
//				" SELECT %s, %s, %s, %s, %s, %s" +
//				" FROM %s.%s";
//		
//		String sql = String.format(insertQueryTemplate, 
//				quoteIdentifiers(outputSchema),
//				quoteIdentifiers(CATEGORY_TABLE_NAME), 
//				quoteIdentifiers(CATEGORY_VARIABLE_ID_COL_NAME),
//				quoteIdentifiers(CATEGORY_ORIGINAL_ID_COL_NAME), 
//				quoteIdentifiers(CATEGORY_CODE_COL_NAME),
//				quoteIdentifiers(CATEGORY_NAME_COL_NAME),
//				quoteIdentifiers(CATEGORY_DESCR_COL_NAME),
//				quoteIdentifiers(CATEGORY_SORT_ORDER_COL_NAME),
//				String.valueOf(variableId),
//				quoteIdentifiers(codeTableIdColumnName),
//				quoteIdentifiers(codeColumnName),
//				quoteIdentifiers(codeTableLabelColumnName),
//				quoteIdentifiers(codeTableDescriptionColumnName),
//				quoteIdentifiers(codeTableIdColumnName),
//				quoteIdentifiers(inputSchema),
//				quoteIdentifiers(codeTableName)
//		);
//
//		Connection c = getConnection();
//		try {
//			Statement stmt = c.createStatement();
//			stmt.execute(sql);
//		} catch(SQLException e) {
//			throw new RuntimeException(e);
//		}
//	}
	
	protected Connection getConnection() {
		return DataSourceUtils.getConnection(dataSource);
	}
	
}
