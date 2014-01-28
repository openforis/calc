/**
 * manager for phase 1 plots manager
 * @author Mino Togna
 */

Phase1Manager = function(container , sdManager) {
	
	this.container = $( container );
	this.sdManager = sdManager;
	
	// upload section
	this.uploadSection = this.container.find(".upload-section");	
	this.uploadBtn = this.container.find( "[name=upload-btn]" );
	this.file = this.container.find( "[name=file]" );
	this.form = this.container.find( "form" );
	
	// table section
//	this.tableSection = this.container.find(".data-table");
//	this.dataTable = new DataTable(this.tableSection);
	
	// import section 
	this.importSection = this.container.find(".import-section");
	
	
	// table join settings
	this.tableJoin = new TableJoin( this.container.find(".table-join") );
	this.tableJoin.hide();
	
	// buttons section
//	this.buttonsSection = this.container.find( ".buttons-section" );
//	this.saveBtn = this.buttonsSection.find( "[name=save-btn]" );
//	this.cancelBtn = this.buttonsSection.find( "[name=cancel-btn]" );
	
	this.init();
};

Phase1Manager.prototype.init = function(){
	var $this = this;

	this.uploadSection.show();
//	this.tableSection.show();
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
//		if( ws.phase1PlotTable ) {
			// update join section
			// now hardcoded
//			$this.updateTable("calc", ws.phase1PlotTable);
//		}
//	});
	
//	this.saveBtn.click(function(e){
//		if($this.save){
//			$this.save();
//		}
//	});
//	this.cancelBtn.click(function(e){
//		if($this.cancel) {
//			$this.cancel();
//		}
//	});
};

//Phase1Manager.prototype.cancel = null;
//Phase1Manager.prototype.save = null;

Phase1Manager.prototype.show = function() {
	this.container.fadeIn(200);
	
	this.uploadSection.show();
//	this.tableJoin.show();
	
	this.importSection.hide();
	
	// update join section
	this.updateTableJoin();
};
Phase1Manager.prototype.hide = function() {
	this.container.hide();
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
				
				// load table info
//				new TableDataProvider("calc" , table ).tableInfo( function(response){
//					$this.phase1TableInfo = response;
//				}  );
				// update join settings
				$this.updateTableJoin();
				
				$this.importSection.hide(0);
				$this.uploadSection.fadeIn();
//				$this.tableJoin.show();
//				$this.tableSection.fadeIn();
//				$this.buttonsSection.fadeIn();
				
//				$this.updateTable(schema, table);
				UI.unlock();
			});
			
			
		});
		
		$this.uploadSection.hide(0);
//		$this.tableJoin.hide(0);
//		$this.tableSection.hide(0);
//		$this.buttonsSection.hide( 0 );
		$this.importSection.fadeIn();
	
	});
};

Phase1Manager.prototype.updateTableJoin = function(){
	var $this  = this;
	WorkspaceManager.getInstance().activeWorkspace(function(ws){
		if( ws.phase1PlotTable ) {
			
			UI.lock();
			new TableDataProvider("calc" , ws.phase1PlotTable ).tableInfo( function(response) {
				$this.phase1TableInfo = response;
				
				$this.tableJoin.setTableInfo( $this.phase1TableInfo , $this.sdManager.samplingUnitTableInfo );
				$this.tableJoin.show();
				
				UI.unlock();
			} );
		} else {
			$this.tableJoin.hide();
		}
	});
	
	console.log(this);
	
};

//Phase1Manager.prototype.updateTable = function(schema, table) {
//	var $this = this;
//	var dataProvider = new TableDataProvider(schema, table);
//	this.dataTable.setDataProvider(dataProvider);
//	this.dataTable.show();
	
//};

