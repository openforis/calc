package org.openforis.calc.model;

import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.DualHashBidiMap;

/**
 * 
 * @author G. Miceli
 *
 */
public class IdentifierMap {
	private BidiMap noMap;
	private BidiMap codeMap;
	
	public void put(Integer id, Integer no, String code) {
		if ( no != null ) {
			if ( noMap == null ) {
				noMap = new DualHashBidiMap();
			}
			noMap.put(id, no);
		}
		if ( code != null ) {
			if ( codeMap == null ) {
				codeMap = new DualHashBidiMap(); 
			}
			codeMap.put(id, code);
		}
	}
	
	public Integer getNoById(int id) {
		return noMap == null ? null : (Integer) noMap.get(id);
	}
	
	public Integer getIdByNo(int no) {
		return noMap == null ? null : (Integer) noMap.getKey(no);
	}
	
	public String getCodeById(int id) {
		return codeMap == null ? null : (String) codeMap.get(id);
	}
	
	public Integer getIdByCode(String code) {
		return codeMap == null ? null : (Integer) codeMap.getKey(code);
	}
	
	public Integer getId(String code, Integer no) {
		Integer id = null;
		if ( code != null ) {
			id = getIdByCode(code);
		} 
		if ( id == null && no != null ) {
			id = getIdByNo(no);					
		}
		return id;
	}
}
