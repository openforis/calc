/**
 * 
 */
package org.openforis.calc.metadata;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jooq.Field;
import org.jooq.Table;
import org.jooq.Update;
import org.jooq.impl.DSL;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceBackup;
import org.openforis.calc.persistence.jooq.Sequences;
import org.openforis.calc.persistence.jooq.Tables;
import org.openforis.calc.persistence.jooq.tables.AoiTable;
import org.openforis.calc.persistence.jooq.tables.daos.AoiHierarchyDao;
import org.openforis.calc.persistence.jooq.tables.daos.AoiLevelDao;
import org.openforis.calc.psql.Psql;
import org.openforis.calc.psql.UpdateWithStep;
import org.openforis.commons.io.csv.CsvReader;
import org.openforis.commons.io.flat.FlatRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Mino Togna
 * 
 */
@Component
public class AoiManager {

	@Autowired
	private AoiHierarchyDao aoiHierarchyDao;

	@Autowired
	private AoiLevelDao aoiLevelDao;

	@Autowired
	private AoiDao aoiDao;

	@Autowired
	private Psql psql;
	
	@SuppressWarnings( "unchecked" )
	@Transactional
	public Workspace csvImport(Workspace workspace, String filepath, String[] levelNames) throws IOException {
		@SuppressWarnings( "resource" )
		CsvReader csvReader = new CsvReader(filepath);
		csvReader.readHeaders();

		// transaction begin
//		DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
//		definition.setName("txName");
//		definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
//		TransactionStatus transaction = transactionManager.getTransaction(definition);
		try {
			// create (for now only admin unit) aoi hierarchy
			AoiHierarchy aoiHierarchy = null;
			List<AoiHierarchy> aoiHierarchies = workspace.getAoiHierarchies();
			if (aoiHierarchies.size() == 0) {
				aoiHierarchy = new AoiHierarchy();
				
				Long aoiHierId = psql.nextval( Sequences.AOI_HIERARCHY_ID_SEQ );
				aoiHierarchy.setId( aoiHierId.intValue() );
				
				aoiHierarchy.setWorkspace(workspace);
				aoiHierarchy.setWorkspaceId(workspace.getId());
				aoiHierarchy.setCaption("Administrative unit");
				aoiHierarchy.setName("Administrative unit");
				workspace.addAoiHierarchy(aoiHierarchy);
				aoiHierarchyDao.insert( aoiHierarchy );
			} else {
				aoiHierarchy = aoiHierarchies.get(0);
			}

			// create levels
			List<AoiLevel> levels = aoiHierarchy.getLevels();
			if (!levels.isEmpty()) {
				// remove old levels
				aoiDao.deleteByHierarchy( aoiHierarchy );
				aoiLevelDao.delete( levels );
				// remove old levels and aois
				aoiHierarchy.clearLevels();
			}

			levels = new ArrayList<AoiLevel>();
			int rank = 0;
			for (String name : levelNames) {
				AoiLevel level = new AoiLevel();
				
				Long nextval = psql.nextval( Sequences.AOI_LEVEL_ID_SEQ );
				level.setId( nextval.intValue() );
				
				level.setName(name);
				level.setCaption(name);
				level.setHierarchy(aoiHierarchy);
				level.setRank(rank);

				aoiLevelDao.insert(level);

				levels.add(level);
				rank++;
			}
			aoiHierarchy.setLevels(levels);

			// import AOIs
			Map<Integer, Map<String, Aoi>> aois = new LinkedHashMap<Integer, Map<String, Aoi>>();
			FlatRecord record = csvReader.nextRecord();
			do {
				Aoi parentAoi = null;
				for (AoiLevel level : levels) {
					int r = level.getRank();
					String code = record.getValue((r) * 2, String.class);
					String caption = record.getValue((r) * 2 + 1, String.class);
					Double area = null;

					if (r == levels.size() - 1) {
						// last element set area and save it
						area = record.getValue((r + 1) * 2, Double.class);
						getOrCreateAoi(aois, code, caption, level, area, parentAoi);
					} else {
						parentAoi = getOrCreateAoi(aois, code, caption, level, area, parentAoi);
					}
				}
				record = csvReader.nextRecord();
			} while (record != null);

			// commit before updating areas
//			transactionManager.commit(transaction);
			
			// update areas for non leaf aois
			Iterator<AoiLevel> iterator = new LinkedList<AoiLevel>(levels).descendingIterator();
			while (iterator.hasNext()) {

				AoiLevel level = iterator.next();
				// skip root aoi level
				if (iterator.hasNext()) {

					String landArea = "landArea";
					String id = "id";
					Table<?> cursor = 
							new Psql()
								.select(Tables.AOI.PARENT_AOI_ID.as(id), DSL.sum(Tables.AOI.LAND_AREA).as(landArea))
								.from(Tables.AOI)
								.where(Tables.AOI.AOI_LEVEL_ID.eq(level.getId()))
								.groupBy(Tables.AOI.PARENT_AOI_ID)
								.asTable("tmp");

					Update<?> update = 
						psql
							.update(Tables.AOI)
							.set(Tables.AOI.LAND_AREA, (Field<BigDecimal>) cursor.field(landArea));

					UpdateWithStep updateWith = psql.updateWith(cursor, update, Tables.AOI.ID.eq((Field<Integer>) cursor.field(id)));
					updateWith.execute();
				} else {
					break;
				}
//				transactionManager.commit(transaction);	
//				aoiDao.assignRootAoi(aoiHierarchy);
				
				
				
				loadByWorkspace( workspace );
				// set root aoi to hierarchy
//				AoiLevel rootLevel = aoiHierarchy.getLevels().get(0);
//				Aoi rootAoi = rootLevel.getAois().iterator().next();
//				aoiHierarchy.setRootAoi(rootAoi);
				
			}
		} catch(Exception e){
//			transactionManager.rollback(transaction);
			throw new RuntimeException("Error while importing areas of interest", e);
		}
		
		
		return workspace;
	}

