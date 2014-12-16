/**
 * sampling design manager
 * @author Mino Togna
 */

SamplingDesignManager = function(container) {
	//main ui sections
	this.container 			= $( container );
	
	// view section
	this.viewSd 			= this.container.find( ".view-sd" );
	this.editBtn 			= this.viewSd.find( "[name=edit-btn]" );
	this.samplingDesignUI 	= this.viewSd.find( ".sampling-design" );
	
	// edit section
	this.editSd 	= this.container.find( ".edit-sd" );
	this.prevBtn 	= this.editSd.find( "button.prev" );
	this.nextBtn 	= this.editSd.find( "button.next" );
	this.editSd.hide();
	
	// edit ui buttons
//	this.srsBtn 		= new OptionButton( this.container.find('[name=srs]') );
//	this.systematicBtn 	= new OptionButton( this.container.find('[name=systematic]') );
	this.twoPhasesBtn 	= new OptionButton( this.container.find('[name=twoPhases]') );
	this.stratifiedBtn 	= new OptionButton( this.container.find('[name=stratified]') );
	this.clusterBtn 	= new OptionButton( this.container.find('[name=cluster]') );
	// save btn
	this.saveBtn 		= this.container.find( "[name=save-btn]" );
	
	// sampling unit combo
	this.samplingUnitCombo =  this.container.find( '[name=sampling-unit]' ).combobox();
	
	// additional managers used in the edit phase
	this.phase1Manager = new Phase1Manager( this.editSd.find(".phase1_section") , this);
	this.phase1Manager.hide();
	
	this.stratumManager = new StratumManager( this.editSd.find(".strata_section") , this );
	this.stratumManager.hide();
	
	this.clusterManager = new ClusterManager( this.editSd.find(".cluster_section") , this);
	this.clusterManager.hide();

	this.aoiJoinManager = new AoiJoinManager( this.editSd.find(".aoi-join-section") , this );
	this.aoiJoinManager.hide();
	
	// r script
	this.weightInput 	= this.editSd.find( "[name=sampling-unit-weigth]" );
	this.weightScript 	= new REditor( this.weightInput.attr('id') );
	this.weightScript.customVariables.push( "weight" );
	
//	this.weightScript = new RScript( this.weightInput );
	
	// inatance variable that contains all sampling design settings
	this.samplingDesign = {};
	
	this.init();
};

/**
 * Init method
 */
SamplingDesignManager.prototype.init = function(){
	var $this = this;
	
	/**
	 * main ui button handlers
	 */
	// start edit
	this.editBtn.click( $.proxy( this.startEdit , this) ); 
	
	this.prevBtn.click( $.proxy(this.prev , this) );
	this.nextBtn.click( $.proxy(this.next , this) );
	
	this.saveBtn.click( $.proxy(this.saveSamplingDesign, this) );

	/**
	 * Sampling desing change event handlers (edit phase)
	 */ 
	// sampling unit change
	this.samplingUnitCombo.change( $.proxy(function(e){
		e.preventDefault();
		this.samplingDesign.samplingUnitId = $this.samplingUnitCombo.val();
		WorkspaceManager.getInstance().activeWorkspace( $.proxy(function(ws){
			this.weightScript.entity = ws.getEntityById( this.samplingDesign.samplingUnitId );
		} , this ) );
		this.loadSamplingUnitTableInfo();
	} , this) );
	
	// simple random sampling
//	this.srsBtn.select( $.proxy(function(){
//		this.samplingDesign.srs = true;
//		this.samplingDesign.systematic = false;
//		this.systematicBtn.deselect();
//	}, this) );	
//	this.srsBtn.deselect( $.proxy(function(){
//		this.samplingDesign.srs = false;
//	}, this) );
	// systematic
//	this.systematicBtn.select( $.proxy(function(){
//		this.samplingDesign.srs = false;
//		this.samplingDesign.systematic = true;
//		this.srsBtn.deselect();
//	}, this) );	
//	this.systematicBtn.deselect( $.proxy(function(){
//		this.samplingDesign.systematic = false;
//	}, this) );
	
	//2 phases
	this.twoPhasesBtn.select( $.proxy(function(){
		this.samplingDesign.twoPhases = true;
		this.phase1Manager.show();
		
	}, this) );	
	this.twoPhasesBtn.deselect( $.proxy(function(){
		this.samplingDesign.twoPhases = false;
		this.phase1Manager.hide();
		
//		this.stratifiedBtn.deselect();
//		this.clusterBtn.deselect();
//		this.samplingDesign.aoiJoinSettings = {};
	}, this) );
	
	//stratified
	this.stratifiedBtn.select( $.proxy(function(){
		this.samplingDesign.stratified = true;
		this.stratumManager.show();
	}, this) );
	this.stratifiedBtn.deselect( $.proxy(function(){
		this.samplingDesign.stratified = false;
		this.stratumManager.hide();
	}, this) );	
	
	// cluster
	this.clusterBtn.select( $.proxy(function(){
		this.samplingDesign.cluster = true;
		this.clusterManager.show();
	}, this) );	
	this.clusterBtn.deselect( $.proxy(function(){
		this.samplingDesign.cluster = false;
		this.clusterManager.hide();
	}, this) );
	
	// populate sampling unit select with workspace entities and update view ui 
	WorkspaceManager.getInstance().activeWorkspace( function(ws){
		//refresh sampling unit select.
		$this.samplingUnitCombo.data( ws.entities, 'id','name' );
		
		//if sampling design is defined for active workspace update ui
		$this.updateSamplingDesign();
	});
	
};

