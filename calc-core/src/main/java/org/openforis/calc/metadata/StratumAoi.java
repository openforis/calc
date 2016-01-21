package org.openforis.calc.metadata;

import org.openforis.calc.engine.Workspace;
import org.openforis.calc.persistence.jooq.tables.pojos.StratumAoiBase;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Provides metadata about a statum/aoi.
 * 
 * @author Mino Togna
 */
public class StratumAoi extends StratumAoiBase {

	private static final long serialVersionUID = 1L;

	@JsonIgnore
	private Stratum stratum;
	
	@JsonIgnore
	private Aoi aoi;
	
	@JsonIgnore
	private Workspace workspace;
	
	public Stratum getStratum() {
		return stratum;
	}
	
	public Aoi getAoi() {
		return aoi;
	}
	
	public Workspace getWorkspace() {
		return workspace;
	}
	
	public void setStratum(Stratum stratum) {
		this.stratum = stratum;
		setStratumId( stratum.getId() );
	}
	
	public void setAoi(Aoi aoi) {
		this.aoi = aoi;
		setAoiId( aoi.getId() );
	}
	
	public void setWorkspace(Workspace workspace) {
		this.workspace = workspace;
		setWorkspaceId( workspace.getId() );
	}
}
