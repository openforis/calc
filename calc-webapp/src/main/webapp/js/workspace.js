/**
 * Workspace object
 * @author Mino Togna
 */

Workspace = function(object) {
	var $this = this;
	
	//set workspace properties
	$.each(object, function(property, value) {
		$this[property] = value;
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
	 * Returns the quantitative variable with id variableId associated with entity with id entityId (if there is)
	 */
	var getQuantitativeVariableById = function(entityId, variableId) {
		var $this = this;
		var entity = $this.getEntityById(entityId);

		if( entity && variableId ) {
			var vars = entity.quantitativeVariables;
			for(var i in vars) {
				var variable = vars[i];
				if( variable.id.toString() == variableId.toString() ) {
					return variable;
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
		,
		getQuantitativeVariableById : getQuantitativeVariableById
	};
	
})();