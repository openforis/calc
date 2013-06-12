package org.openforis.calc.persistence;
import org.openforis.calc.common.Identifiable;

/**
 * 
 * @author G. Miceli
 *
 */
public interface Dao<T extends Identifiable> {

	T create(T object);

	T find(int id);

	T update(T object);
	
	void delete(int id);
}