package org.openforis.calc.metadata;

import org.apache.commons.beanutils.PropertyUtils;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.tools.StringUtils;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.Variable.Scale;

/**
 * 
 * @author Mino Togna
 *
 */
public class VariableRecordMapper implements RecordMapper<Record, Variable<?> >{

	private Workspace workspace;

	public VariableRecordMapper(Workspace workspace) {
		this.workspace = workspace;
	}

	@Override
	public Variable<?> map( Record record ) {
		String string = record.getValue( "scale", String.class );
		Scale scale = Variable.Scale.valueOf( string );

		// create variable instance based on its scale
		// "case when scale='TEXT' then 'T' when scale in ( 'RATIO','INTERVAL','OTHER') then 'Q' when scale='BINARY' then 'B' else 'C' end"
		Variable<?> variable = null;
		switch ( scale ) {
		case TEXT:
			variable = new TextVariable();
			break;
		case BINARY:
			variable = new BinaryVariable();
			break;
		case NOMINAL:
			variable = new MultiwayVariable();
			break;
		default:
			variable = new QuantitativeVariable();
		}
		
		// set variable fields
		Field<?>[] fields = record.fields();
		for (Field<?> field : fields) {
			Object value = record.getValue( field );
			if( value != null ) {
				try {
					String fieldName = StringUtils.toCamelCaseLC( field.getName() );
					PropertyUtils.setProperty( variable, fieldName, value );
				} catch ( Exception e ) {
					throw new IllegalStateException( "Exception while loading variable", e );
				}
			}
		}
		
		// add variable to parent entity
		Entity entity = workspace.getEntityById( variable.getEntityId() );
		entity.addVariable(variable);
		
		return variable;
	}
	
	
	
}