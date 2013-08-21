package org.openforis.calc.rolap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openforis.calc.engine.Workspace;
import org.openforis.calc.rdb.RelationalSchema;

/**
 * 
 * @author G. Miceli
 * @author S. Ricci
 * 
 */
public class RolapSchema {

	private RelationalSchema relationalSchema;
	private Workspace workspace;
	private List<CategoryDimension> categoryDimensions;
	private List<Cube> cubes;

	public RolapSchema(Workspace workspace) {
		this.workspace = workspace;
		this.relationalSchema = new RelationalSchema(workspace.getOutputSchema());
		this.categoryDimensions = new ArrayList<CategoryDimension>();
	}

	public RelationalSchema getRelationalSchema() {
		return relationalSchema;
	}

	public Workspace getWorkspace() {
		return workspace;
	}

	void addCategoryDimension(CategoryDimension dim) {
		categoryDimensions.add(dim);
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

	public List<CategoryDimension> getCategoryDimensions() {
		return Collections.unmodifiableList(categoryDimensions);
	}
	
}
