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
	this.erdSd 	= this.container.find( ".sampling-design-erd-container" );
	
	this.samplingDesignERDManager	= new SamplingDesignERDManager( this.erdSd );
	
	this.samplingDesignEditManager = new SamplingDesignEditManager( this.editSd, this.samplingDesignERDManager, this.container );
	
	this.samplingDesignViewTableDataManager = new SamplingDesignViewTableDataManager( $('.sampling-design-table-data') );
	
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
	
	
	EventBus.addEventListener( "calc.sampling-design.saved", this.showView, this );
	
	EventBus.addEventListener( 'calc.page-update', function(evt, page){
		if( page == 'home' ){
			EventBus.removeEventListenersByGroup( 'calc.sampling-design' );
		}
	} , this );
};

SamplingDesignManager.prototype.showView = function(){
	WorkspaceManager.getInstance().activeWorkspace( $.proxy(function(ws){
		this.samplingDesignEditManager.hide();
		
		this.updateSDSummary();
		
		var samplingDesign = $.extend(true, {}, ws.samplingDesign );
		this.samplingDesignERDManager.show( samplingDesign );
		
	}, this) );

};

/**
 * Update view ui
 */
SamplingDesignManager.prototype.updateSDSummary = function() {
	WorkspaceManager.getInstance().activeWorkspace( $.proxy(function(ws) {
		
		this.viewSd.show();
		this.samplingDesignUI.empty();
		
		if(ws.samplingDesign) {
			
			this.samplingDesign = $.extend(true, {}, ws.samplingDesign );
			
			if(this.samplingDesign.samplingUnitId) {
				
				this.samplingUnit = ws.getEntityById(this.samplingDesign.samplingUnitId);
				this.addToSdUi( "<i>Base unit:</i><br/>" + this.samplingUnit.name );
				
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

