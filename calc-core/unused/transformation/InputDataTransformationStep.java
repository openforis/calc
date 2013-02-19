/**
 * 
 */
package org.openforis.calc.transformation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.openforis.calc.io.flat.FlatDataStream;
import org.openforis.calc.io.flat.FlatRecord;
import org.openforis.calc.model.VariableMetadata;
import org.openforis.calc.persistence.jooq.tables.Aoi;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.datagrid.DataGridMeta;

/**
 * @author Mino Togna
 * @author Gino Miceli
 * 
 */
@Deprecated
public class InputDataTransformationStep extends AbstractTransformationStep {

	private FlatDataStream dataStream;
//	private DataType dataType;
	private StepMeta stepMeta;

	/**
	 * @throws IOException
	 * 
	 */
	//, DataType dataType
	public InputDataTransformationStep(FlatDataStream dataStream) throws IOException {
		this.dataStream = dataStream;
//		this.dataType = dataType;

		initStepMeta();
	}

	private void initStepMeta() throws IOException {
		List<String> columnNames = dataStream.getFieldNames();
		int columnSize = columnNames.size();

		DataGridMeta inputMeta = new DataGridMeta();
		inputMeta.setFieldName(columnNames.toArray(new String[columnSize]));
		String[] dataTypes = getDataTypes();
		// new String[columnSize];
		// Arrays.fill(dataTypes, ValueMeta.getTypeDesc(ValueMeta.TYPE_NUMBER));
		inputMeta.setFieldType(dataTypes);

		inputMeta.setDecimal(new String[columnSize]);
		inputMeta.setFieldFormat(new String[columnSize]);
		inputMeta.setFieldLength(new int[columnSize]);
		int[] precisions = new int[columnSize];
		Arrays.fill(precisions, Double.MAX_EXPONENT);
		inputMeta.setFieldPrecision(precisions);
		inputMeta.setCurrency(new String[columnSize]);
		inputMeta.setGroup(new String[columnSize]);

		setInputValues(inputMeta);

		stepMeta = new StepMeta("dataInputTrans", "dataInput", inputMeta);
	}

	private String[] getDataTypes() {
		List<String> columnNames = dataStream.getFieldNames();
		String[] dataTypes = new String[columnNames.size()];
		int i = 0;
		
		for ( String colName : columnNames ) {
			int kettleType = ValueMeta.TYPE_NUMBER;
			
			//OLD2
//			String sqlType = dataType.getDataType(colName);
//			if( DataType.TYPE_INTEGER.equals(sqlType)){
////			if ( Aoi.AOI.AOI_ID.getName().equals(colName) ) {
//				kettleType = ValueMeta.TYPE_INTEGER;
//			} else if( DataType.TYPE_NUMBER_PRECISION.equals(sqlType)){
//				kettleType = ValueMeta.TYPE_NUMBER;
//			} else {
//				kettleType = ValueMeta.TYPE_NONE;
//			}
			
			// OLD
//				for ( VariableMetadata var : variables ) {
//					if ( var.getVariableName().equals(colName) ) {
//						type = var.isCategorical() ? ValueMeta.TYPE_INTEGER : ValueMeta.TYPE_NUMBER;
//						break;
//					}
//				}
//			}
			
			dataTypes[i++] = ValueMeta.getTypeDesc(kettleType);
		}
		return dataTypes;
	}

	@Override
	public StepMeta getStepMeta() {
		return stepMeta;
	}

	private void setInputValues(DataGridMeta inputMeta) throws IOException {
		FlatRecord r;
		List<String> fieldNames = dataStream.getFieldNames();
		List<List<String>> dataLines = new ArrayList<List<String>>();

		while( (r = dataStream.nextRecord()) != null ) {
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
