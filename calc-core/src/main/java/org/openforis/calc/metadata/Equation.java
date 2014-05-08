/**
 * 
 */
package org.openforis.calc.metadata;

import org.openforis.calc.persistence.jooq.tables.pojos.EquationBase;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Mino Togna
 *
 */
public class Equation extends EquationBase {

	private static final long serialVersionUID = 1L;
	
	@JsonIgnore
	private EquationList equationList;
	
	public void setList(EquationList equationList) {
		this.equationList = equationList;
		setListId( equationList.getId() );
	}
	
	@JsonIgnore
	public EquationList getList() {
		return equationList;
	}
	
}
