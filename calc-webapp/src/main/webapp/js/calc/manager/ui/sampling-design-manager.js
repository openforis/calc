/**
 * sampling design manager
 * @author Mino Togna
 */

SamplingDesignManager = function(container,  strataSection) {
	//main ui sections
	this.container = $( container );
	
	// view section
	this.viewSd = this.container.find(".view-sd");
	this.editBtn = this.viewSd.find("[name=edit-btn]");
	this.samplingDesignUI = this.viewSd.find(".sampling-design");
	
	// edit section
	this.editSd = this.container.find(".edit-sd");
	this.prevBtn = this.editSd.find("button.prev");
	this.nextBtn = this.editSd.find("button.next");
	this.editSd.hide();

	// edit ui buttons
	this.srsBtn = new OptionButton( this.container.find('[name=srs]') );
	this.systematicBtn = new OptionButton( this.container.find('[name=systematic]') );
	this.twoPhasesBtn = new OptionButton( this.container.find('[name=twoPhases]') );
	this.stratifiedBtn = new OptionButton( this.container.find('[name=stratified]') );
	this.clusterBtn = new OptionButton( this.container.find('[name=cluster]') );
	
	
	// sampling unit combo
	this.samplingUnitCombo =  this.container.find('[name=sampling-unit]').combobox();
	
	this.phase1Manager = new Phase1Manager( this.editSd.find(".phase1_section") , this);
	this.phase1Manager.hide();
	
	this.stratumManager = new StratumManager( strataSection );
	this.stratumManager.hide();
	

	
	//sampling design buttons
	// DEPRECATED
//	this.sd = this.container.find('[name=sd]') ;
//	this.srs = this.container.find('[name=sd][value=srs]') ;
//	this.systematic = this.container.find('[name=sd][value=systematic]') ;
//	this.stratified = this.container.find('[name=stratified]') ;
//	this.cluster = this.container.find('[name=cluster]') ;
//	this.twoPhases = this.container.find('[name=twoPhases]') ;
	// settings buttons
//	this.twoPhasesSettingsBtn = this.container.find( "[name=twoPhases-settings-btn]" );
	this.strataSettingsBtn = this.container.find( "[name=strata-settings-btn]" );
	
	
	
	
	
	
	
	
	
	
	// save btn
	this.saveBtn = this.container.find("[name=save-btn]");
	
	// inatance variable that contains all sampling design settings
	this.samplingDesign = {};
	
	this.init();
};

/**
 * Init method
 */