	private Aoi getOrCreateAoi(Map<Integer, Map<String, Aoi>> aois, String code, String caption, AoiLevel level, Double area, Aoi parentAoi) {
		Map<String, Aoi> map = aois.get(level.getRank());
		if (map != null && map.get(code) != null) {
			return map.get(code);
		} else {
			if (map == null) {
				map = new LinkedHashMap<String, Aoi>();
				aois.put(level.getRank(), map);
			}
			Aoi aoi = new Aoi();
			
			Long nextval = psql.nextval( Sequences.AOI_ID_SEQ );
			aoi.setId( nextval.intValue() );
			
			aoi.setCode(code);
			aoi.setCaption(caption);
			aoi.setAoiLevel(level);
			if( area != null ){
				aoi.setLandArea( new BigDecimal(area) );
			}
			aoi.setParentAoi(parentAoi);

			aoiDao.insert(aoi);
			level.addAoi(aoi);

			map.put(code, aoi);
			return aoi;
		}
	}
	
	@Transactional
	public void loadByWorkspace( Workspace workspace ) {
		// clear workspace aois first
		workspace.setAoiHierarchies( null );
		// then loads them
		List<AoiHierarchy> list = aoiHierarchyDao.fetchByWorkspaceId( workspace.getId() );
		for (AoiHierarchy aoiHierarchy : list) {
			workspace.addAoiHierarchy( aoiHierarchy );
			
			List<AoiLevel> aoiLevels = aoiLevelDao.fetchByAoiHierarchyId( aoiHierarchy.getId() );
			
			aoiHierarchy.setLevels( aoiLevels );
			
			AoiLevel rootLevel = aoiLevels.get(0);
			List<Aoi> aois = aoiDao.fetchByAoiLevelId( rootLevel.getId() );
			if( !aois.isEmpty() ) {
				Aoi root = aois.get(0);
				
				root.setAoiLevel( rootLevel );
				
				Collection<Aoi> children = loadAois( root );
				root.setChildren( children  );
				aoiHierarchy.setRootAoi(root);
			}
			
		}
	}

	private Collection<Aoi> loadAois(Aoi aoiParent) {
		List<Aoi> aois = aoiDao.fetchByParentAoiId( aoiParent.getId() );
		for (Aoi aoi : aois) {
			aoi.setAoiLevel( aoiParent.getAoiLevel().getHierarchy().getLevelById(aoi.getAoiLevelId()) );
			aoi.setParentAoi( aoiParent );

			Collection<Aoi> children = loadAois( aoi );
			aoi.setChildren( children  );
		}
		return aois;
	}
	
