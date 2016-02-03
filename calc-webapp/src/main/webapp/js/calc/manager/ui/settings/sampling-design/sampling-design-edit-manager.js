/**
 * Manager for edit Sampling Design page
 */
SamplingDesignEditManager = function( editContainer , editERDContainer , stepButtonsContainer ){
	
	this.container 					= editContainer;
	
	this.prevBtn 					= this.container.find( "button.prev" );
	this.nextBtn 					= this.container.find( "button.next" );
	
	//ERD manager
	this.samplingDesignERDManager	= new SamplingDesignERDManager( editERDContainer, "edit" );
	this.validator					= new SamplingDesignValidator( this );
	this.samplingDesign				= null;
	
	// Step buttons manager
	this.samplingDesignStepButtonsManager = new SamplingDesignStepButtonsManager( stepButtonsContainer, this );
	
	this.init();
};

SamplingDesignEditManager.prototype.init = function(){
	var $this = this;
	this.prevBtn.click( function(){
		$this.prev();
	});
	this.nextBtn.click( function(){
		$this.next();
	}); 
	
	
	EventBus.addEventListener( "calc.sampling-design.base-unit-change", this.baseUnitChange , this );
};

SamplingDesignEditManager.prototype.show = function(){
	this.container.fadeIn();
	
	var $this 		= this;
	WorkspaceManager.getInstance().activeWorkspace(function(ws){
		$this.samplingDesign = $.extend(true, {}, ws.samplingDesign );
		
		$this.samplingDesignERDManager.show( $this.samplingDesign );
		$this.samplingDesignStepButtonsManager.updateView();
		
	});
	
	var steps = this.container.find(".step");
	steps.hide();
	this.step 		= 0;
	this.stepMax 	= steps.length - 1;
	// hide steps
	this.showStep( this.step );
};

SamplingDesignEditManager.prototype.hide = function(){
	this.container.hide();
	this.samplingDesignERDManager.hide();
};

/**
 * Show step section
 * @param step
 */
SamplingDesignEditManager.prototype.showStep = function(step) {
	this.container.find(".step").hide();
	this.container.find(".step-"+step).fadeIn( 200 );
	
	this.updateNavigationBtns();
	
	EventBus.dispatch("calc.sampling-design.show-step", null , step);
};

/**
 * Move ui to previous edit section
 */
SamplingDesignEditManager.prototype.prev = function(){
	if( this.step != 0){
		this.step --;
		this.showStep(this.step);
	}
};

/**
 * Move to next edit section
 */
SamplingDesignEditManager.prototype.next = function(){
	var valid = this.validator.isValid( this.step );
	if( valid ){
		this.step ++ ;
		this.showStep(this.step);
	}
};

/**
 * update edit navigation buttons
 */
SamplingDesignEditManager.prototype.updateNavigationBtns = function(){
	this.step == 0 ? UI.disable( this.prevBtn ) : UI.enable( this.prevBtn );
	this.step == this.stepMax ? UI.disable( this.nextBtn ) : UI.enable( this.nextBtn );
};


// EVENT listeners
SamplingDesignEditManager.prototype.baseUnitChange = function( event, entityId ){
		this.samplingDesign.samplingUnitId =  entityId; 
};
