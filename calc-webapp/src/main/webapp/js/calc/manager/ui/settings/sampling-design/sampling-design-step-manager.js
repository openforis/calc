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
	
	if( this.sdERDManager.editMode && stepNo == this.stepNo ){
		this.table.setEditMode( true );
	} else {
		this.table.setEditMode( false );
	}
	
};
