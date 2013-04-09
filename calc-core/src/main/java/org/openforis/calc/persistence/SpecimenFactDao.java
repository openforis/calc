package org.openforis.calc.persistence;
import static org.jooq.impl.Factory.coalesce;
import static org.jooq.impl.Factory.val;
import static org.openforis.calc.persistence.jooq.Tables.AOI;
import static org.openforis.calc.persistence.jooq.Tables.PLOT_CATEGORICAL_VALUE_VIEW;
import static org.openforis.calc.persistence.jooq.Tables.PLOT_SECTION;
import static org.openforis.calc.persistence.jooq.Tables.PLOT_SECTION_AOI;
import static org.openforis.calc.persistence.jooq.Tables.SAMPLE_PLOT;
import static org.openforis.calc.persistence.jooq.Tables.SPECIMEN;
import static org.openforis.calc.persistence.jooq.Tables.SPECIMEN_CATEGORICAL_VALUE_VIEW;
import static org.openforis.calc.persistence.jooq.Tables.SPECIMEN_NUMERIC_VALUE;
import static org.openforis.calc.persistence.jooq.Tables.STRATUM;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import mondrian.spi.Dialect.Datatype;

import org.jooq.Field;
import org.jooq.JoinType;
import org.jooq.SelectQuery;
import org.jooq.impl.Factory;
import org.openforis.calc.model.AoiHierarchyLevelMetadata;
import org.openforis.calc.model.AoiHierarchyMetadata;
import org.openforis.calc.model.ObservationUnitMetadata;
import org.openforis.calc.model.SurveyMetadata;
import org.openforis.calc.model.VariableMetadata;
import org.openforis.calc.persistence.jooq.rolap.SpecimenFactTable;
import org.openforis.calc.persistence.jooq.tables.Aoi;
import org.openforis.calc.persistence.jooq.tables.PlotCategoricalValueView;
import org.openforis.calc.persistence.jooq.tables.PlotSection;
import org.openforis.calc.persistence.jooq.tables.PlotSectionAoi;
import org.openforis.calc.persistence.jooq.tables.SamplePlot;
import org.openforis.calc.persistence.jooq.tables.Specimen;
import org.openforis.calc.persistence.jooq.tables.SpecimenCategoricalValueView;
import org.openforis.calc.persistence.jooq.tables.SpecimenNumericValue;
import org.openforis.calc.persistence.jooq.tables.Stratum;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author M. Togna
 */
@Component
@Transactional
public class SpecimenFactDao extends RolapFactDao<SpecimenFactTable> {

	private Specimen S = Specimen.SPECIMEN;
	private SpecimenNumericValue SNV = SpecimenNumericValue.SPECIMEN_NUMERIC_VALUE;
	private SpecimenCategoricalValueView SCVV = SpecimenCategoricalValueView.SPECIMEN_CATEGORICAL_VALUE_VIEW;
	private PlotCategoricalValueView PCVV = PlotCategoricalValueView.PLOT_CATEGORICAL_VALUE_VIEW;
	
	@Override
	protected SelectQuery createFactSelect(SpecimenFactTable fact){
		ObservationUnitMetadata unit = fact.getObservationUnitMetadata();
		int unitId = unit.getObsUnitId();

		Specimen s = SPECIMEN.as("s");
		PlotSectionAoi pa = PLOT_SECTION_AOI.as("pa");
		Stratum st = STRATUM.as("st");
		SamplePlot sp = SAMPLE_PLOT.as("sp");
		PlotSection ps = PLOT_SECTION.as("ps");
		
		Factory create = getJooqFactory();
		SelectQuery select = create.selectQuery();
		
		select.addSelect( st.STRATUM_ID );
		select.addSelect( sp.CLUSTER_ID );
		select.addSelect( s.PLOT_SECTION_ID.as(fact.PLOT_ID.getName()) );
		select.addSelect( s.SPECIMEN_ID );
		select.addSelect( s.SPECIMEN_TAXON_ID );
		select.addSelect( s.INCLUSION_AREA );
		select.addSelect( ps.PLOT_SECTION_AREA );		
		select.addSelect( val(1).as(fact.COUNT.getName()) );
		
		select.addFrom(s);
		
		select.addJoin( ps, s.PLOT_SECTION_ID.eq(ps.PLOT_SECTION_ID) );
		select.addJoin( sp , ps.SAMPLE_PLOT_ID.eq(sp.SAMPLE_PLOT_ID) );
		select.addJoin( st, sp.STRATUM_ID.eq(st.STRATUM_ID) );
		
		select.addJoin(pa, s.PLOT_SECTION_ID.eq(pa.PLOT_SECTION_ID));
		
		select.addConditions(s.OBS_UNIT_ID.eq(unitId));
		
		addAoisToSelect(unit, pa, select);
		
//		addUnitVariablesToSelect(unit, s, select, fact);
		addVariablesToSelect(unit.getVariableMetadata(), select);
		ObservationUnitMetadata parentUnit = unit.getObsUnitParent();
		if( parentUnit != null ) {
			addVariablesToSelect(parentUnit.getVariableMetadata(), select);
//			addParentVariablesToSelect(parentUnit, s, select);
		}
				
		return select;
	}
	
