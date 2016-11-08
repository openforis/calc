/**
 * 
 */
package org.openforis.jooq.codegen;

import java.util.List;

import org.jooq.Configuration;
import org.jooq.impl.DAOImpl;
import org.jooq.tools.JooqLogger;
import org.jooq.util.ColumnDefinition;
import org.jooq.util.GeneratorStrategy.Mode;
import org.jooq.util.JavaGenerator;
import org.jooq.util.JavaWriter;
import org.jooq.util.TableDefinition;
import org.jooq.util.UniqueKeyDefinition;

/**
 * @author Mino Togna
 * @author S. Ricci
 */
public class CustomGenerator extends JavaGenerator {
	
    private static final JooqLogger log                          = JooqLogger.getLogger(CustomGenerator.class);

    
	 protected void generateDao(TableDefinition table) {
	        final String className = getStrategy().getJavaClassName(table, Mode.DAO);
	        final String tableRecord = getStrategy().getFullJavaClassName(table, Mode.RECORD);
	        final String daoImpl = DAOImpl.class.getName();
	        final String tableIdentifier = getStrategy().getFullJavaIdentifier(table);

	        String tType = "Void";
//	        String pType = getStrategy().getFullJavaClassName(table, Mode.POJO);
	        String pType = getStrategy().getJavaClassName(table, null);

	        UniqueKeyDefinition key = table.getPrimaryKey();
	        ColumnDefinition keyColumn = null;

	        if (key != null) {
	            List<ColumnDefinition> columns = key.getKeyColumns();

	            if (columns.size() == 1) {
	                keyColumn = columns.get(0);
	                tType = getJavaType(keyColumn.getType());
	            }
	        }

	        // [#2573] Skip DAOs for tables that don't have 1-column-PKs (for now)
	        if (keyColumn == null) {
	            log.info("Skipping DAO generation", getStrategy().getFileName(table, Mode.DAO));
	            return;
	        }
	        else {
	            log.info("Generating DAO", getStrategy().getFileName(table, Mode.DAO));
	        }

	        JavaWriter out = new JavaWriter(getStrategy().getFile(table, Mode.DAO) , null);
	        printPackage(out, table, Mode.DAO);
	        printClassJavadoc(out, table);

	        out.println("public class %s extends %s<%s, %s, %s> {", className, daoImpl, tableRecord, pType, tType);

	        // Default constructor
	        // -------------------
	        out.tab(1).javadoc("Create a new %s without any configuration", className);
	        out.tab(1).println("public %s() {", className);
	        out.tab(2).println("super(%s, %s.class);", tableIdentifier, pType);
	        out.tab(1).println("}");

	        // Initialising constructor
	        // ------------------------
	        out.tab(1).javadoc("Create a new %s with an attached configuration", className);
	        out.tab(1).println("public %s(%s configuration) {", className, Configuration.class);
	        out.tab(2).println("super(%s, %s.class, configuration);", tableIdentifier, pType);
	        out.tab(1).println("}");

	        // Template method implementations
	        // -------------------------------
	        out.tab(1).overrideInherit();
	        out.tab(1).println("protected %s getId(%s object) {", tType, pType);
	        out.tab(2).println("return object.%s();", getStrategy().getJavaGetterName(keyColumn, Mode.POJO));
	        out.tab(1).println("}");

	        for (ColumnDefinition column : table.getColumns()) {
	            final String colName = column.getOutputName();
	            final String colClass = getStrategy().getJavaClassName(column, Mode.POJO);
	            final String colType = getJavaType(column.getType());
	            final String colIdentifier = getStrategy().getFullJavaIdentifier(column);

	            // fetchBy[Column]([T]...)
	            // -----------------------
	            out.tab(1).javadoc("Fetch records that have <code>%s IN (values)</code>", colName);
	            out.tab(1).println("public %s<%s> fetchBy%s(%s... values) {", List.class, pType, colClass, colType);
	            out.tab(2).println("return fetch(%s, values);", colIdentifier);
	            out.tab(1).println("}");

	            // fetchOneBy[Column]([T])
	            // -----------------------
	            ukLoop:
	            for (UniqueKeyDefinition uk : column.getUniqueKeys()) {

	                // If column is part of a single-column unique key...
	                if (uk.getKeyColumns().size() == 1 && uk.getKeyColumns().get(0).equals(column)) {
	                    out.tab(1).javadoc("Fetch a unique record that has <code>%s = value</code>", colName);
	                    out.tab(1).println("public %s fetchOneBy%s(%s value) {", pType, colClass, colType);
	                    out.tab(2).println("return fetchOne(%s, value);", colIdentifier);
	                    out.tab(1).println("}");

	                    break ukLoop;
	                }
	            }
	        }

	        out.println("}");
	        out.close();
	    }
}
