/**
 * Entity model object
 * 
 * @author Mino Togna
 * @author S. Ricci
 */
Entity = function( workspace , jsonObject ) {
    $.extend( this, jsonObject );
    
    this.workspace = workspace;
};

/**
 * Returns the categorical variables
 */
Entity.prototype.categoricalVariables = function(){
	return this.filterVariables( "type" , "CATEGORICAL" );
};

/**
 * Returns the quantitative variables
 */
Entity.prototype.quantitativeVariables = function(){
	return this.filterVariables( "type" , "QUANTITATIVE" );
};

/**
 * Returns the quantitative variables
 */
Entity.prototype.textVariables = function(){
	return this.filterVariables( "type" , "TEXT" );
};

Entity.prototype.filterVariables = function( field , value ){
	var vars = [];
	
	for( var i in this.variables ){
		var variable = this.variables[i];
		
		var variableFieldValue = variable[field];
		if( variableFieldValue == value ){
			vars.push( variable );
		}
	}
	
	return vars;
};

/**
 * Replace the passed variable for the given entity with id as argument
 */
Entity.prototype.replaceVariable = function( variable ) {
    var $this = this;
    $.each($this.variables, function( i , variableToReplace ) {
		if (variableToReplace.id.toString() == variable.id.toString()) {
		    $this.variables[i] = variable;
		    return false;
		}
    });
    return variable;
};

Entity.prototype.addVariable = function( variable ) {
	this.variables.push(variable);
};

/**
 * Returns all the variables up to the root entity
 */
Entity.prototype.hierarchyVariables = function() {
    var result = this.variables;
    var currentParent = this.parent();

    while ( currentParent != null ) {
    	var parentVariables = currentParent.variables;
    	result = parentVariables.concat( result );
    	currentParent = currentParent.parent();
    }
    
    return result;
};

Entity.prototype.isInSamplingUnitHierarchy = function(){
	var entity = this;
	while ( entity != null ) {
    	if( entity.isSamplingUnit() ){
    		return true;
    	}
    	
    	entity = entity.parent();
    }
	return false;
};

Entity.prototype.isSamplingUnit = function(){
	return this.workspace.isSamplingUnit( this );
};
/**
 * Returns all the categorical variables up to the sampling unit entity
 */
Entity.prototype.samplingUnitHierarchyCategoricalVariables = function() {
    var vars = [];
    
    var entity = this;
    while ( entity != null ){
    	if( entity.isInSamplingUnitHierarchy() ){
    		vars = $.merge( vars, entity.categoricalVariables() );
    		
    		if( entity.isSamplingUnit() ){
    			break ;
    		}
    		entity = entity.parent();
    	}
    }
    
    return vars;
};

/**
 * Returns the variable with the given id
 */
Entity.prototype.getVariableById = function( id ) {
	var variables = this.hierarchyVariables();
    for (var i = 0; i < variables.length; i++) {
    	var variable = variables[i];
		if (variable.id == id) {
		    return variable;
		}
    }
    return null;
};

/**
 * Deletes the variable with the given id
 */
Entity.prototype.deleteVariable = function( id ) {
    var v = this.getVariableById( id );
    
    var index = this.variables.indexOf( v );
    if( index > 0 ) {
    	this.variables.splice( index , 1 );
    } 	
};

/**
 * Returns parent entity
 */
Entity.prototype.parent = function() {
    return this.workspace.getEntityById( this.parentEntityId );
};

/**
 * Returns true if it has at least one user defined quantity variable
 */
Entity.prototype.isAggregable = function() {
    return this.quantitativeOutputVariables().length > 0;
};

/**
 * Returns user defined quantitative variables
 */
Entity.prototype.quantitativeOutputVariables = function() {
	var qtyVars = this.quantitativeVariables();
	var vars = [];
	for( var i in qtyVars ){
		var v = qtyVars[ i ];
		if( v.userDefined === true ){
			vars.push( v );
		}
	}
	return vars;
};

/**
 * Returns user defined categorical variables
 */
Entity.prototype.categoricalOutputVariables = function() {
	var vars = [];
	
	var catVars = this.categoricalVariables();
	for( var i in catVars ){
		var v = catVars[ i ];
		if( v.userDefined === true ){
			vars.push( v );
		}
	}
	
	return vars;
};
