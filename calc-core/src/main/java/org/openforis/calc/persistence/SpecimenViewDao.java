package org.openforis.calc.persistence;

import static org.openforis.calc.persistence.jooq.Tables.SPECIMEN_CATEGORICAL_VALUE_VIEW;
import static org.openforis.calc.persistence.jooq.Tables.SPECIMEN_NUMERIC_VALUE;
import static org.openforis.calc.persistence.jooq.Tables.SPECIMEN_VIEW;

import org.jooq.Field;
import org.jooq.JoinType;
import org.jooq.SelectQuery;
import org.jooq.impl.Factory;
import org.openforis.calc.io.flat.FlatDataStream;
import org.openforis.calc.model.ObservationUnitMetadata;
import org.openforis.calc.model.SpecimenView;
import org.openforis.calc.model.SurveyMetadata;
import org.openforis.calc.model.VariableMetadata;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.tables.SpecimenCategoricalValueView;
import org.openforis.calc.persistence.jooq.tables.SpecimenNumericValue;
import org.openforis.calc.persistence.jooq.tables.records.SpecimenViewRecord;
import org.openforis.calc.service.MetadataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 * @author Mino Togna
 */
@Component
@Transactional
public class SpecimenViewDao extends JooqDaoSupport<SpecimenViewRecord, SpecimenView> {

	private static final org.openforis.calc.persistence.jooq.tables.SpecimenView V = SPECIMEN_VIEW;

	@Autowired
	private MetadataService metadataService;

	public SpecimenViewDao() {
		super(SPECIMEN_VIEW, SpecimenView.class);
	}

	@Override
	protected Field<?> pk() {
		return V.SPECIMEN_ID;
	}

	public FlatDataStream streamAll(String[] fields, String surveyName, int observationUnitId) {
		
		if ( fields != null ) {
			Factory create = getJooqFactory();
			SelectQuery select = create.selectQuery();
			//select.addSelect(V.SPECIMEN_ID);
			select.addFrom(V);
			
			int fieldIndex = 0; 
			for ( String fieldName : fields ) {
				VariableMetadata variable = getVariableMetadata(surveyName, observationUnitId, fieldName);
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
				} else {
					select.addSelect( getField(fieldName) );
				}
				select.addConditions( V.SPECIMEN_OBS_UNIT_ID.eq(observationUnitId) );
			}
			
			return stream( select.fetch() );
			
		} else {
			return stream( fields, V.SPECIMEN_OBS_UNIT_ID, observationUnitId );
		}
	}

	private VariableMetadata getVariableMetadata(String surveyName, int observationUnitId, String fieldName) {
		SurveyMetadata survey = metadataService.getSurveyMetadata(surveyName);
		ObservationUnitMetadata obsUnit = survey.getObservationUnitMetadataById(observationUnitId);
		VariableMetadata variable = obsUnit.getVariableMetadataByName(fieldName);
		return variable;
	}

	// public Object[] extractKey(FlatRecord r, int obsUnitId) {
	// return extractKey(r, obsUnitId, V.CLUSTER_CODE, V.PLOT_NO, V.PLOT_SECTION, V.VISIT_TYPE);
	// }
}
