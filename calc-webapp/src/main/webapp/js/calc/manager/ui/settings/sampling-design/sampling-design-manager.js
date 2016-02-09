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
	this.editErdSd 	= this.container.find( ".sampling-design-erd-edit" );
	
	this.samplingDesignERDManager	= new SamplingDesignERDManager( this.editErdSd );
	
	this.samplingDesignEditManager = new SamplingDesignEditManager( this.editSd, this.samplingDesignERDManager, this.container );
	this.samplingDesignEditManager.hide();
	
	
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
	this.editBtn.click( function(e) {
		e.preventDefault();
		$this.viewSd.hide();
		$this.samplingDesignEditManager.show();
	}); 
	
	// populate sampling unit select with workspace entities and update view ui 
	WorkspaceManager.getInstance().activeWorkspace( function(ws){
		//refresh sampling unit select.
//		$this.samplingUnitCombo.data( ws.entities, 'id','name' );
		
		//if sampling design is defined for active workspace update ui
		$this.updateSamplingDesign();
	});
	
	var showView = function(){
		var $this = this;
		WorkspaceManager.getInstance().activeWorkspace( function(ws){
			//refresh sampling unit select.
//			$this.samplingUnitCombo.data( ws.entities, 'id','name' );
			
			//if sampling design is defined for active workspace update ui
			$this.updateSamplingDesign();
			$this.samplingDesignEditManager.hide();
			
			var samplingDesign = $.extend(true, {}, ws.samplingDesign );
			$this.samplingDesignERDManager.show( samplingDesign );
			
		});
	};
	
	EventBus.addEventListener( "calc.sampling-design.saved", showView, this );
	$.proxy( showView , this )();
};


/**
 * Update view ui
 */
SamplingDesignManager.prototype.updateSamplingDesign = function() {
	
	WorkspaceManager.getInstance().activeWorkspace( $.proxy(function(ws) {
		
//		this.editSd.hide();
		this.viewSd.show();
		this.samplingDesignUI.empty();
		
		if(ws.samplingDesign) {
//			UI.lock();
			
			this.samplingDesign = $.extend(true, {}, ws.samplingDesign );
			
			if(this.samplingDesign.samplingUnitId) {
				
				this.samplingUnit = ws.getEntityById(this.samplingDesign.samplingUnitId);
//				this.loadSamplingUnitTableInfo( $.proxy(function(){
					// todo move loading info tables before edit. not necessary here
//					this.loadPhase1TableInfo();
//					this.loadPrimarySUTableInfo();
					
					this.addToSdUi( "<i>Base unit:</i><br/>" + this.samplingUnit.name );
					
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
					if( this.samplingDesign.twoStages === true ){
						this.addToSdUi("Two stages w/ SRS");
					}
					
					if( this.samplingDesign.stratified === true ){
						this.addToSdUi("Stratified");
					}
					if( this.samplingDesign.cluster === true ){
						this.addToSdUi("Cluster");
					}
					
					
//					UI.unlock();
					
//				} , this));
				
			}
			
		} else {
			this.samplingDesign = {};
			this.samplingUnit = {};
		}	
		
	} , this) );
};

SamplingDesignManager.prototype.addToSdUi = function(text) {
	var btn = $( '<button class="btn option-btn-selected"></button>' );
	btn.html( text );
	this.samplingDesignUI.append( btn );
	UI.disable( btn );
};

