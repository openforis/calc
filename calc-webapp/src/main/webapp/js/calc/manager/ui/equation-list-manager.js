/**
 * Equation list manager 
 * @author Mino Togna
 */

EquationListManager = function( container ) {
	/* UI components */
	this.container = $( container );
	
	// View section
	this.viewSection	= this.container.find( ".view-section" );
	
	// upload section
	this.form 		= this.container.find( ".upload-csv-form" );
	this.uploadBtn 	= this.form.find( "[name=upload-btn]" );
	this.file 		= this.form.find( "[name=file]" );
	
	// import section
	this.importSection	= this.container.find( ".import-section" );
	this.listName		= this.importSection.find( "[name=list-name]" );
	this.filePath		= "";
	this.importBtn		= this.importSection.find( "[name=import-btn]" );
	
	
	this.init();
};


EquationListManager.prototype.init = function() {
	var $this = this;
	
	this.importSection.hide();
	this.viewSection.show();
	// bind events
	this.form.ajaxForm({
	    dataType : 'json',
	    beforeSubmit: function() {
	    	UI.lock();
	    },
	    uploadProgress: function ( event, position, total, percentComplete ) {
	    },
	    success: function ( response ) {
	    	$this.showImport( response.fields.filepath, response.fields.headers );
	    },
	    error: function () {
			Calc.error.apply( this , arguments );
	    },
	    complete: function() {
	    	// reset upload form
	    	$this.file.val("");
	    	UI.unlock();
	    }
	});	
	
	this.uploadBtn.click(function(event) {
		event.preventDefault();
		$this.file.click();
	});
	
	this.file.change(function(event) {
		event.preventDefault();
		$this.form.submit();
	});
	
	// import event handlers
	this.importBtn.click( function(e) {
		e.preventDefault();
		
		var listName = $.trim( $this.listName.val() );
		
		if( listName == "" ) {
			UI.showError( "List name cannot be blank" , true );
		} else {
			WorkspaceManager.getInstance().activeWorkspaceImportEquationList( $this.filePath , listName , function(ws) {
				$this.updateLists();
			} );
		}
		
	});
	
	// show lists
	this.updateLists();
};

EquationListManager.prototype.showImport = function( filepath ) {
	this.listName.val("");
	this.filePath= filepath ;
	
	this.viewSection.hide();
	this.importSection.fadeIn();
};
/**
 * Update equation lists UI
 */
EquationListManager.prototype.updateLists = function() {
	WorkspaceManager.getInstance().activeWorkspace( function(ws) {
		console.log( ws.equationLists );
	});
};