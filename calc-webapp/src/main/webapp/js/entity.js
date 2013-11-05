/**
* Entity model object
* @author S. Ricci
*/
Entity = function(object) {
	$.extend(this, object);
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
	
	return {
		constructor : Entity
		,
		getQuantitativeVariableById : getQuantitativeVariableById
		,
		replaceVariable : replaceVariable
		,
		addQuantitativeVariable : addQuantitativeVariable
	};
	
})();