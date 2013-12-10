package org.openforis.calc.r;

/**
 * 
 * @author Mino Togna
 * 
 */
public class DbSendQuery extends DbQuery {

	DbSendQuery(RScript previous, RVariable connection, Object query) {
		super(previous, "dbSendQuery", connection, query);
	}

}