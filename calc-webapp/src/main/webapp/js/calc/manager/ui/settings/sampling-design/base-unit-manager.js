/**
 * Manager for Base Unit step
 * @author M. Togna
 */
BaseUnitManager = function( container , sdERDManager , stepNo ){
	var dataProvider 	= new EntityDataProvider( $.proxy(this.onBaseUnitChange , this) );
	var sd = sdERDManager.samplingDesign;
	if( sd.samplingUnitId ){
		dataProvider.setEntityId(  sd.samplingUnitId );
	}
	
	SamplingDesignStepManager.call( this, container , sdERDManager , stepNo , dataProvider );
};
BaseUnitManager.prototype 				= Object.create(SamplingDesignStepManager.prototype);
BaseUnitManager.prototype.constructor 	= BaseUnitManager;


BaseUnitManager.prototype.show = function( ){
	this.container.fadeIn();
	
	var samplingDesign = this.sdERDManager.samplingDesign;
	if( samplingDesign.samplingUnitId ){
		this.dataProvider.setEntityId(  samplingDesign.samplingUnitId );
	}
};

BaseUnitManager.prototype.onBaseUnitChange = function( entityId ){
	this.dataProvider.setEntityId( entityId );
	EventBus.dispatch( "calc.sampling-design.base-unit-change", null , entityId );
};

