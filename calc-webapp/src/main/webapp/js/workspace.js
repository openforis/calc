/**
 * Workspace object
 * @author Mino Togna
 */

Workspace = function(object) {
	var $this = this;
	
	$.extend(this, object);
	
	//replace json entity objects with custom Entity objects
	$.each($this.entities, function(i, entity) {
		var newEntity = new Entity(entity);
		$this.entities[i] = newEntity;
	});
};

Workspace.prototype = (function(){
	/**
	 * Returns the entity with the id passed as argument if there is.
	 */
	var getEntityById = function(id) {
		if( id ) {
			var $this = this;
			
			for(var i in $this.entities){
				var entity = $this.entities[i];
				if(entity.id.toString() == id.toString()) {
					return entity;
				}
			}
		}
	};
	
	/**
	 * Returns the entities that contains at least one aggregable measure (quantitative var) children of the entity passed as parameter
	 */
	var getAggregableEntities = function(entity) {
		var entities = [];
		$.each(this.entities, function(i, e){
			if(e.parentId == entity.id && e.quantitativeVariables.length > 0) {
				entities.push(e);
			}
		});
		return entities;
	};
	
	return {
		constructor : Workspace
		,
		getEntityById : getEntityById
		,
		getAggregableEntities : getAggregableEntities
	};
	
})();