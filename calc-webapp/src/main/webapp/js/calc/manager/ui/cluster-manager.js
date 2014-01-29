/**
 * manager for cluster settings page
 * @author Mino Togna
 */

ClusterManager = function(container , sdManager) {
	
	this.container = $(container);
	this.sdManager = sdManager;
	
	// upload csv ui components
	this.uploadSection = this.container.find(".upload-section");
	
	this.uploadBtn = this.container.find( "[name=upload-btn]" );
	this.file = this.container.find( "[name=file]" );
	this.form = this.container.find( "form" );
	
	
	this.tableColumnSelector = new TableColumnSelector( this.container.find(".table-column-selector") );
	
//	this.table = this.container.find(".strata-table table");
	
	// buttons section
//	this.buttonsSection = this.container.find( ".buttons-section" );
//	this.saveBtn = this.buttonsSection.find( "[name=save-btn]" );
//	this.cancelBtn = this.buttonsSection.find( "[name=cancel-btn]" );
	
	this.init();
};

ClusterManager.prototype.init = function(){
	var $this = this;

	// upload csv form methods 
	this.form.ajaxForm( {
	    dataType : 'json',
	    beforeSubmit: function() {
	    	UI.lock();
	    },
	    uploadProgress: function ( event, position, total, percentComplete ) {
	    },
	    success: function ( response ) {
	    	$this.import( response.fields.filepath );
	    },
	    error: function (e) {
	    	alert('Error uploading file' + e);
	    	UI.unlock();
	    },
	    complete: function() {
	    	// reset upload form
	    	$this.file.val("");
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
	
	
	// update strata table
	
	
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

//StratumManager.prototype.cancel = null;
//StratumManager.prototype.save = null;

ClusterManager.prototype.show = function() {
	this.container.fadeIn(200);
	
	WorkspaceManager.getInstance().activeWorkspace( $.proxy( function(ws){
		this.updateStrata(ws);
	} , this ) );
};

ClusterManager.prototype.hide = function() {
	this.container.hide();
};

ClusterManager.prototype.import = function(filepath) {
	var $this = this;
	
	WorkspaceManager.getInstance().activeWorkspaceImportStrata(filepath, function(ws){
		UI.unlock();
		UI.showSuccess( ws.strata.length +" strata successfully imported", true);
		$this.updateStrata(ws);
	});
	
};

ClusterManager.prototype.updateJoinColumn = function(ws) {
	if( ws.strata && ws.strata.length > 0 ) {
		
		var tableInfo = ( this.sdManager.samplingDesign.twoPhases === true ) ? this.sdManager.phase1TableInfo : this.sdManager.samplingUnitTableInfo;
		var header = ( this.sdManager.samplingDesign.twoPhases === true ) ? "phase 1 table join column" : this.sdManager.samplingUnitTableInfo.table + " table join column";
		this.tableColumnSelector.setTableInfo( tableInfo , header);
		
		var options = this.sdManager.samplingDesign.stratumJoinSettings; 
		if( options ){
//			this.stratumManager.setJoinOptions( this.sdManager.samplingDesign.stratumJoinSettings );
			this.tableColumnSelector.settings = options;
		}
		this.tableColumnSelector.show();
	} else {
		this.tableColumnSelector.hide();

	}
};

ClusterManager.prototype.validate = function() {
	if( this.tableColumnSelector.joinColumn && this.tableColumnSelector.joinColumn !== "" ) {
		return true;
	} else {
		UI.showError("Join column must be specified", false);
		return false;
	}
};

ClusterManager.prototype.joinOptions = function() {
	return this.tableColumnSelector.jsonSettings();
};

ClusterManager.prototype.setJoinOptions = function(options){
	this.tableColumnSelector.settings = options;
};
