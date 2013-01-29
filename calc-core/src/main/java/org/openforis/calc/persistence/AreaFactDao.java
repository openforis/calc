package org.openforis.calc.persistence;

import java.io.IOException;

import org.openforis.calc.io.flat.FlatDataStream;
import org.openforis.calc.transformation.InputDataTransformationStep;
import org.openforis.calc.transformation.OutputDataTransformationStep;
import org.openforis.calc.transformation.Transformation;
import org.openforis.calc.transformation.TransformationDatabase;
import org.pentaho.di.core.exception.KettleException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Mino Togna
 */
@Component
@Transactional
public class AreaFactDao {
	// extends JooqDaoSupport<AoiRecord, Aoi> {

	@Autowired
	private TransformationDatabase transformationDatabase;

	public AreaFactDao() {
		super();
	}

	synchronized 
	public void createOrUpdateAreaFactTable(FlatDataStream data, String schema, String tableName) {
		try {
			Transformation transformation = new Transformation(transformationDatabase, "importResults");

			// 1. drop table
			String dropTableSql = "drop table if exists " + schema + "." + tableName + " cascade";
			transformation.executeSql(dropTableSql);

			// 2. read data in memory
			InputDataTransformationStep inputDataTransformation = new InputDataTransformationStep(data);
			transformation.addStep(inputDataTransformation);

			OutputDataTransformationStep outputDataTransformation = new OutputDataTransformationStep(transformationDatabase, schema, tableName);
			transformation.addStep(outputDataTransformation);

			// 3. create table
			transformation.executeSQLStatementsString();

			// 4. exec transformation
			transformation.executeAndWaitUntilFinished();
		} catch ( KettleException e ) {
			throw new PersistenceException("Error while importing estimation results", e);
		} catch ( IOException e ) {
			throw new PersistenceException("Error while reading data input", e);
		}

	}

	// public FlatDataStream streamByHierarchyName(String[] fieldNames, String hierarchyName) {
	// Field<?>[] fields = getFields(fieldNames);
	// if(fields == null || fields.length == 0) {
	// fields = AOI.getFields().toArray(new Field[AOI.getFields().size()]);
	// }
	// Factory create = getJooqFactory();
	//
	// Result<?> result =
	// create
	// .select(fields)
	// .from(AOI)
	// .join(AOI_HIERARCHY)
	// .on(AOI.AOI_HIERARCHY_ID.eq(AOI_HIERARCHY.AOI_HIERARCHY_ID))
	// .where(AOI_HIERARCHY.AOI_HIERARCHY_NAME.eq(hierarchyName))
	// .fetch();
	//
	// return stream( result );
	// }

}
