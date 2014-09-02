package org.openforis.calc.chain.post;

import static org.openforis.calc.mondrian.Rolap.DATA_TYPE_NUMERIC;
import static org.openforis.calc.mondrian.Rolap.DIMENSION_TYPE_STANDARD;
import static org.openforis.calc.mondrian.Rolap.NUMBER_FORMAT_STRING;
import static org.openforis.calc.mondrian.Rolap.createAggForeignKey;
import static org.openforis.calc.mondrian.Rolap.createAggLevel;
import static org.openforis.calc.mondrian.Rolap.createAggMeasure;
import static org.openforis.calc.mondrian.Rolap.createAggregateName;
import static org.openforis.calc.mondrian.Rolap.createCalculatedMember;
import static org.openforis.calc.mondrian.Rolap.createCube;
import static org.openforis.calc.mondrian.Rolap.createCubeUsage;
import static org.openforis.calc.mondrian.Rolap.createDimensionUsage;
import static org.openforis.calc.mondrian.Rolap.createLevel;
import static org.openforis.calc.mondrian.Rolap.createMeasure;
import static org.openforis.calc.mondrian.Rolap.createSchema;
import static org.openforis.calc.mondrian.Rolap.createSharedDimension;
import static org.openforis.calc.mondrian.Rolap.createSqlView;
import static org.openforis.calc.mondrian.Rolap.createTable;
import static org.openforis.calc.mondrian.Rolap.createVirtualCube;
import static org.openforis.calc.mondrian.Rolap.createVirtualCubeDimension;
import static org.openforis.calc.mondrian.Rolap.createVirtualCubeMeasure;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.jooq.Field;
import org.openforis.calc.engine.Task;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.mondrian.CalculatedMember;
import org.openforis.calc.mondrian.DimensionUsage;
import org.openforis.calc.mondrian.Hierarchy;
import org.openforis.calc.mondrian.Hierarchy.Level;
import org.openforis.calc.mondrian.Schema;
import org.openforis.calc.mondrian.Schema.Cube;
import org.openforis.calc.mondrian.Schema.Cube.Measure;
import org.openforis.calc.mondrian.Schema.Role;
import org.openforis.calc.mondrian.Schema.Role.SchemaGrant;
import org.openforis.calc.mondrian.Schema.Role.SchemaGrant.CubeGrant;
import org.openforis.calc.mondrian.Schema.VirtualCube;
import org.openforis.calc.mondrian.Schema.VirtualCube.CubeUsages;
import org.openforis.calc.mondrian.Schema.VirtualCube.CubeUsages.CubeUsage;
import org.openforis.calc.mondrian.Schema.VirtualCube.VirtualCubeDimension;
import org.openforis.calc.mondrian.Schema.VirtualCube.VirtualCubeMeasure;
import org.openforis.calc.mondrian.SharedDimension;
import org.openforis.calc.mondrian.Table;
import org.openforis.calc.mondrian.Table.AggName;
import org.openforis.calc.mondrian.Table.AggName.AggForeignKey;
import org.openforis.calc.mondrian.Table.AggName.AggLevel;
import org.openforis.calc.mondrian.Table.AggName.AggMeasure;
import org.openforis.calc.mondrian.View;
import org.openforis.calc.saiku.Saiku;
import org.openforis.calc.schema.AoiDimension;
import org.openforis.calc.schema.CategoryDimension;
import org.openforis.calc.schema.Dimension;
import org.openforis.calc.schema.RolapSchema;
import org.openforis.calc.schema.StratumDimension;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Task responsible for generating the rolap schema (using classes generated from mondrian 3.5.0 schema
 * https://github.com/pentaho/mondrian/blob/3.5.0-R/lib/mondrian.xsd)
 * 
 * @author M. Togna
 * 
 */
public class PublishRolapSchemaTask extends Task {

	private static final String ROLE_ADMIN = "ROLE_ADMIN" ;
	private static final String ROLE_USER = "ROLE_USER" ;
	
	@Autowired
	private Saiku saiku;
	
	public PublishRolapSchemaTask() {
		super();
	}

	@Override
	protected long countTotalItems() {
		return 8;
	}
	
	@Override
	protected void execute() throws Throwable {
		
		Workspace workspace = getWorkspace();
		RolapSchema rolapSchema = getRolapSchema();

		// create schema
		Schema schema = createSchema(rolapSchema.getName());
		incrementItemsProcessed();
		
		// create aoi dimensions
		createAoiDimensions(rolapSchema, schema);
		incrementItemsProcessed();
		
		// create stratum dimension
		createStratumDimension(rolapSchema, schema);
		incrementItemsProcessed();
		
		// create shared dimensions
		createSharedDimensions(rolapSchema, schema);
		incrementItemsProcessed();
		
		// create cubes for each fact table
		createCubes(rolapSchema, schema);
		incrementItemsProcessed();
		
		// create virtual cubes
		createVirtualCubes(rolapSchema, schema);
		incrementItemsProcessed();
		
		// create roles
		createRoles( rolapSchema , schema );
		incrementItemsProcessed();
		
		// publish schema
		this.saiku.publishSchema(workspace, schema);
		incrementItemsProcessed();
	}

	private void createRoles( RolapSchema rolapSchema , Schema schema ) {
		// TODO Auto-generated method stub
		List<Role> roles = schema.getRole();
		
		// add admin role
		Role admin = createRole( ROLE_ADMIN );
		roles.add( admin  );
		
		// add role user
		Role user = createRole( ROLE_USER );
		
		SchemaGrant schemaGrant = user.getSchemaGrant().get(0);
		Collection<org.openforis.calc.schema.VirtualCube> virtualCubes = rolapSchema.getVirtualCubes();
		for ( org.openforis.calc.schema.VirtualCube virtualCube : virtualCubes ) {
			
			CubeGrant cubeGrant = new CubeGrant();
			cubeGrant.setCube( virtualCube.getChildCube().getName() );
			cubeGrant.setAccess( "none" );
			
			schemaGrant.getCubeGrant().add( cubeGrant );
		}
		roles.add( user );
		
	}

	private Role createRole( String roleName ) {
		Role role = new Role();
		role.setName( roleName );
		
		SchemaGrant schemaGrant = new SchemaGrant();
		schemaGrant.setAccess( "all" );
		role.getSchemaGrant().add( schemaGrant );
		
		return role;
	}

	private void createCubes(RolapSchema rolapSchema, Schema schema) {
		Collection<org.openforis.calc.schema.Cube> cubes = rolapSchema.getCubes();
		for ( org.openforis.calc.schema.Cube rolapCube : cubes ) {

			Cube cube = createCube(rolapCube.getName());
			schema.getCube().add(cube);

			// add table to cube
			Table table = createTable(rolapCube.getSchema(), rolapCube.getTable());
			cube.setTable(table);

			// add agg names to table
			createAggNames(rolapCube, table);

			// add members to cube
			createCubeMembers(rolapCube, cube);
		}
	}

	private void createVirtualCubes( RolapSchema rolapSchema, Schema schema ){
		for ( org.openforis.calc.schema.VirtualCube calcCube : rolapSchema.getVirtualCubes() ) {
			VirtualCube virtualCube = createVirtualCube( calcCube.getName() );
			schema.getVirtualCube().add( virtualCube );
			
			CubeUsages cubeUsages = new CubeUsages();
			virtualCube.setCubeUsages( cubeUsages );
			for( org.openforis.calc.schema.VirtualCube.CubeUsage u : calcCube.getCubeUsages() ) {
				CubeUsage cubeUsage = createCubeUsage( u.getCubeName() );
				cubeUsages.getCubeUsage().add( cubeUsage  );
			}
			
			for (org.openforis.calc.schema.VirtualCube.VirtualCubeDimension d : calcCube.getVirtualCubeDimensions()) {
				VirtualCubeDimension virtualDimension = createVirtualCubeDimension( d.getCubeName() , d.getName() );
				virtualCube.getVirtualCubeDimension().add( virtualDimension );
			}
			
			for ( org.openforis.calc.schema.VirtualCube.VirtualCubeMeasure m : calcCube.getVirtualCubeMeasures() ) {
				VirtualCubeMeasure virtualMeasure = createVirtualCubeMeasure( m.getCubeName() , m.getName(), m.isVisible() );
				virtualCube.getVirtualCubeMeasure().add( virtualMeasure  );
			}
			
			for ( org.openforis.calc.schema.VirtualCube.CalculatedMember m : calcCube.getCalculatedMembers() ) {
				CalculatedMember calculatedMember = createCalculatedMember( m.getDimension(), m.getName(), m.getCaption(), m.getFormula(), m.isVisible() );
				virtualCube.getCalculatedMember().add( calculatedMember );
			}
			
		}
	}
	
	private void createCubeMembers( org.openforis.calc.schema.Cube rolapCube, Cube cube ){
		// add stratum dimension usage
		StratumDimension stratumDimension = rolapCube.getStratumDimension();
		if( stratumDimension != null ) {
			Field<Integer> stratumField =  rolapCube.getStratumField();
			DimensionUsage stratumDimUsage = createDimensionUsage( stratumDimension.getName(), stratumField.getName() );
			
			cube.getDimensionUsageOrDimension().add(stratumDimUsage);
		}

		// add aoi dimension usages
		Map<AoiDimension, Field<Integer>> aoiDimensionUsages = rolapCube.getAoiDimensionUsages();
		for ( AoiDimension aoiDimension : aoiDimensionUsages.keySet() ) {
			Field<Integer> field = aoiDimensionUsages.get(aoiDimension);
			DimensionUsage dim = createDimensionUsage(aoiDimension.getName(), field.getName());
			cube.getDimensionUsageOrDimension().add(dim);
		}

		// add dimension usages
		Map<Dimension, Field<Integer>> dimensionUsages = rolapCube.getDimensionUsages();
		for ( Dimension dimension : dimensionUsages.keySet() ) {
			Field<Integer> field = dimensionUsages.get(dimension);

			DimensionUsage dim = createDimensionUsage(dimension.getName(), field.getName());
			cube.getDimensionUsageOrDimension().add(dim);
		}

		// add measures
//		Map<org.openforis.calc.schema.Measure, Field<BigDecimal>> measures = rolapCube.getMeasures();
		for ( org.openforis.calc.schema.Measure measure : rolapCube.getMeasuresOrdered() ) {
			Measure m = createMeasure(measure.getName(), measure.getCaption(), measure.getColumn(), measure.getAggregator(), DATA_TYPE_NUMERIC, NUMBER_FORMAT_STRING);

			cube.getMeasure().add(m);
		}
		// add error measures
		for ( org.openforis.calc.schema.Measure measure : rolapCube.getErrorMeasures() ){
			Measure m = createMeasure(measure.getName(), measure.getCaption(), measure.getColumn(), measure.getAggregator(), DATA_TYPE_NUMERIC, NUMBER_FORMAT_STRING);
			cube.getMeasure().add(m);
		}
	}

	private void createAggNames(org.openforis.calc.schema.Cube rolapCube, Table table) {
		int approxRowCnt = 100;
		List<org.openforis.calc.schema.Cube.AggName> aggNames = rolapCube.getAggNames();
		for ( org.openforis.calc.schema.Cube.AggName aggName : aggNames ) {
			AggName aggTable = createAggregateName(aggName.getName(), approxRowCnt);
			table.getAggTable().add(aggTable);

			for ( org.openforis.calc.schema.Cube.AggForeignKey aggForeignKey : aggName.getAggForeignKeys() ) {
				AggForeignKey aggFK = createAggForeignKey(aggForeignKey.getFactColumn(), aggForeignKey.getAggColumn());
				aggTable.getAggForeignKey().add(aggFK);
			}

			for ( org.openforis.calc.schema.Cube.AggMeasure aggMeasure : aggName.getAggMeasures() ) {
				AggMeasure aggM = createAggMeasure(aggMeasure.getName(), aggMeasure.getColumn());
				aggTable.getAggMeasure().add(aggM);
			}

			for ( org.openforis.calc.schema.Cube.AggLevel aggLevel : aggName.getAggLevels() ) {
				AggLevel aggL = createAggLevel( aggLevel.getHierarchy(), aggLevel.getName(), aggLevel.getColumn() );
				aggTable.getAggLevel().add( aggL );
			}
			approxRowCnt += 100;
		}
	}

	private void createSharedDimensions( RolapSchema rolapSchema, Schema schema ) {
		Collection<CategoryDimension> sharedDimensions = rolapSchema.getSharedDimensions();
		for ( CategoryDimension categoryDimension : sharedDimensions ) {
			SharedDimension dim = createDimension(categoryDimension);
			schema.getDimension().add(dim);
		}
	}

	private void createAoiDimensions(RolapSchema rolapSchema, Schema schema) {
		List<AoiDimension> aoiDimensions = rolapSchema.getAoiDimensions();
		for ( AoiDimension aoiDimension : aoiDimensions ) {
			SharedDimension dim = createAoiDimension(aoiDimension);
			schema.getDimension().add(dim);
		}
	}

	private void createStratumDimension(RolapSchema rolapSchema, Schema schema) {
		StratumDimension stratumDimension = rolapSchema.getStratumDimension();
		if( stratumDimension != null ) {
			
			org.openforis.calc.schema.Hierarchy hierarchy = stratumDimension.getHierarchy();
			org.openforis.calc.schema.Hierarchy.View view = hierarchy.getView();
			SharedDimension dim = new SharedDimension();
			
			dim.setName(stratumDimension.getName());
			dim.setType(DIMENSION_TYPE_STANDARD);
			
			Hierarchy h = new Hierarchy();
			h.setName(hierarchy.getName());
			h.setHasAll(true);
			
			List<org.openforis.calc.schema.Hierarchy.Level> levels = hierarchy.getLevels();
			for ( org.openforis.calc.schema.Hierarchy.Level level : levels ) {
				Level l = createLevel(level.getName(), level.getColumn(), level.getNameColumn(), level.getCaption() );
				h.getLevel().add(l);
			}

			View v = createSqlView(view.getAlias(), view.getSql());
			h.setView(v);

			dim.getHierarchy().add(h);

//			return dim;
//			SharedDimension stratumSharedDimension = createDimension(stratumDimension);
			schema.getDimension().add(dim);
		}
	}

	private SharedDimension createDimension(Dimension dimension) {
		org.openforis.calc.schema.Hierarchy hierarchy = dimension.getHierarchy();
		org.openforis.calc.schema.Hierarchy.Table table = hierarchy.getTable();
		org.openforis.calc.schema.Hierarchy.Level level = hierarchy.getLevels().get(0);

		SharedDimension dim = createSharedDimension(dimension.getName(), table.getName(), table.getSchema(), level.getColumn(), level.getNameColumn(), level.getCaption() ,dimension.getCaption());
		return dim;
	}

	private SharedDimension createAoiDimension(AoiDimension aoiDimension) {
		org.openforis.calc.schema.Hierarchy hierarchy = aoiDimension.getHierarchy();
		org.openforis.calc.schema.Hierarchy.View view = hierarchy.getView();

		SharedDimension dim = new SharedDimension();
		dim.setName(aoiDimension.getName());
		dim.setType(DIMENSION_TYPE_STANDARD);
		// ? dim.setCaption(hierarchy.get)

		Hierarchy h = new Hierarchy();
		h.setName(hierarchy.getName());
		h.setHasAll(false);

		List<org.openforis.calc.schema.Hierarchy.Level> levels = hierarchy.getLevels();
		for ( org.openforis.calc.schema.Hierarchy.Level level : levels ) {
			Level l = createLevel( level.getName(), level.getColumn(), level.getNameColumn() , level.getCaption() );
			h.getLevel().add(l);
		}

		View v = createSqlView(view.getAlias(), view.getSql());
		h.setView(v);

		dim.getHierarchy().add(h);

		return dim;
	}
	
	@Override
	public String getName() {
		return "Publish schema to saiku repository";
	}
	
}
