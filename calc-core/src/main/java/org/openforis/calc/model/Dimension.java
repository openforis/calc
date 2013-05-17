package org.openforis.calc.model;

/**
 * 
 * @author G. Miceli
 * @author M. Togna
 *
 */
public class Dimension extends org.openforis.calc.persistence.jooq.tables.pojos.Dimension {

	private static final long serialVersionUID = 1L;

	private org.openforis.calc.persistence.jooq.tables.pojos.Dimension dimension;

	public Dimension(org.openforis.calc.persistence.jooq.tables.pojos.Dimension dimension) {
		this.dimension = dimension;
	}

	public boolean equals(Object obj) {
		return dimension.equals(obj);
	}

	public String getDimensionName() {
		return dimension.getDimensionName();
	}

	public Integer getDimensionId() {
		return dimension.getDimensionId();
	}

	public String getDimensionCaption() {
		return dimension.getDimensionCaption();
	}

	public String getDimensionTableName() {
		return dimension.getDimensionTableName();
	}

	public Integer getCubeId() {
		return dimension.getCubeId();
	}

	public Boolean getVisible() {
		return dimension.getVisible();
	}

	public String getDimensionTableSchema() {
		return dimension.getDimensionTableSchema();
	}

	public String getDimensionNameColumn() {
		return dimension.getDimensionNameColumn();
	}

	public String getDimensionCaptionColumn() {
		return dimension.getDimensionCaptionColumn();
	}

	public String getDimensionPkColumn() {
		return dimension.getDimensionPkColumn();
	}

	public String getDimensionFkColumn() {
		return dimension.getDimensionFkColumn();
	}

	public int hashCode() {
		return dimension.hashCode();
	}

	public void setDimensionName(String dimensionName) {
		dimension.setDimensionName(dimensionName);
	}

	public void setDimensionId(Integer dimensionId) {
		dimension.setDimensionId(dimensionId);
	}

	public void setDimensionCaption(String dimensionCaption) {
		dimension.setDimensionCaption(dimensionCaption);
	}

	public void setDimensionTableName(String dimensionTableName) {
		dimension.setDimensionTableName(dimensionTableName);
	}

	public void setCubeId(Integer cubeId) {
		dimension.setCubeId(cubeId);
	}

	public void setVisible(Boolean visible) {
		dimension.setVisible(visible);
	}

	public void setDimensionTableSchema(String dimensionTableSchema) {
		dimension.setDimensionTableSchema(dimensionTableSchema);
	}

	public void setDimensionNameColumn(String dimensionNameColumn) {
		dimension.setDimensionNameColumn(dimensionNameColumn);
	}

	public void setDimensionCaptionColumn(String dimensionCaptionColumn) {
		dimension.setDimensionCaptionColumn(dimensionCaptionColumn);
	}

	public void setDimensionPkColumn(String dimensionPkColumn) {
		dimension.setDimensionPkColumn(dimensionPkColumn);
	}

	public void setDimensionFkColumn(String dimensionFkColumn) {
		dimension.setDimensionFkColumn(dimensionFkColumn);
	}

	public String toString() {
		return dimension.toString();
	}
}
