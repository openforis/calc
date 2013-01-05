package org.openforis.calc.model;

import java.util.HashMap;
import java.util.Map;

/**
 * @author G. Miceli
 */
public class PlotIdentifierMap {
	private Map<Integer, IdentifierMap> map;
	
	public PlotIdentifierMap() {
		map = new HashMap<Integer, IdentifierMap>();
	}

	public void put(int clusterId, int plotId, Integer plotNo, String plotCode) {
		IdentifierMap idMap = map.get(clusterId);
		if ( idMap == null ) {
			idMap = new IdentifierMap();
			map.put(clusterId, idMap);
		}
		idMap.put(plotId, plotNo, plotCode);
	}
	
	public Integer getNoById(int clusterId, int plotId) {
		IdentifierMap idMap = map.get(clusterId);
		return idMap == null ? null : idMap.getNoById(plotId);
	}
	
	public Integer getIdByNo(int clusterId, int plotNo) {
		IdentifierMap idMap = map.get(clusterId);
		return idMap == null ? null : idMap.getIdByNo(plotNo);
	}
	
	public String getCodeById(int clusterId, int plotId) {
		IdentifierMap idMap = map.get(clusterId);
		return idMap == null ? null : idMap.getCodeById(plotId);
	}
	
	public Integer getIdByCode(int clusterId, String plotCode) {
		IdentifierMap idMap = map.get(clusterId);
		return idMap == null ? null : idMap.getIdByCode(plotCode);
	}

	public Integer getId(Integer clusterId, String plotCode, Integer plotNo) {
		IdentifierMap idMap = map.get(clusterId);
		return idMap == null ? null : idMap.getId(plotCode, plotNo);
	}

}
