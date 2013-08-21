package org.openforis.calc.rolap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openforis.calc.engine.Workspace;

/**
 * 
 * @author G. Miceli
 * @author S. Ricci
 *
 */
public class RolapSchema {
	private RelationalSchema relationalSchema;
	private Workspace workspace;
	private List<Dimension> sharedDimensions;
	private List<Cube> cubes;
	
	public RolapSchema(Workspace workspace) {
		this.workspace = workspace;
		this.relationalSchema = new RelationalSchema(workspace.getOutputSchema());
		this.sharedDimensions = new ArrayList<Dimension>();
	}
	
	public RelationalSchema getRelationalSchema() {
		return relationalSchema;
	}
	
	public Workspace getWorkspace() {
		return workspace;
	}
	
	void addSharedDimension(Dimension dim) {
		sharedDimensions.add(dim);
	}

	void addCube(Cube cube) {
		if( cubes == null ){
			cubes = new ArrayList<Cube>();
		}
		cubes.add(cube);
	}
	
	public List<Cube> getCubes() {
		return cubes == null?null:Collections.unmodifiableList(cubes);
	}
}
