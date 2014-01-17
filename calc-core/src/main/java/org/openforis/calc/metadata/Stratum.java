package org.openforis.calc.metadata;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.openforis.calc.common.Identifiable;
import org.openforis.calc.engine.Workspace;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Provides metadata about a statum.
 * 
 * @author Mino Togna
 * 
 */
@javax.persistence.Entity
@Table(name = "stratum")
public class Stratum extends Identifiable {

	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "workspace_id")
	private Workspace workspace;

	@Column(name = "stratum_no")
	private Integer stratumNo;

	@Column(name = "caption")
	private String caption;

	@Column(name = "description")
	private String description;

	public Workspace getWorkspace() {
		return workspace;
	}

	public void setWorkspace(Workspace workspace) {
		this.workspace = workspace;
	}

	public Integer getStratumNo() {
		return stratumNo;
	}

	public void setStratumNo(Integer stratumNo) {
		this.stratumNo = stratumNo;
	}

	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
