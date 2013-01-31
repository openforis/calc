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
public class SpecimenFactDao {

	@Autowired
	private TransformationDatabase transformationDatabase;

	public SpecimenFactDao() {
		super();
	}

	synchronized 
	public void createOrUpdateFactTable(FlatDataStream data, String schema, String tableName) {
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
			throw new PersistenceException("Error while saving data into area fact table", e);
		} catch ( IOException e ) {
			throw new PersistenceException("Error while reading data input", e);
		}

	}

}