	/**
	 * Delete all Aoi Hierarchies/levels and aois for the given workspace
	 * @param workspace
	 */
	@Transactional
	public void delete( Workspace workspace ) {
		List<AoiHierarchy> aoiHierarchies = workspace.getAoiHierarchies();
		for ( AoiHierarchy aoiHierarchy : aoiHierarchies ) {
			
			Collection<AoiLevel> levels = aoiHierarchy.getLevelsReverseOrder();
			for ( AoiLevel aoiLevel : levels ) {
				AoiTable T = Tables.AOI;
				psql
					.delete( T )
					.where( T.AOI_LEVEL_ID.eq(aoiLevel.getId()) )
					.execute();
			}
			aoiLevelDao.delete( levels );
			
		}
		aoiHierarchyDao.delete( aoiHierarchies );
		
		workspace.setAoiHierarchies( new ArrayList<AoiHierarchy>() );
	}

	@Transactional
	public void importBackup( Workspace workspace, WorkspaceBackup workspaceBackup ) {
		List<AoiHierarchy> aoiHierarchies = workspaceBackup.getWorkspace().getAoiHierarchies();
		for ( AoiHierarchy aoiHierarchy : aoiHierarchies ) {
			this.createFromBackup( workspace , workspaceBackup , aoiHierarchy );
		}
	}
	
	@Transactional
	private void createFromBackup( Workspace workspace , WorkspaceBackup workspaceBackup, AoiHierarchy aoiHierarchy ) {
		
		aoiHierarchy.setId( psql.nextval(Sequences.AOI_HIERARCHY_ID_SEQ).intValue() );
		workspace.addAoiHierarchy(aoiHierarchy);
		aoiHierarchyDao.insert(aoiHierarchy);
		
		List<AoiLevel> levels = aoiHierarchy.getLevels();
		for ( AoiLevel aoiLevel : levels ) {
			aoiLevel.setId(  psql.nextval(Sequences.AOI_LEVEL_ID_SEQ).intValue()  );
			aoiLevel.setHierarchy(aoiHierarchy);
			
			aoiLevelDao.insert(aoiLevel);
		}
		
		Map<Integer, Integer> aoiIds = new HashMap<Integer, Integer>();
		
		Aoi aoi = aoiHierarchy.getRootAoi();
		createAoiFromBackup( aoi, levels, aoiIds, 0 );
		
		
		// replace aoiIds in error settings
		ErrorSettings errorSettings = workspaceBackup.getWorkspace().getErrorSettings();
		if( errorSettings != null ){
			for ( String key : errorSettings.getParameters().keys() ) {
				long variableId = Long.parseLong(key);
				Collection<? extends Number> aois = errorSettings.getAois( variableId );
				List<Long> newAoiIds = new ArrayList<Long>();
				for ( Number aoiId : aois ) {
					long newAoiId = aoiIds.get( aoiId.intValue() ).longValue();
					newAoiIds.add( newAoiId );
				}
				errorSettings.setAois( variableId, newAoiIds );
			}
		}
	}
	
	@Transactional
	private void createAoiFromBackup( Aoi aoi , List<AoiLevel> levels , Map<Integer, Integer> aoiIds , int indexLevel ) {
		// set aoi level
		AoiLevel aoiLevel = levels.get( indexLevel );
		aoi.setAoiLevel( aoiLevel );
		
		// set new aoi id and keep reference of old id
		Integer aoiId = aoi.getId();
		int newAoiId = psql.nextval( Sequences.AOI_ID_SEQ ).intValue();
		aoiIds.put( aoiId, newAoiId );
		aoi.setId( newAoiId );
		
		// set aoi parent id (taken from aoiIds map)
		Integer parentAoiId = aoi.getParentAoiId();
		if( parentAoiId != null ){
			Integer newParentAoiId = aoiIds.get(parentAoiId);
			aoi.setParentAoiId(newParentAoiId);
		}
		
		aoiDao.insert( aoi );
		
		// create child aois
		for ( Aoi childAoi : aoi.getChildren() ) {
			createAoiFromBackup( childAoi, levels, aoiIds, indexLevel+1 );
		}
		
	}
	
}
