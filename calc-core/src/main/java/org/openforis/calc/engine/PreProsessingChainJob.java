/**
 * 
 */
package org.openforis.calc.engine;

import javax.sql.DataSource;

import org.jooq.Record;
import org.jooq.Select;
import org.jooq.SelectQuery;
import org.jooq.impl.SQLDataType;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.SamplingDesign;
import org.openforis.calc.psql.AlterTableStep.AlterColumnStep;
import org.openforis.calc.psql.CreateViewStep.AsStep;
import org.openforis.calc.psql.DropViewStep;
import org.openforis.calc.psql.Psql;
import org.openforis.calc.r.RScript;
import org.openforis.calc.r.RVariable;
import org.openforis.calc.r.SetValue;
import org.openforis.calc.schema.EntityDataView;
import org.openforis.calc.schema.InputTable;
import org.openforis.calc.schema.ResultTable;
import org.springframework.beans.factory.BeanFactory;

/**
 * @author Mino Togna
 * 
 */
public class PreProsessingChainJob extends CalcJob {

	/**
	 * @param workspace
	 * @param dataSource
	 * @param beanFactory
	 */
	protected PreProsessingChainJob(Workspace workspace, DataSource dataSource, BeanFactory beanFactory) {
		super(workspace, dataSource, beanFactory);
	}

	@Override
	protected void initTasks() {
		Workspace workspace = getWorkspace();
		
		// add plot weight script
		SamplingDesign samplingDesign = workspace.getSamplingDesign();
		String samplingUnitWeightScript = samplingDesign.getSamplingUnitWeightScript();
		Entity samplingUnit = samplingDesign.getSamplingUnit();
		
		//add open r db connection
		openConnection();
		
		// =====  calculat plot weight task
		CalcRTask weightTask = createTask("Calculate " + samplingUnit.getName() + " weight");

		InputTable table = getSchemas().getInputSchema().getDataTable(samplingUnit);
		EntityDataView view = getSchemas().getInputSchema().getDataView(samplingUnit);
		ResultTable resultTable = getSchemas().getInputSchema().getResultTable(samplingUnit);
		
		
		RScript weightScript = new RScript().rScript(samplingUnitWeightScript, samplingUnit.getOriginalVariables());
		
		RVariable dataFrame = new RScript().variable(samplingUnit.getName());
		
		// select data
		SelectQuery<Record> select = new Psql().selectQuery();
		select.addFrom(table);
		select.addSelect(table.getIdField());
		for (String var : weightScript.getVariables() ) {
			select.addSelect(table.field(var));
		}
		weightTask.addScript(r().setValue(dataFrame, r().dbGetQuery(connection, select)));
		addTask(weightTask);
					
		// execute weight script			
		RVariable result = r().variable("result");
		SetValue setValue = r().setValue(result, r().rTry(weightScript));			
		weightTask.addScript(setValue);
		weightTask.addScript( r().checkError(result, connection) );
		
		// write results 
		weightTask.addScript( r().dbSendQuery(connection, new Psql().dropTableIfExists(resultTable).cascade()) );
		weightTask.addScript(r().dbWriteTable(connection, resultTable.getName(), dataFrame));
		// convert id datatype from varchar to bigint
		AlterColumnStep alterPkey = 
			new Psql()
				.alterTable(resultTable)
				.alterColumn(resultTable.getIdField())
				.type(SQLDataType.BIGINT)
				.using(resultTable.getIdField().getName() + "::bigint");

		weightTask.addScript(r().dbSendQuery(connection, alterPkey));
		
		
		// drop view
		DropViewStep dropViewIfExists = new Psql().dropViewIfExists(view);
		weightTask.addScript(r().dbSendQuery( connection, dropViewIfExists ));
		
		Select<?> selectView = view.getSelect(true);
		AsStep createView = new Psql().createView(view).as(selectView);
		weightTask.addScript(r().dbSendQuery( connection, createView ));
		
		// closeconnection
		closeConnection();
	}
}
