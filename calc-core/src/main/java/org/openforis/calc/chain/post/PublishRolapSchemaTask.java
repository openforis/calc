package org.openforis.calc.chain.post;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.apache.commons.lang3.StringUtils;
import org.openforis.calc.engine.Task;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.AoiHierarchy;
import org.openforis.calc.metadata.AoiHierarchyLevel;
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
	private static final String DIMENSION_TYPE_STANDARD = "StandardDimension";

	@Value("${calc.rolapSchemaOutputFile}")
	private String rolapSchemaOutputFile;
	
	@Override
	protected void execute() throws Throwable {

		Workspace ws = getWorkspace();
		String wsName = ws.getName();
		String outputSchema = ws.getOutputSchema();

		Entity entity = getSamplingUnitEntity();
		String entityName = entity.getName();
		String entityDataTable = entity.getDataTable();
		List<Variable> variables = entity.getVariables();

		// create schema
		Schema schema = createSchema(wsName);

		// create aoi dimensions
		List<AoiHierarchy> hierarchies = ws.getAoiHierarchies();
		for ( AoiHierarchy hierarchy : hierarchies ) {
			SharedDimension dim = createAoiDimension(hierarchy, outputSchema);
			schema.getDimension().add(dim);
		}

		// create plot shared dimensions
		for ( Variable variable : variables ) {
			if ( variable instanceof CategoricalVariable ) {
				CategoricalVariable cVariable = (CategoricalVariable) variable;
				if ( cVariable.isDisaggregate() ) {
					String dimensionTable = variable.getDimensionTable();
					String variableName = variable.getName();

					SharedDimension dim = createSharedDimension(variableName, dimensionTable, outputSchema);
					schema.getDimension().add(dim);
				}
			}
		}

		// create sampling unit cube
		Cube cube = createCube(entityName);

		Table t = createTable(outputSchema, entityDataTable);

		// add aoi dimension usages to sampling unit cube
		for ( AoiHierarchy hierarchy : hierarchies ) {
			String hierarchyName = hierarchy.getName();
			List<AoiHierarchyLevel> levels = hierarchy.getLevels();
			AoiHierarchyLevel leafLevel = levels.get(levels.size() - 1);
			String fKey = leafLevel.getFkColumn();

			DimensionUsage dim = createDimensionUsage(hierarchyName, fKey);
			cube.getDimensionUsageOrDimension().add(dim);
		}
		// add members (dimensions and measures) to sampling unit cube
		for ( Variable variable : variables ) {
			// add dimension usages to sampling unit cube
			String variableName = variable.getName();
			if ( variable instanceof CategoricalVariable ) {
				CategoricalVariable catVariable = (CategoricalVariable) variable;
				if ( catVariable.isDisaggregate() ) {
					String fKey = catVariable.getCategoryIdColumn();
					DimensionUsage dim = createDimensionUsage(variableName, fKey);
					cube.getDimensionUsageOrDimension().add(dim);
				}
			}
			// add measures to sampling unit cube
			else if ( variable instanceof QuantitativeVariable ) {
				QuantitativeVariable qVariable = (QuantitativeVariable) variable;
				List<VariableAggregate> aggregates = qVariable.getAggregates();
				for ( VariableAggregate aggregate : aggregates ) {

					String name = aggregate.getName();
					name = (name == null) ? variableName : name;

					String valueColumn = variable.getValueColumn();
					String column = aggregate.getAggregateColumn();
					column = (column == null) ? valueColumn : column;

					String caption = aggregate.getCaption();

					String aggFunction = aggregate.getAggregateFunction();

					String dataType = DATA_TYPE_NUMERIC;
					String formatString = NUMBER_FORMAT_STRING;

					Measure m = createMeasure(name, caption, column, aggFunction, dataType, formatString);

					cube.getMeasure().add(m);
				}

			}
		}

		//add aggregate names to table
		for ( AoiHierarchy hierarchy : hierarchies ) {
			String hierarchyName = hierarchy.getName();
			List<AoiHierarchyLevel> levels = hierarchy.getLevels();
			int approxRowCnt = 100;
			for ( AoiHierarchyLevel level : levels ) {
				String levelName = level.getName();
				String aggName = "_agg_"+levelName +"_stratum_"+ entityDataTable;
				
				AggName aggTable = createAggregateName(aggName, approxRowCnt);
				
				// add aggregates members
				for ( Variable variable : variables ) {
					String variableName = variable.getName();
					if ( variable instanceof CategoricalVariable ) {
						CategoricalVariable catVariable = (CategoricalVariable) variable;
						if ( catVariable.isDisaggregate() ) {
							String fKey = catVariable.getCategoryIdColumn();
							
							AggForeignKey aggForeignKey = createAggForeignKey(fKey, fKey);
							
							aggTable.getAggForeignKey().add(aggForeignKey);
						}
					}
					// add measures to sampling unit cube
					else if ( variable instanceof QuantitativeVariable ) {
						QuantitativeVariable qVariable = (QuantitativeVariable) variable;
						List<VariableAggregate> aggregates = qVariable.getAggregates();
						for ( VariableAggregate aggregate : aggregates ) {

							String name = aggregate.getName();
							name = (name == null) ? variableName : name;

							String valueColumn = variable.getValueColumn();
							String column = aggregate.getAggregateColumn();
							column = (column == null) ? valueColumn : column;

//							String caption = aggregate.getCaption();
//							String aggFunction = aggregate.getAggregateFunction();
//							String dataType = DATA_TYPE_NUMERIC;
//							String formatString = NUMBER_FORMAT_STRING;

							AggMeasure aggMeasure = createAggMeasure(name, column);
							
							aggTable.getAggMeasure().add(aggMeasure);
//							Measure m = createMeasure(name, caption, column, aggFunction, dataType, formatString);

						}

					}
				}
				
				
				//add aoi levels aggregation
				for ( AoiHierarchyLevel aoiHierarchyAggLevel : levels ) {
					String aoiHierarchyName = aoiHierarchyAggLevel.getName();
					String aggLevelName = "["+hierarchyName+ "]" + ".["+aoiHierarchyName+"]";
					String fkColumn = aoiHierarchyAggLevel.getFkColumn();
					
					AggLevel aggLevel = createAggLevel(aggLevelName, fkColumn);
					
					aggTable.getAggLevel().add(aggLevel);
					
					if(aoiHierarchyAggLevel.equals(level)){
						break;
					}
				}
				
				t.getAggTable().add(aggTable);
				
				approxRowCnt+=100;
			}
		}
		
		cube.setTable(t);

		schema.getCube().add(cube);

		JAXBContext jaxbContext = JAXBContext.newInstance(Schema.class);
		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty("jaxb.formatted.output", true);
		
		File f = new File( rolapSchemaOutputFile );
		if ( f.exists() ) {
			f.delete();
		}
		marshaller.marshal(schema, f);
		// suEntity.get

	}

	private AggLevel createAggLevel(String name, String column) {
		AggLevel aggLevel = new AggLevel();
		aggLevel.setColumn(column);
		aggLevel.setName(name);
		return aggLevel;
	}

	private AggMeasure createAggMeasure(String name, String column) {
		AggMeasure aggMeasure = new AggMeasure();
		aggMeasure.setColumn(column);
		String aggMeasureName = "[Measures]."+"["+name+"]";
		aggMeasure.setName(aggMeasureName );
		return aggMeasure;
	}

	private AggForeignKey createAggForeignKey(String factColumn, String aggColumn) {
		AggForeignKey aggForeignKey = new AggForeignKey();
		aggForeignKey.setFactColumn(factColumn);
		aggForeignKey.setAggColumn(aggColumn);
		return aggForeignKey;
	}

	private AggName createAggregateName(String name, int approxRowCnt) {
		AggName aggTable = new AggName();
		aggTable.setName(name);
		aggTable.setApproxRowCount( BigInteger.valueOf(approxRowCnt) );

		AggColumnName aggFactCount = new AggColumnName();
		aggFactCount.setColumn("_agg_cnt");
		aggTable.setAggFactCount(aggFactCount);
		return aggTable;
	}

	private Schema createSchema(String name) {
		Schema schema = new Schema();
		schema.setName(name);
		return schema;
	}

	private Table createTable(String schema, String table) {
		Table t = new Table();
		t.setName(table);
		t.setSchema(schema);
		return t;
	}

	private Cube createCube(String name) {
		Cube cube = new Cube();
		cube.setCache(false);
		cube.setEnabled(true);
		cube.setName(name);
		return cube;
	}

	private Measure createMeasure(String name, String caption, String column, String aggregator, String dataType, String formatString) {
		Measure m = new Measure();
		m.setName(name);
		m.setColumn(column);
		m.setDatatype(dataType);
		m.setFormatString(formatString);
		m.setAggregator(aggregator);
		m.setCaption(caption);
		m.setVisible(true);
		return m;
	}

	private DimensionUsage createDimensionUsage(String name, String fKey) {
		DimensionUsage dim = new DimensionUsage();
		dim.setSource(name);
		dim.setName(name);
		dim.setForeignKey(fKey);
		dim.setHighCardinality(false);
		return dim;
	}

	private SharedDimension createSharedDimension(String name, String table, String schema) {
		SharedDimension dim = new SharedDimension();
		dim.setType(DIMENSION_TYPE_STANDARD);
		dim.setName(name);

		Hierarchy h = new Hierarchy();
		h.setName(name);
		h.setHasAll(true);

		Table t = createTable(schema, table);
		h.setTable(t);

		Level l = new Level();
		l.setName(name);
		l.setTable(table);
		l.setColumn("id");
		l.setNameColumn("caption");
		h.getLevel().add(l);

		dim.getHierarchy().add(h);
		return dim;
	}

	private SharedDimension createAoiDimension(AoiHierarchy aoiHierarchy, String schema) {
		String aoiHierarchyName = aoiHierarchy.getName();
		
		SharedDimension dim = new SharedDimension();
		dim.setName(aoiHierarchyName);
		dim.setType(DIMENSION_TYPE_STANDARD);
		// ? dim.setCaption(hierarchy.get)

		Hierarchy h = new Hierarchy();
		h.setName(aoiHierarchyName);
		h.setHasAll(false);

		List<String> select = new ArrayList<String>();
		String from = "";
		List<String> joins = new ArrayList<String>();

		List<AoiHierarchyLevel> aoiLevels = aoiHierarchy.getLevels();
		for ( int i = aoiLevels.size() - 1 ; i >= 0 ; i-- ) {
			AoiHierarchyLevel aoiLevel = aoiLevels.get(i);
			String aoiLevelName = aoiLevel.getName();
			String dimensionTable = aoiLevel.getDimensionTable();
			
			String levelColumn = aoiLevelName + "_id";
			String levelNameColumn = aoiLevelName + "_caption";

			select.add(dimensionTable + ".id as " + levelColumn);
			select.add(dimensionTable + ".caption as " + levelNameColumn);

			if ( i == aoiLevels.size() - 1 ) {
				from = schema + "." + dimensionTable;
			} else {
				AoiHierarchyLevel childLevel = aoiLevels.get(i + 1);
				String childDimensionTable = childLevel.getDimensionTable();
				joins.add(" inner join " + schema + "." + dimensionTable + " on " + childDimensionTable + ".parent_aoi_id = " + dimensionTable + ".id");
			}

			Level l = createLevel(aoiLevelName, levelColumn, levelNameColumn);

			h.getLevel().add(0, l);
		}

		String sqlContent = "select " + StringUtils.join(select, ",") + " " + " from " + from + " " + StringUtils.join(joins, " ");
		
		View v = createSqlView(aoiHierarchyName, sqlContent);
		
		h.setView(v);

		dim.getHierarchy().add(h);

		return dim;
	}

	private View createSqlView(String alias, String sql) {
		View v = new View();
		v.setAlias(alias);
		SQL s = new SQL();
		s.setContent(sql);
		v.getSQL().add(s);
		return v;
	}

	private Level createLevel(String name, String column, String nameColumn) {
		Level l = new Level();
		l.setName(name);
		l.setColumn(column);
		l.setNameColumn(nameColumn);
		return l;
	}

	private Entity getSamplingUnitEntity() {
		Workspace ws = getWorkspace();

		Entity suEntity = null;
		List<Entity> entities = ws.getEntities();
		for ( Entity entity : entities ) {
			if ( entity.isSamplingUnit() ) {
				suEntity = entity;
				break;
			}
		}
		return suEntity;
	}

}
