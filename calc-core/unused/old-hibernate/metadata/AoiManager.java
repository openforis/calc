/**
 * 
 */
package org.openforis.calc.metadata;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.jooq.Field;
import org.jooq.Table;
import org.jooq.Update;
import org.jooq.impl.DSL;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.persistence.jooq.Tables;
import org.openforis.calc.persistence.jooq.tables.daos.AoiHierarchyDao;
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
	private DataSource dataSource;

//	@Autowired
//	private PlatformTransactionManager transactionManager;
	
	@SuppressWarnings({ "unchecked", "null" })
//	@Transactional
	public Workspace csvImport(Workspace workspace, String filepath, String[] levelNames) throws IOException {
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
			Set<AoiLevel> levels = aoiHierarchy.getLevels();
			if (!levels.isEmpty()) {
				// remove old levels
				aoiDao.deleteByLevels( levels );
				
				aoiLevelDao.delete( levels );
				
				// remove old levels and aois
				aoiHierarchy.setLevels(null);
			}

			levels = new LinkedHashSet<AoiLevel>();
			int rank = 0;
			for (String name : levelNames) {
				AoiLevel level = new AoiLevel();
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
						new Psql()
							.update(Tables.AOI)
							.set(Tables.AOI.LAND_AREA, (Field<BigDecimal>) cursor.field(landArea));

					UpdateWithStep updateWith = new Psql(dataSource)
						.updateWith(cursor, update, Tables.AOI.ID.eq((Field<Integer>) cursor.field(id)));
					updateWith.execute();
				} else {
					break;
				}
//				transactionManager.commit(transaction);	
//				aoiDao.assignRootAoi(aoiHierarchy);
				
				loadByWorkspace(workspace);
				
			}
		} catch(Exception e){
//			transactionManager.rollback(transaction);
			throw new RuntimeException("Error while importing areas of interest", e);
		}
		
		
		return workspace;
	}

	private Aoi getOrCreateAoi(Map<Integer, Map<String, Aoi>> aois, String code, String caption, AoiLevel level, double area, Aoi parentAoi) {
		Map<String, Aoi> map = aois.get(level.getRank());
		if (map != null && map.get(code) != null) {
			return map.get(code);
		} else {
			if (map == null) {
				map = new LinkedHashMap<String, Aoi>();
				aois.put(level.getRank(), map);
			}
			Aoi aoi = new Aoi();
			aoi.setCode(code);
			aoi.setCaption(caption);
			aoi.setAoiLevel(level);
			aoi.setLandArea( new BigDecimal(area) );
			aoi.setParentAoi(parentAoi);

			aoiDao.insert(aoi);
			level.addAoi(aoi);

			map.put(code, aoi);
			return aoi;
		}
	}

	@Transactional
	public void loadByWorkspace( Workspace workspace ) {
		List<AoiHierarchy> list = aoiHierarchyDao.fetchByWorkspaceId( workspace.getId() );
		for (AoiHierarchy aoiHierarchy : list) {
			workspace.addAoiHierarchy( aoiHierarchy );
			
			List<AoiLevel> aoiLevels = aoiLevelDao.fetchByAoiHierarchyId( aoiHierarchy.getId() );
			Collections.sort( aoiLevels, new Comparator<AoiLevel>() {
				@Override
				public int compare(AoiLevel o1, AoiLevel o2) {
					return o1.getRank().compareTo( o2.getRank() );
				}
			});
			
			for (AoiLevel aoiLevel : aoiLevels) {
				aoiHierarchy.addLevel( aoiLevel );
			}
			
			AoiLevel rootLevel = aoiLevels.get(0);
			List<Aoi> aois = aoiDao.fetchByAoiLevelId( rootLevel.getId() );
			if( !aois.isEmpty() ){
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
}
