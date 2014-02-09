package org.openforis.calc.schema;

import static org.openforis.calc.persistence.jooq.Tables.STRATUM;

import org.jooq.Record;
import org.jooq.Select;
import org.jooq.SelectQuery;
import org.jooq.TableField;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.persistence.jooq.Tables;
import org.openforis.calc.psql.Psql;

/**
 * 
 * @author G. Miceli
 */
public class StratumDimensionTable extends AbstractTable {

	private static final long serialVersionUID = 1L;
	private TableField<Record, Integer> stratumNo;
	private TableField<Record, String> caption;
	private TableField<Record, Integer> workspaceId;
	private Workspace workspace;

//	public final TableField<Record, Integer> ID = copyField(STRATUM.ID); 
//	public final TableField<Record, Integer> STRATUM_NO = copyField(STRATUM.STRATUM_NO);
//	public final TableField<Record, String> CAPTION = copyField(STRATUM.CAPTION);
//	public final TableField<Record, String> DESCRIPTION = copyField(STRATUM.DESCRIPTION);
			
	public StratumDimensionTable(Workspace workspace) {
		
		super( Tables.STRATUM.getName() , Tables.STRATUM.getSchema() );
		this.workspace = workspace;
	
		initFields();
	}
	
	protected void initFields() {
		stratumNo = copyField( STRATUM.STRATUM_NO );
		caption = copyField( STRATUM.CAPTION );
		workspaceId = copyField( STRATUM.WORKSPACE_ID );
//		createField( Tables.STRATUM.STRATUM_NO, type, table)
	}
	
	public Select<?> getSelect(){
		SelectQuery<Record> select = new Psql().selectQuery();
		
		select.addSelect( stratumNo );
		select.addSelect( caption );
		select.addFrom( this );
		select.addConditions( workspaceId.eq(this.workspace.getId()) );
		
		return select;
	}
	
	public TableField<Record, Integer> getStratumNo() {
		return stratumNo;
	}
	
	public TableField<Record, String> getCaption() {
		return caption;
	}
}
