package org.openforis.calc.chain.post;

import static org.openforis.calc.mondrian.Rolap.DIMENSION_TYPE_STANDARD;
import static org.openforis.calc.mondrian.Rolap.createAggForeignKey;
import static org.openforis.calc.mondrian.Rolap.createAggLevel;
import static org.openforis.calc.mondrian.Rolap.createAggMeasure;
import static org.openforis.calc.mondrian.Rolap.createAggregateName;
import static org.openforis.calc.mondrian.Rolap.createCube;
import static org.openforis.calc.mondrian.Rolap.createDimensionUsage;
import static org.openforis.calc.mondrian.Rolap.createLevel;
import static org.openforis.calc.mondrian.Rolap.createMeasure;
import static org.openforis.calc.mondrian.Rolap.createSchema;
import static org.openforis.calc.mondrian.Rolap.createSharedDimension;
import static org.openforis.calc.mondrian.Rolap.createSqlView;
import static org.openforis.calc.mondrian.Rolap.createTable;
import static org.openforis.calc.mondrian.Rolap.toMdx;

import java.io.File;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.jooq.Field;
import org.openforis.calc.engine.Task;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.AoiHierarchy;
import org.openforis.calc.metadata.AoiLevel;
import org.openforis.calc.metadata.CategoricalVariable;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.QuantitativeVariable;
import org.openforis.calc.metadata.Variable;
import org.openforis.calc.metadata.VariableAggregate;
import org.openforis.calc.mondrian.DimensionUsage;
import org.openforis.calc.mondrian.Hierarchy;
import org.openforis.calc.mondrian.Hierarchy.Level;
import org.openforis.calc.mondrian.Schema;
import org.openforis.calc.mondrian.Schema.Cube;
import org.openforis.calc.mondrian.Schema.Cube.Measure;
import org.openforis.calc.mondrian.SharedDimension;
import org.openforis.calc.mondrian.Table;
import org.openforis.calc.mondrian.Table.AggName;
import org.openforis.calc.mondrian.Table.AggName.AggForeignKey;
import org.openforis.calc.mondrian.Table.AggName.AggLevel;
import org.openforis.calc.mondrian.Table.AggName.AggMeasure;
import org.openforis.calc.mondrian.View;
import org.openforis.calc.schema.AggregateTable;
import org.openforis.calc.schema.AoiDimension;
import org.openforis.calc.schema.CategoryDimension;
import org.openforis.calc.schema.Dimension;
import org.openforis.calc.schema.FactTable;
import org.openforis.calc.schema.OutputSchema;
import org.openforis.calc.schema.RolapSchema;
import org.springframework.beans.factory.annotation.Value;

/**
 * 
 * Task responsible for generating the rolap schema (using classes generated from mondrian 3.5.0 schema
 * https://github.com/pentaho/mondrian/blob/3.5.0-R/lib/mondrian.xsd)
 * 
 * @author M. Togna
 * 
 */
public class PublishRolapSchemaTask extends Task {

	private static final String NUMBER_FORMAT_STRING = "#,###.##";
	private static final String DATA_TYPE_NUMERIC = "Numeric";
	

	@Value("${calc.rolapSchemaOutputFile}")
	private String rolapSchemaOutputFile;
	
