/**
 * manager for phase 1 plots manager
 * @author Mino Togna
 */

Phase1Manager = function(container) {
	
	this.container = $(container);
	
	// upload section
	this.uploadSection = this.container.find(".upload-section");	
	this.uploadBtn = this.container.find( "[name=upload-btn]" );
	this.file = this.container.find( "[name=file]" );
	this.form = this.container.find( "form" );
	
	// table section
	this.tableSection = this.container.find(".table-section");
	
	// import section 
	this.importSection = this.container.find(".import-section");
	
	this.init();
};

Phase1Manager.prototype.init = function(){
	var $this = this;

	this.uploadSection.show();
	this.tableSection.show();
	this.importSection.hide();
	
	// upload csv form methods 
	this.form.ajaxForm( {
	    dataType : 'json',
	    beforeSubmit: function() {
	    	UI.lock();
	    },
	    uploadProgress: function ( event, position, total, percentComplete ) {
	    },
	    success: function ( response ) {
	    	$this.showImport( response.fields.filepath, response.fields.headers );
	    },
	    error: function (e) {
	    	alert('Error uploading file' + e);
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
	
//	WorkspaceManager.getInstance().activeWorkspace(function(ws){
//		$this.updateTable(ws);
//	});
};

Phase1Manager.prototype.showImport = function( filepath, headers ){
	var $this = this;
	
	WorkspaceManager.getInstance().activeWorkspace(function(ws){
		
		$this.importTable = new CsvTableImport( $this.importSection, filepath, headers, ws.phase1PlotTableName );
		// import csv table
		$this.importTable.import(function(schema, table) {
			UI.lock();
			// then sets the phase1plots table of the active workspace
			WorkspaceManager.getInstance().activeWorkspaceSetPhase1PlotsTable(table, function(ws){
				
				$this.importSection.hide(0);
				$this.uploadSection.fadeIn();
				$this.tableSection.fadeIn();
				
				$this.updateTable(schema, table);
				UI.unlock();
			});
			
			
		});
		
		$this.uploadSection.hide(0);
		$this.tableSection.hide(0);
		$this.importSection.fadeIn();
	
	});
};

Phase1Manager.prototype.updateTable = function(schema, table) {
	var $this = this;
};

