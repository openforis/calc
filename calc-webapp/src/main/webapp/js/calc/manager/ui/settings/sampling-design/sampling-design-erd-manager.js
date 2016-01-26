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
				$this.init();
		});
};

SamplingDesignERDManager.prototype.init = function(){
	this.parentContainer.append( this.container );
	
	this.baseUnitManager = new BaseUnitManager( this.container.find('.base-unit-container'), this.editMode );
};

SamplingDesignERDManager.prototype.show = function( samplingDesign ){
	this.parentContainer.fadeIn();
	this.samplingDesign		= samplingDesign;
	
	this.baseUnitManager.show( samplingDesign );
};

SamplingDesignERDManager.prototype.hide = function(){
	this.parentContainer.hide();
};