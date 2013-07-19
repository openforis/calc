package org.openforis.calc.engine;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * @author G. Miceli
 * @author M. Togna
 * 
 */
@Component
public class ContextManager {
	@Autowired
	private DataSource userDataSource;
	
	/**
	 * Creates a new context for running tasks. It includes a reference to the
	 * workspace and the user data source.
	 * 
	 * @param workspace
	 * @return
	 */
	public TaskContext createContext(Workspace workspace) {
		return new TaskContext(workspace, userDataSource);
	}
}
