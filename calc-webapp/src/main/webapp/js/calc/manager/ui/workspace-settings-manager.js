/**
 * Manager for workspace home settings section 
 */
WorkspaceSettingsManager = function( container ) {
	this.REST_URI = "rest/workspace/";
	
	this.container = $( container );
	
	// home settings ui components
	this.activeWorkspaceLabel 	= this.container.find( ".active-workspace-label" );
	
	this.exportBtn 				= this.container.find( ".export-workspace" );
	this.importBtn 				= this.container.find( ".import-workspace" );
	this.importFormSection		= this.container.find( ".import-workspace-form-section" );
	
	this.init();
};

WorkspaceSettingsManager.prototype.init = function(){
	var $this = this;
	
	var importCallback = function(){
		console.log( "imported" );
	};
	this.formFileUpload = new FormFileUpload( this.importFormSection, null, importCallback );
	this.formFileUpload.showHideForm = false;
	
	// init event handlers
	this.exportBtn.click( function(e){
		e.preventDefault();
		
		WorkspaceManager.getInstance().export();
	});
	
	this.importBtn.click( function(e){
		e.preventDefault();
		$this.formFileUpload.fileInput.click();
	});
	
};

/**
 * Update UI settings for active workspace
 */
WorkspaceSettingsManager.prototype.updateActive = function( ws ) {
	var wsLabel = ( ws ) ? ws.name : "" ;
	this.activeWorkspaceLabel.html( wsLabel );
};

WorkspaceSettingsManager.prototype.updateList = function( container ) {
	var $this = this;
	
	container 		= $( container );
	var wsList 		= container.find( ".workspace-list" );	
	var applyBtn 	= container.find( "button[name=apply-btn]" ); 
	
	/**
	 * first loads all ws 
	 */
	$.ajax({
		url : $this.REST_URI + "list.json" ,
		dataType : "json",
		method : "GET"
	}).done( function(response) {
		var list = response;
		if( list.length > 0 ) {
//			console.log( list );
			$this.selectedWorkspace = null;
			$.each( list , function( i , ws ) {
				
				var addWsButton = function(){
//					var cls = ( ws.active === true ) ? "option-btn-selected" : "option-btn";
					var btn = $( '<button class="btn option-btn workspace-btn"></button>' );
					btn.html( ws.name );
					
					var optionBtn = new OptionButton( btn );
					optionBtn.select( function(w) {						
						// deselect others
						var btns = wsList.find( "button.workspace-btn" );
						$.each( btns , function(j,btn) { 
							var opBtn = $(btn).data( "option-btn" );
							if( opBtn !== optionBtn ) {
								opBtn.deselect();
							}
						});
						
						$this.selectedWorkspace = w.id;
						UI.enable( applyBtn );
						
					} , ws );
					optionBtn.deselect( function(w){
						$this.selectedWorkspace = null;
						UI.disable( applyBtn );
					} , ws );
					
					if( ws.active === true ) {
						optionBtn.select();
					}
					
					btn.data( "option-btn" , optionBtn );
					
					wsList.append( btn );
				}
				
				addWsButton();
			});
			
			
			applyBtn.click( function(e) {
				e.preventDefault();
				UI.lock();
				WorkspaceManager.getInstance().changeActiveWorkspace( $this.selectedWorkspace , function(response){
					UI.unlock();
				});
			});
			
		}
	}).error( function() {
		Calc.error.apply( this , arguments );
	});
	
};

