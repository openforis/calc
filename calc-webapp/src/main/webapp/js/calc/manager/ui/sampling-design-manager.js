/**
 * sampling design manager
 * @author Mino Togna
 */

SamplingDesignManager = function(container, phase1Section, strataSection) {
	//main ui sections
	this.container = $( container );
//	this.phase1Section = $( phase1Section );
	this.phase1Manager = new Phase1Manager( phase1Section );
	this.phase1Manager.hide();
	
//	this.strataSection = $( strataSection );
//	this.strataSection.hide();
	this.stratumManager = new StratumManager( strataSection );
	this.stratumManager.hide();
	
	// sampling unit combo
	this.samplingUnitCombo =  this.container.find('[name=sampling-unit]').combobox();

	//sampling design buttons
	this.sd = this.container.find('[name=sd]') ;
	this.srs = this.container.find('[name=sd][value=srs]') ;
	this.systematic = this.container.find('[name=sd][value=systematic]') ;
	this.stratified = this.container.find('[name=stratified]') ;
	this.cluster = this.container.find('[name=cluster]') ;
	this.twoPhases = this.container.find('[name=twoPhases]') ;
	
	// settings buttons
	this.twoPhasesSettingsBtn = this.container.find( "[name=twoPhases-settings-btn]" );
	this.strataSettingsBtn = this.container.find( "[name=strata-settings-btn]" );
	
//	this.srsBtn = new OptionButton( this.container.find('[name=srs]') );
//	this.systematicBtn = new OptionButton( this.container.find('[name=systematic]') );
//	this.stratifiedBtn = new OptionButton( this.container.find('[name=stratified]') );
//	this.clusterBtn = new OptionButton( this.container.find('[name=cluster]') );
//	this.twoPhasesBtn = new OptionButton( this.container.find('[name=twoPhases]') );
	
	// save btn
	this.saveBtn = this.container.find("[name=save-btn]");
	
	// inatance variable that contains all sampling design settings
	this.samplingDesign = {};
	
	this.init();
};

SamplingDesignManager.prototype.init = function(){
	var $this = this;
	
	WorkspaceManager.getInstance().activeWorkspace(function(ws){
		//refresh sampling unit select.
		$this.samplingUnitCombo.data(ws.entities, 'id','name');
		
		//if sampling design is defined for active workspace update ui
		$this.updateSamplingDesign(ws);
//		if(ws.samplingDesign) {
//			var sd = ws.samplingDesign;
//			$this.setSamplingDesign(sd);
//		} else {
//			$this.disableButtons();
//		}
		
	});
	
	/**
	 * Event handlers
	 */ 
	// sampling unit change
	this.samplingUnitCombo.change( $.proxy(function(e){

		e.preventDefault();
		this.samplingDesign.samplingUnitId = $this.samplingUnitCombo.val();
		
		if ( ! this.samplingDesign.samplingUnitId ){
			this.disableButtons();
		} else {
			this.enableButtons();
		}
		
	} , this) );
	
	// main sd strategy
	this.sd.change( $.proxy(function(){
		this.samplingDesign.srs = this.srs.prop('checked');
		this.samplingDesign.systematic = this.systematic.prop('checked');
	}, this) );
	
	//2 phases
	this.twoPhases.change( $.proxy(function(){
		this.samplingDesign.twoPhases = this.twoPhases.prop('checked');;
	}, this) );	
	//stratified
	this.stratified.change( $.proxy(function(){
//		console.log( this.stratified.prop('checked') );
		this.samplingDesign.stratified = this.stratified.prop('checked');
	}, this) );
	
	// cluster
	this.cluster.change( $.proxy(function(){
		this.samplingDesign.cluster = this.cluster.prop('checked');;
	}, this) );	

	
	// settings buttons click
	this.twoPhasesSettingsBtn.click(function(e){
		e.preventDefault();
		$this.container.hide(20);
		setTimeout( function(e) { 
			$this.phase1Manager.show();
		} , 50 );
	});
	this.strataSettingsBtn.click(function(e){
		e.preventDefault();
		$this.container.hide(20);
		setTimeout( function(e) { 
			$this.stratumManager.show();
		} , 50 );
	});
	
	this.saveBtn.click( $.proxy( $this.saveSamplingDesign, this ) );
	
	
	this.phase1Manager.save = $.proxy( function(){
		this.samplingDesign.twoPhases = true;
		this.twoPhases.prop('checked', true);
		this.phase1Manager.hide();
		this.container.fadeIn(200);
	} , this );
	this.phase1Manager.cancel = $.proxy( function(){
		this.samplingDesign.twoPhases = false;
//		this.twoPhases.removeProp('checked');
		this.twoPhases.prop('checked', false);
		this.phase1Manager.hide();
		this.container.fadeIn(200);
	} , this );
	
	this.stratumManager.save = $.proxy( function(){
		this.samplingDesign.stratified = true;
		this.stratified.prop('checked', true);
		this.stratumManager.hide();
		this.container.fadeIn(200);
	} , this );
	this.stratumManager.cancel = $.proxy( function(){
		this.samplingDesign.stratified = false;
//		this.twoPhases.removeProp('checked');
		this.stratified.prop('checked', false);
		this.stratumManager.hide();
		this.container.fadeIn(200);
	} , this );
};

SamplingDesignManager.prototype.updateSamplingDesign = function(ws){
	if(ws.samplingDesign) {
		this.samplingDesign = ws.samplingDesign;
//		$this.setSamplingDesign(sd);
		if(this.samplingDesign.samplingUnitId){
			var entity = ws.getEntityById(this.samplingDesign.samplingUnitId);
			if(entity){
				this.samplingUnitCombo.val(entity.id);
			}
			this.srs.prop('checked', this.samplingDesign.srs === true );
			this.systematic.prop('checked', this.samplingDesign.systematic === true );
			this.stratified.prop('checked', this.samplingDesign.stratified === true );
			this.cluster.prop('checked', this.samplingDesign.cluster === true );
			this.twoPhases.prop('checked', this.samplingDesign.twoPhases === true );
		}
	} else {
		$this.disableButtons();
	}	
//	WorkspaceManager.getInstance().activeWorkspace($.proxy(function(ws){
//		this.samplingDesign = sd;
//		console.log(this);
//	} , this) );
};

SamplingDesignManager.prototype.disableButtons = function(){
	var $this = this;
	setTimeout( function(e){
		$this.container.find("div.option:not(:first-child)").fadeTo( 200, 0.2 );
	} , 50 );
	
	UI.disable( this.sd );
	UI.disable( this.stratified );
	UI.disable( this.cluster );
	UI.disable( this.twoPhases );
//	UI.disable( this.saveBtn );
};

SamplingDesignManager.prototype.enableButtons = function(){
	var $this = this;
	setTimeout( function(e){
		$this.container.find("div.option:not(:first-child)").fadeTo( 200, 1 );
	} , 50 );
	
	UI.enable( this.sd );
	UI.enable( this.stratified );
	UI.enable( this.cluster );
	UI.enable( this.twoPhases );
//	UI.disable( this.saveBtn );
};

SamplingDesignManager.prototype.saveSamplingDesign = function(){
	console.log("save");
//	if( this.samplingDesign.samplingUnitId ){
		// validate
		WorkspaceManager.getInstance().activeWorkspaceSetSamplingDesign( this.samplingDesign, $.proxy( function(ws){
//			var sd = ws.samplingDesign;
			this.updateSamplingDesign(ws);
		} , this) );
		
//	} else {
		
//	}
};