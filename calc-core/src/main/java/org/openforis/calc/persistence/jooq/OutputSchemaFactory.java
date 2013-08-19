package org.openforis.calc.persistence.jooq;

import org.openforis.calc.engine.Workspace;
import org.springframework.stereotype.Component;

/**
 * 
 * @author G. Miceli
 * @author S. Ricci
 *
 */
@Component
public class OutputSchemaFactory {
	public OutputSchema createOutputSchema(Workspace workspace) {
		OutputSchema os = new OutputSchema(workspace);
		// TODO
		return os;
	}
}
