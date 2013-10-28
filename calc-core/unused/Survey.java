package org.openforis.calc.model;

/**
 * @author G. Miceli
 */
public interface Survey extends Entity<Survey> {

	static final Attribute<Integer> ID = new AttributeImpl<Integer>("survey_id");
	static final Attribute<String> NAME = new AttributeImpl<String>("survey_name");
	static final Attribute<String> URI = new AttributeImpl<String>("survey_uri");
}
