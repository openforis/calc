package org.openforis.calc.schema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.CategoricalVariable;
import org.openforis.calc.metadata.Entity;
import org.openforis.commons.collection.CollectionUtils;

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
	private Map<Integer, Cube> cubes;
	private DataSchema dataSchema;
	private ExtendedSchema extendedSchema;

	private List<VirtualCube> virtualCubes;


	public RolapSchema(Workspace workspace, DataSchema schema, ExtendedSchema extendedSchema) {
		this.name = workspace.getInputSchema();

		this.workspace = workspace;
		this.dataSchema = schema;
		this.extendedSchema = extendedSchema;
		// this.categoryDimensions = new ArrayList<CategoryDimension>();

		createAoiDimensions();
		createStratumDimension();
		createSharedDimensions();
		createCubes();
		createVirtualCubes();
	}

	private void createStratumDimension() {
		StratumDimensionTable stratumDimensionTable = dataSchema.getStratumDimensionTable();
		if( stratumDimensionTable != null ){
			this.stratumDimension = new StratumDimension( this, stratumDimensionTable );
		}
	}

	private void createCubes() {
		this.cubes = new HashMap<Integer, Cube>();
		
		List<FactTable> factTables = dataSchema.getFactTables();
		for (FactTable factTable : factTables) {
			Cube cube = new Cube(this, factTable);
			
			this.cubes.put(factTable.getEntity().getId(), cube);
		}
	}

	private void createVirtualCubes() {
		this.virtualCubes = new ArrayList<VirtualCube>();
		
		Entity samplingUnit = this.workspace.getSamplingUnit();
		if( samplingUnit != null ) {
			Cube suCube = cubes.get( samplingUnit.getId() );
			
			for ( Entity child : samplingUnit.getChildren() ) {
				Cube cube = cubes.get( child.getId() );
				
				// if cube is defined then it creates a virtual cube to join with sampling unit cube (used to calculate per ha measures)
				if( cube != null ) {
					VirtualCube virtualCube = new VirtualCube( suCube, cube );
					this.virtualCubes.add( virtualCube );
				}
				
			}
		}
		
	}

	private void createSharedDimensions() {
		sharedDimensions = new HashMap<CategoricalVariable<?>, CategoryDimension>();

		initSharedDimension( dataSchema );
		initSharedDimension( extendedSchema );
	}

	protected void initSharedDimension(DataSchema dataSchema) {
		Collection<CategoryDimensionTable> categoryDimensionTables = dataSchema.getCategoryDimensionTables();
		for ( CategoryDimensionTable categoryDimensionTable : categoryDimensionTables ) {
			CategoryDimension dimension = new CategoryDimension( this, categoryDimensionTable );
			CategoricalVariable<?> variable = categoryDimensionTable.getVariable();
			sharedDimensions.put( variable, dimension );
		}
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

	public Collection<Cube> getCubes() {
		return CollectionUtils.unmodifiableCollection( this.cubes.values() );
	}

	public Collection<VirtualCube> getVirtualCubes() {
		return CollectionUtils.unmodifiableCollection( this.virtualCubes ); 
	}
	
	//
	// public List<CategoryDimension> getCategoryDimensions() {
	// return Collections.unmodifiableList(categoryDimensions);
	// }

	public DataSchema getDataSchema() {
		return dataSchema;
	}
	
//	@Deprecated
//	public OutputSchema getOutputSchema() {
////		return dataSchema;
//		return null;
//	}

	public String getName() {
		return name;
	}
}
