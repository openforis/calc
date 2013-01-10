package org.openforis.calc.model;

// Generated Aug 8, 2012 5:42:26 PM by Hibernate Tools 3.4.0.CR1

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * StratumPlotObsCount generated by hbm2java
 */
@Entity
@Table(name = "stratum_plot_obs_count")
public class StratumPlotObsCount implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private StratumPlotObsCountId id;

	public StratumPlotObsCount() {
	}

	public StratumPlotObsCount(StratumPlotObsCountId id) {
		this.id = id;
	}

	@EmbeddedId
	@AttributeOverrides({ @AttributeOverride(name = "stratumId", column = @Column(name = "stratum_id")), @AttributeOverride(name = "count", column = @Column(name = "count")) })
	public StratumPlotObsCountId getId() {
		return this.id;
	}

	public void setId(StratumPlotObsCountId id) {
		this.id = id;
	}

}