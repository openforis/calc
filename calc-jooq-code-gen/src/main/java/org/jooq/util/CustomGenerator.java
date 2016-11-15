/**
 * 
 */
package org.jooq.util;

import java.util.List;

import org.jooq.Configuration;
import org.jooq.Constants;
import org.jooq.Record;
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

    private boolean scala = false;
    
    protected void generateDao(TableDefinition table, JavaWriter out) {
        UniqueKeyDefinition key = table.getPrimaryKey();
        if (key == null) {
            log.info("Skipping DAO generation", out.file().getName());
            return;
        }

        final String className = getStrategy().getJavaClassName(table, Mode.DAO);
        final List<String> interfaces = out.ref(getStrategy().getJavaClassImplements(table, Mode.DAO));
        final String tableRecord = out.ref(getStrategy().getFullJavaClassName(table, Mode.RECORD));
        final String daoImpl = out.ref(DAOImpl.class);
        final String tableIdentifier = out.ref(getStrategy().getFullJavaIdentifier(table), 2);

        String tType = (scala ? "Unit" : "Void");
        //String pType = out.ref(getStrategy().getFullJavaClassName(table, Mode.POJO));
        String pType = getStrategy().getJavaClassName(table, null);
        
        List<ColumnDefinition> keyColumns = key.getKeyColumns();

        if (keyColumns.size() == 1) {
            tType = getJavaType(keyColumns.get(0).getType());
        }
        else if (keyColumns.size() <= Constants.MAX_ROW_DEGREE) {
            String generics = "";
            String separator = "";

            for (ColumnDefinition column : keyColumns) {
                generics += separator + out.ref(getJavaType(column.getType()));
                separator = ", ";
            }

            if (scala)
            	tType = Record.class.getName() + keyColumns.size() + "[" + generics + "]";
            else
                tType = Record.class.getName() + keyColumns.size() + "<" + generics + ">";
        }
        else {
            tType = Record.class.getName();
        }

        tType = out.ref(tType);

        printPackage(out, table, Mode.DAO);

        String packageName = getPackageName( pType );
		out.println("import "+packageName+"." + pType + ";" );
        
        generateDaoClassJavadoc(table, out);
        printClassAnnotations(out, table.getSchema());

        if (scala)
            out.println("class %s(configuration : %s) extends %s[%s, %s, %s](%s, classOf[%s], configuration)[[before= with ][%s]] {",
                    className, Configuration.class, daoImpl, tableRecord, pType, tType, tableIdentifier, pType, interfaces);
        else
            out.println("public class %s extends %s<%s, %s, %s>[[before= implements ][%s]] {", className, daoImpl, tableRecord, pType, tType, interfaces);

        // Default constructor
        // -------------------
        out.tab(1).javadoc("Create a new %s without any configuration", className);

        if (scala) {
            out.tab(1).println("def this() = {");
            out.tab(2).println("this(null)");
            out.tab(1).println("}");
        }
        else {
            out.tab(1).println("public %s() {", className);
            out.tab(2).println("super(%s, %s.class);", tableIdentifier, pType);
            out.tab(1).println("}");
        }

        // Initialising constructor
        // ------------------------

        if (scala) {
        }
        else {
            out.tab(1).javadoc("Create a new %s with an attached configuration", className);
            out.tab(1).println("public %s(%s configuration) {", className, Configuration.class);
            out.tab(2).println("super(%s, %s.class, configuration);", tableIdentifier, pType);
            out.tab(1).println("}");
        }

        // Template method implementations
        // -------------------------------
        if (scala) {
            out.println();
            out.tab(1).println("override protected def getId(o : %s) : %s = {", pType, tType);
        }
        else {
            out.tab(1).overrideInherit();
            out.tab(1).println("protected %s getId(%s object) {", tType, pType);
        }

        if (keyColumns.size() == 1) {
        	if (scala)
                out.tab(2).println("o.%s", getStrategy().getJavaGetterName(keyColumns.get(0), Mode.POJO));
        	else
        	    out.tab(2).println("return object.%s();", getStrategy().getJavaGetterName(keyColumns.get(0), Mode.POJO));
        }

        // [#2574] This should be replaced by a call to a method on the target table's Key type
        else {
            String params = "";
            String separator = "";

            for (ColumnDefinition column : keyColumns) {
            	if (scala)
            		params += separator + "o." + getStrategy().getJavaGetterName(column, Mode.POJO);
            	else
            	    params += separator + "object." + getStrategy().getJavaGetterName(column, Mode.POJO) + "()";

                separator = ", ";
            }

            if (scala)
            	out.tab(2).println("compositeKeyRecord(%s)", params);
            else
                out.tab(2).println("return compositeKeyRecord(%s);", params);
        }

        out.tab(1).println("}");

        for (ColumnDefinition column : table.getColumns()) {
            final String colName = column.getOutputName();
            final String colClass = getStrategy().getJavaClassName(column);
            final String colType = out.ref(getJavaType(column.getType()));
            final String colIdentifier = out.ref(getStrategy().getFullJavaIdentifier(column), colRefSegments(column));

            // fetchBy[Column]([T]...)
            // -----------------------
            out.tab(1).javadoc("Fetch records that have <code>%s IN (values)</code>", colName);

            if (scala) {
                out.tab(1).println("def fetchBy%s(values : %s*) : %s[%s] = {", colClass, colType, List.class, pType);
                out.tab(2).println("fetch(%s, values:_*)", colIdentifier);
                out.tab(1).println("}");
            }
            else {
                out.tab(1).println("public %s<%s> fetchBy%s(%s... values) {", List.class, pType, colClass, colType);
                out.tab(2).println("return fetch(%s, values);", colIdentifier);
                out.tab(1).println("}");
            }

            // fetchOneBy[Column]([T])
            // -----------------------
            ukLoop:
            for (UniqueKeyDefinition uk : column.getUniqueKeys()) {

                // If column is part of a single-column unique key...
                if (uk.getKeyColumns().size() == 1 && uk.getKeyColumns().get(0).equals(column)) {
                    out.tab(1).javadoc("Fetch a unique record that has <code>%s = value</code>", colName);

                    if (scala) {
                        out.tab(1).println("def fetchOneBy%s(value : %s) : %s = {", colClass, colType, pType);
                        out.tab(2).println("fetchOne(%s, value)", colIdentifier);
                        out.tab(1).println("}");
                    }
                    else {
                        out.tab(1).println("public %s fetchOneBy%s(%s value) {", pType, colClass, colType);
                        out.tab(2).println("return fetchOne(%s, value);", colIdentifier);
                        out.tab(1).println("}");
                    }

                    break ukLoop;
                }
            }
        }

        generateDaoClassFooter(table, out);
        out.println("}");
    }
    
    private String getPackageName(String pType) {
    	String pName = "org.openforis.calc.metadata";
    	if( pType.equals("CalculationStep") || pType.equals("ProcessingChain")){
    		pName = "org.openforis.calc.chain";
    	} else if( pType.equals("SystemProperty") ){
    		pName = "org.openforis.calc.system";
    	} else if( pType.equals("Workspace") ){
    		pName = "org.openforis.calc.engine";
    	}
		return pName; 
	}

	private int colRefSegments(TypedElementDefinition<?> column) {
        if (column != null && column.getContainer() instanceof UDTDefinition)
            return 2;

        if (!getStrategy().getInstanceFields())
            return 2;

        return 3;
    }
    
    //import org.openforis.calc.metadata.
    
