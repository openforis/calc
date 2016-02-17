/**
 * Manager for step buttons in edit mode 
 *  @author M. Togna
 */
SamplingDesignStepButtonsManager = function( container ){
	this.container 					= container;
	
	this.twoPhasesBtn 	= new OptionButton( this.container.find('[name=twoPhases]') );
	this.twoStagesBtn 	= new OptionButton( this.container.find('[name=twoStages]') );
	this.stratifiedBtn 	= new OptionButton( this.container.find('[name=stratified]') );
	this.clusterBtn 	= new OptionButton( this.container.find('[name=cluster]') );
	
	this._samplingDesign = null;
	
	this.init();
//	this.updateView();
};

SamplingDesignStepButtonsManager.prototype.setSamplingDesign = function( samplingDesign ){
	this._samplingDesign = samplingDesign;
	this.updateView();
};

//shorcut method that returns the current samplign design beign edited
SamplingDesignStepButtonsManager.prototype.sd = function(){
//	return this.samplingDesignEditManager.samplingDesign;
	return this._samplingDesign;
};

SamplingDesignStepButtonsManager.prototype.init = function(){
	
	//2 phases
	this.twoPhasesBtn.select( $.proxy(function(){
		
		this.sd().twoPhases = true;
		this.twoStagesBtn.disable();
		
		EventBus.dispatch( "calc.sampling-design.two-phases-change", null );
	}, this) );	
	
	this.twoPhasesBtn.deselect( $.proxy(function(){
		
		this.sd().twoPhases = false;
		this.twoStagesBtn.enable();
		
		EventBus.dispatch( "calc.sampling-design.two-phases-change", null );
	}, this) );
	
	
	//2 stages
	this.twoStagesBtn.select( $.proxy(function(){
		this.sd().twoStages = true;
		
		this.twoPhasesBtn.disable();
		this.clusterBtn.disable();
		this.stratifiedBtn.deselect();
		this.stratifiedBtn.disable();
		EventBus.dispatch( "calc.sampling-design.two-stages-change", null );
	}, this) );	
	
	this.twoStagesBtn.deselect( $.proxy(function(){
		this.sd().twoStages = false;
		
		this.twoPhasesBtn.enable();
		this.clusterBtn.enable();
		this.stratifiedBtn.enable();
		EventBus.dispatch( "calc.sampling-design.two-stages-change", null );
	}, this) );
	
	//stratified
	this.stratifiedBtn.select( $.proxy(function(){
		this.sd().stratified = true;
		EventBus.dispatch( "calc.sampling-design.stratified-change", null );
	}, this) );
	this.stratifiedBtn.deselect( $.proxy(function(){
		this.sd().stratified = false;
		EventBus.dispatch( "calc.sampling-design.stratified-change", null );
	}, this) );	
	
	// cluster
	this.clusterBtn.select( $.proxy(function(){
		this.sd().cluster = true;
		this.twoStagesBtn.disable();
		EventBus.dispatch( "calc.sampling-design.cluster-change", null );
	}, this) );
	
	this.clusterBtn.deselect( $.proxy(function(){
		this.sd().cluster = false;
		this.twoStagesBtn.enable();
		EventBus.dispatch( "calc.sampling-design.cluster-change", null );
	}, this) );
};


SamplingDesignStepButtonsManager.prototype.updateView = function(){
		
	if( this.sd().twoPhases === true ){
		this.twoPhasesBtn.select();
	} else {
		this.twoPhasesBtn.deselect();
	}
	
	if( this.sd().twoStages === true ){
		this.twoStagesBtn.select();
	} else {
		this.twoStagesBtn.deselect();
	}
	
	if( this.sd().stratified === true ){
		this.stratifiedBtn.select();
	} else {
		this.stratifiedBtn.deselect();
	}
	
	if( this.sd().cluster === true ){
		this.clusterBtn.select();
	} else {
		this.clusterBtn.deselect();
	}
	
};
