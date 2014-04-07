/**
 * 
 */
package org.openforis.calc.chain.pre;

import org.openforis.calc.engine.Task;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceService;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.QuantitativeVariable;
import org.openforis.calc.metadata.Variable;
import org.openforis.calc.metadata.Variable.Scale;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author S. Ricci
 *
 */
public class AddWeightVariableTask extends Task {

	public static final String WEIGHT_INPUT_COLUMN_NAME = "weight";
	public static final String WEIGHT_VAR_NAME = "weight";

	@Autowired
	private WorkspaceService workspaceService;
	
	@Override
	protected void execute() throws Throwable {
		Workspace ws = getWorkspace();
		boolean changed = false;
		for (Entity entity : ws.getEntities()) {
			if ( entity.isSamplingUnit() ) {
				Variable<?> oldWeightVar = entity.getVariableByName(WEIGHT_VAR_NAME);
				if ( oldWeightVar == null ) {
					QuantitativeVariable weightVar = new QuantitativeVariable();
					weightVar.setName(WEIGHT_VAR_NAME);
					weightVar.setScale(Scale.RATIO);
					weightVar.setInputValueColumn(WEIGHT_INPUT_COLUMN_NAME);
					entity.addVariable(weightVar);
					changed = true;
				}
			}
		}
		if ( changed ) {
			workspaceService.save(ws);
		}
	}

}
