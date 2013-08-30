package org.openforis.calc.metadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openforis.calc.common.NamedUserObject;

/**
 * Describes a categorical variable hierarchy, made up of one or more hierarchy {@link Level}s.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
public class CategoryHierarchy extends NamedUserObject {
	private CategoricalVariable variable;
	private List<CategoryLevel> levels = new ArrayList<CategoryLevel>();
	
	public CategoricalVariable getVariable() {
		return variable;
	}
	
	public List<CategoryLevel> getLevels() {
		return Collections.unmodifiableList(levels);
	}
}