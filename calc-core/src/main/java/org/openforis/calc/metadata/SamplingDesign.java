package org.openforis.calc.metadata;

import java.util.ArrayList;
import java.util.List;

import org.jooq.impl.PrimarySamplingUnitTable;
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
	@JsonIgnore
	private PrimarySamplingUnitTable<?> psuTable;
	
	public Entity getSamplingUnit() {
		if( workspace == null ) {
			return null;
		}
		return workspace.getEntityById( getSamplingUnitId() );
	}

	public void setSamplingUnit(Entity samplingUnit) {
		this.samplingUnit = samplingUnit;
		if( samplingUnit != null ){
			setSamplingUnitId( this.samplingUnit.getId() );
		}
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
//	@JsonIgnore
//	public ColumnJoin getClusterColumn() {
//		return new ColumnJoin( getClusterColumnSettings() );
//	}
	 
	@Override
	public ParameterMap getAoiJoinSettings() {
		return super.getAoiJoinSettings();
	}
	@JsonIgnore
	public ColumnJoin getAoiJoin() {
		return new ColumnJoin( getAoiJoinSettings() );
	}
	
	@Override
	public Boolean getTwoStages() {
		return super.getTwoStages() != null ? super.getTwoStages() : false;
	}
	
	@Override
	public ParameterMap getTwoStagesSettings() {
		return super.getTwoStagesSettings();
	}
	@JsonIgnore
	public TwoStagesSettings getTwoStagesSettingsObject(){
		return new TwoStagesSettings(getTwoStagesSettings() );
	}
	
	void setPrimarySamplingUnitTable( PrimarySamplingUnitTable<?> psuTable ){
		this.psuTable = psuTable;
	}
	
	@JsonIgnore
	public PrimarySamplingUnitTable<?> getPrimarySamplingUnitTable() {
		return psuTable;
	}
	
	@JsonIgnore
	public boolean applyAreaWeigthedMethod(){
		return super.getApplyAreaWeighted()!=null && super.getApplyAreaWeighted();
	}
	
	@Override
	public Boolean getCluster() {
		return super.getCluster() != null ? super.getCluster() : Boolean.FALSE;
	}
	
	@Override
	public Boolean getCluster2() {
		return super.getCluster2() != null ? super.getCluster2() : Boolean.FALSE;
	}
	
	@JsonIgnore
	public Boolean applyClusterOnlyForErrorCalculation() {
		return super.getApplyClusterOnlyError() != null && super.getApplyClusterOnlyError();
	}
	
	@JsonIgnore
	public Entity getClusterEntity (){
		Entity cluster = null;
		if( getCluster2() ){
			cluster = workspace.getEntityByOriginalId( getClusterOriginalId().intValue() );
		}
		
		return cluster;
	}
	
//	@JsonIgnore
//	public boolean hasStrataAois(){
//		return super.getStratumAoi() != null && super.getStratumAoi();
//	} 
	
	// hard coded for now
	public String getWeightVariable() {
		return "weight";
	}

	public class TwoStagesSettings {
		private String areaColumn;
		private String noBaseUnitColumn;
		private int ssuOriginalId;
		private TableJoin joinSettings;
		
		TwoStagesSettings( ParameterMap map ){
			this.areaColumn = map.getString( "areaColumn" );
			this.noBaseUnitColumn = map.getString( "noBaseUnitColumn" );
			this.ssuOriginalId = map.getInteger( "ssuOriginalId" );
			this.joinSettings = new TableJoin( map.getMap("joinSettings") );
		}
		
		public String getAreaColumn() {
			return areaColumn;
		}
		
		public String getNoBaseUnitColumn() {
			return noBaseUnitColumn;
		}
		
		public int getSsuOriginalId() {
			return ssuOriginalId;
		}
		public TableJoin getJoinSettings() {
			return joinSettings;
		}

		public List<ColumnJoin> getPsuIdColumns() {
			List<ColumnJoin> columnJoins = getJoinSettings().getLeft().getColumnJoins();
			return columnJoins;
		}
		
		public List<ColumnJoin> getSamplingUnitPsuJoinColumns(){
//			return getJoinSettings().getRight().getColumnJoins().get( 0 ).getColumn();
			List<ColumnJoin> columnJoins = getJoinSettings().getRight().getColumnJoins();
			return columnJoins;
		}
		
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