	@Override
	protected void updateVariableValues(SpecimenFactTable table) {
		ObservationUnitMetadata unit = table.getObservationUnitMetadata();
		Factory create = getJooqFactory();
		
		for ( VariableMetadata var : unit.getVariableMetadata() ) {
			if( var.isForAnalysis() ) {
				String varName = var.getVariableName();
				int varId = var.getVariableId();
				
				StringBuilder sql = new StringBuilder();  
				sql.append("update ");
				sql.append( table.getSchema() );
				sql.append(".");
				sql.append( table.getName() );
				sql.append(" set ");
				sql.append( varName );
				sql.append(" = c.");
				if( var.isCategorical() ) {
					sql.append(SCVV.CATEGORY_ID.getName());
				}
				if( var.isNumeric() ) {
					sql.append(SNV.VALUE.getName());
				}
				sql.append( " from ");
				sql.append(SCVV.getSchema().getName());
				sql.append(".");
				if ( var.isCategorical() ) {
					sql.append(SCVV.getName());
				}
				if ( var.isNumeric() ) {
					sql.append(SNV.getName());
				}
				sql.append(" as c ");
				sql.append(" where ");
				sql.append("c.");
				sql.append(SCVV.VARIABLE_ID.getName());
				sql.append(" = ");
				sql.append( varId );
				sql.append(" and c.");
				sql.append(SCVV.CURRENT.getName());
				sql.append(" and c.");
				sql.append(SCVV.SPECIMEN_ID.getName());
				sql.append(" = ");
				sql.append( table.getName());
				sql.append(".");
				sql.append( SCVV.SPECIMEN_ID.getName() );

				String sqlString = sql.toString();
				getLog().debug("Inserting " + varName + " into " + table);
				getLog().debug(sqlString);
				
				create.execute(sqlString);
				
				getLog().debug("Done inserting " + varName);
			}
		}
		
		ObservationUnitMetadata unitParent = unit.getObsUnitParent();
		for ( VariableMetadata var : unitParent.getVariableMetadata() ) {
			if ( var.isCategorical() && var.isForAnalysis() ) {
				
				String varName = var.getVariableName();
				int varId = var.getVariableId();
				
				StringBuilder sql = new StringBuilder();  
				sql.append("update ");
				sql.append( table.getSchema() );
				sql.append(".");
				sql.append( table.getName() );
				sql.append(" set ");
				sql.append( varName );
				sql.append(" = c.");
				sql.append(PCVV.CATEGORY_ID.getName());
				sql.append( " from ");
//				sql.append(" specimen s join plot_cat_value_view c on s.plot_section_id = c.plot_section_id ") ;
				sql.append(S.getSchema().getName());
				sql.append(".");
				sql.append(S.getName());
				sql.append(" s ");
				sql.append(" join ");
				sql.append(PCVV.getSchema().getName());
				sql.append(".");
				sql.append(PCVV.getName());
				sql.append(" c ");
				sql.append(" on ");
				sql.append("s.");
				sql.append(S.PLOT_SECTION_ID.getName());
				sql.append(" = c.");
				sql.append(PCVV.PLOT_SECTION_ID.getName());
//				sql.append(PCVV.getName());
				sql.append(" where ");
				sql.append("c.");
				sql.append(PCVV.VARIABLE_ID.getName());
				sql.append(" = ");
				sql.append( varId );
				sql.append(" and c.");
				sql.append(PCVV.CURRENT.getName());
				sql.append(" and s.");
				sql.append(S.SPECIMEN_ID.getName());
				sql.append(" = ");
				sql.append( table.getName());
				sql.append(".");
				sql.append(S.SPECIMEN_ID.getName());

				String sqlString = sql.toString();
				getLog().debug("Inserting " + varName + " into " + table);
				getLog().debug(sqlString);
				
				create.execute(sqlString);
				
				getLog().debug("Done inserting " + varName);
			}
		}
	}

