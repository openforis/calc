package org.openforis.calc.persistence;

import static org.openforis.calc.persistence.jooq.Tables.SPECIMEN_CATEGORICAL_VALUE_VIEW;
import static org.openforis.calc.persistence.jooq.Tables.SPECIMEN_NUMERIC_VALUE;
import static org.openforis.calc.persistence.jooq.Tables.SPECIMEN_VIEW;

import java.util.Collection;

import org.jooq.Field;
import org.jooq.JoinType;
import org.jooq.SelectQuery;
import org.jooq.impl.Factory;
import org.openforis.calc.io.flat.FlatDataStream;
import org.openforis.calc.model.SpecimenView;
import org.openforis.calc.model.VariableMetadata;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.tables.PlotCategoricalValueView;
import org.openforis.calc.persistence.jooq.tables.PlotNumericValueView;
import org.openforis.calc.persistence.jooq.tables.SpecimenCategoricalValueView;
import org.openforis.calc.persistence.jooq.tables.SpecimenNumericValue;
import org.openforis.calc.persistence.jooq.tables.records.SpecimenViewRecord;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 * @author M. Togna
 */
@Component
@Transactional
public class SpecimenViewDao extends JooqDaoSupport<SpecimenViewRecord, SpecimenView> {

	private static final org.openforis.calc.persistence.jooq.tables.SpecimenView V = SPECIMEN_VIEW;
	
	public SpecimenViewDao() {
		super(SPECIMEN_VIEW, SpecimenView.class);
	}

	@Override
	protected Field<?> pk() {
		return V.SPECIMEN_ID;
	}

	public FlatDataStream streamAll(Collection<VariableMetadata> variables,Collection<VariableMetadata> parentVariables, String[] fields, int observationUnitId) {
		
		if ( fields != null ) {
			Factory create = getJooqFactory();
			
			SelectQuery select = create.selectQuery();
			//select.addSelect(V.SPECIMEN_ID);
			select.addFrom(V);
			
			int fieldIndex = 0; 
			for ( String fieldName : fields ) {
				VariableMetadata variable = getVariableMetadata(variables, fieldName);
				VariableMetadata parentVariable = getVariableMetadata(parentVariables, fieldName);
				if ( variable != null ) {
					Integer variableId = variable.getVariableId();
					String variableName = variable.getVariableName();
					
					if ( variable.isCategorical() ) {
						SpecimenCategoricalValueView aliasTable = SPECIMEN_CATEGORICAL_VALUE_VIEW.as( "alias_"+ (fieldIndex++) );
						select.addSelect(aliasTable.CATEGORY_CODE.as(variableName));
						select.addJoin(
								aliasTable, 
								JoinType.LEFT_OUTER_JOIN, 
								V.SPECIMEN_ID.eq(aliasTable.SPECIMEN_ID).and(aliasTable.VARIABLE_ID.eq(variableId))
								);
					} else if ( variable.isNumeric() ) {
						SpecimenNumericValue aliasTable = SPECIMEN_NUMERIC_VALUE.as( "alias_"+ (fieldIndex++) );
						select.addSelect(aliasTable.VALUE.as(variableName));
						select.addJoin(
								aliasTable, 
								JoinType.LEFT_OUTER_JOIN, 
								V.SPECIMEN_ID.eq(aliasTable.SPECIMEN_ID).and(aliasTable.VARIABLE_ID.eq(variableId))
								);
					}
				} else if ( parentVariable != null ) {
					Integer variableId = parentVariable.getVariableId();
					String variableName = parentVariable.getVariableName();

					if ( parentVariable.isCategorical() ) {
						PlotCategoricalValueView pcv = PlotCategoricalValueView.PLOT_CATEGORICAL_VALUE_VIEW.as( "pcv_" + (fieldIndex++) );
						select.addSelect( pcv.CATEGORY_CODE.as(variableName) );
						
						select.addJoin(
								pcv, 
								JoinType.LEFT_OUTER_JOIN,
								V.PLOT_SECTION_ID.eq( pcv.PLOT_SECTION_ID ).and( pcv.VARIABLE_ID.eq(variableId) )								
								);
					} else if ( parentVariable.isNumeric() ) {
						PlotNumericValueView pnv = PlotNumericValueView.PLOT_NUMERIC_VALUE_VIEW.as( "pnv_" + (fieldIndex++) );
						select.addSelect( pnv.VALUE.as(variableName) );
						
						select.addJoin(
								pnv, 
								JoinType.LEFT_OUTER_JOIN,
								V.PLOT_SECTION_ID.eq( pnv.PLOT_SECTION_ID ).and( pnv.VARIABLE_ID.eq(variableId) )				
								);
					}
				} else {
					select.addSelect( getFields(fieldName) );
				}
			}
			
			select.addConditions( V.SPECIMEN_OBS_UNIT_ID.eq(observationUnitId) );
			
			return stream( select.fetch() );
			
		} else {
			return stream( fields, V.SPECIMEN_OBS_UNIT_ID, observationUnitId );
		}
	}

	private VariableMetadata getVariableMetadata(Collection<VariableMetadata> variables, String fieldName) {
		VariableMetadata variable = null;
		if ( variables != null ) {
			for ( VariableMetadata variableMetadata : variables ) {
				if ( variableMetadata.getVariableName().equals(fieldName) ) {
					variable = variableMetadata;
				}
			}
		}
		return variable;
	}

}
