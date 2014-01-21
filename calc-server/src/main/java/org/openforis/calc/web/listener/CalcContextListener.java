/**
 * 
 */
package org.openforis.calc.web.listener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.lang3.StringUtils;
import org.openforis.calc.persistence.DatabaseInitializer;
import org.openforis.calc.persistence.DatabaseInitializer.DatabaseInitializationException;

/**
 * Bootstrap context listener that initializes the 
 * 
 * @author S. Ricci
 *
 */
public class CalcContextListener implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent event) {
		ServletContext context = event.getServletContext();
		try {
			initDatabase(context);
		} catch ( Exception e ) {
			throw new RuntimeException("Error initializing the web application: " + e.getMessage(), e);
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		//do nothing
	}
	
	private void initDatabase(ServletContext context) throws DatabaseInitializationException {
		String driver = getParameter(context, "calc.jdbc.driver");
		String url = getParameter(context, "calc.jdbc.url");
		String host = getParameter(context, "calc.jdbc.host");
		String port = getParameter(context, "calc.jdbc.port");
		String username = getParameter(context, "calc.jdbc.superusername");
		String password = getParameter(context, "calc.jdbc.superuserpassword");
		
		DatabaseInitializer dbInitializer = new DatabaseInitializer(driver, url, host, port, username, password);
		if ( ! dbInitializer.isDBInitialized() ) {
			dbInitializer.initDB();
		}
	}

	private String getParameter(ServletContext context, String name) {
		String value = (String) context.getInitParameter(name);
		if ( StringUtils.isBlank(value) ) {
			throw new IllegalArgumentException("Context parameter not found: " + name);
		} else {
			//replace nested parameters with values
			Pattern nestedParameterPattern = Pattern.compile("\\$\\{((\\w|-|_|\\.)+)\\}");
			Matcher matcher = nestedParameterPattern.matcher(value);
			boolean hasNestedParameters = false;
			StringBuffer sb = new StringBuffer();
			while ( matcher.find() ) {
				hasNestedParameters = true;
			    String nestedParameterName = matcher.group(1);
			    String nestedValue = getParameter(context, nestedParameterName);
			    matcher.appendReplacement(sb, nestedValue);
			}
			if ( hasNestedParameters ) {
				value = sb.toString();
			}
			return value;
		}
	}

}
