/**
 * 
 */
package org.openforis.calc.r;

import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

/**
 * @author M. Togna
 * 
 */
public class RClient {

	private RConnectionProperies serverProperies;
	private RConnection connection;

	private RClient() {
		serverProperies = new RConnectionProperies();
	}

	public static RClient getInstance() {
		return new RClient();
	}

	public void connect() throws RserveException {
		connection = new RConnection(serverProperies.getUrl(), serverProperies.getPort());
	}

	public void disconnect() {
		if ( isConnected() ) {
			connection.close();
			connection = null;
		}
	}

	public boolean isConnected() {
		return connection != null && connection.isConnected();
	}

	public void assign(String symbol, byte[] value) throws REngineException {
		connection.assign(symbol, value);
	}

	public void assign(String symbol, double[] value) throws REngineException {
		connection.assign(symbol, value);
	}

	public void assign(String symbol, int[] value) throws REngineException {
		connection.assign(symbol, value);
	}

	public void assign(String symbol, REXP value, REXP env) throws REngineException {
		connection.assign(symbol, value, env);
	}

	public void assign(String symbol, REXP value) throws RserveException {
		connection.assign(symbol, value);
	}

	public void assign(String symbol, String value) throws RserveException {
		connection.assign(symbol, value);
	}

	public void assign(String symbol, String[] value) throws REngineException {
		connection.assign(symbol, value);
	}

	public REXP eval(REXP arg0, REXP arg1, boolean arg2) throws REngineException {
		return connection.eval(arg0, arg1, arg2);
	}

	public REXP eval(String arg0) throws RserveException {
		return connection.eval(arg0);
	}

	public void voidEval(String cmd) throws RserveException {
		connection.voidEval(cmd);
	}

	public REXP get(String symbol) throws REngineException {
		return connection.get(symbol, null, true);
	}

}
