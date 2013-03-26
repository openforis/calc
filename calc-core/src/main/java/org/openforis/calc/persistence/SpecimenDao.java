package org.openforis.calc.persistence;

import static org.openforis.calc.persistence.jooq.Tables.SPECIMEN;
import static org.openforis.calc.persistence.jooq.Tables.SPECIMEN_CATEGORICAL_VALUE_VIEW;
import static org.openforis.calc.persistence.jooq.Tables.SPECIMEN_NUMERIC_VALUE;

import java.util.Collection;

import org.jooq.JoinType;
import org.jooq.Query;
import org.jooq.SelectQuery;
import org.jooq.impl.Factory;
import org.openforis.calc.model.Specimen;
import org.openforis.calc.model.VariableMetadata;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.Sequences;
import org.openforis.calc.persistence.jooq.tables.PlotCategoricalValueView;
import org.openforis.calc.persistence.jooq.tables.PlotNumericValueView;
import org.openforis.calc.persistence.jooq.tables.PlotSection;
import org.openforis.calc.persistence.jooq.tables.SpecimenCategoricalValueView;
import org.openforis.calc.persistence.jooq.tables.SpecimenNumericValue;
import org.openforis.calc.persistence.jooq.tables.Taxon;
import org.openforis.calc.persistence.jooq.tables.records.SpecimenRecord;
import org.openforis.commons.io.flat.FlatDataStream;
import org.openforis.commons.io.flat.FlatRecord;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 * @author M. Togna
 */
@Component
@Transactional
public class SpecimenDao extends JooqDaoSupport<SpecimenRecord, Specimen> {

	private org.openforis.calc.persistence.jooq.tables.Specimen S = org.openforis.calc.persistence.jooq.tables.Specimen.SPECIMEN;

	public SpecimenDao() {
		super(SPECIMEN, Specimen.class, SPECIMEN.PLOT_SECTION_ID, SPECIMEN.SPECIMEN_NO);
		require(SPECIMEN.SPECIMEN_NO);
	}

	@Transactional
	synchronized 
	public void updateInclusionArea(FlatDataStream dataStream) throws Exception {
		try {
			startBatch();
			Factory create = getBatchFactory();

			FlatRecord r = null;
			while ( (r = dataStream.nextRecord()) != null ) {
				int specimenId = r.getValue("specimen_id", Integer.class);
				double inclArea = r.getValue("inclusion_area", Double.class);

				Query update = 
						create
							.update( S )
							.set( S.INCLUSION_AREA, inclArea )
							.where( S.SPECIMEN_ID.eq(specimenId) );
				
				addQueryToBatch(update);
			}

			executeBatch();
		} catch ( Exception e ) {
			closeBatch();
			throw e;
		}
	}

	public Integer nextId() {
		Factory create = getJooqFactory();
		return create.nextval(Sequences.SPECIMEN_ID_SEQ).intValue();
	}

public FlatDataStream streamAll(Collection<VariableMetadata> variables,Collection<VariableMetadata> parentVariables, String[] fields, int observationUnitId) {
		
		if ( fields != null ) {
			
			Factory create = getJooqFactory();
			PlotSection plotSection = PlotSection.PLOT_SECTION.as("ps");
			
			SelectQuery select = create.selectQuery();
			select.addFrom(S);
			
			select.addJoin( plotSection, S.PLOT_SECTION_ID.eq(plotSection.PLOT_SECTION_ID) );
			
			int fieldIndex = 0; 
			for ( String fieldName : fields ) {
				VariableMetadata variable = getVariableMetadata(variables, fieldName);
				VariableMetadata parentVariable = getVariableMetadata(parentVariables, fieldName);
				if ( variable != null ) {
					Integer variableId = variable.getVariableId();
					String variableName = variable.getVariableName();
					
					if ( variable.isCategorical() ) {
						SpecimenCategoricalValueView scv = SPECIMEN_CATEGORICAL_VALUE_VIEW.as( "scv_"+ (fieldIndex++) );
						select.addSelect(scv.CATEGORY_CODE.as(variableName));
						select.addJoin(
								scv, 
								JoinType.LEFT_OUTER_JOIN, 
								S.SPECIMEN_ID.eq(scv.SPECIMEN_ID)
									.and(scv.VARIABLE_ID.eq(variableId))
//									.and( aliasTable.CURRENT.isTrue() )
								);
					} else if ( variable.isNumeric() ) {
						SpecimenNumericValue snv = SPECIMEN_NUMERIC_VALUE.as( "snv_"+ (fieldIndex++) );
						select.addSelect(snv.VALUE.as(variableName));
						select.addJoin(
								snv, 
								JoinType.LEFT_OUTER_JOIN, 
								S.SPECIMEN_ID.eq(snv.SPECIMEN_ID)
									.and(snv.VARIABLE_ID.eq(variableId))
									.and( snv.CURRENT.isTrue() )
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
								S.PLOT_SECTION_ID.eq( pcv.PLOT_SECTION_ID )
									.and( pcv.VARIABLE_ID.eq(variableId) )
//									.and( pcv.CURRENT.isTrue() )
								);
					} else if ( parentVariable.isNumeric() ) {
						PlotNumericValueView pnv = PlotNumericValueView.PLOT_NUMERIC_VALUE_VIEW.as( "pnv_" + (fieldIndex++) );
						select.addSelect( pnv.VALUE.as(variableName) );
						
						select.addJoin(
								pnv, 
								JoinType.LEFT_OUTER_JOIN,
								S.PLOT_SECTION_ID.eq( pnv.PLOT_SECTION_ID )
									.and( pnv.VARIABLE_ID.eq(variableId) )
//									.and( pnv.CURRENT.isTrue() )
								);
					}
				} else if( Taxon.TAXON.TAXON_CODE.getName().equals(fieldName) ) {
					Taxon t = Taxon.TAXON.as("t");
					
					select.addJoin(
							t,
							JoinType.LEFT_OUTER_JOIN,
							S.SPECIMEN_TAXON_ID.eq( t.TAXON_ID )
							);
					
					select.addSelect( t.TAXON_CODE );
					
				} else if( plotSection.getField(fieldName) != null ) {
					select.addSelect( plotSection.getField(fieldName) );
				} else {
					select.addSelect( getFields(fieldName) );
				}
			}
			
			select.addConditions( S.OBS_UNIT_ID.eq(observationUnitId) );
			
			return stream( select.fetch() );
			
		} else {
			return stream( fields, S.OBS_UNIT_ID, observationUnitId );
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
	
	public void deleteByObsUnit(int id) {
		Factory create = getJooqFactory();
		org.openforis.calc.persistence.jooq.tables.Specimen s = SPECIMEN;
		create
			.delete(s)
			.where(s.OBS_UNIT_ID.eq(id))
			.execute();
	}
}
