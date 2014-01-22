/**
* Entity model object
* @author S. Ricci
*/
Entity = function(workspace, object) {
	$.extend(this, object);
	
	this.workspace = workspace;
};

Entity.prototype = (function(){
	
	/**
	 * Returns the quantitative variable with id variableId associated with entity with id entityId (if there is)
	 */
	var getQuantitativeVariableById = function(variableId) {
		var vars = this.quantitativeVariables;
		for(var i in vars) {
			var variable = vars[i];
			if( variable.id.toString() == variableId.toString() ) {
				return variable;
			}
		};
		return null;
	};
	
	/**
	 * Replace the passed variable for the given entity with id as argument
	 */
	var replaceVariable = function(variable) {
		var $this = this;
		$.each($this.quantitativeVariables, function(i, variableToReplace){
			if( variableToReplace.id.toString() == variable.id.toString() ){
				$this.quantitativeVariables[i] = variable;
				return false;
			}
		});
		return variable;
	};
	
	/**
	 * Adds the passed variable to the list of quantitative variables
	 */
	var addQuantitativeVariable = function(variable) {
		this.quantitativeVariables.push(variable);
	};
	
	/**
	 * Returns all variables
	 */
	var getVariables = function() {
		var result = this.categoricalVariables
						.concat(this.quantitativeVariables)
						.concat(this.textVariables);
		return result;
	};
	
	/**
	 * Returns all the variables up to the root entity
	 */
	var getAncestorsVariables = function() {
		var currentParent = this.parent();
		var result = $.proxy(getVariables, this)();
		while ( currentParent != null ) {
			var parentVariables = currentParent.getVariables();
			result = parentVariables.concat(result);
			currentParent = currentParent.parent();
		};
		return result;
	};
	
	/**
	 * Returns the quantitative variable with the given id
	 */
	var getVariableById = function(id) {
		for (var i=0; i < this.quantitativeVariables.length; i++) {
			var variable = this.quantitativeVariables[i];
			if ( variable.id == id ) {
				return variable;
			}
		}
		return null;
	};
	
	/**
	 * Deletes the quantitative variable with the given id
	 */
	var deleteVariable = function(id) {
		var v = this.getVariableById(id);
		var index = this.quantitativeVariables.indexOf(v);
		this.quantitativeVariables.splice(index, 1);
	};
	
	var parent = function() {
		return this.workspace.getEntityById(this.parentId);
	};
	
	return {
		constructor : Entity
		,
		getQuantitativeVariableById : getQuantitativeVariableById
		,
		replaceVariable : replaceVariable
		,
		addQuantitativeVariable : addQuantitativeVariable
		,
		getVariables : getVariables
		,
		getAncestorsVariables : getAncestorsVariables
		,
		getVariableById : getVariableById
		,
		deleteVariable : deleteVariable
		,
		parent : parent
	};
	
})();