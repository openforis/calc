/**
 * 
 */
package org.openforis.calc.metadata;

import java.util.ArrayList;
import java.util.List;

import org.openforis.calc.engine.Workspace;
import org.openforis.calc.persistence.jooq.tables.pojos.EquationListBase;
import org.openforis.commons.collection.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Mino Togna
 *
 */
public class EquationList extends EquationListBase {

	private static final long serialVersionUID = 1L;
	
	@JsonIgnore
	private Workspace workspace;

	@JsonIgnore
	private List<Equation> equations;
	
	public Workspace getWorkspace() {
		return workspace;
	}
	
	public void setWorkspace( Workspace workspace ) {
		this.workspace = workspace;
	}
	
	public List<Equation> getEquations() {
		return CollectionUtils.unmodifiableList( equations );
	}
	
	public void setEquations( List<Equation> equations ) {
		if( equations != null ){
			for (Equation equation : equations) {
				this.addEquation( equation );
			}
		}
	}
	
	public void addEquation( Equation equation ){
		if( this.equations == null ){
			this.equations = new ArrayList<Equation>();
		}
		this.equations.add( equation );
		equation.setList( this );
	}

}
