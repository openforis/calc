/**
 * 
 */
package org.openforis.calc.r;

/**
 * @author Mino Togna
 * creates new r variable. you can optionally pass a dataframe , in this case the result will be dataFrame$variable
 */
public class RVariable extends RScript {
	
	RVariable(RScript previous, String name) {
		this(previous, null, name);
	}
	
	/**
	 * @param previous
	 */
	RVariable(RScript previous, String dataFrame, String name) {
		super(previous);
		
		if(dataFrame != null) {
			append(dataFrame);
			append(DOLLAR);
			append( escape(name) );
		} else {
			append( name );
		}
	}
	
	/**
	 * returns just it's simple name
	 */
	@Override
	public String toString() {
		return toScript();
	}
	
	public RVariable filterByColumn(RScript filter) {
		StringBuilder sb = new StringBuilder();
		sb.append(this.toScript());
		sb.append("[");
		sb.append(filter.toScript());
		sb.append("]");

		return new RVariable(null, sb.toString() );
	}
	
	/**
	 * Returns a string representation of the current variable filtering the columns
	 */
	public RScript filterColumns(RScript filter){
		StringBuilder sb = new StringBuilder();
		sb.append(this.toScript());
		sb.append("[ , ");
		sb.append(filter.toScript());
		sb.append("]");
		
		RScript rScript = new RScript();
		rScript.append(sb.toString());
		return rScript;
	}
	
}
