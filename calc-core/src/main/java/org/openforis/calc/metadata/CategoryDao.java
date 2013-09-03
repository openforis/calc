package org.openforis.calc.metadata;

import static org.openforis.calc.persistence.jooq.tables.CategoryTable.CATEGORY;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.openforis.calc.persistence.jpa.AbstractJpaDao;
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
public class CategoryDao extends AbstractJpaDao<Category> {
	
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
				quoteIdentifiers(outputSchema),
				quoteIdentifiers(CATEGORY.getName()), 
				quoteIdentifiers(CATEGORY.VARIABLE_ID.getName()),
				quoteIdentifiers(CATEGORY.ORIGINAL_ID.getName()), 
				quoteIdentifiers(CATEGORY.CODE.getName()),
				quoteIdentifiers(CATEGORY.CAPTION.getName()),
				quoteIdentifiers(CATEGORY.DESCRIPTION.getName()),
				quoteIdentifiers(CATEGORY.SORT_ORDER.getName()),
				String.valueOf(variableId),
				quoteIdentifiers(codeTableIdColumnName),
				quoteIdentifiers(codeColumnName),
				quoteIdentifiers(codeTableLabelColumnName),
				quoteIdentifiers(codeTableDescriptionColumnName),
				quoteIdentifiers(codeTableIdColumnName),
				quoteIdentifiers(inputSchema),
				quoteIdentifiers(codeTableName)
		);

		Connection c = getConnection();
		try {
			Statement stmt = c.createStatement();
			stmt.execute(sql);
		} catch(SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	private Object quoteIdentifiers(String name) {
		return "\"" + name + "\"";
	}

	protected Connection getConnection() {
		return DataSourceUtils.getConnection(dataSource);
	}
	
}
