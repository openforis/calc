/**
 * 
 */
package org.openforis.calc.schema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jooq.Record;
import org.jooq.impl.DynamicTable;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.AoiHierarchy;
import org.openforis.calc.metadata.AoiLevel;
import org.openforis.calc.metadata.Entity;

/**
 * @author G. Miceli
 * @author M. Togna
 * 
 */
// TODO rename InputSchema to Schema? or DataSchema?
public class InputSchema extends RelationalSchema {

	private static final long serialVersionUID = 1L;

	private Workspace workspace;

	private Map<Integer, InputTable> dataTables;
//	private Map<Integer, ResultTable> resultTables;
	
	private Map<Integer, EntityDataView> dataViews;
	
	private Phase1AoiTable phase1AoiTable;
	
	private List<AoiHierarchyFlatTable> aoiHierchyTables;
	private Map<AoiLevel, ExpansionFactorTable> expansionFactorTables;

	private StratumDimensionTable stratumDimensionTable;
	
	public InputSchema(Workspace workspace) {
		super(workspace.getInputSchema());
		this.workspace = workspace;
		
		initDataTables();
//		initResultTables();
		
		initDataViews();
		
		this.phase1AoiTable = new Phase1AoiTable(this);
		
		initAoiHirerchyTables();
		
		initExpansionFactorTables();
		
		initStratumDimensionTable();
	}
	
	private void initStratumDimensionTable() {
		if( this.workspace.getSamplingDesign().getStratified() ){
			this.stratumDimensionTable = new StratumDimensionTable(workspace);
		}
	}

	private void initDataTables() {
		this.dataTables = new HashMap<Integer, InputTable>();
		for ( Entity entity : workspace.getEntities() ) {
			InputTable inputTable = new InputTable(entity, this);
			addTable(inputTable);
			dataTables.put(entity.getId(), inputTable);
		}
	}

//	private void initResultTables() {
//		this.resultTables = new HashMap<Integer, ResultTable>();
//		for ( Entity entity : workspace.getEntities() ) {
//			ResultTable table = new ResultTable(entity, this);
//			addTable(table);
//			resultTables.put(entity.getId(), table);
//		}
//	}
	
	private void initDataViews() {
		this.dataViews = new HashMap<Integer, EntityDataView>();
		for ( Entity entity : workspace.getEntities() ) {
			EntityDataView view = new EntityDataView(entity, this);
			addView(view);
			dataViews.put(entity.getId(), view);
		}
	}

	public Workspace getWorkspace() {
		return workspace;
	}
	
	public InputTable getDataTable(Entity entity) {
		return dataTables.get(entity.getId());
	}
	
	public ResultTable getResultTable(Entity entity) {
		return this.getResultTable(entity, false);
	}

	public ResultTable getResultTable(Entity entity, boolean temporary) {
		if( entity.getOutputVariables().size() > 0 || entity.isSamplingUnit() ){
			ResultTable table = new ResultTable(entity, this, temporary);
			return table;
		}
		return null;
	}
	
	public EntityDataView getDataView(Entity entity) {
		return dataViews.get(entity.getId());
	}
	
	public List<NewFactTable> getFactTables() {
		List<NewFactTable> tables = new ArrayList<NewFactTable>();
		for ( Entity entity : workspace.getEntities() ) {
			if( entity.isAggregable() ) {
				NewFactTable factTable = getFactTable(entity);
				tables.add(factTable);
			}
		}
		return tables;
	}

	public NewFactTable getFactTable(Entity entity) {
		if (entity.isAggregable()) {
			NewFactTable factTable = new NewFactTable(entity, this);
			return factTable;
		}
		return null;
	}
	
	public Phase1AoiTable getPhase1AoiTable() {
		return phase1AoiTable;
	}
	
	public DataAoiTable getSamplingUnitAoiTable() {
		Entity su = workspace.getSamplingUnit();
		if( su != null ){
			return new DataAoiTable( "_" + su.getDataTable()+"_aoi", this );
		}
		return null;
	}
	
	public EntityAoiTable getEntityAoiTable(Entity entity){
		return new EntityAoiTable(entity, this);
	}
	
	private void initAoiHirerchyTables() {
		this.aoiHierchyTables = new ArrayList<AoiHierarchyFlatTable>();
		
		List<AoiHierarchy> aoiHierarchies = getWorkspace().getAoiHierarchies();
		for (AoiHierarchy hierarchy : aoiHierarchies) {
			AoiHierarchyFlatTable table = new AoiHierarchyFlatTable(hierarchy, this);
			this.aoiHierchyTables.add( table );
		}
		
	}
	
	public List<AoiHierarchyFlatTable> getAoiHierchyTables() {
		return aoiHierchyTables;
	}

	private void initExpansionFactorTables() {
		this.expansionFactorTables = new HashMap<AoiLevel, ExpansionFactorTable>();
		
		for (AoiHierarchy aoiHierarchy : workspace.getAoiHierarchies()) {
			for (AoiLevel level : aoiHierarchy.getLevels()) {
				
				ExpansionFactorTable table = new ExpansionFactorTable(level, this);
				this.expansionFactorTables.put( level, table );
			
			}
		}
	}
	
	public ExpansionFactorTable getExpansionFactorTable(AoiLevel aoiLevel) {
		return this.expansionFactorTables.get(aoiLevel);
	}

	public DynamicTable<Record> getPhase1Table() {
		String phase1PlotTable = workspace.getPhase1PlotTable();
		
		if( StringUtils.isNotBlank(phase1PlotTable) ) {
			return new DynamicTable<Record>( phase1PlotTable, "calc" );
		}
		
		return null;
	}

	public StratumDimensionTable getStratumDimensionTable() {
		return this.stratumDimensionTable;
	}
	
}