/**
 * ======================================
 *  	Edit sampling design methods
 * ======================================
 */

/**
 * Start showing the edit section
 */
SamplingDesignManager.prototype.startEdit = function() {
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
	this.editSd.find(".step-"+step).fadeIn( 200 );
	
	if( step == this.stepMax ){
		this.weightScript.refresh();
	}
	
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
	var validate =  this[ "validateStep" + this.step ] ;
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
/**
 * update edit navigation buttons
 */
SamplingDesignManager.prototype.updateEditNavigationBtns = function(){
	this.step == 0 ? UI.disable( this.prevBtn ) : UI.enable( this.prevBtn );
	this.step == this.stepMax ? UI.disable( this.nextBtn ) : UI.enable( this.nextBtn );
};


/**
 * Step validation methods
 */

/**
 * Sampling unit validation
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
 * Two phases validation
 */
SamplingDesignManager.prototype.validateStep1 = function(){
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
				
				$this.loadPhase1TableInfo( function() {
					$this.aoiJoinManager.updateJoinColumn();
				} );
//				valid = true;
			}
		} );
		return valid;
	} else {
		$this.aoiJoinManager.updateJoinColumn();
		return true;
	}
};
/**
 * Validate strata settings
 */
SamplingDesignManager.prototype.validateStep2 = function(){
	var $this = this;
	if( this.samplingDesign.stratified === true ){
		
		var valid = false;
		WorkspaceManager.getInstance().activeWorkspace( function(ws){
			if (! ws.strata || ws.strata.length <= 0) {
				UI.showError("Import a valid csv file", false);
				valid = false;
			} else {
				valid = $this.stratumManager.validate();
				if(valid) {
					$this.samplingDesign.stratumJoinSettings = $this.stratumManager.joinOptions();
				}
			}
		} );
		return valid;
		
	} else {
		return true;
	}
};

/**
 * Validate cluster settings
 */
SamplingDesignManager.prototype.validateStep3 = function(){
	var valid = true;
	
	if( this.samplingDesign.cluster === true ){
		valid =  this.clusterManager.validate();
		if(valid) {
			this.samplingDesign.clusterColumnSettings = this.clusterManager.joinOptions();
		}
	} 
	
	return valid;
	
};

/**
 * Validate aoi column 
 */
SamplingDesignManager.prototype.validateStep4 = function(){
	var	valid =  this.aoiJoinManager.validate();
	if(valid) {
		this.samplingDesign.aoiJoinSettings = this.aoiJoinManager.joinOptions();
	}
	return valid;
};

/**
 * Validate weight script 
 */
SamplingDesignManager.prototype.validateStep5 = function(){
	var script = this.weightScript.getValue();
	if( StringUtils.isBlank(script) ){
		UI.showError("Sampling unit weigth script must be filled", true);
		return false;
	} else {
//		this.samplingDesign.samplingUnitWeightScript = this.weightInput.val();
		this.samplingDesign.samplingUnitWeightScript = this.weightScript.getValue();
		return true;
	}
};


/**
 * Update view ui
 */
SamplingDesignManager.prototype.updateSamplingDesign = function() {
	
	WorkspaceManager.getInstance().activeWorkspace( $.proxy(function(ws) {
		
		this.editSd.hide();
		this.viewSd.show(10);
		this.samplingDesignUI.empty();
		
		if(ws.samplingDesign) {
			UI.lock();
			
			this.samplingDesign = $.extend( {}, ws.samplingDesign );
			
			if(this.samplingDesign.samplingUnitId) {
				this.samplingUnit = ws.getEntityById(this.samplingDesign.samplingUnitId);
				this.loadSamplingUnitTableInfo( $.proxy(function(){
					// todo move loading info tables before edit. not necessary here
					this.loadPhase1TableInfo();
					
					this.addToSdUi( "Sampling unit<br/>" + this.samplingUnit.name );
					
					// view properties
//					if( this.samplingDesign.srs === true ){
//						this.addToSdUi("Srs");
//					}
//					if( this.samplingDesign.systematic === true ){
//						this.addToSdUi("Systematic");
//					}
					if( this.samplingDesign.twoPhases === true ){
						this.addToSdUi("Two phases");
					}
					
					if( this.samplingDesign.stratified === true ){
						this.addToSdUi("Stratified");
					}
					if( this.samplingDesign.cluster === true ){
						this.addToSdUi("Cluster");
					}
					
					
					UI.unlock();
					
				} , this));
				
			}
			
		} else {
			this.samplingDesign = {};
			this.samplingUnit = {};
		}	
		
	} , this) );
};
/**
 * Update edit form with current sampling design
 */
