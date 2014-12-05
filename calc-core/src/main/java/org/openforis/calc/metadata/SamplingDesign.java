package org.openforis.calc.metadata;

import java.util.ArrayList;
import java.util.List;

import org.openforis.calc.engine.ParameterMap;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.persistence.jooq.tables.pojos.SamplingDesignBase;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Provides metadata about the sampling design
 * 
 * @author M. Togna
 */
public class SamplingDesign extends SamplingDesignBase {
	
	private static final long serialVersionUID = 1L;
	
	@JsonIgnore
	private Entity samplingUnit;
	@JsonIgnore
	private Workspace workspace;
	
	public Entity getSamplingUnit() {
		if( workspace == null ) {
			return null;
		}
		return workspace.getEntityById( getSamplingUnitId() );
	}

	public void setSamplingUnit(Entity samplingUnit) {
		this.samplingUnit = samplingUnit;
		setSamplingUnitId( this.samplingUnit.getId() );
	}
	
	public Workspace getWorkspace() {
		return this.workspace;
	}
	
	public void setWorkspace(Workspace workspace) {
		this.workspace = workspace;
		setWorkspaceId( workspace.getId() );
	}
	
	@Override
	public ParameterMap getPhase1JoinSettings() {
		return super.getPhase1JoinSettings();
	}
	@JsonIgnore
	public TableJoin getPhase1Join() {
		return new TableJoin( getPhase1JoinSettings() );
	}
	
	@Override
	public ParameterMap getStratumJoinSettings() {
		return super.getStratumJoinSettings();
	}
	@JsonIgnore
	public ColumnJoin getStratumJoin() {
		return new ColumnJoin( getStratumJoinSettings() );
	}
	
	@Override
	public ParameterMap getClusterColumnSettings() {
		return super.getClusterColumnSettings();
	}
	@JsonIgnore
	public ColumnJoin getClusterColumn() {
		return new ColumnJoin( getClusterColumnSettings() );
	}
	 
	@Override
	public ParameterMap getAoiJoinSettings() {
		return super.getAoiJoinSettings();
	}
	@JsonIgnore
	public ColumnJoin getAoiJoin() {
		return new ColumnJoin( getAoiJoinSettings() );
	}
	
	// hard coded for now
	public String getWeightVariable() {
		return "weight";
	}

	//		{"schema":"","column":"","table":""}
	public class ColumnJoin {
		private String schema;
		private String table;
		private String column;
		
		ColumnJoin(ParameterMap map) {
			if( map != null ) {
				this.schema = map.getString("schema");
				this.table = map.getString("table");
				this.column = map.getString("column");
			}
		}
		
		public String getSchema() {
			return schema;
		}
		public String getTable() {
			return table;
		}
		public String getColumn() {
			return column;
		}
		
	}
	
	public class TableJoin {
		private Table left;
		private Table right;
		
		TableJoin(ParameterMap map) {
			 this.left = new Table( map.getMap("leftTable") );
			 this.right = new Table( map.getMap("rightTable") );
			 
			 List<ParameterMap> list = map.getList("columns");
			 for (ParameterMap parameterMap : list) {
				 this.left.addColumnJoin( parameterMap.getString("left") );
				 this.right.addColumnJoin( parameterMap.getString("right") );
			 }
		}
		
		public Table getLeft() {
			return left;
		}
		
		public Table getRight() {
			return right;
		}
		
		public int getColumnJoinSize(){
			return this.left.columnJoins.size();
		}
		
		public class Table {
			
			private String schema;
			private String table;
			private List<ColumnJoin> columnJoins;
			
			public Table(ParameterMap map) {
				if( map != null ) {
					this.schema = map.getString("schema");
					this.table = map.getString("table");
					
				}
				this.columnJoins = new ArrayList<SamplingDesign.ColumnJoin>();
			}
			public String getSchema() {
				return schema;
			}
			public String getTable() {
				return table;
			}
			
			public List<ColumnJoin> getColumnJoins() {
				return columnJoins;
			}
			
			private void addColumnJoin( String column ){
				ColumnJoin columnJoin = new ColumnJoin(null);
				columnJoin.schema = this.schema;
				columnJoin.table = this.table;
				columnJoin.column = column;
				this.columnJoins.add(columnJoin);
			}
		}
	}
	
	//{"leftTable":{"schema":"calc","table":"phase1_plot_naforma1"},"rightTable":{"schema":"naforma1","table":"plot"},"columns":[{"left":"cluster","right":"cluster_id"},{"left":"plot","right":"no"}]}
	
}
