/**
 * 
 */
package org.openforis.calc.engine;

import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.openforis.calc.metadata.CategoryLevel.CategoryLevelValue;

/**
 * Wrapper class for workspace backup used for export/import functionality
 * 
 * @author Mino Togna
 * 
 */
public class WorkspaceBackup {

	private Workspace workspace;

	private String version;

	/**
	 * Map of variables (Calc id -> Original id)
	 */
	private Map<Integer, Integer> variableOriginalIds;

	/**
	 * map of category classes ( CategoryLevelId -> List<CategoryLevelValue> )
	 */
	private Map<Integer, List<CategoryLevelValue>> categoryLevelValues;
	
	private Phase1Data phase1Data;
	
	public WorkspaceBackup(){
		super();
	}

	public WorkspaceBackup( Workspace ws ){
		this.workspace = ws;
	}

	public Workspace getWorkspace() {
		return workspace;
	}

	public void setWorkspace( Workspace workspace ) {
		this.workspace = workspace;
	}

	public String getVersion() {
		return version;
	}

	public Phase1Data getPhase1Data() {
		return phase1Data;
	}
	
	public Map<Integer, Integer> getVariableOriginalIds() {
		return variableOriginalIds;
	}
	
	public Map<Integer, List<CategoryLevelValue>> getCategoryLevelValues() {
		return categoryLevelValues;
	}
	
	void setVersion( String version ) {
		this.version = version;
	}

	void setVariableOriginalIds( Map<Integer, Integer> variableOriginalIds ) {
		this.variableOriginalIds = variableOriginalIds;
	}

	void setCategoryLevelValues( Map<Integer, List<CategoryLevelValue>> categoryLevelValues ) {
		this.categoryLevelValues = categoryLevelValues;
	}
	
	void setPhase1Data( Phase1Data phase1Data ) {
		this.phase1Data = phase1Data;
	}

	public static class Phase1Data {
		
		public Phase1Data(){
		}
		
		public Phase1Data( JSONArray tableInfo , List<DataRecord> records ){
			this.tableInfo = tableInfo;
			this.records = records;
		}
		private JSONArray tableInfo;
		private List<DataRecord> records;
		
		public JSONArray getTableInfo() {
			return tableInfo;
		}
		
		public List<DataRecord> getRecords() {
			return records;
		}
		
	}
}