	@Override
	protected void execute() throws Throwable {
		
		RolapSchema rolapSchema = getRolapSchema();
		
		OutputSchema outputSchema = getOutputSchema();
		String outputSchemaName = outputSchema.getName();
		Workspace workspace = getWorkspace();
//		String workspaceName = workspace.getInputSchema();	
		
		// create schema
		Schema schema = createSchema(rolapSchema.getName());
		
		// create aoi dimensions
		List<AoiDimension> aoiDimensions = rolapSchema.getAoiDimensions();
		for ( AoiDimension aoiDimension : aoiDimensions ) {
			SharedDimension dim = createAoiSharedDimension(aoiDimension);
			schema.getDimension().add(dim);
		}
		List<AoiHierarchy> hierarchies = workspace.getAoiHierarchies();
//		for ( AoiHierarchy hierarchy : hierarchies ) {
//			SharedDimension dim = createAoiSharedDimension(hierarchy);
//			schema.getDimension().add(dim);
//		}
		
		// create shared dimensions
		Collection<CategoryDimension> sharedDimensions = rolapSchema.getSharedDimensions();
		for ( CategoryDimension categoryDimension : sharedDimensions ) {
			org.openforis.calc.schema.Hierarchy hierarchy = categoryDimension.getHierarchy();
			org.openforis.calc.schema.Hierarchy.Table table = hierarchy.getTable();
			org.openforis.calc.schema.Hierarchy.Level level = hierarchy.getLevels().get(0);
			SharedDimension dim = createSharedDimension(categoryDimension.getName(), table.getName(), table.getSchema(), level.getColumn(), level.getNameColumn());
			schema.getDimension().add(dim);
		}
//		Collection<CategoryDimensionTable> categoryDimensionTables = outputSchema.getCategoryDimensionTables();
//		for ( CategoryDimensionTable categoryDimTable : categoryDimensionTables ) {
//			CategoricalVariable variable = categoryDimTable.getVariable();
//			SharedDimension dim = createSharedDimension(variable.getName(), categoryDimTable.getName(), categoryDimTable.getSchema().getName(), "id", "caption");
//			schema.getDimension().add(dim);
//		}
		
		
		
		// create cubes for each fact table
		List<org.openforis.calc.schema.Cube> cubes = rolapSchema.getCubes();
		for ( org.openforis.calc.schema.Cube rolapCube : cubes ) {
			Cube cube = createCube( rolapCube.getName() );
			
			Table table = createTable( rolapCube.getSchema(), rolapCube.getTable() );
			cube.setTable(table);
			
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
			
		}
		
		
		
		Collection<FactTable> factTables = outputSchema.getFactTables();
		for ( FactTable factTable : factTables ) {
			Entity entity = factTable.getEntity();
			
			Cube cube = createCube( entity.getName() );
			Table table = createTable( outputSchemaName, factTable.getName() );
			
			// add aoi dimension usages to sampling unit cube if table is geo referenced
//			if( factTable.isGeoreferenced() ) {
//				for ( AoiHierarchy hierarchy : hierarchies ) {
//					String hierarchyName = hierarchy.getName();
//					List<AoiLevel> levels = hierarchy.getLevels();
//					AoiLevel leafLevel = levels.get(levels.size() - 1);
//					Field<Integer> aoiIdField = factTable.getAoiIdField(leafLevel);
//					DimensionUsage dim = createDimensionUsage(hierarchyName, aoiIdField.getName());
//					cube.getDimensionUsageOrDimension().add(dim);
//				}
//			}
			
			// add members (dimensions and measures) to cube
			List<Variable<?>> variables = entity.getVariables();
			for ( Variable<?> variable : variables ) {
				
				// add dimension usages to cube
				String variableName = variable.getName();
				if ( variable instanceof CategoricalVariable ) {
//					CategoricalVariable catVariable = (CategoricalVariable) variable;
//					Field<Integer> dimensionIdField = factTable.getDimensionIdField(catVariable);
//					if ( dimensionIdField != null ) { // it's null when variable is not disaggregate
//						DimensionUsage dim = createDimensionUsage(variableName, dimensionIdField.getName());
//						cube.getDimensionUsageOrDimension().add(dim);
//					}
				}
				
				// add measures to sampling unit cube
				else if ( variable instanceof QuantitativeVariable ) {
					QuantitativeVariable qVariable = (QuantitativeVariable) variable;
					List<VariableAggregate> aggregates = qVariable.getAggregates();
					
					for ( VariableAggregate aggregate : aggregates ) {
						Field<BigDecimal> measureField = factTable.getMeasureField(aggregate);
						
						String measureName = aggregate.getName(); 
						String fieldName = measureField.getName();
						String caption = aggregate.getCaption();
						String aggFunction = aggregate.getAggregateFunction();
						
						Measure m = createMeasure(measureName, caption, fieldName, aggFunction, DATA_TYPE_NUMERIC, NUMBER_FORMAT_STRING);
						
						cube.getMeasure().add(m);
					}
					
				}
			}
			
			//add aggregate names to table
			for ( AoiHierarchy hierarchy : hierarchies ) {
				List<AggregateTable> aggregateTables = factTable.getAggregateTables(hierarchy);
				
				int approxRowCnt = 100;
				for ( AggregateTable aggTable : aggregateTables ) {
					AggName aggName = createAggregateName(aggTable.getName(), approxRowCnt);
					
					// add aggregates members
					for ( Variable<?> variable : variables ) {
						if ( variable instanceof CategoricalVariable ) {
							CategoricalVariable<?> catVariable = (CategoricalVariable<?>) variable;
							Field<Integer> dimensionIdField = aggTable.getDimensionIdField(catVariable);
							if ( dimensionIdField != null ) {
								String fKey = dimensionIdField.getName();
								AggForeignKey aggForeignKey = createAggForeignKey(fKey, fKey);
								aggName.getAggForeignKey().add(aggForeignKey);
							}
						}
						// add measures to sampling unit cube
						else if ( variable instanceof QuantitativeVariable ) {
							QuantitativeVariable qVariable = (QuantitativeVariable) variable;
							List<VariableAggregate> aggregates = qVariable.getAggregates();
							for ( VariableAggregate aggregate : aggregates ) {
								Field<BigDecimal> measureField = factTable.getMeasureField(aggregate);
								
								String measureName = aggregate.getName(); 
								String fieldName = measureField.getName();
	
								AggMeasure aggMeasure = createAggMeasure(measureName, fieldName);
								
								aggName.getAggMeasure().add(aggMeasure);
							}
	
						}
					}
					
					//add aoi levels aggregation
					AoiLevel level = aggTable.getAoiHierarchyLevel();
					for ( AoiLevel aoiHierarchyAggLevel : hierarchy.getLevels() ) {
						Field<Integer> aoiIdField = aggTable.getAoiIdField(aoiHierarchyAggLevel);
						String aoiAggLevelName = aoiHierarchyAggLevel.getName();
						String aggLevelName = toMdx( hierarchy.getName(), aoiAggLevelName );
						
						AggLevel aggLevel = createAggLevel(aggLevelName, aoiIdField.getName());
						
						aggName.getAggLevel().add(aggLevel);
						
						if( aoiHierarchyAggLevel.equals(level) ) {
							break;
						}
					}
					 
					table.getAggTable().add(aggName);

					approxRowCnt+=100;
				}
			}
			
			cube.setTable(table);

			schema.getCube().add(cube);
		}
		



		JAXBContext jaxbContext = JAXBContext.newInstance(Schema.class);
		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty("jaxb.formatted.output", true);
		
		File f = new File( rolapSchemaOutputFile );
		if ( f.exists() ) {
			f.delete();
		}
		marshaller.marshal(schema, f);

	}

	
	
//	private SharedDimension createAoiSharedDimension(AoiDimension aoiDimension) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//


	private SharedDimension createAoiSharedDimension(AoiDimension aoiDimension) {
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
			Level l = createLevel(level.getName(), level.getColumn(), level.getNameColumn());
			h.getLevel().add(l);
		}
		
		View v = createSqlView ( view.getAlias(), view.getSql() );
		h.setView(v);

		dim.getHierarchy().add(h);

		return dim;
	}

}
