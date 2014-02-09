package org.openforis.calc.schema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.CategoricalVariable;

/**
 * @author G. Miceli
 * @author S. Ricci
 * @author M. Togna
 * 
 */
public class RolapSchema {

	private String name;

	private List<AoiDimension> aoiDimensions;
	private Map<CategoricalVariable<?>, CategoryDimension> sharedDimensions;
	private StratumDimension stratumDimension;

	private Workspace workspace;
	// private List<CategoryDimension> categoryDimensions;
	private List<Cube> cubes;
	private InputSchema dataSchema;

	public RolapSchema(Workspace workspace, InputSchema schema) {
		this.name = workspace.getInputSchema();

		this.workspace = workspace;
		this.dataSchema = schema;
		// this.categoryDimensions = new ArrayList<CategoryDimension>();

		createAoiDimensions();
		createStratumDimension();
//		createSharedDimensions();
//		createCubes();
	}

	private void createStratumDimension() {
		StratumDimensionTable stratumDimensionTable = dataSchema.getStratumDimensionTable();
		if( stratumDimensionTable != null ){
			this.stratumDimension = new StratumDimension( this, stratumDimensionTable );
		}
	}

	private void createCubes() {
		this.cubes = new ArrayList<Cube>();

//		Collection<NewFactTable> factTables = outputSchema.getFactTables();
//		for ( NewFactTable factTable : factTables ) {
//			Cube cube = new Cube(this, factTable);
//			cubes.add(cube);
//		}
	}

	private void createSharedDimensions() {
		sharedDimensions = new HashMap<CategoricalVariable<?>, CategoryDimension>();

//		Collection<CategoryDimensionTable> categoryDimensionTables = dataSchema.getCategoryDimensionTables();
//		for ( CategoryDimensionTable categoryDimensionTable : categoryDimensionTables ) {
//			CategoryDimension dimension = new CategoryDimension(this, categoryDimensionTable);
//			CategoricalVariable<?> variable = categoryDimensionTable.getVariable();
//			sharedDimensions.put(variable, dimension);
//		}
	}

	private void createAoiDimensions() {
		aoiDimensions = new ArrayList<AoiDimension>();

		List<AoiHierarchyFlatTable> aoiHierchyTables = dataSchema.getAoiHierchyTables();
		for (AoiHierarchyFlatTable table : aoiHierchyTables) {
			AoiDimension aoiDimension = new AoiDimension( this, table );
			aoiDimensions.add(aoiDimension);
		}

	}

	public Collection<CategoryDimension> getSharedDimensions() {
		return Collections.unmodifiableCollection(sharedDimensions.values());
	}

	Map<CategoricalVariable<?>, CategoryDimension> getSharedDimensionsMap() {
		return Collections.unmodifiableMap(sharedDimensions);
	}

	public List<AoiDimension> getAoiDimensions() {
		return Collections.unmodifiableList(aoiDimensions);
	}

	public StratumDimension getStratumDimension() {
		return stratumDimension;
	}

	public Workspace getWorkspace() {
		return workspace;
	}

	// void addCategoryDimension(CategoryDimension dim) {
	// categoryDimensions.add(dim);
	// }

	// void addCube(Cube cube) {
	// cubes.add(cube);
	// }

	public List<Cube> getCubes() {
		return Collections.unmodifiableList(cubes);
	}

	//
	// public List<CategoryDimension> getCategoryDimensions() {
	// return Collections.unmodifiableList(categoryDimensions);
	// }

	public InputSchema getDataSchema() {
		return dataSchema;
	}
	
	@Deprecated
	public OutputSchema getOutputSchema() {
//		return dataSchema;
		return null;
	}

	public String getName() {
		return name;
	}
}
