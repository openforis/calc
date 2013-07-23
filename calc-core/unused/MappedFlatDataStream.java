package org.openforis.calc.io.flat;

/**
 * 
 * @author G. Miceli
 *
 */
public interface MappedFlatDataStream<A extends Record<A>, B extends Record<B>> extends FlatDataStream<A> {
	FlatDataStream<A> getSourceStream();
	RecordMapper<A, B> getRecordMapper();
}
