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
};

SamplingDesignStepManager.prototype.showEditStep = function( evt, stepNo ){
	this.currentStepNo = stepNo;
	
	if( this.sdERDManager.editMode && this.currentStepNo == this.stepNo ){
		
		this.table.setEditMode( true );
		this.highlight();
		
	} else {
		this.table.setEditMode( false );
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