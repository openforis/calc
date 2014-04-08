package org.openforis.calc.chain.pre;

import static org.openforis.calc.persistence.jooq.Tables.SAMPLING_UNIT;

import java.util.Collection;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.Update;
import org.jooq.impl.SQLDataType;
import org.openforis.calc.engine.Task;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.Variable;
import org.openforis.calc.persistence.jooq.tables.SamplingUnitTable;
import org.openforis.calc.psql.Psql;
import org.openforis.calc.schema.OldFactTable;
import org.openforis.calc.schema.OutputSchema;

/**
 * Assign stratum to data tables 
 * 
 * @author G. Miceli
 *
 */
public class AssignStratumTask extends Task {

	@Override
	protected void execute() throws Throwable {
		OutputSchema outputSchema = getOutputSchema();
		Collection<OldFactTable> factTables = outputSchema.getFactTables();
		for (OldFactTable factTable : factTables) {
			OldFactTable f = factTable;
			while ( f != null ){
				Entity entity = f.getEntity();
				Variable<?> clusterVar = entity.getClusterVariable();
				Variable<?> plotVar = entity.getUnitNoVariable();
				if ( clusterVar != null || plotVar != null ) {
					String clusterColumn = clusterVar.getOutputValueColumn();
					String plotColumn = plotVar.getOutputValueColumn();
					if ( clusterColumn == null || plotColumn == null ) {
						log().warn("Ignorning cluster/plot variable with null output column");
						// TODO warn user
					} else {
						assignStratumIds(factTable, clusterColumn, plotColumn);
						break;
					}
				}
				f = factTable.getParentTable(); 
			}
		}
	}

	private void assignStratumIds(OldFactTable factTable, String clusterColumn, String plotColumn) {
		Entity entity = factTable.getEntity();
		Integer entityId = entity.getId();
		Field<Integer> stratumId = factTable.getStratumIdField();
		SamplingUnitTable s = SAMPLING_UNIT.as("s");
		Table<?> su = new Psql()
			.select(s.STRATUM_ID, s.CLUSTER, s.UNIT_NO)
			.from(s)
			.where(s.ENTITY_ID.eq(entityId))
			.asTable("su");
			
		Update<?> update = new Psql()
			.update(factTable)
			.set(stratumId, su.field(s.STRATUM_ID));
			
		Condition cond = null;
		if ( clusterColumn != null ) {
			Field<String> fld = getAsVarchar(factTable, clusterColumn);
			if ( fld != null ) {
				cond = su.field(s.CLUSTER).equalIgnoreCase(fld);
			}
		}
		if ( plotColumn != null ) {
			Field<String> fld = getAsVarchar(factTable, plotColumn);
			if ( fld != null ) {
				Condition c2 = su.field(s.UNIT_NO).equalIgnoreCase(fld);
				cond = cond == null ? c2 : cond.and(c2);
			}
		}
		
		psql().updateWith(su, update, cond).execute();
	}

	private Field<String> getAsVarchar(OldFactTable factTable, String colName) {
		Field<?> fld = factTable.field(colName);
		if ( fld == null ) {
			throw new IllegalStateException(colName+" not defined in "+factTable);
		}
		return fld.cast(SQLDataType.VARCHAR);
	}

}
