/**
 * 
 */
package org.openforis.calc.persistence.jooq;

import org.jooq.impl.EnumConverter;
import org.openforis.calc.engine.Worker;
import org.openforis.calc.engine.Worker.Status;

/**
 * @author Mino Togna
 * 
 */
public class WorkerStatusConverter extends EnumConverter<String, Status> {

	private static final long serialVersionUID = 1L;

	public WorkerStatusConverter() {
		super( String.class, Worker.Status.class );
	}

}
