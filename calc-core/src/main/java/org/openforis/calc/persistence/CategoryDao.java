package org.openforis.calc.persistence;

import java.sql.Connection;

import javax.sql.DataSource;

import org.jooq.Insert;
import org.jooq.Record;
import org.jooq.SelectJoinStep;
import org.jooq.TableField;
import org.jooq.impl.Factory;
import org.jooq.impl.SchemaImpl;
import org.jooq.impl.UpdatableTableImpl;
import org.openforis.calc.metadata.Category;
import org.openforis.calc.persistence.jpa.AbstractDao;
import org.openforis.collect.persistence.jooq.DialectAwareJooqFactory;
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
	private static final String CATEGORY_SORT_ORDER_COL_NAME = "sort_order";
	
	@Autowired
	private DataSource dataSource;

	@Transactional
	public void copyCodesIntoCategories(String inputSchema, String outputSchema,
			int variableId, String codeTableName, String codeColumnName, String descriptionColumnName) {
		Factory jf = getJooqFactory();

		DynamicTable codeTable = new DynamicTable(codeTableName, inputSchema);
		TableField<Record, String> codeField = codeTable.createStringField(codeColumnName);
		
		SelectJoinStep codesSelect = jf.select(Factory.val(variableId), codeField, Factory.rowNumber().over()).from(codeTable);
		
		DynamicTable categoryTable = new DynamicTable(CATEGORY_TABLE_NAME, outputSchema);
		TableField<Record, String> categoryCodeField = categoryTable.createStringField(CATEGORY_CODE_COL_NAME);
		TableField<Record, Integer> categoryVariableIdField = categoryTable.createIntegerField(CATEGORY_VARIABLE_ID_COL_NAME);
		TableField<Record, Integer> categorySortOrderField = categoryTable.createIntegerField(CATEGORY_SORT_ORDER_COL_NAME);
		
		Insert<Record> insert = 
				jf.insertInto(categoryTable, categoryVariableIdField, categoryCodeField, categorySortOrderField)
				.select(codesSelect);
		insert.execute();
	}
	
	protected DialectAwareJooqFactory getJooqFactory() {
		Connection conn = DataSourceUtils.getConnection(dataSource);
		return new DialectAwareJooqFactory(conn);
	}
	
	static class DynamicTable extends UpdatableTableImpl<Record> {

		private static final long serialVersionUID = 1L;
		
		public DynamicTable(String name, String schema) {
			super(name, new SchemaImpl(schema));
		}
		
		public TableField<Record, String> createStringField(String name) {
			return createField(name, org.jooq.impl.SQLDataType.VARCHAR, this);
		}

		public TableField<Record, Integer> createIntegerField(String name) {
			return createField(name, org.jooq.impl.SQLDataType.INTEGER, this);
		}
		
	}

}
