/**
 * Manager for edit Sampling Design page
 */
SamplingDesignEditManager = function( editContainer , editERDContainer ){
	
	this.container 					= editContainer;
	
	this.prevBtn 					= this.container.find( "button.prev" );
	this.nextBtn 					= this.container.find( "button.next" );
	
	
	this.samplingDesignERDManager	= new SamplingDesignERDManager( editERDContainer, "edit" );
	this.samplingDesign				= null;
	
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
	var validate =  this[ "validateStep" + this.step ] ;
	if( validate ){
		var valid = $.proxy( validate, this )();
		if( valid ){
			this.step ++ ;
			this.showStep(this.step);
		}
	} else {
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


//EVENTS listeners
SamplingDesignEditManager.prototype.baseUnitChange = function( event, entityId ){
		this.samplingDesign.samplingUnitId =  entityId; 
};

// Validations methods
/**
 * Sampling unit validation
 */
SamplingDesignEditManager.prototype.validateStep0 = function(){
	if( this.samplingDesign.samplingUnitId ){
		return true;
	} else {
		UI.showError("Select a valid sampling unit", false);
		return false;
	}
};