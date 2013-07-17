/**
 * 
 */
package org.openforis.calc.collect;

import java.util.List;

import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.Entity;
import org.openforis.collect.relational.CollectRdbException;
import org.openforis.collect.relational.model.RelationalSchema;
import org.openforis.collect.relational.model.RelationalSchemaGenerator;
import org.openforis.idm.metamodel.Survey;


/**
 * @author S. Ricci
 *
 */
public class CollectMetadataSynchronizer {
	
	public void importMetadata(Workspace workspace, Survey survey, String targetSchemaName) throws CollectRdbException {
		//generate rdb schema
		RelationalSchemaGenerator rdbGenerator = new RelationalSchemaGenerator();
		RelationalSchema schema = rdbGenerator.generateSchema(survey, targetSchemaName);
		
		//convert into entities
		CollectMetadataConverter metadataConverter = new CollectMetadataConverter();
		List<Entity> entities = metadataConverter.convert(workspace, schema);
		
		//TODO store entities??
		workspace.setEntities(entities);
	}

}
