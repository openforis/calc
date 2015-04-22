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
import org.jooq.impl.PrimarySamplingUnitTable;
import org.json.simple.JSONArray;
import org.openforis.calc.engine.DataRecord;
import org.openforis.calc.engine.ParameterMap;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceBackup;
import org.openforis.calc.engine.WorkspaceBackup.ExternalData;
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
			loadExternalData(samplingDesign);
		}
	}
	
	@Transactional
	private void loadExternalData(SamplingDesign samplingDesign) {
		if( samplingDesign.getTwoStages() ){
			Workspace workspace = samplingDesign.getWorkspace();
			
			PrimarySamplingUnitTable<?> psuTable = new PrimarySamplingUnitTable<Record>( workspace.getPrimarySUTableName(), workspace.getExtendedSchemaName() );
			JSONArray info = tableDao.info( psuTable );
			psuTable.initSamplingDesignFields( samplingDesign , info );
			
			samplingDesign.setPrimarySamplingUnitTable( psuTable );
			
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
		
		loadExternalData(samplingDesign);
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
		}
	}
	
	@Transactional
	public void deletePhase1Table( Workspace workspace ) {
		if( workspace.has2PhasesSamplingDesign() ){
			
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
	public void deletePrimarySUTable( Workspace workspace ) {
		if( workspace.has2StagesSamplingDesign() ) {
			
			// remove primary su data if there is
			DynamicTable<?> table = new DynamicTable<Record>( workspace.getPrimarySUTableName(), workspace.getExtendedSchemaName() );
			if( tableDao.exists( table ) ){
				psql
					.dropTableIfExists(table)
					.execute();
			}
			
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
			
			String origInputSchema 	= workspaceToImport.getInputSchema();
			String inputSchema		= workspace.getInputSchema();
			String origExtSchema	= workspaceToImport.getExtendedSchemaName();
			String extSchema 		= workspace.getExtendedSchemaName();
			
			// replace schema
			ParameterMap aoiJoinSettings = samplingDesign.getAoiJoinSettings();
			if(aoiJoinSettings != null ) {
				String schema = aoiJoinSettings.getString( "schema" );
				if( origExtSchema.equals( schema ) ){
					aoiJoinSettings.setString( "schema" , extSchema );
				}
			}
			
			ParameterMap stratumJoinSettings = samplingDesign.getStratumJoinSettings();
			if( samplingDesign.getStratified() ){
				String schema = stratumJoinSettings.getString( "schema" );
				if( origExtSchema.equals( schema ) ){
					stratumJoinSettings.setString( "schema" , extSchema );
				}
			}
			
			if( workspaceToImport.has2PhasesSamplingDesign() ){
				// replace phase 1 table name
				String originalPhase1PlotTableName = workspaceToImport.getPhase1PlotTableName();
				String phase1PlotTableName = workspace.getPhase1PlotTableName();
				
				if( aoiJoinSettings.getString("table").equals(originalPhase1PlotTableName)){
					aoiJoinSettings.setString( "table" , phase1PlotTableName );
				}
				if( samplingDesign.getCluster() && samplingDesign.getClusterColumnSettings().getString( "table" ).equals(originalPhase1PlotTableName)){
					samplingDesign.getClusterColumnSettings().setString( "table" , phase1PlotTableName );
				}
				if( samplingDesign.getStratified() && stratumJoinSettings.getString( "table" ).equals(originalPhase1PlotTableName)){
					stratumJoinSettings.setString( "table" , phase1PlotTableName );
				}
				ParameterMap map = samplingDesign.getPhase1JoinSettings().getMap( "leftTable" );
				if( map.getString( "table" ).equals(originalPhase1PlotTableName)){
					map.setString( "table" , phase1PlotTableName );
				}
			}
			
			if( workspaceToImport.has2StagesSamplingDesign() ){
				ParameterMap twoStagesSettings = samplingDesign.getTwoStagesSettings();
				ParameterMap left = twoStagesSettings.getMap("joinSettings").getMap( "leftTable" );
				if( left.getString( "schema" ).equals( origExtSchema) ){
					left.setString( "schema" , extSchema );
				}
				
				ParameterMap right = twoStagesSettings.getMap("joinSettings").getMap( "rightTable" );
				if( right.getString( "schema" ).equals( origInputSchema) ){
					right.setString( "schema" , inputSchema );
				}
			}
			
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
		ExternalData externalData = workspaceBackup.getPhase1Data();
		
		String tableName = workspace.getPhase1PlotTableName();
		String schema = CalcSchema.CALC.getName();
		importExternalData( workspace, externalData, tableName, schema );
	}

	@Transactional
	public <T extends Object> void importBackupPrimarySUData(Workspace workspace, WorkspaceBackup workspaceBackup ) {
		ExternalData externalData = workspaceBackup.getPrimarySuData();
		
		String tableName = workspace.getPrimarySUTableName();
		String schema = workspace.getExtendedSchemaName();
		importExternalData( workspace, externalData, tableName, schema );
	}

	private <T> void importExternalData( Workspace workspace, ExternalData externalData, String tableName, String schema ) {
		if( externalData != null ){
			
			DynamicTable<?> table = new DynamicTable<Record>( tableName, schema );
			table.initFields( externalData.getTableInfo() );
			@SuppressWarnings( "unchecked" )
			Field<T>[] tableFields = (Field<T>[]) table.fields();
			
			// drop table first
			psql
				.dropTableIfExists( table )
				.execute();
			
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
			for ( DataRecord dataRecord : externalData.getRecords() ) {
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
		for ( Stratum stratum : strata ) {
			addStrata( workspace, stratum.getStratumNo(), stratum.getCaption() );
		}
	}
	
}
