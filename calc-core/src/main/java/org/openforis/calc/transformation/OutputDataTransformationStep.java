package org.openforis.calc.transformation;

import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.tableoutput.TableOutputMeta;

/**
 * 
 * @author Mino Togna
 *
 */
public class OutputDataTransformationStep extends AbstractTransformationStep {

	private StepMeta stepMeta;
	private TransformationDatabase transformationDatabase;
	private String tableName;
	private String schema;
	
	public OutputDataTransformationStep(TransformationDatabase transformationDatabase, String schema, String tableName) {
		this.transformationDatabase = transformationDatabase;
		this.schema = schema;
		this.tableName = tableName;
		
		initStepMeta();
		
	}

	private void initStepMeta() {
		TableOutputMeta tableOutputMeta = new TableOutputMeta();
		tableOutputMeta.setDatabaseMeta(transformationDatabase.getDatabaseMeta());
		tableOutputMeta.setCommitSize(5000);
		tableOutputMeta.setSchemaName(schema);
		tableOutputMeta.setTableName(tableName);
		tableOutputMeta.setUseBatchUpdate(true);
		tableOutputMeta.setSpecifyFields(false);
		tableOutputMeta.setTableNameInField(false);
		tableOutputMeta.setTableNameInTable(false);
		tableOutputMeta.setTruncateTable(true);
		
		stepMeta = new StepMeta("dataOutputStep", "dataOutput", tableOutputMeta);
	}
	
	@Override
	public StepMeta getStepMeta() {
		return stepMeta;
	}
}
