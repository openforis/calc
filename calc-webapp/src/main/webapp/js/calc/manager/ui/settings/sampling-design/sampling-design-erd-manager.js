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
	var data = $(
		'<div class="sampling-design-erd">'+
			'<div>'+
				'<div class="base-unit-container two-rows-center"></div>'+
				'<div class="ssu-container two-rows"></div>'+
				'<div class="cluster-container two-rows"></div>'+
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
		'</div>'
		);
	this.container = $( data );
	this.parentContainer.append( this.container );
};

SamplingDesignERDManager.prototype.show = function( samplingDesign , mode ){
	this.parentContainer.show();
	this.samplingDesign		= samplingDesign;
	this.editMode			= ( mode === 'edit' );
	
	this.init();
};

SamplingDesignERDManager.prototype.init = function(){
	this.baseUnitManager 	= new BaseUnitManager	( this.container.find('.base-unit-container') , this , 1 );
	this.twoPhasesManager 	= new TwoPhasesManager	( this.container.find('.two-phases-container') , this , 2 );
	this.psuManager		 	= new PSUTwoStagesManager( this.container.find('.psu-container') , this , 3 );
	this.ssuManager		 	= new SSUTwoStagesManager( this.container.find('.ssu-container') , this , 3 );
	this.stratumManager		= new StratumManager	( this.container.find( '.stratum-container'), 	this , 4 );
//	this.clusterManager	 	= new ClusterManager( this.container.find('.base-unit-container') , this , 5 );
	this.clusterManager	 	= new ClusterManager( this.container.find('.cluster-container') , this , 5 );
	this.aoiManager	 		= new ReportingUnitManager( this.container.find('.aoi-container') , this , 0 );
	this.aoiManager.joinColumnStepNo = 6;
	
	var ConnectionManager = new ConnectionManagerClass();
	
	if( this.editMode ){
		
		this.container.find( '.stratum-aoi-container' ).show();
		this.aoiManager.show();
		
	} else {
		if( this.samplingDesign.samplingUnitId ){
			this.container.find( '.stratum-aoi-container' ).show();
			this.baseUnitManager.show();
			this.aoiManager.show();
		} else {
			this.container.find( '.stratum-aoi-container' ).hide();
			this.aoiManager.container.hide();
			this.baseUnitManager.container.parent().hide();
			
		}
	}
	
	var sdChange = $.proxy(function(){
		
		
		if( this.samplingDesign.twoStages === true || this.samplingDesign.cluster2 === true ){
			this.baseUnitManager.container.removeClass( 'two-rows-center' ).addClass( 'two-rows' );
		} else {
			this.baseUnitManager.container.removeClass( 'two-rows' ).addClass( 'two-rows-center' );
		}
		
//		if( this.samplingDesign.cluster2 === true ){
//			this.baseUnitManager.container.removeClass( 'two-rows-center' ).addClass( 'two-rows' );
//		} else {
//			this.baseUnitManager.container.removeClass( 'two-rows' ).addClass( 'two-rows-center' );
//		}
		
		if( this.samplingDesign.stratified === true ){
			this.aoiManager.container.removeClass( 'two-rows-center' ).addClass( 'two-rows' );
		} else {
			this.aoiManager.container.removeClass( 'two-rows' ).addClass( 'two-rows-center' );
		}
		
		var updConnections = function(){
//			console.log( 'updating conns');
			EventBus.dispatch('calc.sampling-design.update-connections', null);
		};
		var setIntUpdConn = setInterval(updConnections, 15);
		var clearIntervalFunctx = function(){
			clearInterval(setIntUpdConn);
		};
		if( this.samplingDesign.twoStages === true || this.samplingDesign.twoPhases === true){
//			this.container.animate({'margin-left':'0%'}, 200);
//			this.baseUnitManager.container.parent().animate({'margin-left':'0%'}, 200, clearIntervalFunctx);
			this.baseUnitManager.container.parent().css({'margin-left':'0%'});
			
		}else{
//			this.container.animate({'margin-left':'16.5%'}, 200);
//			this.baseUnitManager.container.parent().animate({'margin-left':'16.66666666666667%'}, 200, clearIntervalFunctx);
			this.baseUnitManager.container.parent().css({'margin-left':'16.66666666666667%'});
		}
		
		setTimeout(function(){
			clearInterval(setIntUpdConn);
		}, 500);
		
	}, this);
	
	EventBus.addEventListener( "calc.sampling-design.stratified-change", sdChange, this );
	EventBus.addEventListener( "calc.sampling-design.two-phases-change", sdChange, this );
	EventBus.addEventListener( "calc.sampling-design.two-stages-change", sdChange, this );
	EventBus.addEventListener( "calc.sampling-design.cluster-change", sdChange, this );
	
	sdChange();
};


SamplingDesignERDManager.prototype.hide = function(){
	this.parentContainer.hide();
};

