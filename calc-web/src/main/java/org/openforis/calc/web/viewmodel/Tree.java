/**
 * 
 */
package org.openforis.calc.web.viewmodel;

import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * @author Mino Togna
 * 
 */
public class Tree {

	private String id;
	private Double dbh;
	private Double height;
	private Double heightPredicted;

	public Tree(String id, Double dbh, Double height) {
		super();
		this.id = id;
		this.dbh = dbh;
		this.height = height;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Double getDbh() {
		return dbh;
	}

	public void setDbh(Double dbh) {
		this.dbh = dbh;
	}

	public Double getHeight() {
		return height;
	}

	public void setHeight(Double height) {
		this.height = height;
	}

	public Double getHeightPredicted() {
		return heightPredicted;
	}

	public void setHeightPredicted(Double heightPredicted) {
		this.heightPredicted = heightPredicted;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append(id).append(dbh).append(height).append(heightPredicted).toString();
	}
	
}
