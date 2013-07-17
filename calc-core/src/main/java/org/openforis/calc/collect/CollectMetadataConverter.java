package org.openforis.calc.collect;

import java.util.ArrayList;
import java.util.List;

import org.openforis.calc.metadata.Entity;
import org.openforis.collect.relational.model.RelationalSchema;

/**
 * Converts IDM survey metadata and Collect RDB schema into Calc metadata
 *   
 * @author G. Miceli
 * @author S. Ricci
 *
 */
public class CollectMetadataConverter {
	
	public List<Entity> convert(RelationalSchema schema) {
		List<Entity> entities = new ArrayList<Entity>();
		/// TODO
		return entities;
	}
}
