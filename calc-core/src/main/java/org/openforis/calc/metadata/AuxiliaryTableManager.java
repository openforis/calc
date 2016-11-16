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
import org.openforis.calc.engine.WorkspaceBackup.ExternalData;
import org.openforis.calc.persistence.jooq.Sequences;
import org.openforis.calc.persistence.jooq.tables.daos.AuxiliaryTableDao;
import org.openforis.calc.psql.Psql;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author M. Togna
 *
 */
@Repository
public class AuxiliaryTableManager {

	@Autowired
	private AuxiliaryTableDao dao;
	@Autowired
	private Psql psql;
	
	@Transactional
	public void loadAll(Workspace workspace) {
		List<AuxiliaryTable> tables = dao.fetchByWorkspaceId(workspace.getId().longValue());
		workspace.setAuxiliaryTables(tables);
	}

	@Transactional
	public AuxiliaryTable getById(Long tableId) {
		return dao.fetchOneById(tableId);
	}

	@Transactional
	public void persist(AuxiliaryTable table) {
		Workspace workspace = table.getWorkspace();
		
		if(table.getId() == null){
			Long id = psql.nextval( Sequences.AUXILIARY_TABLE_ID_SEQ ) ;
			table.setId( id );
			
			dao.insert(table);
		} else {
			dao.update(table);
		}
		
		loadAll(workspace);
		
	}
	
	@Transactional
	public void delete(AuxiliaryTable table) {
		Workspace workspace = table.getWorkspace();
		
		DynamicTable<?> extTable = new DynamicTable<>( table.getName(), table.getSchema() );
		psql.dropTableIfExists(extTable).execute();
		
		dao.delete(table);
		loadAll(workspace);
	}

	@Transactional
	public void deleteAll( Workspace workspace ) {
		for (AuxiliaryTable table : workspace.getAuxiliaryTables()) {
			DynamicTable<?> extTable = new DynamicTable<>( table.getName(), table.getSchema() );
			psql.dropTableIfExists(extTable).execute();
			
			dao.delete(table);
		}
		
		loadAll(workspace);
	}

	@Transactional
	public void importBackup(Workspace workspace, WorkspaceBackup workspaceBackup) {
		List<AuxiliaryTable> tables = workspaceBackup.getWorkspace().getAuxiliaryTables();
		for (AuxiliaryTable table : tables) {
			
			AuxiliaryTable newTable = new AuxiliaryTable();
			String tableName = table.getName();
			newTable.setName( tableName );
			newTable.setSchema( workspace.getExtendedSchemaName() );
			newTable.setWorkspace( workspace );
			
			ExternalData tableData = workspaceBackup.getAuxiliaryTableData( tableName );
			//import table data
			importTable(newTable, tableData);
			//save auxiliary table metadata
			Long id = psql.nextval( Sequences.AUXILIARY_TABLE_ID_SEQ ) ;
			newTable.setId( id );
			dao.insert(newTable);
		}
		
		loadAll(workspace);
	}

	
	private <T> void importTable( AuxiliaryTable auxiliaryTable, ExternalData externalData ) {
		if( externalData != null ){
			
			DynamicTable<?> table = new DynamicTable<Record>( auxiliaryTable.getName(), auxiliaryTable.getSchema() );
			table.initFields( externalData.getTableInfo() );
			@SuppressWarnings( "unchecked" )
			Field<T>[] tableFields = (Field<T>[]) table.fields();
			
			// drop table first
			psql
				.dropTableIfExistsLegacy( table )
				.execute();
			
			// create table
			psql
				.createTable( table, tableFields )
				.execute();
			psql
				.alterTableLegacy( table )
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
			
		}
	}
}
