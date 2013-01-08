package org.openforis.calc.model;

import java.util.List;

/**
 * @author G. Miceli
 */
public interface DataFrame {
	List<Column<?,?>> getColumns();
	
	<V, C extends Column<?,V>> V getValue(C column);
	
	<E extends Entity<E>> List<E> getEntities();
}
