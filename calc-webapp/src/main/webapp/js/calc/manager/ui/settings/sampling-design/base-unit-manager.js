/**
 * Manager for Base Unit step
 * @author M. Togna
 */
BaseUnitManager = function( container , sdERDManager , stepNo ){
	var dataProvider 	= new EntityDataProvider( $.proxy(this.baseUnitChange , this) );
	dataProvider.tableTitle = 'Base Unit: ';
	var sd = sdERDManager.samplingDesign;
	if( sd.samplingUnitId ){
		dataProvider.setEntityId(  sd.samplingUnitId );
	}
	
	SamplingDesignStepManager.call( this, container , sdERDManager , stepNo , dataProvider );
};
BaseUnitManager.prototype 				= Object.create(SamplingDesignStepManager.prototype);
BaseUnitManager.prototype.constructor 	= BaseUnitManager;


BaseUnitManager.prototype.show = function( ){
	this.container.parent().fadeIn();
	this.container.fadeIn();
	
	if( this.sd().samplingUnitId ){
		this.dataProvider.setEntityId(  this.sd().samplingUnitId );
	}
};

BaseUnitManager.prototype.baseUnitChange = function( entityId ){
	this.dataProvider.setEntityId( entityId );
	this.sd().samplingUnitId =  entityId;
	
	EventBus.dispatch( "calc.sampling-design.base-unit-change", null , entityId );
	EventBus.dispatch( "calc.sampling-design.update-connections" , null );
};

