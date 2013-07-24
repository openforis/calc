package org.openforis.calc.persistence;

import static org.openforis.calc.persistence.sql.Sql.quoteIdentifier;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.openforis.calc.metadata.Category;
import org.openforis.calc.persistence.jpa.AbstractDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author S. Ricci
 *
 */
@Component
public class CategoryDao extends AbstractDao<Category> {
	
	private static final String CATEGORY_TABLE_NAME = "category";
	private static final String CATEGORY_VARIABLE_ID_COL_NAME = "variable_id";
	private static final String CATEGORY_CODE_COL_NAME = "code";
	private static final String CATEGORY_NAME_COL_NAME = "name";
	private static final String CATEGORY_DESCR_COL_NAME = "description";
	private static final String CATEGORY_SORT_ORDER_COL_NAME = "sort_order";
	private static final String CATEGORY_ORIGINAL_ID_COL_NAME = "original_id";
	
	@Autowired
	private DataSource dataSource;

	@Transactional
	public void copyCodesIntoCategories(String inputSchema, String outputSchema,
			int variableId, String codeTableName,
			String codeColumnName, String codeTableLabelColumnName,
			String codeTableDescriptionColumnName) {
		
		String codeTableIdColumnName = codeTableName + "_id";
		
		String insertQueryTemplate = 
				"INSERT INTO %s.%s (%s, %s, %s, %s, %s, %s)" +
				" SELECT %s, %s, %s, %s, %s, %s" +
				" FROM %s.%s";
		
		String sql = String.format(insertQueryTemplate, 
				quoteIdentifier(outputSchema),
				quoteIdentifier(CATEGORY_TABLE_NAME), 
				quoteIdentifier(CATEGORY_VARIABLE_ID_COL_NAME),
				quoteIdentifier(CATEGORY_ORIGINAL_ID_COL_NAME), 
				quoteIdentifier(CATEGORY_CODE_COL_NAME),
				quoteIdentifier(CATEGORY_NAME_COL_NAME),
				quoteIdentifier(CATEGORY_DESCR_COL_NAME),
				quoteIdentifier(CATEGORY_SORT_ORDER_COL_NAME),
				String.valueOf(variableId),
				quoteIdentifier(codeTableIdColumnName),
				quoteIdentifier(codeColumnName),
				quoteIdentifier(codeTableLabelColumnName),
				quoteIdentifier(codeTableDescriptionColumnName),
				quoteIdentifier(codeTableIdColumnName),
				quoteIdentifier(inputSchema),
				quoteIdentifier(codeTableName)
		);

		Connection c = getConnection();
		try {
			Statement stmt = c.createStatement();
			stmt.execute(sql);
		} catch(SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	protected Connection getConnection() {
		return DataSourceUtils.getConnection(dataSource);
	}
	
}
