package org.openforis.calc.transformation;

import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.tableoutput.TableOutputMeta;

/**
 * 
 * @author Mino Togna
 *
 */
public class OutputDataTransformation extends AbstractTransformation {

	private StepMeta stepMeta;
	private DatabaseMeta databaseMeta;
	private String tableName;
	private String schema;
	
	public OutputDataTransformation(DatabaseMeta databaseMeta, String schema, String tableName) {
		this.databaseMeta = databaseMeta;
		this.schema = schema;
		this.tableName = tableName;
		
		initStepMeta();
		
	}

	private void initStepMeta() {
		TableOutputMeta tableOutputMeta = new TableOutputMeta();
		tableOutputMeta.setDatabaseMeta(databaseMeta);
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
