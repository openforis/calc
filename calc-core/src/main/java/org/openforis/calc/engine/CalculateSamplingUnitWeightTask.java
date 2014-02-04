/**
 * 
 */
package org.openforis.calc.engine;

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
import org.openforis.calc.r.REnvironment;
import org.openforis.calc.r.RLogger;
import org.openforis.calc.r.RScript;
import org.openforis.calc.r.RVariable;
import org.openforis.calc.r.SetValue;
import org.openforis.calc.schema.EntityDataView;
import org.openforis.calc.schema.InputTable;
import org.openforis.calc.schema.ResultTable;

/**
 * @author Mino Togna
 * 
 */
public class CalculateSamplingUnitWeightTask extends CalcRTask {

	private RLogger rLogger;

	protected CalculateSamplingUnitWeightTask(REnvironment rEnvironment) {
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
		
		InputTable table = getInputSchema().getDataTable(samplingUnit);
		EntityDataView view = getInputSchema().getDataView(samplingUnit);
		ResultTable resultTable = getInputSchema().getResultTable(samplingUnit);
		
		// add plot weight script
		
		RScript weightScript = new RScript().rScript(samplingUnitWeightScript, samplingUnit.getOriginalVariables());
		
		RVariable dataFrame = new RScript().variable(samplingUnit.getName());
		
		// select data
		SelectQuery<Record> select = new Psql().selectQuery();
		select.addFrom(table);
		select.addSelect(table.getIdField());
		for (String var : weightScript.getVariables() ) {
			select.addSelect(table.field(var));
		}
		addScript(r().setValue(dataFrame, r().dbGetQuery(getrConnection(), select)));
					
		// execute weight script			
		RVariable result = r().variable("result");
		SetValue setValue = r().setValue(result, r().rTry(weightScript));			
		addScript(setValue);
		addScript( r().checkError(result, getrConnection()) );
		
		// write results 
		addScript( r().dbSendQuery(getrConnection(), new Psql().dropTableIfExists(resultTable).cascade()) );
		addScript(r().dbWriteTable(getrConnection(), resultTable.getName(), dataFrame));
		// convert id datatype from varchar to bigint
		AlterColumnStep alterPkey = 
			new Psql()
				.alterTable(resultTable)
				.alterColumn(resultTable.getIdField())
				.type(SQLDataType.BIGINT)
				.using(resultTable.getIdField().getName() + "::bigint");

		addScript(r().dbSendQuery(getrConnection(), alterPkey));
		
		// drop view
		DropViewStep dropViewIfExists = new Psql().dropViewIfExists(view);
		addScript(r().dbSendQuery( getrConnection(), dropViewIfExists ));
		
		Select<?> selectView = view.getSelect(true);
		AsStep createView = new Psql().createView(view).as(selectView);
		addScript(r().dbSendQuery( getrConnection(), createView ));
	}
}
