/**
 * Manager for step buttons in edit mode 
 *  @author M. Togna
 */
SamplingDesignStepButtonsManager = function( container , samplingDesignEditManager ){
	this.container 					= container;
	this.samplingDesignEditManager 	= samplingDesignEditManager;
	
	this.twoPhasesBtn 	= new OptionButton( this.container.find('[name=twoPhases]') );
	this.twoStagesBtn 	= new OptionButton( this.container.find('[name=twoStages]') );
	this.stratifiedBtn 	= new OptionButton( this.container.find('[name=stratified]') );
	this.clusterBtn 	= new OptionButton( this.container.find('[name=cluster]') );
	
	this.init();
};

//shorcut method that returns the current samplign design beign edited
SamplingDesignStepButtonsManager.prototype.sd = function(){
	return this.samplingDesignEditManager.samplingDesign;
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
		EventBus.dispatch( "calc.sampling-design-stratified-change", null );
	}, this) );
	this.stratifiedBtn.deselect( $.proxy(function(){
		this.sd().stratified = false;
		EventBus.dispatch( "calc.sampling-design-stratified-change", null );
	}, this) );	
	
	// cluster
//	this.clusterBtn.select( $.proxy(function(){
//		this.samplingDesign.cluster = true;
//		this.clusterManager.show();
//		
//		this.twoStagesBtn.disable();
//	}, this) );	
//	this.clusterBtn.deselect( $.proxy(function(){
//		this.samplingDesign.cluster = false;
//		this.clusterManager.hide();
//
//		this.twoStagesBtn.enable();
//	}, this) );
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
		
//		if( this.samplingDesign.cluster === true ){
//			this.clusterBtn.select();
//		} else {
//			this.clusterBtn.deselect();
//		}
//		
//		if( this.samplingDesign.samplingUnitId ){
//			this.samplingUnitCombo.val( this.samplingUnit.id );
//			
//			var applyAreaWeighted = this.samplingDesign.applyAreaWeighted === true;
//			this.applyAreaWeighted.prop( 'checked' , applyAreaWeighted );
//			// update weight script
//		}
		
//		if( this.samplingDesign.phase1JoinSettings ) {
//			this.phase1Manager.setJoinOptions( this.samplingDesign.phase1JoinSettings );
//		}
		
//		this.aoiJoinManager.show();
};
