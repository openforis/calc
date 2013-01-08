package org.openforis.calc.model;

import java.util.List;

/**
 * @author G. Miceli
 */
public interface Entity<E extends Entity<E>> extends Identifiable {
	List<Attribute<E,?>> getAttributes();
	
	boolean isInitialized(Attribute<E,?> attribute);
	
	<V, A extends Attribute<E,V>> V get(A attribute);
}
