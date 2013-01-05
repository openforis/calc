package org.openforis.calc.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.calc.persistence.jooq.tables.pojos.VariableBase;

/**
 * @author G. Miceli
 */
public class Variable extends VariableBase implements ImportableModelObject {

	private static final long serialVersionUID = 1L;

	private Map<String, Category> categoryCodeMap;
	
	public boolean isCategorical() {
		String type = getType();
		return "nominal".equals(type) || "multiple".equals(type) || "ordinal".equals(type);
	}

	public boolean isNumeric() {
		return !isCategorical();
	}
	
	public void setCategories(List<Category> categories) {
		this.categoryCodeMap = new HashMap<String, Category>();
		for (Category cat : categories) {
			categoryCodeMap.put(cat.getCode(), cat);
		}
	}

	public Category getCategory(String code) {
		if ( categoryCodeMap == null ) {
			throw new NullPointerException("categories not initialized");
		}
		return categoryCodeMap.get(code);
	}
}
