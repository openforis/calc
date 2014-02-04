package org.openforis.calc.metadata;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.openforis.calc.common.Identifiable;
import org.openforis.calc.engine.ParameterMap;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.json.ParameterMapJsonSerializer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Provides metadata about the sampling design
 * 
 * @author M. Togna
 */
@javax.persistence.Entity
@Table(name = "sampling_design")
public class SamplingDesign extends Identifiable {

	@JsonIgnore
	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "sampling_unit_id")
	private Entity samplingUnit;
	
	@JsonIgnore
	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "workspace_id")
	private Workspace workspace;
	
	@Column(name = "srs")
	private Boolean srs;

	@Column(name = "systematic")
	private Boolean systematic;

	@Column(name = "two_phases")
	private Boolean twoPhases;

	@Column(name = "stratified")
	private Boolean stratified;

	@Column(name = "cluster")
	private Boolean cluster;

	@JsonSerialize(using = ParameterMapJsonSerializer.class)
	@Type(type = "org.openforis.calc.persistence.hibernate.JsonParameterMapType")
	@Column(name = "phase1_join_settings")
	private ParameterMap phase1JoinSettings;
	
	@JsonSerialize(using = ParameterMapJsonSerializer.class)
	@Type(type = "org.openforis.calc.persistence.hibernate.JsonParameterMapType")
	@Column(name = "stratum_join_settings")
	private ParameterMap stratumJoinSettings;
	
	@JsonSerialize(using = ParameterMapJsonSerializer.class)
	@Type(type = "org.openforis.calc.persistence.hibernate.JsonParameterMapType")
	@Column(name = "cluster_column_settings")
	private ParameterMap clusterColumnSettings;
	
	@JsonSerialize(using = ParameterMapJsonSerializer.class)
	@Type(type = "org.openforis.calc.persistence.hibernate.JsonParameterMapType")
	@Column(name = "aoi_join_settings")
	private ParameterMap aoiJoinSettings;
	
	@Column( name = "sampling_unit_weight_script" )
	private String samplingUnitWeightScript;
	
	public Entity getSamplingUnit() {
		return samplingUnit;
	}

	public void setSamplingUnit(Entity samplingUnit) {
		this.samplingUnit = samplingUnit;
	}

	public Workspace getWorkspace() {
		return workspace;
	}
	
	public void setWorkspace(Workspace workspace) {
		this.workspace = workspace;
	}
	
	public Boolean getSrs() {
		return srs;
	}

	public void setSrs(Boolean srs) {
		this.srs = srs;
	}

	public Boolean getSystematic() {
		return systematic;
	}

	public void setSystematic(Boolean systematic) {
		this.systematic = systematic;
	}

	public Boolean getTwoPhases() {
		return twoPhases;
	}

	public void setTwoPhases(Boolean twoPhases) {
		this.twoPhases = twoPhases;
	}

	public Boolean getStratified() {
		return stratified;
	}

	public void setStratified(Boolean stratified) {
		this.stratified = stratified;
	}

	public Boolean getCluster() {
		return cluster;
	}

	public void setCluster(Boolean cluster) {
		this.cluster = cluster;
	}

	public ParameterMap getPhase1JoinSettings() {
		return phase1JoinSettings;
	}

	public void setPhase1JoinSettings(ParameterMap phase1JoinSettings) {
		this.phase1JoinSettings = phase1JoinSettings;
	}

	public ParameterMap getStratumJoinSettings() {
		return stratumJoinSettings;
	}

	@JsonIgnore
	public ColumnJoin getStratumJoin() {
		return new ColumnJoin( stratumJoinSettings );
	}
	
	public void setStratumJoinSettings(ParameterMap stratumJoinSettings) {
		this.stratumJoinSettings = stratumJoinSettings;
	}

	public ParameterMap getClusterColumnSettings() {
		return clusterColumnSettings;
	}

	public void setClusterColumnSettings(ParameterMap clusterColumnSettings) {
		this.clusterColumnSettings = clusterColumnSettings;
	}

	public ParameterMap getAoiJoinSettings() {
		return aoiJoinSettings;
	}

	@JsonIgnore
	public ColumnJoin getAoiJoin() {
		return new ColumnJoin( aoiJoinSettings );
	}
	
	public void setAoiJoinSettings(ParameterMap aoiJoinSettings) {
		this.aoiJoinSettings = aoiJoinSettings;
	}

	public String getSamplingUnitWeightScript() {
		return samplingUnitWeightScript;
	}
	
	public void setSamplingUnitWeightScript(String samplingUnitWeightScript) {
		this.samplingUnitWeightScript = samplingUnitWeightScript;
	}
	
	@JsonInclude
	public Integer getSamplingUnitId() {
		if (this.samplingUnit != null) {
			return this.samplingUnit.getId();
		}
		return null;
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
		TableJoin(ParameterMap map) {
			
		}
	}
	
	//{"leftTable":{"schema":"calc","table":"phase1_plot_naforma1"},"rightTable":{"schema":"naforma1","table":"plot"},"columns":[{"left":"cluster","right":"cluster_id"},{"left":"plot","right":"no"}]}
	
}