	private void addAoisToSelect(ObservationUnitMetadata unit, PlotSectionAoi pa, SelectQuery select) {
		SurveyMetadata survey = unit.getSurveyMetadata();
		List<AoiHierarchyMetadata> aoiHierarchies = survey.getAoiHierarchyMetadata();
		// TODO multiple AOI hierarchies
		AoiHierarchyMetadata aoiHierarchy = aoiHierarchies.get(0);
		AoiHierarchyLevelMetadata leafLevel = aoiHierarchy.getMaxLevel();
		String leafLevelName = leafLevel.getAoiHierarchyLevelName();
		
		// Select leaf AOI
		Aoi a = AOI.as("a");
		select.addJoin(a, 
					a.AOI_ID
						.eq( pa.AOI_ID )
						.and( a.AOI_HIERARCHY_LEVEL_ID.eq(leafLevel.getAoiHierarchyLevelId()) )
				);
//		select.addConditions(a.AOI_HIERARCHY_LEVEL_ID.eq(leafLevel.getAoiHierarchyLevelId()));
		select.addSelect(pa.AOI_ID.as(leafLevelName));
		
		// Add select and join for non-leaf AOIs
		List<AoiHierarchyLevelMetadata> aoiLevels = aoiHierarchy.getLevelMetadata();
		Aoi table = a;
		for (int i = aoiLevels.size()-2; i >= 0; i--) {
			AoiHierarchyLevelMetadata level = aoiLevels.get(i);
			String levelName = level.getAoiHierarchyLevelName();
			
			Aoi parentTable = AOI.as("a"+i);
			select.addSelect(parentTable.AOI_ID.as(levelName));
			select.addJoin(parentTable, table.AOI_PARENT_ID.eq(parentTable.AOI_ID));
			table = parentTable;
		}
	}
	
//	@SuppressWarnings("unchecked")	
	private void addVariablesToSelect(  Collection<VariableMetadata> variables , SelectQuery select ) {
		//		int idx = 0;
		for ( VariableMetadata var : variables ) {			
			if ( var.isForAnalysis() ) {
				
//				Integer varId = var.getVariableId();
				String varName = var.getVariableName();
				
				Field<?> field = ( var.isCategorical() ? Factory.val( -1 ) : Factory.castNull(BigDecimal.class) ).as(varName); 
				select.addSelect( field );
//				idx += 1;
//				
//				if( var.isCategorical() ) {
//					SpecimenCategoricalValueView v = SPECIMEN_CATEGORICAL_VALUE_VIEW.as("v"+idx);
//					
//					select.addSelect( 
//							coalesce( v.CATEGORY_ID , -1 ).as(varName)
//							);
//					
//					select.addJoin(
//							v,
//							JoinType.LEFT_OUTER_JOIN,
//							s.SPECIMEN_ID.eq(v.SPECIMEN_ID)
//							.and(v.CURRENT.isTrue())
//							.and(v.VARIABLE_ID.eq(varId))
//							);
//					
//				} else if ( var.isNumeric() ) {
//					// join with specimen_numeric_value
//					//
//					SpecimenNumericValue v = SPECIMEN_NUMERIC_VALUE.as("v"+idx);
//					
//					select.addSelect( v.VALUE.as(varName) );
//					
//					select.addJoin(
//							v,
//							JoinType.LEFT_OUTER_JOIN,
//							s.SPECIMEN_ID.eq( v.SPECIMEN_ID )
//							.and( v.VARIABLE_ID.eq(varId) )
//							.and( v.CURRENT.isTrue() )
//							);
//				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void addParentVariablesToSelect(ObservationUnitMetadata unit, Specimen view, SelectQuery select) {
		Collection<VariableMetadata> variables = unit.getVariableMetadata();	
		int idx = 0;		
		for ( VariableMetadata var : variables ) {
			if ( var.isCategorical() && var.isForAnalysis() ) {
				Integer varId = var.getVariableId();
				String varName = var.getVariableName();
				idx += 1;
				
				PlotCategoricalValueView c = PLOT_CATEGORICAL_VALUE_VIEW.as("c" + idx);
	
				select.addSelect( coalesce(c.CATEGORY_ID, -1).as(varName) );
	
				select.addJoin(
						c, 
						JoinType.LEFT_OUTER_JOIN, 
						view.PLOT_SECTION_ID.eq(c.PLOT_SECTION_ID)
						.and( c.VARIABLE_ID.eq(varId) )
						.and( c.CURRENT.isTrue() )
				
				);
			}
		}
	}
}
