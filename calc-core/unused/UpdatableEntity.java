package org.openforis.calc.model;

/**
 * @author G. Miceli
 */
public interface UpdatableEntity<E extends UpdatableEntity<E>> extends Entity<E> {

	<V, A extends Attribute<E, V>> void set(A attribute, V value);
}
