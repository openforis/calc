package org.openforis.calc.metadata;

import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.openforis.calc.common.Identifiable;

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

	public Entity getSamplingUnit() {
		return samplingUnit;
	}

	public void setSamplingUnit(Entity samplingUnit) {
		this.samplingUnit = samplingUnit;
	}

	@JsonInclude
	public Integer getSamplingUnitId() {
		if (this.samplingUnit != null) {
			return this.samplingUnit.getId();
		}
		return null;
	}

}
