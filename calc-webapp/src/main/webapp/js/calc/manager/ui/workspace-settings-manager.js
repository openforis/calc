/**
 * Manager for workspace home settings section 
 */
WorkspaceSettingsManager = function( container ) {
	this.REST_URI = "rest/workspace/";
	
	this.container = $( container );
	
	this.wsListContainer	= this.container.find( ".workspace-list" );	
	this.activateBtn 		= this.container.find( "button[name=activate-btn]" );
	this.deleteBtn 			= this.container.find( "button[name=delete-btn]" );
	this.cloneBtn 			= this.container.find( "button[name=clone-btn]" );
	
	this.init();
};

WorkspaceSettingsManager.prototype.init = function(){

	this.loadWorkspaces(); 
	
	var $this = this;
	
	this.activateBtn.click( function(e) {
		e.preventDefault();
		
		UI.lock();
		WorkspaceManager.getInstance().changeActiveWorkspace( $this.selectedWorkspace , function(response){
			UI.unlock();
		});
	});
	
	this.deleteBtn.click( function(e){
		var message = "Are you sure you want to delete this workspace? This operation cannot be undone."
		var confirmDelete = function(){ 
			WorkspaceManager.getInstance().deleteWorkspace( $this.selectedWorkspace , function(job){
				Calc.workspaceChange( function(){
					$this.loadWorkspaces();
					UI.showSuccess( "Workspace deleted" , true );
				});
			});
		};
		
		UI.showConfirm(message, confirmDelete );
	});
	
};

WorkspaceSettingsManager.prototype.loadWorkspaces = function(){
	this.wsListContainer.empty();
	
	var $this = this;
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
			$this.selectedWorkspace = null;
			$.each( list , function( i , ws ) {
				
				var addWsButton = function(){
//					var cls = ( ws.active === true ) ? "option-btn-selected" : "option-btn";
					var btn = $( '<button class="btn option-btn workspace-btn"></button>' );
					btn.html( ws.name );
					
					var optionBtn = new OptionButton( btn );
					optionBtn.select( function(w) {						
						// deselect others
						var btns = $this.wsListContainer.find( "button.workspace-btn" );
						$.each( btns , function(j,btn) { 
							var opBtn = $(btn).data( "option-btn" );
							if( opBtn !== optionBtn ) {
								opBtn.deselect();
							}
						});
						
						$this.selectedWorkspace = w.id;
						UI.enable( $this.activateBtn );
						UI.enable( $this.deleteBtn );
//						UI.enable( $this.cloneBtn );
						
					} , ws );
					optionBtn.deselect( function(w){
						$this.selectedWorkspace = null;
						UI.disable( $this.activateBtn );
						UI.disable( $this.deleteBtn );
						UI.disable( $this.cloneBtn );
						
					} , ws );
					
					if( ws.active === true ) {
						optionBtn.select();
					}
					
					btn.data( "option-btn" , optionBtn );
					
					$this.wsListContainer.append( btn );
				}
				
				addWsButton();
			});
			
			
		}
	}).error( function() {
		Calc.error.apply( this , arguments );
	});	
};
