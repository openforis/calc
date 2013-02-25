package org.openforis.calc.model;


/**
 * @author G. Miceli
 * @author M. Togna
 */
public class Variable extends org.openforis.calc.persistence.jooq.tables.pojos.Variable implements Identifiable {
	
	public enum Type {
		BINARY("binary"), RATIO("ratio"), MULTIPLE("multiple"), NOMINAL("nominal");

		private String type;

		Type(String type) {
			this.type = type;
		}

		public boolean equals(String otherType) {
			return this.type.equals(otherType);
		}
		
		@Override
		public String toString() {
			return type;
		}
	}
	
	private static final long serialVersionUID = 1L;
		
//	private Map<String, Category> categoryCodeMap;

	public boolean isCategorical() {
		String type = getVariableType();
		return "nominal".equals(type) || "multiple".equals(type) || "ordinal".equals(type) || "binary".equals(type);
	}

	public boolean isBinary() {
		String type = getVariableType();
		return "binary".equals(type);
	}

	public boolean isMultipleResponse() {
		String type = getVariableType();
		return "multiple".equals(type);
	}

	public boolean isNumeric() {
		return !isCategorical();
	}
	
	public void setType(Type type) {
		super.setVariableType( type.toString() );
	}
	
//	public void setCategories(List<Category> categories) {
//		this.categoryCodeMap = new HashMap<String, Category>();
//		for ( Category cat : categories ) {
//			categoryCodeMap.put(cat.getCategoryCode(), cat);
//		}
//	}
//
//	public Category getCategory(String code) {
//		if ( categoryCodeMap == null ) {
//			throw new NullPointerException("categories not initialized");
//		}
//		return categoryCodeMap.get(code);
//	}

	@Override
	public Integer getId() {
		return super.getVariableId();
	}

	@Override
	public void setId(Integer id) {
		super.setVariableId(id);
	}
}
