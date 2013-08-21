package org.openforis.calc.rolap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openforis.calc.engine.Workspace;
import org.openforis.calc.rdb.OutputSchema;

/**
 * 
 * @author G. Miceli
 * @author S. Ricci
 * 
 */
public class RolapSchema {

	private Workspace workspace;
	private List<CategoryDimension> categoryDimensions;
	private List<Cube> cubes;
	private OutputSchema outputSchema;

	public RolapSchema(Workspace workspace, OutputSchema outputSchema) {
		this.workspace = workspace;
		this.outputSchema = outputSchema;
		this.categoryDimensions = new ArrayList<CategoryDimension>();
		this.cubes = new ArrayList<Cube>();
	}

	public Workspace getWorkspace() {
		return workspace;
	}

	void addCategoryDimension(CategoryDimension dim) {
		categoryDimensions.add(dim);
	}

	void addCube(Cube cube) {
		cubes.add(cube);
	}

	public List<Cube> getCubes() {
		return Collections.unmodifiableList(cubes);
	}

	public List<CategoryDimension> getCategoryDimensions() {
		return Collections.unmodifiableList(categoryDimensions);
	}
	
	public OutputSchema getOutputSchema() {
		return outputSchema;
	}
}
