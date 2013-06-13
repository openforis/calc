package org.openforis.calc.engine;

import java.util.Iterator;
import java.util.Set;

import org.openforis.calc.engine.Parameters.NameValuePair;

/**
 * 
 * @author G. Miceli
 *
 */
public final class Parameters implements Iterable<NameValuePair> {

	public Set<String> names() {
		throw new UnsupportedOperationException();
	}

	public Set<Object> values() {
		throw new UnsupportedOperationException();
	}

	public String getValue(String name) {
		throw new UnsupportedOperationException();
	}

	public void setValue(String name, String value) {
		throw new UnsupportedOperationException();
	}

	public void removeValue(String name) {
		throw new UnsupportedOperationException();
	}

	public Set<NameValuePair> pairs() {
		throw new UnsupportedOperationException();
	}
	
	public final class NameValuePair {

		public String getName() {
			throw new UnsupportedOperationException();
		}

		public String getValue() {
			throw new UnsupportedOperationException();
		}
	}
	
	@Override
	public Iterator<NameValuePair> iterator() {
		return pairs().iterator();
	}
}