SamplingDesignManager.prototype.init = function(){
	var $this = this;
	
	// start edit
	this.editBtn.click( $.proxy( this.startEdit 
//			function(){
//		$this.updateSamplingDesign();
//		$this.updateEditView();
//		this.viewSd.hide();
//		this.startEdit();
//	} 
, this) );
	
	this.prevBtn.click( $.proxy(this.prev , this) );
	this.nextBtn.click( $.proxy(this.next , this) );
	
	this.saveBtn.click( $.proxy(this.saveSamplingDesign, this) );

	/**
	 * Sampling desing change event handlers
	 */ 
	
	// sampling unit change
	this.samplingUnitCombo.change( $.proxy(function(e){
		e.preventDefault();
		this.samplingDesign.samplingUnitId = $this.samplingUnitCombo.val();
		this.loadSamplingUnitTableInfo();
	} , this) );
	
	// simple random sampling
	this.srsBtn.select( $.proxy(function(){
		this.samplingDesign.srs = true;
		this.samplingDesign.systematic = false;
		this.systematicBtn.deselect();
	}, this) );	
	this.srsBtn.deselect( $.proxy(function(){
		this.samplingDesign.srs = false;
	}, this) );
	// systematic
	this.systematicBtn.select( $.proxy(function(){
		this.samplingDesign.srs = false;
		this.samplingDesign.systematic = true;
		this.srsBtn.deselect();
	}, this) );	
	this.systematicBtn.deselect( $.proxy(function(){
		this.samplingDesign.systematic = false;
	}, this) );
	//2 phases
	this.twoPhasesBtn.select( $.proxy(function(){
		this.samplingDesign.twoPhases = true;
		this.phase1Manager.show();
	}, this) );	
	this.twoPhasesBtn.deselect( $.proxy(function(){
		this.samplingDesign.twoPhases = false;
		this.phase1Manager.hide();
	}, this) );
	//stratified
	this.stratifiedBtn.select( $.proxy(function(){
		this.samplingDesign.stratified = true;
	}, this) );
	this.stratifiedBtn.deselect( $.proxy(function(){
		this.samplingDesign.stratified = false;
	}, this) );	
	// cluster
	this.clusterBtn.select( $.proxy(function(){
		this.samplingDesign.cluster = true;
	}, this) );	
	this.clusterBtn.deselect( $.proxy(function(){
		this.samplingDesign.cluster = false;
	}, this) );
	
	
	WorkspaceManager.getInstance().activeWorkspace(function(ws){
		//refresh sampling unit select.
		$this.samplingUnitCombo.data(ws.entities, 'id','name');
		
		//if sampling design is defined for active workspace update ui
		$this.updateSamplingDesign();
	});
	
//	// main sd strategy
//	this.sd.change( $.proxy(function(){
//		this.samplingDesign.srs = this.srs.prop('checked');
//		this.samplingDesign.systematic = this.systematic.prop('checked');
//	}, this) );
//	
//	//2 phases
//	this.twoPhases.change( $.proxy(function(){
//		this.samplingDesign.twoPhases = this.twoPhases.prop('checked');;
//	}, this) );	
//	//stratified
//	this.stratified.change( $.proxy(function(){
////		console.log( this.stratified.prop('checked') );
//		this.samplingDesign.stratified = this.stratified.prop('checked');
//	}, this) );
//	
//	// cluster
//	this.cluster.change( $.proxy(function(){
//		this.samplingDesign.cluster = this.cluster.prop('checked');;
//	}, this) );	
	

	
	// settings buttons click
//	this.twoPhasesSettingsBtn.click(function(e){
//		e.preventDefault();
//		$this.container.hide(20);
//		setTimeout( function(e) { 
//			$this.phase1Manager.show();
//		} , 50 );
//	});
//	this.strataSettingsBtn.click(function(e){
//		e.preventDefault();
//		$this.container.hide(20);
//		setTimeout( function(e) { 
//			$this.stratumManager.show();
//		} , 50 );
//	});
	
	
	
	/**
	 * External managers event handlers
	 */
//	this.phase1Manager.save = $.proxy( function(){
//		this.samplingDesign.twoPhases = true;
//		this.twoPhases.prop('checked', true);
//		this.phase1Manager.hide();
//		this.container.fadeIn(200);
//	} , this );
//	this.phase1Manager.cancel = $.proxy( function(){
//		this.samplingDesign.twoPhases = false;
////		this.twoPhases.removeProp('checked');
//		this.twoPhases.prop('checked', false);
//		this.phase1Manager.hide();
//		this.container.fadeIn(200);
//	} , this );
	
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

/**
 * Start showing the edit section
 */
SamplingDesignManager.prototype.startEdit = function(){
	this.updateEditView();
	this.viewSd.hide();
	
	var steps = this.editSd.find(".step");
	this.step = 0;
	this.stepMax = steps.length - 1;
	// hide steps
	steps.hide();
	this.editSd.fadeIn(200);
	this.showStep(this.step);
};
/**
 * Show step section
 * @param step
 */
SamplingDesignManager.prototype.showStep = function(step) {
	this.editSd.find(".step").hide();
	this.editSd.find(".step-"+step).fadeIn(200);
	this.updateEditNavigationBtns();
};
/**
 * Move ui to previous edit section
 */
SamplingDesignManager.prototype.prev = function(){
	if( this.step != 0){
		this.step --;
		this.showStep(this.step);
	}
};
/**
 * Move to next edit section
 */
SamplingDesignManager.prototype.next = function(){
	var validate =  this["validateStep"+this.step] ;
	if( validate ){
		var valid = $.proxy( validate, this )();
		if( valid ){
			this.step ++ ;
			this.showStep(this.step);
		}
	} else {
		this.step ++ ;
		this.showStep(this.step);
	}
};
SamplingDesignManager.prototype.updateEditNavigationBtns = function(){
	this.step == 0 ? UI.disable( this.prevBtn ) : UI.enable( this.prevBtn );
	this.step == this.stepMax ? UI.disable( this.nextBtn ) : UI.enable( this.nextBtn );
};
/**
 * Sampling unit validation (step 0)
 */
SamplingDesignManager.prototype.validateStep0 = function(){
	if( this.samplingDesign.samplingUnitId ){
		return true;
	} else {
		UI.showError("Select a valid sampling unit", false);
		return false;
	}
};
/**
 * Two phases validation (step 2)
 */
SamplingDesignManager.prototype.validateStep2 = function(){
	var $this = this;
	if( this.samplingDesign.twoPhases === true ){
		
		var valid = false;
		WorkspaceManager.getInstance().activeWorkspace( function(ws){
			if (! ws.phase1PlotTable ){
				UI.showError("Import a valid csv file", false);
				valid = false;
			} else {
				valid = $this.phase1Manager.validate();
				if(valid){
					$this.samplingDesign.phase1JoinSettings = $this.phase1Manager.joinOptions();
				}
//				valid = true;
			}
		} );
		return valid;
	} else {
		return true;
	}
};
SamplingDesignManager.prototype.updateSamplingDesign = function() {
	
	WorkspaceManager.getInstance().activeWorkspace( $.proxy(function(ws) {
		
		this.editSd.hide();
		this.viewSd.show(10);
		this.samplingDesignUI.empty();
		
		if(ws.samplingDesign) {
			this.samplingDesign = $.extend( {}, ws.samplingDesign );
			
//				ws.samplingDesign;
//			$this.setSamplingDesign(sd);
			if(this.samplingDesign.samplingUnitId){
				// edit form properties
				this.samplingUnit = ws.getEntityById(this.samplingDesign.samplingUnitId);
				this.loadSamplingUnitTableInfo( $.proxy(function(){
					
					// view properties
					if( this.samplingDesign.srs === true ){
						this.addToSdUi("Srs");
//						this.srsBtn.select();
					} else {
//						this.srsBtn.deselect();
					}
					
					if( this.samplingDesign.systematic === true ){
						this.addToSdUi("Systematic");
//						this.systematicBtn.select();
					} else {
//						this.systematicBtn.deselect(); 
					}
					
					if( this.samplingDesign.twoPhases === true ){
						this.addToSdUi("Two phases");
//						this.twoPhasesBtn.select();
//						this.phase1Manager.show();
					} else {
//						this.twoPhasesBtn.deselect();
//						this.phase1Manager.hide();
					}
					
					if( this.samplingDesign.stratified === true ){
						this.addToSdUi("Stratified");
					}
					if( this.samplingDesign.cluster === true ){
						this.addToSdUi("Cluster");
					}
					// edit form properties
//					this.samplingUnitCombo.val( this.samplingUnit.id );
					
//					if( this.samplingDesign.phase1JoinSettings ) {
//						this.phase1Manager.setJoinOptions( $.parseJSON( this.samplingDesign.phase1JoinSettings ) );
//					}
				} , this));
				
			}
		} else {
//			this.disableButtons();
			this.samplingDesign = {};
			this.samplingUnit = {};
		}	
		
	} , this) );
};

SamplingDesignManager.prototype.updateEditView = function(){
	// view properties
	if( this.samplingDesign.srs === true ){
//		this.addToSdUi("Srs");
		this.srsBtn.select();
	} else {
		this.srsBtn.deselect();
	}
	
	if( this.samplingDesign.systematic === true ){
//		this.addToSdUi("Systematic");
		this.systematicBtn.select();
	} else {
		this.systematicBtn.deselect(); 
	}
	
	if( this.samplingDesign.twoPhases === true ){
//		this.addToSdUi("Two phases");
		this.twoPhasesBtn.select();
		this.phase1Manager.show();
	} else {
		this.twoPhasesBtn.deselect();
		this.phase1Manager.hide();
	}
	
	if( this.samplingDesign.stratified === true ){
//		this.addToSdUi("Stratified");
	}
	if( this.samplingDesign.cluster === true ){
//		this.addToSdUi("Cluster");
	}
	// edit form properties
	this.samplingUnitCombo.val( this.samplingUnit.id );
	
	if( this.samplingDesign.phase1JoinSettings ) {
		this.phase1Manager.setJoinOptions( $.parseJSON( this.samplingDesign.phase1JoinSettings ) );
	}
};

SamplingDesignManager.prototype.addToSdUi = function(text) {
	var btn = $( '<button class="btn option-btn-selected"></button>' );
	btn.html( text );
	this.samplingDesignUI.append( btn );
	UI.disable( btn );
};

/**
 * Load plot data table info
 */
SamplingDesignManager.prototype.loadSamplingUnitTableInfo = function(callback){
	if( this.samplingDesign.samplingUnitId ) {
		WorkspaceManager.getInstance().activeWorkspace( $.proxy( function(ws){
			var entity = ws.getEntityById( this.samplingDesign.samplingUnitId );
			// load sampling unit table info
			new TableDataProvider(ws.dataSchema , entity.name ).tableInfo( $.proxy( function(response){
				this.samplingUnitTableInfo = response;
				if(callback){
					callback();
				}
			} , this ) );
		} , this ) );
	}
};
//SamplingDesignManager.prototype.disableButtons = function(){
//	var $this = this;
//	setTimeout( function(e){
//		$this.container.find("div.option:not(:first-child)").fadeTo( 200, 0.2 );
//	} , 50 );
//	
//	UI.disable( this.sd );
//	UI.disable( this.stratified );
//	UI.disable( this.cluster );
//	UI.disable( this.twoPhases );
////	UI.disable( this.saveBtn );
//};
//
//SamplingDesignManager.prototype.enableButtons = function(){
//	var $this = this;
//	setTimeout( function(e){
//		$this.container.find("div.option:not(:first-child)").fadeTo( 200, 1 );
//	} , 50 );
//	
//	UI.enable( this.sd );
//	UI.enable( this.stratified );
//	UI.enable( this.cluster );
//	UI.enable( this.twoPhases );
////	UI.disable( this.saveBtn );
//};

SamplingDesignManager.prototype.saveSamplingDesign = function(){
//	console.log("save");
//	if( this.samplingDesign.samplingUnitId ){
		// validate
		WorkspaceManager.getInstance().activeWorkspaceSetSamplingDesign( this.samplingDesign, $.proxy( function(ws){
//			var sd = ws.samplingDesign;
			this.updateSamplingDesign();
		} , this) );
		
//	} else {
		
//	}
};