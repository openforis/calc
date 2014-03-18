/**
 * Workspace object
 * @author Mino Togna
 */

Workspace = function(object) {
	var $this = this;
	
	$.extend(this, object);
	
	//replace json entity objects with custom Entity objects
	$.each($this.entities, function(i, entity) {
		var newEntity = new Entity($this, entity);
		$this.entities[i] = newEntity;
	});
};

/**
 * Returns the entity with the id passed as argument if there is.
 */
Workspace.prototype.getEntityById = function(id) {
	if( id ) {
		for( var i in this.entities ) {
			var entity = this.entities[i];
			if( entity.id.toString() == id.toString() ) {
				return entity;
			}
		}
	}
	return null;
};

/**
 * Returns the entities that contains at least one aggregable measure (quantitative var) children of the entity passed as parameter
 */
Workspace.prototype.getAggregableEntities = function(entityId) {
	var entities = [];
	$.each(this.entities, function(i, e) {
		if( e.parentId == entityId  ) {
			//&& e.isAggregable()
			entities.push(e);
		}
	});
	return entities;
};

/**
 * update an entity with the passed argument if Ids match
 */
Workspace.prototype.updateEntity = function(entity) {
	if ( entity ) {
		var $this = this;
		$.each($this.entities, function(i, currEntity) {
			if(currEntity.id == entity.id){
				$this.entities[i] = entity;
				return false;
			}
		});
	}
};

Workspace.prototype.isSamplingUnit = function(entity) {
	var result = false;
	if( this.samplingDesign ){
		var suId = this.samplingDesign.samplingUnitId;
		result = ( suId === entity.id);
	}
    return result;
};
