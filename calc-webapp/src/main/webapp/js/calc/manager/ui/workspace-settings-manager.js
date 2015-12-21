/**
 * Manager for workspace home settings section 
 */
WorkspaceSettingsManager = function( container ) {
	this.REST_URI = "rest/workspace/";
	
	this.container = $( container );
	
	this.wsListContainer			= this.container.find( ".workspace-list" );	
	this.wsListBtnsContainer		= this.container.find( ".ws-list-view" );	
	this.wsListView					= this.container.find( ".workspace-list-buttons" );	
	this.addWsFormContainer			= this.container.find( ".add-workspace-form" );	
	this.addWsForm					= this.addWsFormContainer.find( "form" );	
	
	this.uploadCollectFormContainer 			= this.container.find( '.upload-collect-backup' );
	this.uploadCollectFormProgressContainer 	= this.container.find(".upload-progress-section");
	//form file upload manager
	this.formFileUpload							= null; 
	
	this.activateBtn 		= this.container.find( "button[name=activate-btn]" );
	this.deleteBtn 			= this.container.find( "button[name=delete-btn]" );
	
//	this.saveBtn 			= this.addWsFormContainer.find( "button[name=save]" );
	this.cancelBtn 			= this.addWsFormContainer.find( "button[name=cancel]" );
//	this.cloneBtn 			= this.container.find( "button[name=clone-btn]" );
	
	var workspaceTreeContainer	= this.container.find( '.workspace-tree' );
	this.tree = new Tree( workspaceTreeContainer );
	
	this.workspaceManager 	= WorkspaceManager.getInstance();
	this.init();
};

WorkspaceSettingsManager.prototype.init = function(){
	var $this = this;
	
	this.addWsFormContainer.hide();
	
	this.loadWorkspaces(); 
	
	this.activateBtn.click( function(e) {
		e.preventDefault();
		
		UI.lock();
		$this.workspaceManager.changeActiveWorkspace( $this.selectedWorkspace , function(response){
			UI.unlock();
		});
	});
	
	this.deleteBtn.click( function(e){
		var message = "Are you sure you want to delete this workspace? This operation cannot be undone."
		var confirmDelete = function(){ 
			$this.workspaceManager.deleteWorkspace( $this.selectedWorkspace , function(job){
				UI.enableAll();
				Calc.workspaceChange( function(){
					$this.loadWorkspaces();
					UI.showSuccess( "Workspace deleted" , true );
				});
			});
		};
		var position = $this.wsListContainer.offset();
		position.top += 150; 
		position.left += 250;
		UI.disableAll();
		UI.showConfirm( message, confirmDelete , function(){ UI.enableAll(); } , position );
	});
	
	this.addWsForm.submit(function(e){
		e.preventDefault();
		e.stopPropagation();

		var data = $this.addWsForm.serialize();
		$this.workspaceManager.createWorkspace( data , function(response){
	    	if(response.status == "ERROR" ) {
	    		UI.Form.updateErrors( $this.addWsForm , response.errors );
	    		UI.showError("There are errors in the form. Please fix them before proceeding.", true);
	    	} else {
	    		Calc.workspaceChange( function(){
	    			$this.loadWorkspaces();
	    			$this.showList();
	    			UI.showSuccess( "Workspace created" , true );
	    		});
	    	}
			
			
		});
	});
	
	this.cancelBtn.click( function(){
		$this.showList();
		$this.loadHierarchy();
	});
	
	// upload collect backup 

	//file upload success handler
	var uploadSuccess = function ( response ) {
		if ( response.status == "OK" ) {
			var job = response.fields.job;
			
			JobManager.getInstance().start( job , function() {
				Calc.workspaceChange();
				$this.loadWorkspaces();
			});
		} else {
			var errors = response.errors;
			var message = UI.Form.getFieldErrorMessage(errors);
			UI.showError(message);
		}
	};
	
	this.formFileUpload					= new FormFileUpload( this.uploadCollectFormContainer, this.uploadCollectFormProgressContainer , uploadSuccess );
	this.formFileUpload.showHideForm 	= false;
	this.formFileUpload.beforeSerializeFunction = function(){
		$this.tree.hide();
		
		$this.uploadCollectFormContainer.find( '[name=workspaceId]' ).val( $this.selectedWorkspace );
	};
};

WorkspaceSettingsManager.prototype.showList = function(){
	this.addWsFormContainer.hide();

//	this.wsListContainer.fadeIn();
	this.wsListBtnsContainer.fadeIn();
	this.wsListView.fadeIn();
};

WorkspaceSettingsManager.prototype.loadWorkspaces = function(){
	this.wsListContainer.empty();
	this.tree.hide();
	
	UI.disable( this.wsListBtnsContainer.find( 'button' ) );
	
	var $this = this;
	/**
	 * loads all ws 
	 */
	$.ajax({
		url : $this.REST_URI + "list.json" ,
		dataType : "json",
		method : "GET"
	}).done( function(response) {
		
		$this.addAddWsButton();
		
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
						
						$this.loadHierarchy();
						
						UI.enable( $this.wsListBtnsContainer.find( 'button' ) );
						
					} , ws );
					
					optionBtn.deselect( function(w){
						$this.selectedWorkspace = null;
						$this.tree.hide();
						UI.disable( $this.wsListBtnsContainer.find( 'button' ) );
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


WorkspaceSettingsManager.prototype.loadHierarchy = function(){
	var $this = this;
	if( this.selectedWorkspace ){
		this.workspaceManager.getHierarchy( this.selectedWorkspace , function(entity){
			if( entity ){
				$this.tree.show();
				$this.tree.init( entity );
			} else {
				$this.tree.hide();
			}
		});
	}
};

/**
 * Add the add workspace button
 */
WorkspaceSettingsManager.prototype.addAddWsButton = function(){
	var addBtn = $( '<button class="btn blue-btn add-ws"></button>' );
	addBtn.append( $('<i class="fa fa-plus-square-o"></i>') );
	this.wsListContainer.append( addBtn );	
	
	var $this = this;
	addBtn.click( function(e){
		UI.Form.reset( $this.addWsForm );
		
//		$this.wsListContainer.hide();
//		$this.tree.hide();
		$this.wsListBtnsContainer.hide();
		$this.wsListView.hide();
		$this.addWsFormContainer.fadeIn();
	});
};
