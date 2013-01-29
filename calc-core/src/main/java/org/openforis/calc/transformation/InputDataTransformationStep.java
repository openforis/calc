/**
 * 
 */
package org.openforis.calc.transformation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openforis.calc.io.flat.FlatDataStream;
import org.openforis.calc.io.flat.FlatRecord;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.datagrid.DataGridMeta;

/**
 * @author Mino Togna
 * @author Gino Miceli
 * 
 */
public class InputDataTransformationStep extends AbstractTransformationStep {

	private FlatDataStream dataStream;
	private StepMeta stepMeta;

	/**
	 * @throws IOException
	 * 
	 */
	public InputDataTransformationStep(FlatDataStream dataStream) throws IOException {
		this.dataStream = dataStream;

		initStepMeta();
	}

	private void initStepMeta() throws IOException {
		List<String> columnNames = dataStream.getFieldNames();
		int columnSize = columnNames.size();

		DataGridMeta inputMeta = new DataGridMeta();
		inputMeta.setFieldName(columnNames.toArray(new String[columnSize]));
		String[] dataTypes = new String[columnSize];
		Arrays.fill(dataTypes, ValueMeta.getTypeDesc(ValueMeta.TYPE_NUMBER));
		inputMeta.setFieldType(dataTypes);

		inputMeta.setDecimal(new String[columnSize]);
		inputMeta.setFieldFormat(new String[columnSize]);
		inputMeta.setFieldLength(new int[columnSize]);
		inputMeta.setFieldPrecision(new int[columnSize]);
		inputMeta.setCurrency(new String[columnSize]);
		inputMeta.setGroup(new String[columnSize]);

		setInputValues(inputMeta);

		stepMeta = new StepMeta("dataInputTrans", "dataInput", inputMeta);
	}

	@Override
	public StepMeta getStepMeta() {
		return stepMeta;
	}

	private void setInputValues(DataGridMeta inputMeta) throws IOException {
		FlatRecord r;
		List<String> fieldNames = dataStream.getFieldNames();
		List<List<String>> dataLines = new ArrayList<List<String>>();

		while ( (r = dataStream.nextRecord()) != null ) {
			List<String> line = new ArrayList<String>();
			for ( String fName : fieldNames ) {
				String value = r.getValue(fName, String.class);
				line.add(value);
			}

			dataLines.add(line);
		}
		inputMeta.setDataLines(dataLines);
	}

}
