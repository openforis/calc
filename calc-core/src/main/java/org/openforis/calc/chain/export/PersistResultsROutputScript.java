package org.openforis.calc.chain.export;

import java.math.BigDecimal;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Select;
import org.jooq.Table;
import org.jooq.UpdateSetMoreStep;
import org.jooq.impl.DynamicTable;
import org.jooq.impl.SQLDataType;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.EntityManager;
import org.openforis.calc.psql.AlterTableStep.AlterColumnStep;
import org.openforis.calc.psql.CreateViewStep.AsStep;
import org.openforis.calc.psql.DropViewStep;
import org.openforis.calc.psql.Psql;
import org.openforis.calc.r.RScript;
import org.openforis.calc.r.RVariable;
import org.openforis.calc.r.RVector;
import org.openforis.calc.r.SetValue;
import org.openforis.calc.schema.DataSchema;
import org.openforis.calc.schema.DataTable;
import org.openforis.calc.schema.EntityDataView;
import org.openforis.calc.schema.ResultTable;
import org.openforis.calc.schema.Schemas;

/**
 * 
 * @author M. Togna
 *
 */
public class PersistResultsROutputScript extends ROutputScript {

	public PersistResultsROutputScript(int index , CalculationStepsGroup group, Schemas schemas, EntityManager entityManager) {
		super( "persist-results.R", createScript(group , schemas, entityManager), Type.SYSTEM , index );
	}

	private static RScript createScript(CalculationStepsGroup group, Schemas schemas, EntityManager entityManager) {
		RScript r = r();
		
		Workspace workspace = group.getWorkspace();
		if( workspace.hasSamplingDesign() ){
			// add read base unit script
			Entity baseUnit 	= workspace.getSamplingUnit();
			RScript persistBaseUnitResults = createPersistBaseUnitResultsScript( schemas , baseUnit , group, entityManager );
			r.addScript( persistBaseUnitResults );
		}
		
		for (Integer entityId : group.entityIds()) {
			Entity entity = workspace.getEntityById( entityId );
			
			RScript persistEntityResults = createPersistEntityResultsScript( schemas , entity , group, entityManager );
			r.addScript( persistEntityResults );
		}
		
		return r;
	}

	private static RScript createPersistBaseUnitResultsScript(Schemas schemas, Entity entity, CalculationStepsGroup group, EntityManager entityManager) {
		RScript r					= r();
		
		DataSchema schema 			= schemas.getDataSchema();
		
		EntityDataView view 		= schema.getDataView( entity );
		DataTable table 			= schema.getDataTable( entity );
		RVariable dataFrame 		= r().variable( entity.getName() );

		// drop weight column if exists
		r.addScript( r().dbSendQuery( CONNECTION_VAR, psql().alterTable( table ).dropColumnIfExists( table.getWeightField(),true ) ));
		// add weight column to sammpling unit table
		r.addScript( r().dbSendQuery( CONNECTION_VAR, psql().alterTable( table ).addColumn( table.getWeightField() ) ));
		
		// temporary results table
		DynamicTable<Record> resultTable 			= new DynamicTable<Record>( "_tmp_weight_result" , schema.getName() );
		Field<Long> resultTableIdField 				= resultTable.getLongField( table.getIdField().getName() );
		Field<BigDecimal> resultTableWeightField 	= resultTable.getBigDecimalField( table.getWeightField().getName() );
		
		// write results to temporary table
		r.addScript( r().dbRemoveTable(CONNECTION_VAR, resultTable.getName()) );
		r.addScript( r().dbWriteTable(CONNECTION_VAR, resultTable.getName(), dataFrame) );
		
		// convert id datatype from varchar to bigint
		AlterColumnStep alterPkey = psql()
				.alterTable( resultTable )
				.alterColumn( resultTableIdField )
				.type( SQLDataType.BIGINT )
				.using( resultTableIdField.getName() + "::bigint" );

		r.addScript( r().dbSendQuery(CONNECTION_VAR, alterPkey) );
		
		// update plot weight column joining with temp result table
		Table<Record> cursor = psql()
			.select()
			.from( resultTable )
			.asTable( "r" );
		
		UpdateSetMoreStep<Record> update = psql()
			.update( table )
			.set( table.getWeightField() , cursor.field(resultTableWeightField) );
		
		Condition joinCondition 		= table.getIdField().eq( cursor.field(resultTableIdField) );
		
		r.addScript( r().dbSendQuery(CONNECTION_VAR, psql().updateWith(cursor, update, joinCondition)) );
		
		// drop view and recreate
		DropViewStep dropViewIfExists 	= psql().dropViewIfExists(view);
		r.addScript( r().dbSendQuery(CONNECTION_VAR, dropViewIfExists ) );
		
		Select<?> selectView 			= entityManager.getViewSelect( entity , true );
		AsStep createView 				= new Psql().createView(view).as(selectView);
		r.addScript(r().dbSendQuery( CONNECTION_VAR, createView ));
		
		// remove temporary result table
		r.addScript( r().dbRemoveTable( CONNECTION_VAR, resultTable.getName()) );
		
		return r;
	}

