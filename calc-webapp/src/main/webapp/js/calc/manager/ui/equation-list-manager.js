/**
 * Equation list manager 
 * @author Mino Togna
 */

EquationListManager = function( container ) {
	/* UI components */
	this.container = $( container );
	
	this.viewSection	= this.container.find( ".view-section" );
	this.importSection	= this.container.find( ".import-section" );
	
	// upload section
	this.form 		= this.container.find( ".upload-csv-form" );
	this.uploadBtn 	= this.form.find( "[name=upload-btn]" );
	this.file 		= this.form.find( "[name=file]" );
	
	
	this.init();
};


EquationListManager.prototype.init = function() {
	var $this = this;
	
	this.importSection.hide();
	this.viewSection.show();
	// bind events
	this.form.ajaxForm( {
	    dataType : 'json',
	    beforeSubmit: function() {
	    	UI.lock();
	    },
	    uploadProgress: function ( event, position, total, percentComplete ) {
	    },
	    success: function ( response ) {
	    	console.log( response );
	    	//$this.showImport( response.fields.filepath, response.fields.headers );
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
	
	// show lists
	this.updateLists();
};

/**
 * Update equation lists UI
 */
EquationListManager.prototype.updateLists = function() {
	WorkspaceManager.getInstance().activeWorkspace( function(ws){
		console.log( ws.equationLists );
	});
};