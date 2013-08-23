package org.openforis.calc.chain.pre;
import static org.openforis.calc.persistence.jooq.Tables.AOI;
import static org.openforis.calc.persistence.jooq.Tables.AOI_HIERARCHY;
import static org.openforis.calc.persistence.jooq.Tables.AOI_LEVEL;

import org.jooq.Select;
import org.openforis.calc.engine.Task;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.AoiHierarchyLevel;
import org.openforis.calc.persistence.postgis.Psql;
import org.openforis.calc.persistence.postgis.Psql.Privilege;
import org.openforis.calc.schema.AoiDimensionTable;
import org.openforis.calc.schema.OutputSchema;

/**
 * Creates Aoi Dimension tables for each aoi level for the current workspace
 * 
 * @author M. Togna
 */

public final class CreateAoiDimensionTablesTask extends Task {

	@Override
	protected void execute() throws Throwable {
		
		Workspace workspace = getWorkspace();
		Integer workspaceId = workspace.getId();
		OutputSchema outputSchema = getOutputSchema();
		
		for ( AoiDimensionTable aoiDimensionTable : outputSchema.getAoiDimensionTables() ) {
			AoiHierarchyLevel hierarchyLevel = aoiDimensionTable.getHierarchyLevel();
			Integer aoiLevelId = hierarchyLevel.getId();
			
			//selects from calc.aoi table
			Select<?> select = new Psql()
				.select( AOI.ID, AOI.AOI_LEVEL_ID, AOI.PARENT_AOI_ID, AOI.CODE, AOI.CAPTION, AOI.SHAPE, AOI.TOTAL_AREA, AOI.LAND_AREA )
				.from( AOI )
				.join( AOI_LEVEL )
				.on( AOI.AOI_LEVEL_ID.eq(AOI_LEVEL.ID) )
				.and( AOI_LEVEL.ID.eq(aoiLevelId) )
				.join( AOI_HIERARCHY )
				.on( AOI_LEVEL.AOI_HIERARCHY_ID.eq(AOI_HIERARCHY.ID) )
				.where( AOI_HIERARCHY.WORKSPACE_ID.eq(workspaceId) );
			
			// create table from select
			psql()
				.createTable(aoiDimensionTable)
				.as(select)
				.execute();
			
			//add PK to aoi dim table
			psql()
				.alterTable( aoiDimensionTable )
				.addPrimaryKey( aoiDimensionTable.getPrimaryKey() )
				.execute();
			
			// Grant access to system user
			psql()
				.grant( Privilege.ALL )
				.on( aoiDimensionTable )
				.to(getSystemUser())
				.execute();		

		}
		
	}

}