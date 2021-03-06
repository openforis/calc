/**
 * Workspace object
 * @author Mino Togna
 */

Workspace = function(object) {
	var $this = this;
	
	$.extend(this, object);
	
	//replace json entity objects with custom Entity objects
	$.each($this.entities, function(i, entity) {
		var newEntity = new Entity( $this, entity );
		$this.entities[i] = newEntity;
	});
	
	this.entities.sort( function( a, b ){
		if(a.name < b.name) return -1;
	    if(a.name > b.name) return 1;
	    return 0;
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
 * Returns the entity with the id passed as argument if there is.
 */
Workspace.prototype.getEntityByOriginalId = function(id) {
	if( id ) {
		for( var i in this.entities ) {
			var entity = this.entities[i];
			if( entity.originalId.toString() == id.toString() ) {
				return entity;
			}
		}
	}
	return null;
};

/**
 * Returns the variable with the id passed as argument if there is.
 */
Workspace.prototype.getVariableById = function(id) {
	if( id ) {
		for( var i in this.entities ) {
			var entity = this.entities[i];
			var variable = entity.getVariableById( id );
			if( variable ) {
				return variable;
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
		if( e.parentEntityId === entityId  ) {
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
	var isSamplingUnit = false;
	if( this.samplingDesign ){
		var suId = this.samplingDesign.samplingUnitId;
		isSamplingUnit = ( suId === entity.id);
	}
    return isSamplingUnit;
};

/**
 * return the entity defined as sampling unit, if there is
 */
Workspace.prototype.samplingUnit = function(){
	for( var i in this.entities ){
		var entity = this.entities[ i ];
		if( this.isSamplingUnit(entity) ){
			return entity;
		}
	}
	return null;
};

Workspace.prototype.getRootAoi = function() {
	var aoi = {};
	if( this.aoiHierarchies && this.aoiHierarchies.length >0 ) {
		aoi = this.aoiHierarchies[0].rootAoi;		
	}
	return aoi;
};

Workspace.prototype.getAdminUnitFlatAois = function() {
	var addAoi = function( aoi , aois , captionPrefix ) {
		var a = {};
		a.caption = aoi.caption;
		if ( captionPrefix != "" ) {
			a.caption = captionPrefix + a.caption;
		}
		a.id = aoi.id;
		aois.push( a );
		
		if( aoi.children ) {
			for( var i in aoi.children ) {
				var child = aoi.children[i];
				addAoi( child, aois , (a.caption + " -> ") );
			}
		}
	};
	var aois = [];
	if( this.aoiHierarchies && this.aoiHierarchies.length >0 ) {
		var aoi = this.aoiHierarchies[0].rootAoi;
		addAoi( aoi, aois , "" );
	}
	return aois;
};

// returns the equation list with given id if there is
Workspace.prototype.getEquationList = function( listId ) {
	for( var i in this.equationLists ){
		var list = this.equationLists[i];
		if( list.id == listId ) {
			return list;
		}
	}
};
/**
 * Returns the default processing chain
 */
Workspace.prototype.getDefaultProcessingChain = function(){
	if( this.processingChains.length > 0 ){
		for( var i in this.processingChains ) {
			var chain = this.processingChains[i];
			if( chain.caption == 'default' ){
				return chain;
			}
		}
	}
};

/**
 * Update the processing chain with the one passed as argument
 */
Workspace.prototype.updateProcessingChain = function( processingChain ) {
	for( var i in this.processingChains ) {
		var wsChain = this.processingChains[i];
		if( wsChain.id == processingChain.id ){
			this.processingChains[i] = processingChain;
			break;
		}
	}
};
/**
 * Update the calculation step of the default processing chain passed as argument
 * @param step
 */
Workspace.prototype.updateCalculationStep = function( step ){
	var chain = this.getDefaultProcessingChain();
	if( chain && chain.calculationSteps ){
		for( var i in chain.calculationSteps ){
			var chainStep = chain.calculationSteps[ i ];
			if( chainStep.id == step.id ){
				chain.calculationSteps[ i ] = step;
				return;
			}
		}
	}
};

/**
 * Returns the user defined categories
 */
Workspace.prototype.userDefinedcategories = function(){
	var list = [];
	$.each( this.categories , function( i , category){
		if( !category.originalId ){
			list.push( category );
		}
	});
	return list;
	
};