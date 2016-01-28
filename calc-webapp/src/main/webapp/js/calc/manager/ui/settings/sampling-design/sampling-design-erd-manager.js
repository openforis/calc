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
	
	this.parentContainer.hide();
	this.load();
	
};

SamplingDesignERDManager.prototype.load = function(){
	var $this = this;
	$.get( 'html/settings/sampling-design-erd.html',
			function(data){
				$this.container = $( data );
				$this.parentContainer.append( $this.container );
		});
};

SamplingDesignERDManager.prototype.show = function( samplingDesign ){
	this.parentContainer.fadeIn();
	this.samplingDesign		= samplingDesign;
	
	this.baseUnitManager 	= new BaseUnitManager	( this.container.find('.base-unit-container') , this , 0 );
	this.twoPhasesManager 	= new TwoPhasesManager	( this.container.find('.two-phases-container') , this , 1 );
	this.stratumManager		= new StratumManager	( this.container.find( '.stratum-container'), 	this , 3 );

	if( this.samplingDesign.samplingUnitId || this.editMode ){
		
		this.baseUnitManager.show();
	
		this.container.find( '.stratum-aoi-container' ).show();
	}
};

SamplingDesignERDManager.prototype.hide = function(){
	this.parentContainer.hide();
};

