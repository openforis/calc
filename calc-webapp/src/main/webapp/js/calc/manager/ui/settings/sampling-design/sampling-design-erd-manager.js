/**
 * SamplingDesignERDManager
 * 
 * @author M. Togna
 */
SamplingDesignERDManager = function( parentContainer , mode ){
	
	this.parentContainer 	= $( parentContainer );
	this.container 			= null;
	this.samplingDesign		= null;
	this.editMode			= ( mode === 'edit' );
	this._initialized 		= false;
	
	this.parentContainer.hide();
	this.load();
	
};

SamplingDesignERDManager.prototype.load = function(){
	var data = $('<div class="sampling-design-erd">'+
		'<div>'+
			'<div class="base-unit-container one-row"></div>'+
		'</div>'+
		'<div>'+
			'<div class="two-phases-container one-row"></div>'+
		'</div>'+
		'<div class="two-stages-container">'+
			'<div class="psu-container one-row"></div>'+
		'</div>'+
		'<div class="stratum-aoi-container">'+
			'<div class="ssu-container two-rows"></div>'+
			'<div class="stratum-container two-rows"></div>'+
			'<div class="aoi-container two-rows"></div>'+
		'</div>'+
	'</div>');
	this.container = $( data );
	this.parentContainer.append( this.container );
};

SamplingDesignERDManager.prototype.show = function( samplingDesign ){
	this.parentContainer.fadeIn();
	this.samplingDesign		= samplingDesign;
	
	if( this._initialized === false ){
		this.init();
	}

	if( this.samplingDesign.samplingUnitId || this.editMode ){
		
		this.baseUnitManager.show();
	
		this.container.find( '.stratum-aoi-container' ).show();
	}
	EventBus.dispatch( 'calc.sampling-design.update-connections', null );
};

SamplingDesignERDManager.prototype.init = function(){
	this.baseUnitManager 	= new BaseUnitManager	( this.container.find('.base-unit-container') , this , 0 );
	this.twoPhasesManager 	= new TwoPhasesManager	( this.container.find('.two-phases-container') , this , 1 );
	this.psuManager		 	= new PSUTwoStagesManager( this.container.find('.psu-container') , this , 2 );
	this.ssuManager		 	= new SSUTwoStagesManager( this.container.find('.ssu-container') , this , 2 );
	this.stratumManager		= new StratumManager	( this.container.find( '.stratum-container'), 	this , 3 );
	this.clusterManager	 	= new ClusterManager( this.container.find('.base-unit-container') , this , 4 );
	this.aoiManager	 		= new ReportingUnitManager( this.container.find('.aoi-container') , this , 5 );
	
	 this._initialized 		= true; 
};
SamplingDesignERDManager.prototype.hide = function(){
	this.parentContainer.hide();
};

