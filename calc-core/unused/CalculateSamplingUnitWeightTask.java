/**
 * 
 */
package org.openforis.calc.chain.pre;

import java.math.BigDecimal;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Select;
import org.jooq.SelectQuery;
import org.jooq.Table;
import org.jooq.UpdateSetMoreStep;
import org.jooq.impl.DynamicTable;
import org.jooq.impl.SQLDataType;
import org.openforis.calc.engine.CalcRTask;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.SamplingDesign;
import org.openforis.calc.psql.AlterTableStep.AlterColumnStep;
import org.openforis.calc.psql.CreateViewStep.AsStep;
import org.openforis.calc.psql.DropViewStep;
import org.openforis.calc.psql.Psql;
import org.openforis.calc.r.REnvironment;
import org.openforis.calc.r.RLogger;
import org.openforis.calc.r.RScript;
import org.openforis.calc.r.RVariable;
import org.openforis.calc.r.SetValue;
import org.openforis.calc.schema.DataTable;
import org.openforis.calc.schema.EntityDataView;

/**
 * @author Mino Togna
 * 
 */
@Deprecated
public class CalculateSamplingUnitWeightTask extends CalcRTask {

	private RLogger rLogger;

	public CalculateSamplingUnitWeightTask( REnvironment rEnvironment ) {
		super(rEnvironment, "Calculate sampling unit weight");
	}

	@Override
	synchronized 
	public void init() {
		super.init();
		
		this.rLogger = new RLogger();
		
		initScript();
	}

	@Override
	protected RLogger getJobLogger() {
		return rLogger;
	}
	
	protected void initScript() {
		addOpenConnectionScript();
		
		addSamplingUnitWeightTask();
		
		addCloseConnectionScript();
	}
	
	private void addSamplingUnitWeightTask() {
		Workspace workspace = getWorkspace();
		
		SamplingDesign samplingDesign = workspace.getSamplingDesign();
		String samplingUnitWeightScript = samplingDesign.getSamplingUnitWeightScript();
		Entity samplingUnit = samplingDesign.getSamplingUnit();
		
		DataTable table = getInputSchema().getDataTable(samplingUnit);
		EntityDataView view = getInputSchema().getDataView(samplingUnit);

		// add plot weight script
		
		RScript weightScript = new RScript().rScript( samplingUnitWeightScript, samplingUnit.getHierarchyVariables() );
		
		RVariable dataFrame = new RScript().variable( samplingUnit.getName() );
		
		// select data
		SelectQuery<Record> select = new Psql().selectQuery();
		select.addFrom( view );
		select.addSelect( view.getIdField());
		select.addSelect( view.getAncestorIdFields() );
		for (String var : weightScript.getVariables() ) {
			select.addSelect( view.field(var) );
		}
		addScript(r().setValue( dataFrame, r().dbGetQuery(getrConnection(), select)) );
					
		// execute weight script			
		RVariable result = r().variable("result");
		SetValue setValue = r().setValue(result, r().rTry(weightScript));			
		addScript(setValue);
		addScript( r().checkError(result, getrConnection()) );
		
		// write results
		
		// drop weight column if exists
		addScript( r().dbSendQuery(getrConnection(), new Psql().alterTable( table ).dropColumnIfExists( table.getWeightField(),true ) ));
		// add weight column to sammpling unit table
		addScript( r().dbSendQuery(getrConnection(), new Psql().alterTable( table ).addColumn( table.getWeightField() ) ));
		
		// temporary results table
		DynamicTable<Record> resultTable = new DynamicTable<Record>( "_tmp_weight_result" , getInputSchema().getName() );
		Field<Long> resultTableIdField = resultTable.getLongField( table.getIdField().getName() );
		Field<BigDecimal> resultTableWeightField = resultTable.getBigDecimalField( table.getWeightField().getName() );
		
		// write results to temporary table
		addScript( r().dbRemoveTable(getrConnection(), resultTable.getName()) );
		addScript( r().dbWriteTable(getrConnection(), resultTable.getName(), dataFrame) );
		
		// convert id datatype from varchar to bigint
		AlterColumnStep alterPkey = 
			new Psql()
				.alterTable( resultTable )
				.alterColumn( resultTableIdField )
				.type( SQLDataType.BIGINT )
				.using( resultTableIdField.getName() + "::bigint" );

		addScript(r().dbSendQuery( getrConnection(), alterPkey) );
		
		// update plot weight column joining with temp result table
		Table<Record> cursor = new Psql()
			.select()
			.from( resultTable )
			.asTable( "r" );
		
		UpdateSetMoreStep<Record> update = new Psql()
			.update( table )
			.set( table.getWeightField() , cursor.field(resultTableWeightField) );
		
		Condition joinCondition = table.getIdField().eq( cursor.field(resultTableIdField) );
		
		addScript(r().dbSendQuery( getrConnection(), new Psql().updateWith(cursor, update, joinCondition)) );
		
		// drop view and recreate
		DropViewStep dropViewIfExists = new Psql().dropViewIfExists(view);
		addScript(r().dbSendQuery( getrConnection(), dropViewIfExists ));
		
		Select<?> selectView = view.getSelect(true);
		AsStep createView = new Psql().createView(view).as(selectView);
		addScript(r().dbSendQuery( getrConnection(), createView ));
		
		// remove temporary result table
		addScript( r().dbRemoveTable(getrConnection(), resultTable.getName()) );
	}
}
