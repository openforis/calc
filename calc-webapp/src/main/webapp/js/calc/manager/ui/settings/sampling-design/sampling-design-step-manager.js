/**
 * Super class for sampling design edit steps
 * 
 *  @author M. Togna
 */
SamplingDesignStepManager = function( container , sdERDManager , stepNo , dataProvider ){
	this.container 		= container;
	this.sdERDManager	= sdERDManager;
	this.stepNo			= stepNo;
	this.dataProvider 	= dataProvider;
	this.table 			= new ERDTable( this.container , this.dataProvider );
	
	this.currentStepNo 	= -1;
	
	// event listeners
	EventBus.addEventListener( "calc.sampling-design.show-step", this.showEditStep , this );
	
	this.erdTableJoins				= new Array();
	this.erdTableColumnSelectors	= new Array();
};

SamplingDesignStepManager.prototype.addJoin = function( tableJoin ){
	this.erdTableJoins.push( tableJoin )
};

SamplingDesignStepManager.prototype.addColumnSelector = function( columnSelector ){
	this.erdTableJoins.push( columnSelector )
};

SamplingDesignStepManager.prototype.showEditStep = function( evt, stepNo ){
	this.currentStepNo = stepNo;
	this.updateEditMode();
};

SamplingDesignStepManager.prototype.updateEditMode = function(){
	var edit = false;
	if( this.sdERDManager.editMode && this.currentStepNo == this.stepNo ){
		edit = true ;
		this.highlight();
	}
	
	this.table.setEditMode( edit );
	
	for( var i in this.erdTableColumnSelectors ){
		var col = this.erdTableColumnSelectors[ i ];
		col.setEditMode( edit );
	}
	for( var i in this.erdTableJoins ){
		var col = this.erdTableJoins[ i ];
		col.setEditMode( edit );
	}
};

SamplingDesignStepManager.prototype.highlight = function(){
	if( this.sdERDManager.editMode === true && this.currentStepNo == this.stepNo ){
		this.table.highlight();
	}
};

SamplingDesignStepManager.prototype.sd = function(){
	return this.sdERDManager.samplingDesign;
};