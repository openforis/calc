/**
 * Entity model object
 * 
 * @author S. Ricci
 * @author Mino Togna
 */
Entity = function( workspace , object ) {
    $.extend(this, object);
    
    this.workspace = workspace;
};

/**
 * Returns the quantitative variable with id variableId associated with entity
 * with id entityId (if there is)
 */
Entity.prototype.getQuantitativeVariableById = function( variableId ) {
    var vars = this.quantitativeVariables;
    for ( var i in vars) {
	var variable = vars[i];
	if (variable.id.toString() == variableId.toString()) {
	    return variable;
	}
    }
    ;
    return null;
};

/**
 * Replace the passed variable for the given entity with id as argument
 */
Entity.prototype.replaceVariable = function( variable ) {
    var $this = this;
    $.each($this.quantitativeVariables, function( i , variableToReplace ) {
	if (variableToReplace.id.toString() == variable.id.toString()) {
	    $this.quantitativeVariables[i] = variable;
	    return false;
	}
    });
    return variable;
};

/**
 * Adds the passed variable to the list of quantitative variables
 */
Entity.prototype.addQuantitativeVariable = function( variable ) {
    this.quantitativeVariables.push(variable);
};

/**
 * Returns all variables
 */
Entity.prototype.getVariables = function() {
    var result = this.categoricalVariables.concat(this.quantitativeVariables).concat(this.textVariables);
    
    //TODO temp workaround
    if (this.workspace.isSamplingUnit(this)) {
	result = result.concat({
	    name : "weight"
	});
    }
    
    return result;
};

/**
 * Returns all the variables up to the root entity
 */
Entity.prototype.getAncestorsVariables = function() {
    var currentParent = this.parent();
    var result = this.getVariables();
    while (currentParent != null) {
	var parentVariables = currentParent.getVariables();
	result = parentVariables.concat(result);
	currentParent = currentParent.parent();
    }
    return result;
};

/**
 * Returns the quantitative variable with the given id
 */
Entity.prototype.getVariableById = function( id ) {
    for (var i = 0; i < this.quantitativeVariables.length; i++) {
	var variable = this.quantitativeVariables[i];
	if (variable.id == id) {
	    return variable;
	}
    }
    return null;
};

/**
 * Deletes the quantitative variable with the given id
 */
Entity.prototype.deleteVariable = function( id ) {
    var v = this.getVariableById(id);
    var index = this.quantitativeVariables.indexOf(v);
    this.quantitativeVariables.splice(index, 1);
};

/**
 * Returns parent entity
 */
Entity.prototype.parent = function() {
    return this.workspace.getEntityById( this.parentId );
};

/**
 * Returns true if it has at least one user defined quantity variable
 */
Entity.prototype.isAggregable = function() {
    for ( var i in this.quantitativeVariables) {
	var qtyVar = this.quantitativeVariables[i];
	if (qtyVar.userDefined === true) {
	    return true;
	}
    }
    return false;
};
