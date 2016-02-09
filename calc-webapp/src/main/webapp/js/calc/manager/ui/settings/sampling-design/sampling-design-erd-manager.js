/**
 * SamplingDesignERDManager
 * 
 * @author M. Togna
 */
SamplingDesignERDManager = function( parentContainer ){
	
	this.parentContainer 	= $( parentContainer );
	this.container 			= null;
	this.samplingDesign		= null;
	this.editMode			= false;
	this._initialized 		= false;
	
	this.parentContainer.hide();
	this.load();
	
};

SamplingDesignERDManager.prototype.load = function(){
	var data = $('<div class="sampling-design-erd">'+
		//'<div class="left-placeholder"></div>'+
		'<div>'+
			'<div class="base-unit-container two-rows-center"></div>'+
			'<div class="ssu-container two-rows"></div>'+
		'</div>'+
		'<div>'+
			'<div class="two-phases-container one-row"></div>'+
		'</div>'+
		'<div class="two-stages-container">'+
			'<div class="psu-container one-row"></div>'+
		'</div>'+
		'<div class="stratum-aoi-container">'+
			'<div class="aoi-container two-rows-center"></div>'+
			'<div class="stratum-container two-rows"></div>'+
		'</div>'+
	'</div>');
	this.container = $( data );
	this.parentContainer.append( this.container );
};

SamplingDesignERDManager.prototype.show = function( samplingDesign , mode ){
	this.parentContainer.show();
	this.samplingDesign		= samplingDesign;
	this.editMode			= ( mode === 'edit' );
	
	if( this._initialized === false ){
		this.init();
	}
	
	this.sdChange();
	if( this.editMode ){
		this.container.find( '.stratum-aoi-container' ).show();
		this.aoiManager.show();
	} else {
		if( this.samplingDesign.samplingUnitId ){
			this.aoiManager.update();
			this.baseUnitManager.show();
			this.container.find( '.stratum-aoi-container' ).show();
			this.aoiManager.show();
		}
	}

	EventBus.dispatch( 'calc.sampling-design.update-connections', null );
};

SamplingDesignERDManager.prototype.init = function(){
	this.aoiManager	 		= new ReportingUnitManager( this.container.find('.aoi-container') , this , 0 );
	this.aoiManager.joinColumnStepNo = 6;
	this.baseUnitManager 	= new BaseUnitManager	( this.container.find('.base-unit-container') , this , 1 );
	this.twoPhasesManager 	= new TwoPhasesManager	( this.container.find('.two-phases-container') , this , 2 );
	this.psuManager		 	= new PSUTwoStagesManager( this.container.find('.psu-container') , this , 3 );
	this.ssuManager		 	= new SSUTwoStagesManager( this.container.find('.ssu-container') , this , 3 );
	this.stratumManager		= new StratumManager	( this.container.find( '.stratum-container'), 	this , 4 );
	this.clusterManager	 	= new ClusterManager( this.container.find('.base-unit-container') , this , 5 );
	

//	if( this.samplingDesign.samplingUnitId ){
//		setTimeout( function(){
////			EventBus.dispatch('calc.sampling-design.base-unit-change' );
//		}, 100);
//	};
	
	EventBus.addEventListener( "calc.sampling-design-stratified-change", this.sdChange, this );
	EventBus.addEventListener( "calc.sampling-design.two-phases-change", this.sdChange, this );
	EventBus.addEventListener( "calc.sampling-design.two-stages-change", this.sdChange, this );
	
	this._initialized 		= true;
};
SamplingDesignERDManager.prototype.hide = function(){
	this.parentContainer.hide();
};

SamplingDesignERDManager.prototype.sdChange = function(){
	if( this.samplingDesign.twoStages === true ){
		this.baseUnitManager.container.removeClass( 'two-rows-center' ).addClass( 'two-rows' );
	} else {
		this.baseUnitManager.container.removeClass( 'two-rows' ).addClass( 'two-rows-center' );
	}
	
	if( this.samplingDesign.stratified === true ){
		this.aoiManager.container.removeClass( 'two-rows-center' ).addClass( 'two-rows' );
	} else {
		this.aoiManager.container.removeClass( 'two-rows' ).addClass( 'two-rows-center' );
	}
	
	if( this.samplingDesign.twoStages === true || this.samplingDesign.twoPhases === true){
//		this.container.find( '.left-placeholder' ).hide();
		this.container.animate({'margin-left':'0%'},400);
	}else{
		this.container.animate({'margin-left':'16.5%'},400);
//		this.container.find( '.left-placeholder' ).show();
	}
	var updConnections = function(){
		 EventBus.dispatch('calc.sampling-design.update-connections', null);
	};
	var setIntUpdConn = setInterval(updConnections, 5);
	setTimeout(function(){
		clearInterval(setIntUpdConn);
	},480);
//	two-rows-center
};