package org.openforis.calc.chain.post;

import static org.openforis.calc.mondrian.Rolap.*;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.apache.commons.lang3.StringUtils;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SelectQuery;
import org.openforis.calc.engine.Task;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.AoiHierarchy;
import org.openforis.calc.metadata.AoiLevel;
import org.openforis.calc.metadata.CategoricalVariable;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.QuantitativeVariable;
import org.openforis.calc.metadata.Variable;
import org.openforis.calc.metadata.VariableAggregate;
import org.openforis.calc.mondrian.AggColumnName;
import org.openforis.calc.mondrian.DimensionUsage;
import org.openforis.calc.mondrian.Hierarchy;
import org.openforis.calc.mondrian.Hierarchy.Level;
import org.openforis.calc.mondrian.SQL;
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
import org.openforis.calc.psql.Psql;
import org.openforis.calc.schema.AggregateTable;
import org.openforis.calc.schema.AoiDimensionTable;
import org.openforis.calc.schema.CategoryDimensionTable;
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
		String workspaceName = workspace.getInputSchema();	
		
		// create schema
		Schema schema = createSchema(workspaceName);

		// create aoi dimensions
		List<AoiHierarchy> hierarchies = workspace.getAoiHierarchies();
		for ( AoiHierarchy hierarchy : hierarchies ) {
			SharedDimension dim = createAoiSharedDimension(hierarchy);
			schema.getDimension().add(dim);
		}
		
		// create shared dimensions
		Collection<CategoryDimensionTable> categoryDimensionTables = outputSchema.getCategoryDimensionTables();
		for ( CategoryDimensionTable categoryDimTable : categoryDimensionTables ) {
			CategoricalVariable variable = categoryDimTable.getVariable();
			SharedDimension dim = createSharedDimension(variable.getName(), categoryDimTable.getName(), categoryDimTable.getSchema().getName(), "id", "caption");
			schema.getDimension().add(dim);
		}
		// create cubes for each fact table
		Collection<FactTable> factTables = outputSchema.getFactTables();
		for ( FactTable factTable : factTables ) {
			Entity entity = factTable.getEntity();
			
			Cube cube = createCube( entity.getName() );
			Table table = createTable( outputSchemaName, factTable.getName() );
			
			// add aoi dimension usages to sampling unit cube if table is geo referenced
			if( factTable.isGeoreferenced() ) {
				for ( AoiHierarchy hierarchy : hierarchies ) {
					String hierarchyName = hierarchy.getName();
					List<AoiLevel> levels = hierarchy.getLevels();
					AoiLevel leafLevel = levels.get(levels.size() - 1);
					Field<Integer> aoiIdField = factTable.getAoiIdField(leafLevel);
					DimensionUsage dim = createDimensionUsage(hierarchyName, aoiIdField.getName());
	//				String fKey = leafLevel.getFkColumn();
					cube.getDimensionUsageOrDimension().add(dim);
				}
			}
			
			// add members (dimensions and measures) to cube
			List<Variable> variables = entity.getVariables();
			for ( Variable variable : variables ) {
				
				// add dimension usages to cube
				String variableName = variable.getName();
				if ( variable instanceof CategoricalVariable ) {
					CategoricalVariable catVariable = (CategoricalVariable) variable;
					Field<Integer> dimensionIdField = factTable.getDimensionIdField(catVariable);
					if ( dimensionIdField != null ) { // it's null when variable is not disaggregate
//						String fKey = catVariable.getCategoryIdColumn();
						DimensionUsage dim = createDimensionUsage(variableName, dimensionIdField.getName());
						cube.getDimensionUsageOrDimension().add(dim);
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
					for ( Variable variable : variables ) {
						if ( variable instanceof CategoricalVariable ) {
							CategoricalVariable catVariable = (CategoricalVariable) variable;
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

	private SharedDimension createAoiSharedDimension(AoiHierarchy aoiHierarchy) {
		OutputSchema outputSchema = getOutputSchema();
		String aoiHierarchyName = aoiHierarchy.getName();
		
		SharedDimension dim = new SharedDimension();
		dim.setName(aoiHierarchyName);
		dim.setType(DIMENSION_TYPE_STANDARD);
		// ? dim.setCaption(hierarchy.get)

		Hierarchy h = new Hierarchy();
		h.setName(aoiHierarchyName);
		h.setHasAll(false);
		
		SelectQuery<Record> select = new Psql().selectQuery();
		List<AoiLevel> aoiLevels = aoiHierarchy.getLevels();
		for ( int i = aoiLevels.size() - 1 ; i >= 0 ; i-- ) {
			AoiLevel aoiLevel = aoiLevels.get(i);
			AoiDimensionTable aoiDimTable = outputSchema.getAoiDimensionTable(aoiLevel);
			
			String aoiLevelName = aoiLevel.getName();
			
			String aliasIdColumn = aoiLevelName + "_id";
			String aliasCaptionColumn = aoiLevelName + "_caption";

			select.addSelect( aoiDimTable.ID.as(aliasIdColumn) );
			select.addSelect( aoiDimTable.CAPTION.as(aliasCaptionColumn) );
			
			if ( i == aoiLevels.size() - 1 ) {
				select.addFrom( aoiDimTable );
			} else {
				AoiLevel childLevel = aoiLevels.get(i + 1);
				AoiDimensionTable childAoiDimTable = outputSchema.getAoiDimensionTable(childLevel);
				
				select.addJoin(aoiDimTable, childAoiDimTable.PARENT_AOI_ID.eq(aoiDimTable.ID) );
			}

			Level l = createLevel(aoiLevelName, aliasIdColumn, aliasCaptionColumn);

			h.getLevel().add(0, l);
		}
		View v = createSqlView ( aoiHierarchyName, select.getSQL() );
		
		h.setView(v);

		dim.getHierarchy().add(h);

		return dim;
	}

}
