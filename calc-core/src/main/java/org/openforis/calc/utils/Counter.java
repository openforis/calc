/**
 * 
 */
package org.openforis.calc.utils;

/**
 * @author M. Togna
 *
 */
public class Counter {
	
	private int count;
	
	public Counter( int startFrom ) {
		this.count = startFrom;
	}

	public Counter() {
		this( 0 );
	}
	
	public int current(){
		return count;
	}
	
	public int increment(){
		++count;
		
		return current();
	}
	
}