SamplingDesignManager.prototype.updateEditView = function(){
//	if( this.samplingDesign.srs === true ){
//		this.srsBtn.select();
//	} else {
//		this.srsBtn.deselect();
//	}
	
//	if( this.samplingDesign.systematic === true ){
//		this.systematicBtn.select();
//	} else {
//		this.systematicBtn.deselect(); 
//	}
	
	if( this.samplingDesign.twoPhases === true ){
		this.twoPhasesBtn.select();
	} else {
		this.twoPhasesBtn.deselect();
	}
	
	if( this.samplingDesign.stratified === true ){
		this.stratifiedBtn.select();
	} else {
		this.stratifiedBtn.deselect();
	}
	
	if( this.samplingDesign.cluster === true ){
		this.clusterBtn.select();
	} else {
		this.clusterBtn.deselect();
	}
	
	if( this.samplingDesign.samplingUnitId ){
		this.samplingUnitCombo.val( this.samplingUnit.id );
		// update weight script
		WorkspaceManager.getInstance().activeWorkspace( $.proxy(function(ws){
			this.weightScript.entity = ws.getEntityById( this.samplingDesign.samplingUnitId );
		} , this ) );
		
		if( this.samplingDesign.samplingUnitWeightScript ){
//			this.weightInput.html( this.samplingDesign.samplingUnitWeightScript );
			this.weightScript.setValue( this.samplingDesign.samplingUnitWeightScript );
		}
	}
	
	if( this.samplingDesign.phase1JoinSettings ) {
		this.phase1Manager.setJoinOptions( this.samplingDesign.phase1JoinSettings );
	}
	
	this.aoiJoinManager.show();
};

SamplingDesignManager.prototype.addToSdUi = function(text) {
	var btn = $( '<button class="btn option-btn-selected"></button>' );
	btn.html( text );
	this.samplingDesignUI.append( btn );
	UI.disable( btn );
};

/**
 * Load sampling unit data table info
 */
SamplingDesignManager.prototype.loadSamplingUnitTableInfo = function(callback){
	if( this.samplingDesign.samplingUnitId ) {
		WorkspaceManager.getInstance().activeWorkspace( $.proxy( function(ws){
			var entity = ws.getEntityById( this.samplingDesign.samplingUnitId );
			
			var tableInfo = function(){
				this.table 			= entity.name;
				this.fields 		= {};
				this.fields.table 	= entity.name;
				this.fields.columns = [];
				
				var vars = entity.hierarchyVariables();
				for( var i in vars ){
					var variable = vars[ i ];
					this.fields.columns.push( { 'column_name' : variable.name } );
				}
			};
			
			this.samplingUnitTableInfo = new tableInfo();
			Utils.applyFunction( callback );
			
			// load sampling unit table info
//			new TableDataProvider( ws.inputSchema , entity.name +"_view" ).tableInfo( $.proxy( function(response){
//				this.samplingUnitTableInfo = response;
//				if(callback){
//					callback();
//				}
//			} , this ) );
			
		} , this ) );
	}
};

/**
 * load phase 1 table info
 */
SamplingDesignManager.prototype.loadPhase1TableInfo = function(callback){
	var $this  = this;
	WorkspaceManager.getInstance().activeWorkspace(function(ws){
		if( ws.phase1PlotTable ) {

			new TableDataProvider( "calc" , ws.phase1PlotTable ).tableInfo( function(response) {
				$this.phase1TableInfo = response;
				Utils.applyFunction( callback );
			});
			
		}
	});
};

/**
 * Save current sampling design for the current workspace
 */
SamplingDesignManager.prototype.saveSamplingDesign = function(){
	var $this = this;
	var validate = this[ "validateStep" + this.stepMax ] ;
	if( !validate || $.proxy(validate, this)() ){
		
		WorkspaceManager.getInstance().activeWorkspaceSetSamplingDesign( this.samplingDesign, $.proxy( function(job) {
			var complete = function(){
				$this.updateSamplingDesign();
				Calc.updateButtonStatus();
			};
			JobManager.getInstance().start( job , complete, false );
//			JobManager.getInstance().checkJobStatus(function(){
//				$this.updateSamplingDesign();
//				Calc.updateButtonStatus();
//			});
		} , this) );
		
	}

};