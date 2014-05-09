/**
 * 
 */
package org.openforis.calc.persistence.liquibase;

import java.io.IOException;
import java.sql.Connection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.core.SQLiteDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.integration.spring.SpringLiquibase;

import org.apache.commons.lang3.StringUtils;

/**
 * @author S. Ricci
 *
 */
public class DatabaseAwareSpringLiquibase extends SpringLiquibase {

	@Override
	protected Database createDatabase(Connection c) throws DatabaseException {
		Database database;
		String dbProductName = getDatabaseProductName();
		if ( SQLiteDatabase.PRODUCT_NAME.equals(dbProductName) ) {
			//schemas are not supported
			DatabaseFactory dbFactory = DatabaseFactory.getInstance();
			JdbcConnection jdbcConnection = new JdbcConnection(c);
			database = dbFactory.findCorrectDatabaseImplementation(jdbcConnection);
		} else {
			database = super.createDatabase(c);
		}
		return database;
	}

	private static final Pattern VARIABLE_PATTERN = Pattern.compile("[\\b\\(\\s]([A-Za-z_]+)[\\b\\)\\s]");

	public static void main(String[] args) throws IOException {
        
        String expr = "round(as.numeric(vegetation_type)/100) == 2";
        Set<String> variables = extractVariables(expr);
        for (String string : variables) {
                System.out.println(string);
        }
	}

	private static Set<String> extractVariables( String equation ) throws IOException {
        Set<String> variables = new HashSet<String>();

        if( StringUtils.isNotBlank(equation) ) {
                Matcher matcher = VARIABLE_PATTERN.matcher( equation );
                while( matcher.find() ) {
                        String var = matcher.group( 1 );
//                      if( !r.getBaseFunctions().contains( var ) ) {
                                variables.add( var );
//                      }
                }
        }
        
        return variables;
	}
	
	
}
