package org.openforis.calc.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author G. Miceli
 */
public class Variable extends org.openforis.calc.persistence.jooq.tables.pojos.Variable implements Identifiable {

	private static final long serialVersionUID = 1L;

	private Map<String, Category> categoryCodeMap;

	public boolean isCategorical() {
		String type = getVariableType();
		return "nominal".equals(type) || "multiple".equals(type) || "ordinal".equals(type);
	}

	public boolean isNumeric() {
		return !isCategorical();
	}

	public void setCategories(List<Category> categories) {
		this.categoryCodeMap = new HashMap<String, Category>();
		for ( Category cat : categories ) {
			categoryCodeMap.put(cat.getCategoryCode(), cat);
		}
	}

	public Category getCategory(String code) {
		if ( categoryCodeMap == null ) {
			throw new NullPointerException("categories not initialized");
		}
		return categoryCodeMap.get(code);
	}

	@Override
	public Integer getId() {
		return super.getVariableId();
	}

	@Override
	public void setId(Integer id) {
		super.setVariableId(id);
	}
}
