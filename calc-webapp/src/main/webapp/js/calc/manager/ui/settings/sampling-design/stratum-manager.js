/**
 * Manager for Stratified step
 * @author M. Togna
 */
StratumManager = function( container , sdERDManager , stepNo ){
	var dataProvider 	= new CsvFileDataProvider();
	dataProvider.tableAlias = 'Stratum';
	SamplingDesignStepManager.call( this, container , sdERDManager , stepNo , dataProvider );
	
	EventBus.addEventListener( "calc.sampling-design-stratified-change", this.update, this );
};
StratumManager.prototype 				= Object.create(SamplingDesignStepManager.prototype);
StratumManager.prototype.constructor 	= StratumManager;


StratumManager.prototype.update = function(){
	var sd = this.sdERDManager.samplingDesign;
	
	if( sd.stratified === true  ){
		this.container.fadeIn();
		if( this.sdERDManager.editMode === true && this.currentStepNo == this.stepNo ){
			this.table.highlight();
		}
	} else {
		this.container.hide();
	}

};
