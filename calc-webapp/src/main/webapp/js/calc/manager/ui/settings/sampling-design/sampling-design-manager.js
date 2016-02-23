/**
 * sampling design manager
 * @author Mino Togna
 */

SamplingDesignManager = function(container) {
	this.container 			= $( container );
	this.editBtn 			= this.container.find( "[name=edit-btn]" );
	this.erdSd 				= this.container.find( ".sampling-design-erd-container" );
	
	this.navManager 				= new SamplingDesignNavManager( this.container );
	this.samplingDesignERDManager	= new SamplingDesignERDManager( this.erdSd );
	this.samplingDesignEditManager 	= new SamplingDesignEditManager( this.container, this.samplingDesignERDManager, this.navManager );
	
	this.samplingDesignViewTableDataManager = new SamplingDesignViewTableDataManager( $('.sampling-design-table-data') );
	
	this.samplingDesign = {};
	
	this.init();
};

/**
 * Init method
 */
SamplingDesignManager.prototype.init = function(){
	var $this = this;
	
	// start edit
	this.editBtn.click( function(e) {
		e.preventDefault();
		$this.samplingDesignEditManager.show();
	}); 
	
	
	EventBus.addEventListener( "calc.sampling-design.saved", this.showView, this );
	EventBus.addEventListener( "calc.sampling-design.show-view", this.showView, this );
	
	EventBus.addEventListener( 'calc.page-update', function(evt, page){
		if( page == 'home' ){
			EventBus.removeEventListenersByGroup( 'calc.sampling-design' );
		}
	} , this );
};

SamplingDesignManager.prototype.showView = function(){
	WorkspaceManager.getInstance().activeWorkspace( $.proxy(function(ws){
		this.samplingDesignEditManager.hide();
		
		var samplingDesign = $.extend(true, {}, ws.samplingDesign );
		this.navManager.update( samplingDesign );
		this.samplingDesignERDManager.show( samplingDesign );
		
	}, this) );

};
