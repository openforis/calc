/**
 * 
 */
package org.openforis.calc.r;

/**
 * @author Mino Togna
 *
 */
public class DbConnect extends RScript {
	
	DbConnect(RScript previous, RVariable driver, String host, String database, String user, String password, int port) {
		super(previous);
		
		// dbConnect(driver, host='localhost', dbname='calc', user='calc', password='calc', port=5432)
		
		append("dbConnect(");
		append(driver.toScript());
		append(", host=");
		append( escape(host) );
		append(", dbname=");
		append( escape(database) );
		append(", user=");
		append( escape(user) );
		append(", password=");
		append( escape(password) );
		append(", port=");
		append( port );
		append(")");
		
	}
	
}