	private static RScript createPersistEntityResultsScript(Schemas schemas, Entity entity, CalculationStepsGroup group, EntityManager entityManager) {
		RScript r					= r();
		
		DataSchema schema 			= schemas.getDataSchema();
		EntityDataView view 		= schema.getDataView( entity );
		RVariable dataFrame 		= r().variable( entity.getName() );
		
		Field<?> primaryKeyField 	= view.getPrimaryKey().getFields().get(0);
		String primaryKey 			= primaryKeyField.getName();
		RVariable primaryKeyRVar 	= r().variable(dataFrame, primaryKey);

		
		// 1. convert primary key field to string otherwise integers are
		// stored as real. (R doesn't manage int type. all numbers are real)
		SetValue setPkeyAsChar 		= r().setValue( primaryKeyRVar, r().asCharacter(primaryKeyRVar) );
		r.addScript( setPkeyAsChar );

		// 2. keep results (only pkey and output variables)
		RVariable results = r().variable( entity.getName() + "_results" );
		RVector cols = r().c(group.getResultVariables( entity.getId() ).toArray(new String[] {})).addValue(primaryKey);

		r.addScript( r().setValue(results, dataFrame.filterColumns(cols)) );

		// remove Inf numbers from results
		RScript removeInf = r().rScript( "is.na(" + results + "[ , unlist(lapply(" + results + ", is.numeric))] ) <-  " + results + "[ , unlist(lapply(" + results + ", is.numeric))] == Inf" );
		r.addScript(removeInf);

		// 3. drop results table
		ResultTable resultTable = schema.getResultTable( entity );
		r.addScript( r().dbSendQuery( CONNECTION_VAR, new Psql().dropTableIfExists(resultTable).cascade()) );
		// 4. create results table into db
		r.addScript(r().dbWriteTable( CONNECTION_VAR, resultTable.getName(), results) );
		
		// 5. for each output field, update table with results joining with
		// results table
		// convert id datatype from varchar to bigint first
		AlterColumnStep alterPkey = 
				new Psql()
					.alterTable(resultTable)
					.alterColumn(resultTable.getIdField())
					.type(SQLDataType.BIGINT)
					.using(resultTable.getIdField().getName() + "::bigint");

		r.addScript( r().dbSendQuery(CONNECTION_VAR , alterPkey) );

					
		// 6. Recreate view
		DropViewStep dropViewIfExists = new Psql().dropViewIfExists(view);
		r.addScript(r().dbSendQuery( CONNECTION_VAR, dropViewIfExists ));
		
		Select<?> selectView = entityManager.getViewSelect(entity , true );
		AsStep createView = new Psql().createView(view).as(selectView);
		r.addScript(r().dbSendQuery( CONNECTION_VAR, createView ));
		
		return r;
	}
	
//	private static void addSamplingUnitWeightTask() {
//		
//	}

}
