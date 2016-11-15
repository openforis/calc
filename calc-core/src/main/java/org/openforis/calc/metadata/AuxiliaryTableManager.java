/**
 * 
 */
package org.openforis.calc.metadata;

import java.util.List;

import org.jooq.impl.DynamicTable;
import org.openforis.calc.engine.Workspace;
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


}
