/**
 * 
 */
package org.openforis.calc.r;

import org.junit.Assert;
import org.junit.Test;
import org.rosuda.REngine.Rserve.RserveException;

/**
 * @author Mino Togna
 * 
 */
public class RClientConnectionTest {

	public static void main(String[] args) throws RserveException {
		testConnection();
	}

	// @Test
	public void testIsConnected() throws RserveException {
		testConnection();
	}

	private static void testConnection() throws RserveException {
		RClient client = RClient.getInstance();

		Assert.assertFalse(client.isConnected());

		client.connect();

		Assert.assertTrue(client.isConnected());
		System.out.println("Client connected? " + client.isConnected());
		client.disconnect();
		System.out.println("Client connected? " + client.isConnected());
		Assert.assertFalse(client.isConnected());
	}

}