//	 protected void generateDao(TableDefinition table) {
//	        final String className = getStrategy().getJavaClassName(table, Mode.DAO);
//	        final String tableRecord = getStrategy().getFullJavaClassName(table, Mode.RECORD);
//	        final String daoImpl = DAOImpl.class.getName();
//	        final String tableIdentifier = getStrategy().getFullJavaIdentifier(table);
//
//	        String tType = "Void";
////	        String pType = getStrategy().getFullJavaClassName(table, Mode.POJO);
//	        String pType = getStrategy().getJavaClassName(table, null);
//
//	        UniqueKeyDefinition key = table.getPrimaryKey();
//	        ColumnDefinition keyColumn = null;
//
//	        if (key != null) {
//	            List<ColumnDefinition> columns = key.getKeyColumns();
//
//	            if (columns.size() == 1) {
//	                keyColumn = columns.get(0);
//	                tType = getJavaType(keyColumn.getType());
//	            }
//	        }
//
//	        // [#2573] Skip DAOs for tables that don't have 1-column-PKs (for now)
//	        if (keyColumn == null) {
//	            log.info("Skipping DAO generation", getStrategy().getFileName(table, Mode.DAO));
//	            return;
//	        }
//	        else {
//	            log.info("Generating DAO", getStrategy().getFileName(table, Mode.DAO));
//	        }
//
//	        JavaWriter out = new JavaWriter(getStrategy().getFile(table, Mode.DAO) , null);
//	        printPackage(out, table, Mode.DAO);
//	        printClassJavadoc(out, table);
//
//	        out.println("public class %s extends %s<%s, %s, %s> {", className, daoImpl, tableRecord, pType, tType);
//
//	        // Default constructor
//	        // -------------------
//	        out.tab(1).javadoc("Create a new %s without any configuration", className);
//	        out.tab(1).println("public %s() {", className);
//	        out.tab(2).println("super(%s, %s.class);", tableIdentifier, pType);
//	        out.tab(1).println("}");
//
//	        // Initialising constructor
//	        // ------------------------
//	        out.tab(1).javadoc("Create a new %s with an attached configuration", className);
//	        out.tab(1).println("public %s(%s configuration) {", className, Configuration.class);
//	        out.tab(2).println("super(%s, %s.class, configuration);", tableIdentifier, pType);
//	        out.tab(1).println("}");
//
//	        // Template method implementations
//	        // -------------------------------
//	        out.tab(1).overrideInherit();
//	        out.tab(1).println("protected %s getId(%s object) {", tType, pType);
//	        out.tab(2).println("return object.%s();", getStrategy().getJavaGetterName(keyColumn, Mode.POJO));
//	        out.tab(1).println("}");
//
//	        for (ColumnDefinition column : table.getColumns()) {
//	            final String colName = column.getOutputName();
//	            final String colClass = getStrategy().getJavaClassName(column, Mode.POJO);
//	            final String colType = getJavaType(column.getType());
//	            final String colIdentifier = getStrategy().getFullJavaIdentifier(column);
//
//	            // fetchBy[Column]([T]...)
//	            // -----------------------
//	            out.tab(1).javadoc("Fetch records that have <code>%s IN (values)</code>", colName);
//	            out.tab(1).println("public %s<%s> fetchBy%s(%s... values) {", List.class, pType, colClass, colType);
//	            out.tab(2).println("return fetch(%s, values);", colIdentifier);
//	            out.tab(1).println("}");
//
//	            // fetchOneBy[Column]([T])
//	            // -----------------------
//	            ukLoop:
//	            for (UniqueKeyDefinition uk : column.getUniqueKeys()) {
//
//	                // If column is part of a single-column unique key...
//	                if (uk.getKeyColumns().size() == 1 && uk.getKeyColumns().get(0).equals(column)) {
//	                    out.tab(1).javadoc("Fetch a unique record that has <code>%s = value</code>", colName);
//	                    out.tab(1).println("public %s fetchOneBy%s(%s value) {", pType, colClass, colType);
//	                    out.tab(2).println("return fetchOne(%s, value);", colIdentifier);
//	                    out.tab(1).println("}");
//
//	                    break ukLoop;
//	                }
//	            }
//	        }
//
//	        out.println("}");
//	        out.close();
//	    }
}
