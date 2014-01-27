package org.openforis.calc.metadata;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.openforis.calc.common.Identifiable;
import org.openforis.calc.engine.Workspace;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

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

	@JsonInclude
	public Integer getSamplingUnitId() {
		if (this.samplingUnit != null) {
			return this.samplingUnit.getId();
		}
		return null;
	}

}
