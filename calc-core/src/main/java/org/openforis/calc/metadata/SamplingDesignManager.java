/**
 * 
 */
package org.openforis.calc.metadata;

import java.util.ArrayList;
import java.util.List;

import org.jooq.Field;
import org.jooq.InsertQuery;
import org.jooq.Query;
import org.jooq.Record;
import org.jooq.impl.DynamicTable;
import org.openforis.calc.engine.DataRecord;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceBackup;
import org.openforis.calc.engine.WorkspaceBackup.Phase1Data;
import org.openforis.calc.persistence.jooq.CalcSchema;
import org.openforis.calc.persistence.jooq.Sequences;
import org.openforis.calc.persistence.jooq.Tables;
import org.openforis.calc.persistence.jooq.tables.daos.SamplingDesignDao;
import org.openforis.calc.persistence.jooq.tables.daos.StratumDao;
import org.openforis.calc.persistence.jooq.tables.daos.WorkspaceDao;
import org.openforis.calc.psql.Psql;
import org.openforis.calc.schema.DataTable;
import org.openforis.calc.schema.TableDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Mino Togna
 *
 */
@Repository
public class SamplingDesignManager {

	@Autowired
	private SamplingDesignDao samplingDesignDao;
	@Autowired
	private StratumDao stratumDao;
	@Autowired
	private WorkspaceDao workspaceDao;
	@Autowired
	private TableDao tableDao;
	
	@Autowired
	private Psql psql;
	
	// ===== load methods
	@Transactional
	public void loadSampligDesign( Workspace workspace ) {
		SamplingDesign samplingDesign = samplingDesignDao.fetchOne( Tables.SAMPLING_DESIGN.WORKSPACE_ID , workspace.getId() );
		if( samplingDesign != null ){
			workspace.setSamplingDesign( samplingDesign );
		}
	}
	
	@Transactional
	public void loadStrata( Workspace workspace ) {
		List<Stratum> strata = stratumDao.fetchByWorkspaceId( workspace.getId() );
		for (Stratum stratum : strata) {
			workspace.addStratum(stratum);
		}
	}
	
	// insert methods
	
	@Transactional
	public void insert( Workspace workspace , SamplingDesign samplingDesign ) {
		Long nextval = psql.nextval( Sequences.SAMPLING_DESIGN_ID_SEQ );
		samplingDesign.setId( nextval.intValue() );
		
		workspace.setSamplingDesign( samplingDesign );
		
		samplingDesignDao.insert( samplingDesign );
	}
	
	@Transactional
	public void addStrata( Workspace workspace , Integer stratumNo , String caption ) {
		Stratum stratum = new Stratum();
		
		Long nextval = psql.nextval( Sequences.STRATUM_ID_SEQ );
		stratum.setId( nextval.intValue() );
		
		stratum.setStratumNo( stratumNo );
		stratum.setCaption( caption );
		
		workspace.addStratum( stratum );
		
		stratumDao.insert( stratum );
	}
	
	// ========== delete methods
	@Transactional
	public void deleteSamplingDesign( Workspace workspace ){
		if( workspace.hasSamplingDesign() ){
			samplingDesignDao.delete( workspace.getSamplingDesign() );
			workspace.setSamplingDesign( null );
			
			// remove phase 1 data if there is
			DynamicTable<?> phase1Table = new DynamicTable<Record>( workspace.getPhase1PlotTableName(), CalcSchema.CALC.getName() );
			if( tableDao.exists( phase1Table ) ){
				psql
					.dropTableIfExists(phase1Table)
					.execute();
			}
			
			workspace.setPhase1PlotTable( null );
			workspaceDao.update( workspace );
		}
	}
	
	@Transactional
	public void deleteStrata( Workspace workspace ){
		stratumDao.delete( workspace.getStrata() );
		workspace.emptyStrata();
	}
	
	// import from backup methods
	@Transactional
	public void importBackup( Workspace workspace, WorkspaceBackup workspaceBackup ) {
		Workspace workspaceToImport = workspaceBackup.getWorkspace();
		
		if( workspaceToImport.hasSamplingDesign() ){
			SamplingDesign samplingDesign = workspaceToImport.getSamplingDesign();
			
			Entity entity = workspaceToImport.getEntityById( samplingDesign.getSamplingUnitId() );
			Entity samplingUnit = workspace.getEntityByOriginalId( entity.getOriginalId() );
			
			samplingDesign.setSamplingUnit(samplingUnit);
			
			this.insert( workspace, samplingDesign  );
			
			DataTable table = workspace.schemas().getDataSchema().getDataTable(samplingUnit);
			// drop weight column if exists
			psql.alterTable( table ).dropColumnIfExists( table.getWeightField(),true ).execute();
			// add weight column to sammpling unit table
			psql.alterTable( table ).addColumn( table.getWeightField() ).execute() ;

			
		}
	}
	
	@Transactional
	public <T extends Object> void importBackupPhase1Data(Workspace workspace, WorkspaceBackup workspaceBackup ) {
		Phase1Data phase1Data = workspaceBackup.getPhase1Data();
		Workspace workspaceToImport = workspaceBackup.getWorkspace();
		
		if( phase1Data != null ){
			DynamicTable<?> table = new DynamicTable<Record>( workspaceToImport.getPhase1PlotTableName(), CalcSchema.CALC.getName() );
			table.initFields( phase1Data.getTableInfo() );
			@SuppressWarnings( "unchecked" )
			Field<T>[] tableFields = (Field<T>[]) table.fields();
			
			// create table
			psql
				.createTable( table, tableFields )
				.execute();
			psql
				.alterTable( table )
				.addPrimaryKey( table.getPrimaryKey() )
				.execute();
			// populate table
			List<Query> queries = new ArrayList<Query>();
			for ( DataRecord dataRecord : phase1Data.getRecords() ) {
				InsertQuery<?> insert = psql.insertQuery( table );
				for ( Field<T> field : tableFields ) {
					@SuppressWarnings( "unchecked" )
					T value = (T) dataRecord.getValue( field.getName() );
					insert.addValue( field, value );
				}
				queries.add( insert );
				
				if( queries.size() % 5000 == 0 ){
					psql
						.batch(queries)
						.execute();
					queries.clear();
				}
			}
			psql
				.batch(queries)
				.execute();
			
			
			workspace.setPhase1PlotTable( table.getName() );
			workspaceDao.update( workspace );
		}
		
	}

	@Transactional
	public void importBackupStrata( Workspace workspace , WorkspaceBackup workspaceBackup ) {
		List<Stratum> strata = workspaceBackup.getWorkspace().getStrata();
		
		workspace.setStrata( strata );
		stratumDao.insert( workspace.getStrata() );
		
	}
	
}
