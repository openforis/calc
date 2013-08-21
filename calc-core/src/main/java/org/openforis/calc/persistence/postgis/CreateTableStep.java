package org.openforis.calc.persistence.postgis;


/**
 * 
 * @author G. Miceli	
 * @author M. Togna
 *
 */
public class CreateTableStep extends DdlStep {

	CreateTableStep(Psql psql, Object table) {
		super(psql);
		append("create table ");
		append(table);
	}

	public AsStep as(Object select) {
		return new AsStep(select);
	}
	
	public class AsStep extends DdlStep {
		
		AsStep(Object select) {
			super(CreateTableStep.this);
			append("as ");
			append(select);
		}
		
	}
	
}
