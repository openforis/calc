package org.openforis.calc.io.flat;


/**
 * 
 * @author G. Miceli
 *
 * @param <A> source record type
 * @param <B> destination record type
 */
public interface RecordMapper<A extends Record<A>, B extends Record<B>> {
	void map(A sourceRecord, B targetRecord);
}
